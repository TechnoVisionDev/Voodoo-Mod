package com.technovision.voodoo;

import com.technovision.voodoo.events.ClientEvents;
import com.technovision.voodoo.events.VoodooEvents;
import com.technovision.voodoo.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Voodoo Poppet Mod
 *
 * @version 1.0.0
 * @author TechnoVision
 */
public class Voodoo implements ModInitializer {

    public static final String MOD_ID = "voodoo";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "voodoo_group"),
            () -> new ItemStack(ModItems.poppetMap.get(Poppet.PoppetType.BLANK))
    );

    @Override
    public void onInitialize() {
        ModItems.registerItems();
        ModBlocks.registerBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreens.registerScreens();
        ModRecipes.registerRecipes();
        ModSounds.registerSounds();
        VoodooEvents.onServerTickEvent();
        VoodooEvents.onPlayerDeathEvent();
        ClientEvents.propertyOverrideRegistry();
    }
}
