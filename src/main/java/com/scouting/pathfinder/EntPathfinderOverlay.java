package com.scouting.pathfinder;

import com.scouting.ScoutingConfig;
import com.scouting.ScoutingPlugin;
import com.scouting.pathfinder.EntTracker.Ent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Ents not yet reached, oldest first: place on the left, time left on the right. The first line,
 * in white, is where the path points; the rest wait in grey.
 */
class EntPathfinderOverlay extends OverlayPanel
{
	private static final int WIDTH = 210;

	private final EntPathfinder pathfinder;
	private final ScoutingConfig config;

	EntPathfinderOverlay(ScoutingPlugin plugin, EntPathfinder pathfinder, ScoutingConfig config)
	{
		super(plugin);
		this.pathfinder = pathfinder;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(WIDTH, 0));
		setPriority(PRIORITY_LOW);
		addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Event Scouting");
	}

	void addSkipEntry(Ent ent)
	{
		addMenuEntry(RUNELITE_OVERLAY, "Skip", ent.getLabel(), e -> pathfinder.skip(ent));
	}

	void removeSkipEntry(Ent ent)
	{
		removeMenuEntry(RUNELITE_OVERLAY, "Skip", ent.getLabel());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.entShowOverlay())
		{
			return null;
		}

		List<Ent> live = pathfinder.getLiveEnts();
		// A debug aid, off by default: to most players a connection line is noise.
		boolean offline = config.warnOnRelayDisconnect() && pathfinder.isRelayOffline();
		if (live.isEmpty() && !offline)
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Event Scouting")
			.color(Color.WHITE)
			.build());

		if (offline)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Ent relay")
				.leftColor(Color.RED)
				.right("reconnecting")
				.rightColor(Color.GRAY)
				.build());
		}

		long now = pathfinder.serverNow();
		for (Ent ent : live)
		{
			long remaining = ent.secondsRemaining(now);
			panelComponent.getChildren().add(LineComponent.builder()
				.left(ent.getLabel())
				.leftColor(ent == live.get(0) ? Color.WHITE : Color.LIGHT_GRAY)
				.right("(" + remaining + "s)")
				.rightColor(remaining <= 20 ? Color.RED : remaining <= 45 ? Color.ORANGE : Color.GREEN)
				.build());

			if (config.entQuickTele() && ent.getLocation() != null)
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(ent.getLocation().getQuickTele())
					.leftColor(Color.GRAY)
					.build());
			}
		}

		return super.render(graphics);
	}
}
