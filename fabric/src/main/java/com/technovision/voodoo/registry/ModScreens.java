package com.technovision.voodoo.registry;
import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.screens.PoppetShelfScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
public class ModScreens {
    public static final MenuType<PoppetShelfScreenHandler> POPPET_SHELF_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, Voodoo.id("poppet_shelf"), new MenuType<>(PoppetShelfScreenHandler::new, FeatureFlags.DEFAULT_FLAGS));
    public static void registerScreens() {}
}
