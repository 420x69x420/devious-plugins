package net.unethicalite.scripts.runner;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.unethicalite.scripts.runner.paint.CustomPaint;
import net.unethicalite.scripts.runner.paint.PaintInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
@Slf4j
class RunnerOverlay extends Overlay implements PaintInfo
{
	private final Client client;
	private final RunnerPlugin plugin;
	@Override
	public String[] getPaintInfo()
	{
		String version =  " v0";
		return new String[] {
				"Runecrafting Runner Builder "+version+" by Dreambotter420 ^_^"
		};
	}
	private final CustomPaint CUSTOM_PAINT = new CustomPaint(this,
			CustomPaint.PaintLocations.BOTTOM_LEFT_PLAY_SCREEN,
			new Color[] {new Color(255, 251, 255)},
			"Impact",
			new Color[] {new Color(50, 50, 50, 175)},
			new Color[] {new Color(28, 28, 29)},
			1, false, 3, 3, 1);
	private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	public void drawScriptPaint(Graphics2D graphics2D)
	{
		// Set the rendering hints
		graphics2D.setRenderingHints(aa);
		// Draw the custom paint
		CUSTOM_PAINT.paint(graphics2D);
	}

	@Inject
	private RunnerOverlay(final Client client, final RunnerPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGHEST);
	}
	@Override
	public Dimension render(Graphics2D graphics) {
		drawScriptPaint(graphics);
		return null;
	}
}