package com.piggyplugins.krakenprayerflicker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

/**
 * Same options as PiggyPlugins {@code PrayerFlickerConfig}. The group name is
 * separate so this Kraken plugin does not overwrite the vanilla plugin's saved keys.
 */
@ConfigGroup("KrakenPrayerFlicker")
public interface KrakenPrayerFlickerConfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "Toggle",
            name = "Toggle",
            description = ""
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            position = 1,
            keyName = "pauseWhileSelected",
            name = "Pause while spell/item selected",
            description = "Leave this on to keep High Alchemy and other targeted spells from being cancelled. Turning it off flicks every tick, which cancels a spell or item you have selected."
    )
    default boolean pauseWhileSelected() {
        return true;
    }
}
