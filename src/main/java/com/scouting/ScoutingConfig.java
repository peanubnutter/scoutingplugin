package com.scouting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;

import static com.scouting.ScoutingConfig.SCOUTING_CONFIG_GROUP;

@ConfigGroup(SCOUTING_CONFIG_GROUP)
public interface ScoutingConfig extends Config
{
	String SCOUTING_CONFIG_GROUP = "event-scouting";
	String SCOUTING_CONFIG_VERSION_KEY = "event_scouting_version";
	String ENT_PATHFINDER_KEY = "entPathfinder";
	String ENT_NOTIFY_KEY = "entNotify";
	String ENT_OVERLAY_KEY = "entShowOverlay";
	String ENT_MARK_TARGET_KEY = "entMarkTarget";

	/////////////////// Overall Info / settings

	@ConfigSection(
			name = "Info",
			description = "",
			position = 0
	)
	String infoSection = "Info section";

	@ConfigItem(
			keyName = "Calls discord link",
			name = "Calls Discord link [Hover]",
			description = "Join Log Hunters to view calls on Discord and enable notifications for certain events." +
					" Your calls may take some time to start showing up.",
			position = 0,
			section = infoSection
	)
	default String discordLink() {
		return "https://discord.gg/loghunters";
	}

	@ConfigItem(
			keyName = "showPluginUpdates",
			name = "Show plugin update message",
			description = "On first login after a plugin update, show an update message in chat.",
			position = 1,
			section = infoSection
	)
	default boolean showPluginUpdates()
	{
		return true;
	}

	/////////////////// Event settings

	@ConfigSection(
			name = "Events",
			description = "Which events to scout for",
			position = 1
	)
	String eventsSection = "Events section";

	@ConfigItem(
			keyName = "forestry",
			name = "Forestry Events",
			description = "Send Forestry events to server",
			position = 0,
			section = eventsSection
	)
	default boolean forestryEventsEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "shoal",
			name = "Sailing Shoals",
			description = "Send special Sailing Shoals (boosted drop / catch rates) events to server",
			position = 1,
			section = eventsSection
	)
	default boolean shoalEventsEnabled()
	{
		return true;
	}

	/////////////////// Ent Pathfinding

	@ConfigSection(
		name = "Ent Scout Tracking",
		description = "Track ent locations reported by other players, with optional pathfinding and notifications",
		position = 2
	)
	String entScoutTrackingSection = "Ent Scout Tracking";

	@ConfigItem(
			keyName = ENT_PATHFINDER_KEY,
			name = "Use 'Shortest Path' Plugin",
			description = "On world 444, ask the Shortest Path plugin to route to each called ent in turn, oldest first.",
			warning = "This will only work if you have the 'Shortest Path' plugin installed and enabled.",
			position = 0,
			section = entScoutTrackingSection
	)
	default boolean entPathfinderEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = ENT_OVERLAY_KEY,
		name = "Enable overlay",
		description = "List called ents and their time left on screen",
		position = 1,
		section = entScoutTrackingSection
	)
	default boolean entShowOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "entQuickTele",
		name = "Show quick tele",
		description = "Show the quick teleport under each ent in the overlay",
		position = 2,
		section = entScoutTrackingSection
	)
	default boolean entQuickTele()
	{
		return false;
	}

	@ConfigItem(
		keyName = ENT_MARK_TARGET_KEY,
		name = "Mark current ent call",
		description = "Show a hint arrow and a world map marker at the current ent call",
		position = 3,
		section = entScoutTrackingSection
	)
	default boolean entMarkTarget()
	{
		return false;
	}

	@ConfigItem(
			keyName = ENT_NOTIFY_KEY,
			name = "Notify on ent call",
			description = "Send a notification when a new ent is called",
			position = 4,
			section = entScoutTrackingSection
	)
	default Notification entNotify()
	{
		return Notification.OFF;
	}

	@ConfigItem(
		keyName = "entCalloutsEnabled",
		name = "Enable chat callouts",
		description = "Recognize ent callouts (Location ent) in Friends Chat",
		position = 5,
		section = entScoutTrackingSection
	)
	default boolean entCalloutsEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "entCallEndingsEnabled",
		name = "Enable chat call endings",
		description = "Recognize call endings (Location D) in Friends Chat",
		position = 6,
		section = entScoutTrackingSection
	)
	default boolean entCallEndingsEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "entIgnoreQuestLocked",
		name = "Ignore quest-locked locations",
		description = "Skip ents at locations that need a quest you haven't completed",
		position = 7,
		section = entScoutTrackingSection
	)
	default boolean entIgnoreQuestLocked()
	{
		return true;
	}

	@ConfigItem(
			keyName = "entIgnoreSunbleak",
			name = "Ignore Sunbleak Island",
			description = "Skip ents on Sunbleak Island, which requires 72 Sailing, adamant helm,"
					+ " and greater teleport focus. Very crowded.",
			position = 8,
			section = entScoutTrackingSection
	)
	default boolean entIgnoreSunbleak()
	{
		return false;
	}

	@ConfigItem(
			keyName = "entIgnoreDrumstickIsle",
			name = "Ignore Drumstick Isle",
			description = "Skip ents on Drumstick Isle, which requires 79 Sailing and an adamant keel",
			position = 9,
			section = entScoutTrackingSection
	)
	default boolean entIgnoreDrumstickIsle()
	{
		return false;
	}

	/////////////////// Debug Info / settings

	@ConfigSection(
			name = "Debug",
			description = "Extra settings for advanced users",
			position = 3,
			closedByDefault = true
	)
	String debugSection = "Debug section";

	@ConfigItem(
			keyName = "callsEndpoint",
			name = "Calls URL",
			description = "Which URL to send events to",
			position = 0,
			section = debugSection
	)
	default String postEventsEndpoint()
	{
		return "https://g98c6e9efd32fb1-scouting.adb.us-ashburn-1.oraclecloudapps.com/ords/scouting/calls/";
	}

	@ConfigItem(
			keyName = SCOUTING_CONFIG_VERSION_KEY,
			name = "Event Scouting plugin version",
			description = "Version of the plugin for update message",
			section = debugSection,
			position = 1
	) default String getVersion() { return ""; }

	@ConfigItem(
			keyName = "entRelayUrl",
			name = "Ent relay URL",
			description = "Where ent calls are received from",
			position = 2,
			section = debugSection
	)
	default String entRelayUrl()
	{
		return "wss://ent-relay.ent-calls.workers.dev/ws";
	}

	@ConfigItem(
			keyName = "warnOnRelayDisconnect",
			name = "Warn when relay disconnects",
			description = "Add a warning to the ent overlay while the ent relay is disconnected",
			position = 3,
			section = debugSection
	)
	default boolean warnOnRelayDisconnect()
	{
		return false;
	}

}
