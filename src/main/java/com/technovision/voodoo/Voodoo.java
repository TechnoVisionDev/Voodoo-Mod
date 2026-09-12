package com.technovision.voodoo;
import com.technovision.voodoo.events.VoodooEvents;
import com.technovision.voodoo.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
public class Voodoo implements ModInitializer {
    public static final String MOD_ID = "voodoo";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
    @Override public void onInitialize() {
        ModBlocks.registerBlocks();
        ModItems.registerItems();
        ModBlockEntities.registerBlockEntities();
        ModScreens.registerScreens();
        ModRecipes.registerRecipes();
        ModSounds.registerSounds();
        VoodooDamageSource.register();
        VoodooEvents.onServerTickEvent();
        VoodooEvents.onPlayerDeathEvent();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("voodoo_group"), FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.voodoo.voodoo_group"))
            .icon(() -> ModItems.poppetMap.get(Poppet.PoppetType.BLANK).getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var type : Poppet.PoppetType.values()) output.accept(ModItems.poppetMap.get(type));
                output.accept(ModItems.NEEDLE); output.accept(ModItems.TAGLOCK_KIT);
                output.accept(ModItems.POPPET_SHELF); output.accept(ModItems.VOODOO_MANUAL);
            }).build());
    }
}
