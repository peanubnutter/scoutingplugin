package com.scouting;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Event Scouting"
)
public class ScoutingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ScoutingConfig config;

	@Inject
	private ScoutingWebManager webManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Getter(AccessLevel.PACKAGE)
	private List<EventData> eventsToUpload = new ArrayList<>();

	protected static String postEventsEndpoint =
			"https://g98c6e9efd32fb1-scouting.adb.us-ashburn-1.oraclecloudapps.com/ords/scouting/calls/";

	// Make sure to update this version to show the plugin message below.
	private final String pluginVersion = "v1.0.1";
	private final String pluginMessage = "<colHIGHLIGHT>Event Scouting " + pluginVersion + ":<br>" +
			"<colHIGHLIGHT>* Added special Sailing Shoals tracking<br>" +
			"<colHIGHLIGHT>* Plugin update messages (can be disabled)<br>";

	// Every X seconds, upload any events found since the last check
	private static final int UPLOAD_INTERVAL_SECONDS = 3;

	List<EventData> recentEvents = new ArrayList<>();

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (!isValidWorldType()) {
			return;
		}
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN) {
			return;
		};

		// Send message about plugin updates one time
		if (!config.getVersion().equals(pluginVersion)) {
			configManager.setConfiguration(
					ScoutingConfig.SCOUTING_CONFIG_GROUP,
					ScoutingConfig.SCOUTING_CONFIG_VERSION_KEY,
					pluginVersion);
			if (config.showPluginUpdates()) {
				chatMessageManager.queue(QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(pluginMessage)
						.build()
				);
			}
		}
	}

	private boolean isValidWorldType() {
		List<WorldType> invalidTypes = ImmutableList.of(
				WorldType.DEADMAN,
				WorldType.NOSAVE_MODE,
				WorldType.SEASONAL,
				WorldType.TOURNAMENT_WORLD
		);

		for (WorldType worldType : invalidTypes) {
			if (client.getWorldType().contains(worldType)) {
				return false;
			}
		}

		return true;
	}


	@Subscribe
	public void onGameObjectSpawned(final GameObjectSpawned event) {
		GameObject gameObject = event.getGameObject();

		SupportedEventsEnum eventType = SupportedEventsEnum.findByObjectId(gameObject.getId());

		eventSpawned(eventType, gameObject.getWorldLocation(), gameObject.getId(), null);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned) {
		final NPC npc = npcSpawned.getNpc();

		SupportedEventsEnum eventType = SupportedEventsEnum.findByNpcId(npc.getId());

		eventSpawned(eventType, npc.getWorldArea().toWorldPoint(), npc.getId(), npc.getIndex());
	}

	private void eventSpawned(SupportedEventsEnum eventType, WorldPoint location, Integer id, Integer npcIndex) {
		if (eventType == null) {
			// event not found for this NPC
			return;
		}

		if (!clientOptedIntoEventType(eventType))
			return;

		EventData event = makeEvent(eventType, location, id, null);

		// remove any stale events, since events older than the dedupe duration could never match any new events anyway.
		recentEvents.removeIf(e -> Math.abs(e.getDiscovered_time().getEpochSecond() - Instant.now().getEpochSecond())
				> EventData.EVENT_DEDUPE_DURATION);
		// only attempt to upload the event if it has not already been seen
		if (!recentEvents.contains(event)) {
			eventsToUpload.add(event);
			recentEvents.add(event);
		}
	}

	private EventData makeEvent(SupportedEventsEnum eventType, WorldPoint eventLocation, int id, Integer npcIndex) {
		int world = client.getWorld();
		return EventData.builder()
				.eventType(eventType.name())
				.world(world)
				.xcoord(eventLocation.getX())
				.ycoord(eventLocation.getY())
				.plane(eventLocation.getPlane())
				.discovered_time(Instant.now())
				.npcId(id)
				.npcIndex(npcIndex)
				.rsn(client.getLocalPlayer().getName())
				.build();
	}

	// Only send events if the client is interested in contributing to scouting this event type
	private boolean clientOptedIntoEventType(SupportedEventsEnum eventType) {
		if (eventType == SupportedEventsEnum.ENT
				|| eventType == SupportedEventsEnum.PHEASANT
				|| eventType == SupportedEventsEnum.FOX
				|| eventType == SupportedEventsEnum.BEEHIVE
				|| eventType == SupportedEventsEnum.RITUAL
				|| eventType == SupportedEventsEnum.LEPRECHAUN
				|| eventType == SupportedEventsEnum.ROOTS
				|| eventType == SupportedEventsEnum.SAPLING
				|| eventType == SupportedEventsEnum.FLOWERS
		) {
			return config.forestryEventsEnabled();
		}

		if (eventType == SupportedEventsEnum.SHIMMERING_SHOAL
			|| eventType == SupportedEventsEnum.GLISTENING_SHOAL
			|| eventType == SupportedEventsEnum.VIBRANT_SHOAL
		) {
			return config.shoalEventsEnabled();
		}

		// Future event types go here.

		return false;
	}

	@Schedule(
			period = UPLOAD_INTERVAL_SECONDS,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void uploadEvents() {
		// List is cleared by webManager after uploading successfully
		if (eventsToUpload.size() > 0)
			webManager.postEvents();
	}

	@Provides
	ScoutingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ScoutingConfig.class);
	}
}
