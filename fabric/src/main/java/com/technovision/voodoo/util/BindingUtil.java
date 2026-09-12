package com.technovision.voodoo.util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.UUID;
public class BindingUtil {
    public static final String BOUND_UUID = "BoundUUID", BOUND_NAME = "BoundName";
    public static void bind(ItemStack stack, Player player) { bind(stack, player.getUUID(), player.getName().getString()); }
    public static void bind(ItemStack stack, UUID uuid, String name) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.store(BOUND_UUID, UUIDUtil.CODEC, uuid); tag.putString(BOUND_NAME, name); });
        // The vanilla component predicate drives the filled taglock model.
        if (stack.getItem() instanceof com.technovision.voodoo.items.TaglockKitItem)
            stack.set(DataComponents.ITEM_MODEL, com.technovision.voodoo.Voodoo.id("taglock_kit_filled"));
    }
    public static void unbind(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.remove(BOUND_UUID); tag.remove(BOUND_NAME); });
        if (stack.getItem() instanceof com.technovision.voodoo.items.TaglockKitItem) stack.remove(DataComponents.ITEM_MODEL);
    }
    public static void transfer(ItemStack from, ItemStack to) { if (isBound(from)) bind(to, getBoundUUID(from), getBoundName(from)); }
    public static UUID getBoundUUID(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().read(BOUND_UUID, UUIDUtil.CODEC).orElse(null); }
    public static boolean isBound(ItemStack stack) { return getBoundUUID(stack) != null; }
    public static String getBoundName(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr(BOUND_NAME, ""); }
    public static Player getBoundPlayer(ItemStack stack, Level level) {
        UUID id = getBoundUUID(stack);
        if (id == null) return null;
        Player player = level.getServer() == null ? level.getPlayerByUUID(id) : level.getServer().getPlayerList().getPlayer(id);
        if (player != null && !player.getName().getString().equals(getBoundName(stack))) bind(stack, player);
        return player;
    }
}
