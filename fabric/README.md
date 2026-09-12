# Voodoo Poppets: Reborn — Fabric 26.2

This standalone subfolder preserves the original Fabric 26.2 implementation and game tests from commit `a84edf8`, with the updated mod name. The primary NeoForge 26.1.2 project lives in the parent folder.

Requires Java 25, Minecraft 26.2, Fabric Loader 0.19.3 or later, and Fabric API 0.160.0+26.2 or later.

From the repository root:

```sh
./gradlew -p fabric build
```

Or run `./gradlew build` within this directory. The output is `build/libs/voodoo-poppets-reborn-2.0.0+26.2.jar`, relative to this folder. Use `gradlew.bat` on Windows.

The Fabric edition retains its existing built-in Voodoo Manual. The parent NeoForge edition includes the Patchouli guidebook.

Original Fabric version by TechnoVision, based on the mod by HialusFX. Licensed under LGPL-3.0; see LICENSE.
