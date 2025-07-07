package com.scouting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("event-scouting")
public interface ScoutingConfig extends Config
{

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
			position = 1,
			section = eventsSection
	)
	default boolean forestryEventsEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "imps",
			name = "Overworld Impling Events",
			description = "Send Impling events to server (Does not report Puro Puro Implings)",
			position = 2,
			section = eventsSection
	)
	default boolean impEventsEnabled() { return false; }

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
}
