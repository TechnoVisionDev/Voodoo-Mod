package com.technovision.voodoo;

import com.technovision.voodoo.client.PoppetShelfRenderer;
import com.technovision.voodoo.registry.*;
import com.technovision.voodoo.screens.PoppetShelfScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Voodoo.MOD_ID, dist = Dist.CLIENT)
public class VoodooClient {
    public VoodooClient(IEventBus modBus) {
        modBus.addListener(VoodooClient::registerScreens);
        modBus.addListener(VoodooClient::registerRenderers);
    }
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModScreens.POPPET_SHELF_SCREEN_HANDLER, PoppetShelfScreen::new);
    }
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.POPPET_SHELF_ENTITY, PoppetShelfRenderer::new);
    }
}
