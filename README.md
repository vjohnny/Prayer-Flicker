# Prayer Flicker

Quick-prayer flicker for vanilla RuneLite 1.13.8 on Java 11. The plugin name in the client is `[VJ]Prayer Flicker`.

The quick-prayer behavior comes from Ethan Vann's Prayer Flicker in PiggyPlugins. Clicks go through Kraken API 5.5.1. This project is AGPL-3.0 because the plugin derives from that AGPL code. The vendored Kraken sources remain GPL-3.0. See `NOTICE` and `LICENSE`.

The saved-settings key is still `KrakenPrayerFlicker`, so a hotkey saved by the earlier sideload build is kept.

## Build

Java 11 or newer. This was built with JDK 21 and emits class file version 55.

```bash
./gradlew shadedJarJava11
```

`./gradlew` runs that task. The jar name comes from the Gradle `version`, currently `build/libs/VJPrayerFlicker-v3.jar`.

That jar is the plugin plus the Kraken types the flicker actually reaches. Quick-prayer clicks still go through `InteractionManager`, the `EVENT_MOUSE_CLICK` packet, and `DoActionInvoker`. Walker data, DPS tables, banking, the grand exchange, shops, queries, pathfinding, teleports, the virtual mouse, scripts, and the click scheduler are not in it. Login and break-handler code stay excluded. See `NOTICE`.

`./gradlew shadedJar` writes `VJPrayerFlicker-v3-java17.jar`, which shades the published Java 17 Kraken binary and does not load on a Java 11 JRE. `./gradlew jar` writes `VJPrayerFlicker-v3-kraken-client.jar`, plugin classes only, for a Kraken client that already provides the API.

## Install

1. Turn on RuneLite developer mode.
2. Copy `VJPrayerFlicker-v3.jar` into `~/.runelite/sideloaded-plugins`.
3. Start RuneLite. On Windows the bundled JRE is Temurin 11, which can load this jar.
4. Enable `[VJ]Prayer Flicker` and set its toggle hotkey.

`hooks.json` is the unmodified Kraken 5.5.1 resource, mapped for RuneLite 1.13.8, game revision 243. The jar does not download hooks. A client still on revision 242 will not match those hook names.
