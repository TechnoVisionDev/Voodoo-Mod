package com.technovision.voodoo;

import com.technovision.voodoo.events.VoodooEvents;
import com.technovision.voodoo.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Voodoo.MOD_ID)
public class Voodoo {
    // Keep the original namespace so items, recipes and saved bindings retain their IDs.
    public static final String MOD_ID = "voodoo";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    public Voodoo(IEventBus modBus) {
        modBus.addListener(Voodoo::register);
        NeoForge.EVENT_BUS.addListener(VoodooEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(VoodooEvents::onServerStopped);
        NeoForge.EVENT_BUS.addListener(VoodooEvents::onLivingDeath);
    }

    private static void register(RegisterEvent event) {
        var key = event.getRegistryKey();
        if (key.equals(Registries.BLOCK)) ModBlocks.registerBlocks();
        else if (key.equals(Registries.ITEM)) ModItems.registerItems();
        else if (key.equals(Registries.BLOCK_ENTITY_TYPE)) ModBlockEntities.registerBlockEntities();
        else if (key.equals(Registries.MENU)) ModScreens.registerScreens();
        else if (key.equals(Registries.RECIPE_SERIALIZER)) ModRecipes.registerRecipes();
        else if (key.equals(Registries.SOUND_EVENT)) ModSounds.registerSounds();
        else if (key.equals(Registries.CREATIVE_MODE_TAB)) {
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("voodoo_group"), CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.voodoo.voodoo_group"))
                .icon(() -> ModItems.poppetMap.get(Poppet.PoppetType.BLANK).getDefaultInstance())
                .displayItems((parameters, output) -> {
                    for (var type : Poppet.PoppetType.values()) output.accept(ModItems.poppetMap.get(type));
                    output.accept(ModItems.NEEDLE);
                    output.accept(ModItems.TAGLOCK_KIT);
                    output.accept(ModItems.POPPET_SHELF);
                    output.accept(ModItems.VOODOO_MANUAL);
                }).build());
        }
    }
}
