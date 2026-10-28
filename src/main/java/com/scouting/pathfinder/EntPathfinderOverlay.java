package com.scouting.pathfinder;

import com.scouting.ScoutingConfig;
import com.scouting.ScoutingPlugin;
import com.scouting.pathfinder.EntTracker.Ent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

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
		boolean offline = config.entRelayStatus() && pathfinder.isRelayOffline();
		if (live.isEmpty() && !offline)
		{
			return null;
		}

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
				.left(ent.getPlaceName())
				.leftColor(ent == live.get(0) ? Color.WHITE : Color.LIGHT_GRAY)
				.right("(" + remaining + "s)")
				.rightColor(remaining <= 20 ? Color.RED : remaining <= 45 ? Color.ORANGE : Color.GREEN)
				.build());
		}

		return super.render(graphics);
	}
}
