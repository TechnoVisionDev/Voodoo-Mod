package com.technovision.voodoo.registry;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.items.TaglockKitItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Creates and registers mod items.
 * @author TechnoVision
 */
public class ModItems {

    public static final Item NEEDLE = new Item(new FabricItemSettings().group(Voodoo.ITEM_GROUP));
    public static final TaglockKitItem TAGLOCK_KIT = new TaglockKitItem();
    public static final Item POPPET = new Item(new FabricItemSettings().group(Voodoo.ITEM_GROUP).maxCount(1));

    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, "needle"), NEEDLE);
        Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, "taglock_kit"), TAGLOCK_KIT);
        Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, "poppet"), POPPET);
    }
}
