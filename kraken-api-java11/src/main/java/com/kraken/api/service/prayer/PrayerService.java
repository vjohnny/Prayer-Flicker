package com.kraken.api.service.prayer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.interaction.InteractionManager;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;

/**
 * Quick-prayer controls used by [VJ]Prayer Flicker.
 *
 * <p>The flicker calls {@link #toggleQuickPrayers()}, {@link #flickQuickPrayers()},
 * {@link #disableQuickPrayers()}, and {@link #isOutOfPrayer()}. Individual prayer scheduling
 * is not part of this build.</p>
 */
@Singleton
public class PrayerService {

    @Inject
    private Context ctx;

    @Inject
    private InteractionManager interactionManager;

    /**
     * Checks if the player is out of prayer.
     * @return Returns true if the player is out of prayer and false if they still have prayer.
     */
    public boolean isOutOfPrayer() {
        return ctx.getClient().getBoostedSkillLevel(Skill.PRAYER) <= 0;
    }

    /**
     * Determines if quick prayers are currently enabled for the player.
     *
     * <p>The method checks the value of the {@literal @VarbitID.QUICKPRAYER_ACTIVE}
     * game state variable to assess whether quick prayers are active. A return value
     * of {@code true} indicates that quick prayers are enabled, while {@code false}
     * signifies that they are disabled.</p>
     *
     * @return {@code true} if quick prayers are enabled, {@code false} otherwise.
     */
    public boolean isQuickPrayerEnabled() {
        return ctx.runOnClientThread(() -> ctx.getClient().getVarbitValue(VarbitID.QUICKPRAYER_ACTIVE) == 1);
    }

    /**
     * Toggles the set quick prayers. If the quick prayers are on, they will be turned off.
     * If they are off, they will be turned on.
     */
    public void toggleQuickPrayers() {
        interactionManager.interact(InterfaceID.Orbs.PRAYERBUTTON, -1, -1, 1);
    }

    /**
     * Turns on the set quick prayers
     */
    public void enableQuickPrayers() {
        if (!isQuickPrayerEnabled()) {
            toggleQuickPrayers();
        }
    }

    /**
     * Turns off the set quick prayers
     */
    public void disableQuickPrayers() {
        if (isQuickPrayerEnabled()) {
            toggleQuickPrayers();
        }
    }

    /**
     * Performs a quick flick of the player's quick prayers.
     *
     * <p>This method is designed to activate and deactivate the player's quick prayers
     * in a single operation. If quick prayers are currently disabled, they will be
     * turned on by invoking {@code enableQuickPrayers()}. If quick prayers are already
     * enabled, the method performs a rapid sequence of toggles by calling
     * {@code toggleQuickPrayers()} twice.</p>
     *
     * <p>The behavior of this method ensures that quick prayers are briefly flicked
     * and restored, which may be used in scenarios such as prayer management or minimizing
     * prayer drain during certain game activities.</p>
     *
     * <ul>
     *   <li>If quick prayers are not enabled, they are activated, and no toggle operation is performed.</li>
     *   <li>If quick prayers are enabled, they are toggled off and back on in quick succession.</li>
     * </ul>
     *
     */
    public void flickQuickPrayers() {
        if (!isQuickPrayerEnabled()) {
            enableQuickPrayers();
            return;
        }
        toggleQuickPrayers();
        toggleQuickPrayers();
    }
}
