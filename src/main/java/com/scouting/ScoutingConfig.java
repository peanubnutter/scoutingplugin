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

	/////////////////// Debug Info / settings

	@ConfigSection(
			name = "Debug",
			description = "Extra settings for advanced users",
			position = 2,
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

}
