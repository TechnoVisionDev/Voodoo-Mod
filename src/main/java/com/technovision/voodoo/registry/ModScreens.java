package com.technovision.voodoo.registry;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.screens.PoppetShelfScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Creates and registers screen handlers
 *
 * @author TechnoVision
 */
public class ModScreens {

    public static ScreenHandlerType<PoppetShelfScreenHandler> POPPET_SHELF_SCREEN_HANDLER = new ScreenHandlerType<>(PoppetShelfScreenHandler::new);

    public static void registerScreens() {
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Voodoo.MOD_ID, "poppet_shelf"), new ScreenHandlerType<>(PoppetShelfScreenHandler::new));
    }
}
