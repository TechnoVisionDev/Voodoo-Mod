package com.technovision.voodoo.registry;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.items.PoppetItem;
import com.technovision.voodoo.items.TaglockKitItem;
import com.technovision.voodoo.items.VampiricPoppetItem;
import com.technovision.voodoo.items.VoodooPoppetItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

import static com.technovision.voodoo.Poppet.PoppetType.VAMPIRIC;
import static com.technovision.voodoo.Poppet.PoppetType.VOODOO;

/**
 * Creates and registers mod items.
 *
 * @author TechnoVision
 */
public class ModItems {

    public static final Item NEEDLE = new Item(new FabricItemSettings().group(Voodoo.ITEM_GROUP));
    public static final TaglockKitItem TAGLOCK_KIT = new TaglockKitItem();
    public static final BlockItem POPPET_SHELF = new BlockItem(ModBlocks.POPPET_SHELF_BLOCK, new FabricItemSettings().group(Voodoo.ITEM_GROUP));
    public static final Map<Poppet.PoppetType, PoppetItem> poppetMap = new HashMap<>();

    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, "needle"), NEEDLE);
        Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, "taglock_kit"), TAGLOCK_KIT);
        Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, "poppet_shelf"), POPPET_SHELF);

        // Register all poppets
        for (Poppet.PoppetType poppetType : Poppet.PoppetType.values()) {
            PoppetItem poppetItem;
            if (poppetType == VOODOO) {
                poppetItem = new VoodooPoppetItem();
            } else if (poppetType == VAMPIRIC) {
                poppetItem = new VampiricPoppetItem();
            } else {
                poppetItem = new PoppetItem(poppetType);
            }
            String poppetName = poppetType.name().toLowerCase() + "_poppet";
            Registry.register(Registry.ITEM, new Identifier(Voodoo.MOD_ID, poppetName), poppetItem);
            poppetMap.put(poppetType, poppetItem);
        }
    }
}
