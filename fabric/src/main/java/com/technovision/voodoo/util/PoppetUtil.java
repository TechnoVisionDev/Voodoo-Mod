package com.technovision.voodoo.util;
import com.technovision.voodoo.*;
import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.items.PoppetItem;
import com.technovision.voodoo.registry.ModSounds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import java.util.*;
public class PoppetUtil {
    // Only loaded shelves belong here. Never cache ItemStacks or players across inventory changes/respawns.
    private static final Set<PoppetShelfBlockEntity> shelves = Collections.newSetFromMap(new WeakHashMap<>());
    public static Poppet getPlayerPoppet(ServerPlayer player, Poppet.PoppetType type) {
        for (Poppet p : getPoppetsInInventory(player)) if (p.getItem().getPoppetType() == type) return p;
        for (Poppet p : getPoppetsInShelves(player)) if (p.getItem().getPoppetType() == type) return p;
        return null;
    }
    public static void damageStack(ItemStack stack, int damage, Player player) {
        if (stack.isEmpty()) return;
        stack.setDamageValue(stack.getDamageValue() + damage);
        if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1);
    }
    public static void useVoodooProtectionPuppet(ItemStack stack, Entity source) {
        if (source instanceof Player player) player.sendOverlayMessage(Component.translatable("text.voodoo.voodoo_protection.had", BindingUtil.getBoundName(stack)));
        source.level().playSound(null, source.getX(), source.getY(), source.getZ(), ModSounds.VOODOO_PROTECTION_POPPET_USED, SoundSource.PLAYERS, 1, 1);
        stack.shrink(1);
    }
    public static List<Poppet> getPoppetsInInventory(Player player) {
        List<Poppet> result = new ArrayList<>();
        add(result, player.getOffhandItem(), player, null);
        for (int i = 0; i < 36; i++) add(result, player.getInventory().getItem(i), player, null);
        return result;
    }
    private static void add(List<Poppet> result, ItemStack stack, Player player, PoppetShelfBlockEntity shelf) {
        if (!stack.isEmpty() && stack.getItem() instanceof PoppetItem item && player.getUUID().equals(BindingUtil.getBoundUUID(stack)))
            result.add(shelf == null ? new Poppet(player, item, stack) : new Poppet(shelf, player, item, stack));
    }
    public static List<Poppet> getPoppetsInShelves(ServerPlayer player) {
        List<Poppet> result = new ArrayList<>();
        for (var shelf : shelves) {
            if (!shelf.isRemoved() && shelf.getLevel() != null && shelf.getLevel().getServer() == player.level().getServer()
                && shelf.getLevel().hasChunkAt(shelf.getBlockPos()) && player.getUUID().equals(shelf.getOwnerUuid()))
                for (var stack : shelf.getItems()) add(result, stack, player, shelf);
        }
        return result;
    }
    public static void addPoppetShelf(UUID owner, PoppetShelfBlockEntity shelf) { if (shelf.getLevel() != null && !shelf.getLevel().isClientSide()) shelves.add(shelf); }
    public static void removePoppetShelf(UUID owner, PoppetShelfBlockEntity shelf) { shelves.remove(shelf); }
    public static void invalidateShelfCache(PoppetShelfBlockEntity shelf) { }
    public static void clear() { shelves.clear(); }
}
