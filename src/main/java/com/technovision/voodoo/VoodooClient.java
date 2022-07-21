package com.technovision.voodoo;

import com.technovision.voodoo.registry.ModScreens;
import com.technovision.voodoo.screens.PoppetShelfScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * Client side initialization of Voodoo mod.
 * Used to register screens on the client side only.
 *
 * @author TechnoVision
 */
public class VoodooClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.POPPET_SHELF_SCREEN_HANDLER, PoppetShelfScreen::new);
    }
}
