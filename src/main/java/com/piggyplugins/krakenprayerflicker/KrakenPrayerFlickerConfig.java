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
            keyName = "Toggle",
            name = "Toggle",
            description = ""
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }
}
