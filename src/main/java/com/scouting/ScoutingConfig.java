package com.scouting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import static com.scouting.ScoutingConfig.SCOUTING_CONFIG_GROUP;

@ConfigGroup(SCOUTING_CONFIG_GROUP)
public interface ScoutingConfig extends Config
{
	String SCOUTING_CONFIG_GROUP = "event-scouting";
	String SCOUTING_CONFIG_VERSION_KEY = "event_scouting_version";
	String ENT_PATHFINDER_KEY = "entPathfinder";

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
			name = "Ent Pathfinding",
			description = "Route to ents called on world 444 with the Shortest Path plugin",
			position = 2
	)
	String entPathfindingSection = "Ent Pathfinding section";

	@ConfigItem(
			keyName = ENT_PATHFINDER_KEY,
			name = "Use 'Shortest Path' Plugin",
			description = "On world 444, ask the Shortest Path plugin to route to each called ent in turn, oldest first.",
			warning = "This will only work if you have the 'Shortest Path' plugin installed and enabled.",
			position = 0,
			section = entPathfindingSection
	)
	default boolean entPathfinderEnabled()
	{
		return false;
	}

	@ConfigItem(
			keyName = "entNotify",
			name = "Notify on ent call",
			description = "Send a notification when a new ent is called",
			position = 1,
			section = entPathfindingSection
	)
	default boolean entNotify()
	{
		return true;
	}

	@ConfigItem(
			keyName = "entShowOverlay",
			name = "Enable overlay",
			description = "List called ents and their time left on screen",
			position = 2,
			section = entPathfindingSection
	)
	default boolean entShowOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "entIgnoreSunbleak",
			name = "Ignore Sunbleak Island",
			description = "Skip ents on Sunbleak Island, which needs 72 Sailing and an adamant helm to reach, "
					+ "docked with a greater teleport focus. It is the less ideal location, due to the large number of woodcutters",
			position = 3,
			section = entPathfindingSection
	)
	default boolean entIgnoreSunbleak()
	{
		return false;
	}

	@ConfigItem(
			keyName = "entIgnoreDrumstickIsle",
			name = "Ignore Drumstick Isle",
			description = "Skip ents on Drumstick Isle, which needs 79 Sailing and an adamant keel to reach",
			position = 4,
			section = entPathfindingSection
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
			keyName = "entRelayStatus",
			name = "Show ent relay status",
			description = "Add a line to the ent overlay while the ent relay is reconnecting",
			position = 3,
			section = debugSection
	)
	default boolean entRelayStatus()
	{
		return false;
	}

}
