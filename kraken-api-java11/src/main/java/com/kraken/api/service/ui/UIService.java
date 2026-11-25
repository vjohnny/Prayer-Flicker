package com.kraken.api.service.ui;

import com.kraken.api.Context;
import com.kraken.api.core.Services;
import com.kraken.api.service.util.RandomService;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;

import javax.inject.Singleton;
import java.awt.Rectangle;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Widget clickbox helpers used by quick-prayer clicks.
 *
 * <p>The methods below are the upstream implementations. Actor, object, tile, and inventory
 * clickboxes are not part of this build.</p>
 */
@Singleton
public class UIService {

    private static Context ctx() {
        return Services.context();
    }

    /**
     * Returns a default rectangle representing the entire game canvas with a slight random offset.
     *
     * @return Rectangle covering the canvas area
     */
    public static Rectangle getDefaultRectangle() {
        int randomValue = ThreadLocalRandom.current().nextInt(3) - 1;
        return new Rectangle(randomValue, randomValue, ctx().getClient().getCanvasWidth(), ctx().getClient().getCanvasHeight());
    }

    /**
     * Gets the clickbox for a Widget.
     *
     * @param widget the widget to get the clickbox for
     * @return the widget's bounds, or default rectangle if unavailable
     */
    public static Rectangle getWidgetClickbox(Widget widget) {
        if (widget == null) return getDefaultRectangle();
        return widget.getBounds();
    }

    /**
     * Calculates a click point from a rectangle with optional randomization.
     * When randomized, uses RandomService.randomPointEx with a distribution factor of 0.78.
     *
     * @param rectangle the rectangle to calculate the point from
     * @param randomize whether to randomize the point within the rectangle
     * @return a point within the rectangle (randomized or centered)
     */
    public static Point getClickingPoint(Rectangle rectangle, boolean randomize) {
        if (isFallbackClickbox(rectangle)) return new Point(1, 1);

        if (!randomize) return new Point((int) rectangle.getCenterX(), (int) rectangle.getCenterY());
        return RandomService.randomPointEx(new Point((int) rectangle.getCenterX(), (int) rectangle.getCenterY()), rectangle, 0.78);
    }

    /**
     * Reports whether a clickbox is missing or is the full-canvas stand-in {@link #getDefaultRectangle()} returns
     * when a target's real clickbox could not be read. Its origin is one of (-1,-1), (0,0) or (1,1); clicks on such a
     * box go to (1, 1) rather than to a random point on the screen.
     *
     * @param rectangle the clickbox to check, may be null.
     * @return true when the clickbox is null or a fallback rectangle.
     */
    public static boolean isFallbackClickbox(Rectangle rectangle) {
        if (rectangle == null) return true;
        if (rectangle.getX() == -1 && rectangle.getY() == -1) return true;
        if (rectangle.getX() == 1 && rectangle.getY() == 1) return true;
        return rectangle.getX() == 0 && rectangle.getY() == 0;
    }
}
