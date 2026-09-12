package com.technovision.voodoo;
import com.technovision.voodoo.client.*;
import com.technovision.voodoo.items.VoodooManualItem;
import com.technovision.voodoo.registry.*;
import com.technovision.voodoo.screens.PoppetShelfScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
public class VoodooClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MenuScreens.register(ModScreens.POPPET_SHELF_SCREEN_HANDLER, PoppetShelfScreen::new);
        BlockEntityRenderers.register(ModBlockEntities.POPPET_SHELF_ENTITY, PoppetShelfRenderer::new);
        VoodooManualItem.openGuide = VoodooManual::open;
    }
}
