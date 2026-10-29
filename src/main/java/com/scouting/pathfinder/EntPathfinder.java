package com.scouting.pathfinder;

import com.google.gson.Gson;
import com.scouting.ScoutingConfig;
import com.scouting.ScoutingPlugin;
import com.scouting.pathfinder.EntTracker.Ent;
import com.scouting.pathfinder.RelayClient.Call;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import okhttp3.OkHttpClient;

/**
 * Routes the player to ents called on world 444, oldest first, with the Shortest Path plugin.
 * {@link RelayClient} receives the calls, {@link EntTracker} turns sightings into ents, and this
 * class picks the target, hands it to Shortest Path and notices arrival.
 * <p>
 * Everything but the relay connection runs on the client thread, which is also where the overlay
 * renders and Shortest Path reads its messages, so none of the state needs locking.
 */
@Slf4j
@Singleton
public class EntPathfinder
{
	/** The community forestry world, and the only world the feature connects on. */
	private static final int FORESTRY_WORLD = 444;

	/** Wider than Shortest Path's own default of 5, so the path moves on before it clears itself. */
	private static final int ARRIVAL_TILES = 7;

	/**
	 * How long the relay connection outlives leaving the world, so hops, breaks and relogs reuse one
	 * connection. An idle connection costs the relay nothing; each new one counts against its quota.
	 */
	private static final long DISCONNECT_GRACE_MILLIS = 10 * 60_000;

	private static final String ENT = "ENT";
	private static final String SHORTEST_PATH = "shortestpath";

	private static final String ENT_DONE = "d(?:ead|one|oa)?[0-5]?|fake";

	private static final Pattern ENT_COMPLETED_PATTERN = Pattern.compile(
		"^(?:(?<before>.+?)\\s+(?:" + ENT_DONE + ")"
			+ "|(?:" + ENT_DONE + ")\\s+(?<after>.+))$",
		Pattern.CASE_INSENSITIVE);

	private static final String ENT_SUFFIX = "(?:\\s+(?:p|plugin|l|late|again|solo|duo|stall|spam))?";

	private static final Pattern ENT_CALL_REGEX = Pattern.compile(
		"^(?!.*\\b(?:" + ENT_DONE + "|branch(?:es)?)\\b)"
			+ "(?:(?<before>.+?)\\s+ents?" + ENT_SUFFIX
			+ "|ents?\\s+(?<after>.+?)" + ENT_SUFFIX + ")$",
		Pattern.CASE_INSENSITIVE);

	private final ScoutingConfig config;
	private final EntPathfinderOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	/** Used instead of {@code @Schedule}, which stalls while the client is minimised. */
	@Inject
	private ScheduledExecutorService executor;

	@Inject
	@Named("developerMode")
	private boolean developerMode;

	private volatile RelayClient relay;
	private volatile boolean running;
	private ScheduledFuture<?> connectionTask;
	private long lastNeededRelayMillis;

	// Client thread only.
	private final EntTracker tracker = new EntTracker();
	/** Running ents not yet reached, oldest first. The path points at the first. */
	private List<Ent> live = Collections.emptyList();
	private boolean pathIsOurs;
	/** The last point we pointed the hint arrow at, to tell our arrow from another source's. */
	private WorldPoint arrowPoint;
	/** The world map marker for the current target, and the point it sits on. */
	private WorldMapPoint mapPoint;
	private WorldPoint mapTarget;
	/** When the player logged in on the forestry world, by the relay's clock; 0 while not logged in there. */
	private long loggedInSince;

	@Inject
	EntPathfinder(ScoutingPlugin plugin, ScoutingConfig config)
	{
		this.config = config;
		// Built here rather than injected, so Guice never has to resolve a cycle between the two.
		this.overlay = new EntPathfinderOverlay(plugin, this, config);
	}

	/** Switch the feature on or off. Safe to repeat. */
	public synchronized void setEnabled(boolean enabled)
	{
		if (enabled && !running)
		{
			start();
		}
		else if (!enabled && running)
		{
			stop();
		}
	}

	private void start()
	{
		relay = new RelayClient(okHttpClient, gson, executor, calls -> clientThread.invokeLater(() -> receive(calls)));
		eventBus.register(this);
		overlayManager.add(overlay);
		running = true;
		connectionTask = executor.scheduleWithFixedDelay(this::updateRelayConnection, 1, 1, TimeUnit.SECONDS);

		// Switched on mid-session, ents count from now: the actual login time is not known.
		clientThread.invoke(() -> loggedInSince = isLoggedInOnForestryWorld() ? serverNow() : 0);
	}

	private void stop()
	{
		running = false;
		connectionTask.cancel(false);
		relay.close();
		eventBus.unregister(this);
		overlayManager.remove(overlay);

		// The state, and Shortest Path, belong to the client thread.
		clientThread.invoke(() ->
		{
			for (Ent ent : live)
			{
				overlay.removeSkipEntry(ent);
			}
			tracker.reset();
			live = Collections.emptyList();
			loggedInSince = 0;
			clearPath();
			clearHintArrow();
			clearMapPoint();
		});
	}

	public void onPathSettingChanged()
	{
		clientThread.invoke(() ->
		{
			if (!config.entPathfinderEnabled())
			{
				clearPath();
			}
			else if (!live.isEmpty())
			{
				pathTo(live.get(0));
			}
		});
	}

	public void onMarkTargetSettingChanged()
	{
		clientThread.invoke(() ->
		{
			if (config.entMarkTarget())
			{
				updateHintArrow();
				updateMapPoint();
			}
			else
			{
				clearHintArrow();
				clearMapPoint();
			}
		});
	}

	/** Every second, on the executor: hold a relay connection only while on the forestry world. */
	private void updateRelayConnection()
	{
		// Catches everything, since one uncaught throw would cancel this task for the session.
		try
		{
			long now = System.currentTimeMillis();
			GameState state = client.getGameState();
			// Loading screens and brief disconnects count, or every region load would reconnect.
			boolean inGame = state == GameState.LOGGED_IN || state == GameState.LOADING || state == GameState.CONNECTION_LOST;

			if (inGame && client.getWorld() == FORESTRY_WORLD)
			{
				lastNeededRelayMillis = now;
				relay.connect(config.entRelayUrl().trim() + "?world=" + FORESTRY_WORLD);
			}
			else if (now - lastNeededRelayMillis > DISCONNECT_GRACE_MILLIS)
			{
				relay.disconnect();
			}
		}
		catch (Throwable t)
		{
			log.warn("Ent relay connection check failed", t);
		}
	}

	private void receive(List<Call> calls)
	{
		if (!running)
		{
			return;
		}

		// The relay only sends ents; this guards against a misconfigured one.
		List<Call> entCalls = calls.stream().filter(c -> ENT.equals(c.getEventType())).collect(Collectors.toList());

		long now = serverNow();
		for (Ent ent : tracker.accept(entCalls, now))
		{
			if (isEligible(ent) && !isIgnored(ent))
			{
				notifier.notify(config.entNotify(), "Ent called: " + ent.getLabel() + " (" + ent.secondsRemaining(now) + "s left)");
			}
		}
		refresh();
	}

	/** A region load or a brief disconnect is not a new login; logging out or hopping away is. */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (isLoggedInOnForestryWorld())
		{
			if (loggedInSince == 0)
			{
				loggedInSince = serverNow();
			}
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			loggedInSince = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		refresh();

		// Measured here because Shortest Path reports nothing about progress.
		if (!live.isEmpty() && isReached(live.get(0)))
		{
			live.get(0).setReached(true);
			refresh();
		}
	}

	// Developer mode only: {@code ::mockcall message}
	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		if (!developerMode || !"mockcall".equals(event.getCommand()) || !isLoggedInOnForestryWorld())
		{
			return;
		}

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setType(ChatMessageType.FRIENDSCHAT);
		chatMessage.setMessage(String.join(" ", event.getArguments()));

		// avoid reentrant script calls
		clientThread.invokeLater(() -> onChatMessage(chatMessage));
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.FRIENDSCHAT || !isLoggedInOnForestryWorld())
		{
			return;
		}

		String message = chatMessage.getMessage().trim().toLowerCase(Locale.ROOT);;

		if (config.entCallEndingsEnabled())
		{
			Matcher matcher = ENT_COMPLETED_PATTERN.matcher(message);
			if (matcher.matches())
			{
				String name = matcher.group("before") != null ? matcher.group("before") : matcher.group("after");
				Set<CallLocation> locations = CallLocationFinder.find(name);
				if (!locations.isEmpty())
				{
					tracker.getActive(serverNow()).forEach(ent ->
					{
						if (locations.contains(ent.getLocation()))
						{
							ent.setReached(true);
						}
					});
					// avoid reentrant script calls
					clientThread.invokeLater(this::refresh);
				}
			}
		}

		if (config.entCalloutsEnabled())
		{
			Matcher matcher = ENT_CALL_REGEX.matcher(message);
			if (matcher.matches())
			{
				String name = matcher.group("before") != null ? matcher.group("before") : matcher.group("after");
				CallLocationFinder.find(name).stream().findFirst().ifPresent(this::createCall);
			}
		}
	}

	private void createCall(CallLocation location)
	{
		String json = String.format(
			"{\"event_type\":\"%s\",\"world\":%d,\"x_coord\":%d,\"y_coord\":%d,\"plane\":0,\"discovered_time\":%d}",
			ENT, FORESTRY_WORLD, location.getCentreX(), location.getCentreY(), serverNow());
		Call call = gson.fromJson(json, Call.class);

		// avoid reentrant script calls
		clientThread.invokeLater(() -> receive(Collections.singletonList(call)));
	}

	/**
	 * Rebuild the list and point the path at its first entry: the oldest ent not yet reached, and
	 * the closest to ending. Filtering here rather than in the tracker lets a late report still find
	 * its ent, and makes an island setting apply at once.
	 */
	private void refresh()
	{
		List<Ent> previous = live;
		live = tracker.getActive(serverNow()).stream()
			.filter(e -> !e.isReached() && isEligible(e) && !isIgnored(e))
			.collect(Collectors.toList());

		for (Ent ent : previous)
		{
			if (!live.contains(ent))
			{
				overlay.removeSkipEntry(ent);
			}
		}

		for (Ent ent : live)
		{
			if (!previous.contains(ent))
			{
				overlay.addSkipEntry(ent);
			}
		}

		// The centre drifts as sightings arrive.
		if (config.entMarkTarget())
		{
			updateHintArrow();
			updateMapPoint();
		}

		if (!config.entPathfinderEnabled())
		{
			return;
		}

		Ent next = live.isEmpty() ? null : live.get(0);

		if (!previous.isEmpty() && previous.get(0) == next)
		{
			return;
		}

		if (next == null)
		{
			clearPath();
		}
		else
		{
			pathTo(next);
		}
	}

	/** Drop ent from the list and move on to the next ent. */
	void skip(Ent ent)
	{
		ent.setReached(true);
		refresh();
	}

	/** Only an ent that spawned while the player was logged in on the world can reward them. */
	private boolean isEligible(Ent ent)
	{
		return loggedInSince > 0 && ent.getFirstSeen() >= loggedInSince;
	}

	private boolean isLoggedInOnForestryWorld()
	{
		return client.getGameState() == GameState.LOGGED_IN && client.getWorld() == FORESTRY_WORLD;
	}

	private boolean isIgnored(Ent ent)
	{
		CallLocation location = ent.getLocation();
		return location != null
			&& ((config.entIgnoreSunbleak() && location == CallLocation.SUNBLEAK)
			|| (config.entIgnoreDrumstickIsle() && location == CallLocation.DRUMSTICK)
			|| (config.entIgnoreQuestLocked() && !location.isUnlocked(client)));
	}

	/**
	 * One target, never a set: Shortest Path treats a set as "any of these" and clears the whole
	 * path on reaching any one, dropping the route to every other ent.
	 */
	private void pathTo(Ent ent)
	{
		WorldPoint point = new WorldPoint(ent.getCentreX(), ent.getCentreY(), ent.getPlane());
		pathIsOurs = true;
		eventBus.post(new PluginMessage(SHORTEST_PATH, "path", Collections.singletonMap("target", point)));
	}

	/** Shortest Path does not broadcast its target, so this never clears a path the player set. */
	private void clearPath()
	{
		if (pathIsOurs)
		{
			pathIsOurs = false;
			eventBus.post(new PluginMessage(SHORTEST_PATH, "clear", Collections.emptyMap()));
		}
	}

	/** Point the hint arrow at the first live ent, or take ours down. Client thread. */
	private void updateHintArrow()
	{
		if (live.isEmpty())
		{
			clearHintArrow();
			return;
		}

		// Another source's arrow is not ours to replace.
		if (client.hasHintArrow() && !isOurArrow())
		{
			arrowPoint = null;
			return;
		}

		Ent ent = live.get(0);
		arrowPoint = new WorldPoint(ent.getCentreX(), ent.getCentreY(), ent.getPlane());
		client.setHintArrow(arrowPoint);
	}

	private void clearHintArrow()
	{
		if (isOurArrow())
		{
			client.clearHintArrow();
		}
		arrowPoint = null;
	}

	/** The current hint arrow is still the one we set. */
	private boolean isOurArrow()
	{
		return arrowPoint != null
			&& client.hasHintArrow()
			&& arrowPoint.equals(client.getHintArrowPoint());
	}

	/** Mark the first live ent on the world map, or take the marker down. Client thread. */
	private void updateMapPoint()
	{
		if (live.isEmpty())
		{
			clearMapPoint();
			return;
		}

		Ent ent = live.get(0);
		WorldPoint target = new WorldPoint(ent.getCentreX(), ent.getCentreY(), ent.getPlane());
		if (target.equals(mapTarget))
		{
			return;
		}

		clearMapPoint();
		mapTarget = target;
		mapPoint = WorldMapPoint.builder()
			.worldPoint(target)
			.image(itemManager.getImage(ItemID.BIRD_NEST_EGG_RED))
			.name(ent.getLabel())
			.tooltip(ent.getLabel())
			.snapToEdge(true)
			.jumpOnClick(true)
			.build();
		worldMapPointManager.add(mapPoint);
	}

	private void clearMapPoint()
	{
		if (mapPoint != null)
		{
			worldMapPointManager.remove(mapPoint);
			mapPoint = null;
			mapTarget = null;
		}
	}

	/** Within arrival distance, measured as the game does (Chebyshev). */
	private boolean isReached(Ent ent)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return false;
		}
		WorldPoint location = player.getWorldLocation();
		return Math.max(
			Math.abs(location.getX() - ent.getCentreX()),
			Math.abs(location.getY() - ent.getCentreY())) <= ARRIVAL_TILES;
	}

	// ------------------------------------------------------------------ for the overlay

	List<Ent> getLiveEnts()
	{
		return live;
	}

	boolean isRelayOffline()
	{
		return relay.getStatus() == RelayClient.Status.WAITING_TO_RETRY;
	}

	long serverNow()
	{
		return relay.serverNow();
	}
}
