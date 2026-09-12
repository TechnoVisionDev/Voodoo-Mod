# Voodoo Poppets: Reborn — NeoForge 26.1.2

A native NeoForge port of the Voodoo Poppets mod, preserving its fifteen poppets, recipes, taglock bindings, shelf storage and effects. Based on the Fabric version by TechnoVision and the original mod by HialusFX.

## Project layout

- The root project (`src/`, `build.gradle`) is the primary **NeoForge 26.1.2** source.
- `fabric/` is the standalone **Fabric 26.2** project, restored from the pre-port source. It includes its own source, Gradle wrapper, build settings and tests.
- Both versions display **Voodoo Poppets: Reborn** and retain the internal `voodoo` identifier.

Build NeoForge from the root with `./gradlew build`. Build Fabric with `./gradlew -p fabric build`; its output goes to `fabric/build/libs/`.

## Install

Use Minecraft **26.1.2**, **NeoForge 26.1.2.107 or a newer 26.1.2 build**, and **Java 25**. Put both files in the client and server `mods` folders:

- `voodoo-poppets-reborn-1.0.2+neoforge-26.1.2.jar`
- `patchouli-neoforge-26.1-94.jar`

The build puts the main mod in `build/libs/` and the Patchouli dependency in `build/libs/dependencies/`. Patchouli is a separate required mod; the guidebook content is included in Voodoo Poppets: Reborn.

## Features

- Blank, voodoo, vampiric, reflector, voodoo protection, death protection, fire protection, water protection, fall protection, explosion protection, projectile protection, wither protection, hunger protection, potion protection, and void protection poppets.
- Taglocks collected from yourself, another player, or a bed associated with an online player. Craft a filled taglock with an unbound, non-blank poppet to bind it.
- Needle attacks, remote pushing, dropped-poppet fire and drowning, and vampiric health transfer.
- Protection in the bound player's inventory or an owned, loaded poppet shelf. Durability, partial protection, reflection safeguards and effects retain the source implementation's behavior.
- Nine-slot shelves with persistent ownership, hopper filtering, inventory synchronization and floating poppet displays.
- All original item models, animations, sounds, translations, recipes, advancements and damage types.

The internal mod ID remains `voodoo`, preserving registry and recipe identifiers. This does not make Minecraft 26.2 worlds safe to downgrade to 26.1.2.

## Patchouli guidebook

Craft the **Voodoo Manual** with a book, needle, string and rabbit hide in any crafting arrangement. Use it to open the Patchouli guide. The book has two categories and eighteen entries covering binding, shelves, all fifteen poppet types, crafting recipes, durability and effects.

## Build and verify

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClientSmoke
```

Use `gradlew.bat` on Windows. The client smoke test needs a graphical environment; it creates its own fresh world, checks shelf synchronization and the shelf screen, opens the actual manual item, validates every guide entry, saves screenshots under `run-client-smoke/screenshots/`, and exits. Test classes are excluded from the release JAR.

Normal development runs are `./gradlew runClient` and `./gradlew runServer`.

## Credits and license

- TechnoVision: Fabric version.
- HialusFX: original Voodoo Poppets mod and artwork.
- Vazkii and Violet Moon: [Patchouli](https://github.com/VazkiiMods/Patchouli).

This mod is licensed under LGPL-3.0; see [LICENSE](LICENSE). Patchouli retains its own license.
