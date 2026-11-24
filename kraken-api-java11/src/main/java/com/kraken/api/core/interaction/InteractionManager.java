package com.kraken.api.core.interaction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.interaction.model.MenuOption;
import com.kraken.api.core.interaction.model.ResolvedMenuAction;
import com.kraken.api.service.ui.UIService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

import java.awt.Rectangle;

/**
 * Widget clicks used by quick prayers.
 *
 * <p>The point is the same one {@code ClickPoints} returned when no click-point sampler was
 * registered: {@link UIService#getClickingPoint} on the widget bounds, randomized toward the
 * center. This build does not register a sampler.</p>
 */
@Slf4j
@Singleton
public class InteractionManager {

    @Inject
    private InteractionDispatcher dispatcher;

    @Inject
    private Provider<Context> ctxProvider;

    /**
     * Interacts with a widget by resolving its context and dispatching the specified interaction.
     * This method performs the following tasks:
     * <ul>
     *   <li>Resolves the widget using the provided {@code widgetId}.</li>
     *   <li>Determines the display name for the action based on the specified {@code action} index.</li>
     *   <li>Dispatches the interaction to the game's input system.</li>
     * </ul>
     * If the widget cannot be resolved or the action name cannot be determined, appropriate warnings
     * will be logged.
     *
     * @param widgetId    the unique identifier of the widget to be interacted with.
     *                    This corresponds to the parent widget in the user interface.
     * @param childId     the identifier of the child element within the widget being targeted.
     *                    If no child element is specified, this is typically {@code -1}.
     * @param itemId      the identifier of a specific item within the widget, if applicable.
     *                    If not targeting an item, this is typically {@code -1}.
     * @param action      the zero-based index of the action to be performed from the widget's action list.
     *                    This determines the type of interaction executed (e.g., click, examine).
     * @return true if the widget resolved and the action was dispatched, false otherwise.
     */
    public boolean interact(int widgetId, int childId, int itemId, int action) {
        return interact(widgetId, childId, itemId, action, null);
    }

    /**
     * Interacts with a widget like {@link #interact(int, int, int, int)}, clicking a point chosen beforehand.
     *
     * @param widgetId the packed id of the widget to interact with.
     * @param childId  the child index within the widget, or {@code -1}.
     * @param itemId   the item id within the widget, or {@code -1}.
     * @param action   the 1-based index of the action in the widget's action list.
     * @param point    the canvas point to click, or null to choose one now.
     * @return true if the widget resolved and the action was dispatched, false otherwise.
     */
    public boolean interact(int widgetId, int childId, int itemId, int action, Point point) {
        Context ctx = ctxProvider.get();
        Widget widget = ctx.getWidget(widgetId);
        if (widget == null) {
            log.error("Failed to resolve widget for interaction: widgetId={}", widgetId);
            return false;
        }

        Point pt = point != null ? point : pointOn(widget);
        int worldView = ctx.getClient().getTopLevelWorldView().getId();

        // Resolve the display name for this action index (1-based)
        String actionName = "";
        String[] actions = widget.getActions();
        if (actions != null && action > 0 && action <= actions.length) {
            String candidate = actions[action - 1];
            if (candidate != null && !candidate.isEmpty()) {
                actionName = candidate;
            }
        }

        if (actionName.isEmpty()) {
            log.warn("Failed to resolve action name for action index={} on widgetId={}", action, widgetId);
        }

        String target = widget.getName();
        if (target == null || target.isBlank()) {
            target = widget.getText();
        }

        MenuOption option = new MenuOption(MenuAction.CC_OP, action, childId, widgetId, itemId, worldView);
        return dispatcher.dispatch(pt, actionName, new ResolvedMenuAction(option, target == null ? "" : Text.removeTags(target)));
    }

    /**
     * @param widget a widget.
     * @param action the 1-based action index.
     * @return the widget's text for that action, or null when it has none.
     */
    private static String actionName(Widget widget, int action) {
        String[] actions = widget.getActions();
        if (actions == null || action <= 0 || action > actions.length) {
            return null;
        }
        String name = actions[action - 1];
        return name == null || name.isEmpty() ? null : name;
    }

    /**
     * The default click point. With no sampler registered this is {@link UIService#getClickingPoint}
     * on the widget bounds, which is what {@code ClickPoints.choose} returned.
     */
    private Point pointOn(Widget widget) {
        Rectangle bounds = widget == null ? UIService.getDefaultRectangle() : UIService.getWidgetClickbox(widget);
        return UIService.getClickingPoint(bounds, true);
    }
}
