package com.technovision.voodoo.client;

import com.technovision.voodoo.Voodoo;
import vazkii.patchouli.api.PatchouliAPI;

public final class VoodooManual {
    private VoodooManual() {}
    public static void open() { PatchouliAPI.get().openBookGUI(Voodoo.id("voodoo_manual")); }
}
