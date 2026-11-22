package com.piggyplugins.krakenprayerflicker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.kraken.api.Context;
import com.kraken.api.core.interaction.InteractionManager;
import com.kraken.api.service.prayer.PrayerService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

/**
 * Quick-prayer flicker for the Kraken client.
 *
 * <p>Behavior follows Ethan Vann's Prayer Flicker, maintained by Piggy Plugins
 * ({@code com.example.PrayerFlicker.EthanPrayerFlickerPlugin}). Clicks go through
 * Kraken API 5.4.1 ({@link PrayerService} and {@link InteractionManager}) instead
 * of PacketUtils. Kraken API is GPL-3.0: https://github.com/Kraken-Plugins/kraken-api
 */
@PluginDescriptor(
        name = "[VJ]Prayer Flicker",
        description = "[VJ]Prayer Flicker",
        enabledByDefault = false,
        tags = {"ethan", "kraken", "prayer"}
)
@Slf4j
public class KrakenPrayerFlickerPlugin extends Plugin {
    @Inject
    private Context ctx;
    @Inject
    private PrayerService prayerService;
    @Inject
    private InteractionManager interactionManager;
    @Inject
    private KeyManager keyManager;
    @Inject
    private KrakenPrayerFlickerConfig config;

    /**
     * Written from the AWT thread (hotkey, plugin shutdown) and read on the client thread.
     */
    private volatile boolean toggle;

    @Provides
    public KrakenPrayerFlickerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(KrakenPrayerFlickerConfig.class);
    }

    @Override
    public void startUp() {
        keyManager.registerKeyListener(prayerToggle);
    }

    @Override
    public void shutDown() {
        log.info("Shutdown");
        keyManager.unregisterKeyListener(prayerToggle);
        toggle = false;
        // PluginManager.stopPlugin runs on the EDT. disableQuickPrayers waits on the
        // client thread, so hand it off instead of blocking shutdown.
        disableQuickPrayersOnClientThread();
    }

    /**
     * Click one quick-prayer slot, then toggle the quick-prayer orb twice so the
     * on/off state is unchanged. Same sequence as
     * {@code EthanPrayerFlickerPlugin.switchAndUpdatePrayers}.
     */
    public void switchAndUpdatePrayers(int quickPrayerIndex) {
        interactionManager.interact(InterfaceID.Quickprayer.BUTTONS, quickPrayerIndex, -1, 1);
        prayerService.toggleQuickPrayers();
        prayerService.toggleQuickPrayers();
    }

    /**
     * Two orb toggles, so quick prayers end in the state they started in.
     * {@link PrayerService#flickQuickPrayers()} is not used here: when quick prayers
     * are off it only enables them once.
     */
    public void updatePrayers() {
        prayerService.toggleQuickPrayers();
        prayerService.toggleQuickPrayers();
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (toggle) {
            if (event.getParam1() == InterfaceID.Quickprayer.BUTTONS) {
                if ("Quick Prayer Update".equals(event.getMenuOption())) {
                    updatePrayers();
                    event.consume();
                    return;
                }
                event.consume();
                switchAndUpdatePrayers(event.getParam0());
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!loggedIn() || !toggle) {
            return;
        }
        if (prayerService.isOutOfPrayer()) {
            return;
        }
        // Same tick sequence as EthanPrayerFlickerPlugin.onGameTick:
        // off then on when already active, otherwise just turn them on.
        prayerService.flickQuickPrayers();
    }

    private final HotkeyListener prayerToggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggleFlicker();
        }
    };

    public void toggleFlicker() {
        toggle = !toggle;
        if (!toggle) {
            disableQuickPrayersOnClientThread();
        }
    }

    public void toggleFlicker(boolean on) {
        toggle = on;
        if (!toggle) {
            disableQuickPrayersOnClientThread();
        }
    }

    /**
     * {@link PrayerService#disableQuickPrayers()} reads quick-prayer state through
     * {@code Context.runOnClientThread}, which blocks the caller for up to
     * {@code Context.CLIENT_THREAD_TIMEOUT_MS}. The hotkey listener runs on the EDT,
     * so that wait freezes the client. Queue the call when we are not already on the
     * client thread. On the client thread, run it directly: Kraken's gateway executes
     * inline there and does not wait.
     */
    private void disableQuickPrayersOnClientThread() {
        Runnable disable = () -> {
            if (loggedIn()) {
                prayerService.disableQuickPrayers();
            }
        };
        if (ctx.getClient().isClientThread()) {
            disable.run();
            return;
        }
        ctx.getClientThread().invokeLater(disable);
    }

    private boolean loggedIn() {
        return ctx.getClient().getGameState() == GameState.LOGGED_IN;
    }
}
