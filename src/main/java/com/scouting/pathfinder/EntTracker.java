package com.scouting.pathfinder;

import com.scouting.pathfinder.RelayClient.Call;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Turns the relay's sightings into distinct ents. One ent arrives as a dozen or more rows -- each
 * scout reports the entling they saw -- scattered over a few tiles and up to a minute apart.
 */
class EntTracker
{
	/**
	 * Ents are kept for a second lifetime after they end, so a late report from inside one still
	 * lands there instead of passing for a new ent at the same tree. The relay sends this much history.
	 */
	private static final int RETAIN_SECONDS = 2 * Ent.LIFETIME_SECONDS;

	private final List<Ent> ents = new ArrayList<>();

	/** Feed in a snapshot. Returns the ents seen for the first time that are still running. */
	List<Ent> accept(Collection<Call> calls, long now)
	{
		ents.removeIf(e -> now - e.getFirstSeen() > RETAIN_SECONDS);

		List<Call> ordered = new ArrayList<>(calls);
		ordered.sort(Comparator.comparingLong(Call::getDiscoveredTime));

		List<Ent> started = new ArrayList<>();
		for (Call call : ordered)
		{
			Ent match = ents.stream().filter(e -> e.matches(call)).findFirst().orElse(null);
			if (match != null)
			{
				match.sightings.add(call);
				continue;
			}

			Ent ent = new Ent(call);
			ents.add(ent);
			if (!ent.isExpired(now))
			{
				started.add(ent);
			}
		}
		return started;
	}

	/** Running ents, oldest first: the order they were called and the order they end. */
	List<Ent> getActive(long now)
	{
		return ents.stream()
			.filter(e -> !e.isExpired(now))
			.sorted(Comparator.comparingLong(Ent::getFirstSeen))
			.collect(Collectors.toList());
	}

	void reset()
	{
		ents.clear();
	}

	/** One ent, assembled from every scout's sighting of it. */
	@Getter
	static class Ent
	{
		/** Stated outright on the wiki. */
		private static final int LIFETIME_SECONDS = 120;

		/** The Event Scouting plugin's own dedupe distance. */
		private static final int MATCH_RADIUS_TILES = 20;

		private final int world;
		private final int plane;

		/** The earliest sighting: the best estimate of when the ent spawned. */
		private final long firstSeen;

		/** From the first sighting only, so the name never drifts as more reports move the centre. */
		private final CallLocation location;

		@Getter(AccessLevel.NONE)
		private final Set<Call> sightings = new HashSet<>();

		/** Kept rather than dropped once reached, or the next snapshot would rebuild it. */
		@Setter(AccessLevel.PACKAGE)
		private boolean reached;

		Ent(Call seed)
		{
			this.world = seed.getWorld();
			this.plane = seed.getPlane();
			this.firstSeen = seed.getDiscoveredTime();
			this.location = CallLocation.nearest(seed.getXCoord(), seed.getYCoord());
			sightings.add(seed);
		}

		/**
		 * Same world and plane, near the centre, and within the ent's lifetime of its first sighting.
		 * Popular trees spawn repeat ents, so a wider time window would swallow the next one.
		 */
		private boolean matches(Call call)
		{
			return world == call.getWorld()
				&& plane == call.getPlane()
				&& Math.abs(call.getDiscoveredTime() - firstSeen) <= LIFETIME_SECONDS
				&& Math.abs(call.getXCoord() - getCentreX()) <= MATCH_RADIUS_TILES
				&& Math.abs(call.getYCoord() - getCentreY()) <= MATCH_RADIUS_TILES;
		}

		/** Each sighting is where a scout saw an entling, so the average lands among them. */
		int getCentreX()
		{
			return (int) Math.round(sightings.stream().mapToInt(Call::getXCoord).average().orElse(0));
		}

		int getCentreY()
		{
			return (int) Math.round(sightings.stream().mapToInt(Call::getYCoord).average().orElse(0));
		}

		String getPlaceName()
		{
			return location == null ? "Friendly ent" : location.getDisplayName();
		}

		long secondsRemaining(long now)
		{
			return Math.max(0, LIFETIME_SECONDS - (now - firstSeen));
		}

		boolean isExpired(long now)
		{
			return secondsRemaining(now) <= 0;
		}
	}
}
