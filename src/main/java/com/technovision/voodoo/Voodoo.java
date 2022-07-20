package com.technovision.voodoo;

import com.technovision.voodoo.event.ClientEvents;
import com.technovision.voodoo.registry.ModItems;
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
            new Identifier(MOD_ID, "main"),
            () -> new ItemStack(ModItems.POPPET)
    );

    @Override
    public void onInitialize() {
        ModItems.registerItems();
        ClientEvents.propertyOverrideRegistry();
    }
}
