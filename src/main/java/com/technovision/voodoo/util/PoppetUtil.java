package com.technovision.voodoo.util;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.items.PoppetItem;
import com.technovision.voodoo.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Useful methods for accessing and using poppets.
 *
 * @author TechnoVision
 */
public class PoppetUtil {

    private static final Map<UUID, List<WeakReference<PoppetShelfBlockEntity>>> poppetShelvesCache;
    private static final WeakHashMap<PoppetShelfBlockEntity, List<Poppet>> poppetCache;

    static {
        poppetShelvesCache = new HashMap<>();
        poppetCache = new WeakHashMap<>();
    }

    /**
     * Searches for a specific poppet that is bound to a player.
     *
     * @param player     The player that the poppet must be bound to
     * @param poppetType The type of the poppet
     * @return The found poppet or null
     */
    public static Poppet getPlayerPoppet(ServerPlayerEntity player, Poppet.PoppetType poppetType) {
        return getPoppetsInInventory(player)
                .stream()
                .filter(poppet -> poppet.getItem().getPoppetType() == poppetType)
                .findFirst()
                .orElseGet(() -> getPoppetsInShelves(player)
                    .stream()
                    .filter(poppet -> poppet.getItem().getPoppetType() == poppetType)
                    .findFirst()
                    .orElse(null));
    }

    /**
     * Code for handling all the events that happen when a voodoo protection poppet is activated.
     * If the voodoo poppet was used by a player they will get a message telling them that the target had a protection poppet.
     * If the voodoo poppet was used by the item entity it will not send such a message.
     * In both cases the voodoo poppet that was used will be destroyed.
     *
     * @param voodooPoppet The voodoo poppet that was used
     * @param source       The entity that used the voodoo poppet
     */
    public static void useVoodooProtectionPuppet(ItemStack voodooPoppet, Entity source) {
        if (source instanceof final PlayerEntity fromPlayer) {
            fromPlayer.sendMessage(Text.translatable("text.voodoo.voodoo_protection.had", BindingUtil.getBoundName(voodooPoppet)), true);
            voodooPoppet.damage(Poppet.PoppetType.VOODOO.getDurability(), fromPlayer, playerEntity -> {
                playerEntity.sendToolBreakStatus(playerEntity.getActiveHand());
                playerEntity.getWorld().playSoundFromEntity(null, playerEntity, ModSounds.VOODOO_PROTECTION_POPPET_USED, SoundCategory.PLAYERS, 1.0f, 1.0f);
            });
        } else {
            source.getWorld().playSoundFromEntity(null, source, ModSounds.VOODOO_PROTECTION_POPPET_USED, SoundCategory.PLAYERS, 1.0f, 1.0f);
            voodooPoppet.decrement(1);
        }
    }

    /**
     * Retrieve all poppets in the inventory of a player.
     *
     * @param player The player
     * @return The found poppets
     */
    public static List<Poppet> getPoppetsInInventory(PlayerEntity player) {
        List<ItemStack> playerItems = new ArrayList<>();
        playerItems.addAll(player.getInventory().offHand);
        playerItems.addAll(player.getInventory().main);
        return playerItems.stream()
                .filter(stack -> stack.getItem() instanceof PoppetItem)
                .filter(stack -> player.getUuid().equals(BindingUtil.getBoundUUID(stack)))
                .map(stack -> new Poppet(player, (PoppetItem) stack.getItem(), stack))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all poppets of a player that are in his poppet shelves.
     * This method utilises a cache, so that it does not have to iterate through all loaded TileEntities on each call.
     *
     * @param player The player
     * @return The found poppets
     */
    public static List<Poppet> getPoppetsInShelves(ServerPlayerEntity player) {
        List<WeakReference<PoppetShelfBlockEntity>> cachedShelves = poppetShelvesCache.get(player.getUuid());
        if (cachedShelves == null) {
            cachedShelves = StreamSupport
                    .stream(player.server.getWorlds().spliterator(), false)
                    .flatMap(world -> getPoppetShelvesStream(player.server))
                    .filter(poppetShelf -> player.getUuid().equals(poppetShelf.getOwnerUuid()))
                    .map(WeakReference::new)
                    .collect(Collectors.toList());
            poppetShelvesCache.put(player.getUuid(), cachedShelves);
        }
        final List<Poppet> poppets = new ArrayList<>();
        for (Iterator<WeakReference<PoppetShelfBlockEntity>> iterator = cachedShelves.iterator(); iterator.hasNext(); ) {
            WeakReference<PoppetShelfBlockEntity> cachedShelf = iterator.next();
            final PoppetShelfBlockEntity poppetShelf = cachedShelf.get();
            if (poppetShelf == null) {
                iterator.remove();
                continue;
            }
            List<Poppet> poppetList = poppetCache.get(poppetShelf);
            if (poppetList == null || poppetList.size() == 0) {
                poppetList = poppetShelf
                        .getItems()
                        .stream()
                        .filter(stack -> player.getUuid().equals(BindingUtil.getBoundUUID(stack)))
                        .map(stack -> new Poppet(poppetShelf, player, (PoppetItem) stack.getItem(), stack))
                        .collect(Collectors.toList());
                poppetCache.put(poppetShelf, poppetList);
            }
            poppets.addAll(poppetList);
        }
        return poppets;
    }

    /**
     * Clear the cached poppets of a poppet shelf.
     * Should be used everytime the inventory of a poppet shelf changes
     *
     * @param poppetShelf The poppet shelf
     */
    public static void invalidateShelfCache(PoppetShelfBlockEntity poppetShelf) {
        if (poppetShelf != null) {
            poppetCache.remove(poppetShelf);
        }
    }

    /**
     * Clear the cached poppets shelves of a player.
     * Should be used everytime a new poppet shelf of a player is created.
     *
     * @param playerUUD The UUID of the owner of the player
     */
    public static void invalidateShelvesCache(UUID playerUUD) {
        if (playerUUD != null) {
            poppetShelvesCache.remove(playerUUD);
        }
    }

    public static void removePoppetShelf(UUID ownerUUID, PoppetShelfBlockEntity poppetShelf) {
        if (ownerUUID == null || poppetShelf.getWorld() == null || poppetShelf.getWorld().isClient()) return;
        final List<WeakReference<PoppetShelfBlockEntity>> weakShelves = poppetShelvesCache.get(ownerUUID);
        if (weakShelves != null) {
            weakShelves.removeIf(weakShelf -> weakShelf.get() == null || weakShelf.get() == poppetShelf);
            if (weakShelves.isEmpty())
                poppetShelvesCache.remove(ownerUUID);
        }
    }

    public static void addPoppetShelf(UUID ownerUUID, PoppetShelfBlockEntity poppetShelf) {
        if (ownerUUID == null || (poppetShelf.getWorld() != null && poppetShelf.getWorld().isClient())) return;
        removePoppetShelf(ownerUUID, poppetShelf);
        poppetShelvesCache.putIfAbsent(ownerUUID, new ArrayList<>());
        final List<WeakReference<PoppetShelfBlockEntity>> weakShelves = poppetShelvesCache.get(ownerUUID);
        weakShelves.add(new WeakReference<>(poppetShelf));
    }

    private static Stream<PoppetShelfBlockEntity> getPoppetShelvesStream(MinecraftServer server) {
        return poppetShelvesCache.values().stream()
                .flatMap(Collection::stream)
                .map(WeakReference::get)
                .filter(Objects::nonNull);
    }
}
