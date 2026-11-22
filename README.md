# Prayer Flicker

Quick-prayer flicker for vanilla RuneLite 1.13.7 on Java 11. The plugin name in the client is `[VJ]Prayer Flicker`.

The quick-prayer behavior comes from Ethan Vann's Prayer Flicker in PiggyPlugins. Clicks go through Kraken API 5.4.1. This project is AGPL-3.0 because the plugin derives from that AGPL code. The vendored Kraken sources remain GPL-3.0. See `NOTICE` and `LICENSE`.

The saved-settings key is still `KrakenPrayerFlicker`, so a hotkey saved by the earlier sideload build is kept.

## Build

Java 11 or newer. This was built with JDK 21 and emits class file version 55.

```bash
./gradlew shadedJarJava11
```

`./gradlew` runs that task. The jar is `build/libs/VJPrayerFlicker.jar`.

`./gradlew shadedJar` writes `VJPrayerFlicker-java17.jar`, which shades the published Java 17 Kraken binary and does not load on a Java 11 JRE. `./gradlew jar` writes `VJPrayerFlicker-kraken-client.jar`, plugin classes only, for a Kraken client that already provides the API.

## Install

1. Turn on RuneLite developer mode.
2. Copy `VJPrayerFlicker.jar` into `~/.runelite/sideloaded-plugins`.
3. Start RuneLite 1.13.7. On Windows this is the bundled Temurin 11 JRE.
4. Enable `[VJ]Prayer Flicker` and set its toggle hotkey.

`hooks.json` is the unmodified Kraken 5.4.1 resource, mapped for RuneLite 1.13.7, game revision 242. The jar does not download hooks.
