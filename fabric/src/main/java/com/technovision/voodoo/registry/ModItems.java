package com.technovision.voodoo.registry;
import com.technovision.voodoo.*;
import com.technovision.voodoo.items.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import java.util.*;
public class ModItems {
    public static Item.Properties properties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Voodoo.id(name)));
    }
    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Voodoo.id(name), item);
    }
    public static final Item NEEDLE = register("needle", new Item(properties("needle")));
    public static final TaglockKitItem TAGLOCK_KIT = register("taglock_kit", new TaglockKitItem(properties("taglock_kit").stacksTo(8)));
    public static final BlockItem POPPET_SHELF = register("poppet_shelf", new BlockItem(ModBlocks.POPPET_SHELF_BLOCK, properties("poppet_shelf").useBlockDescriptionPrefix()));
    public static final Item VOODOO_MANUAL = register("voodoo_manual", new VoodooManualItem(properties("voodoo_manual").stacksTo(1)));
    public static final Map<Poppet.PoppetType, PoppetItem> poppetMap = new EnumMap<>(Poppet.PoppetType.class);
    public static void registerItems() {
        for (var type : Poppet.PoppetType.values()) {
            String name = type.name().toLowerCase(Locale.ROOT) + "_poppet";
            Item.Properties properties = properties(name).stacksTo(1);
            if (type.hasDurability()) properties.durability(type.getDurability());
            properties.rarity(type == Poppet.PoppetType.VOODOO_PROTECTION || type == Poppet.PoppetType.REFLECTOR ? Rarity.RARE : type == Poppet.PoppetType.DEATH_PROTECTION ? Rarity.UNCOMMON : Rarity.COMMON);
            PoppetItem item = switch (type) {
                case VOODOO -> new VoodooPoppetItem(properties);
                case VAMPIRIC -> new VampiricPoppetItem(properties);
                default -> new PoppetItem(type, properties);
            };
            poppetMap.put(type, register(name, item));
        }
    }
}
