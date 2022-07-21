package com.technovision.voodoo.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Useful methods for binding a user to NBT data tag,
 *
 * @author TechnoVision
 */
public class BindingUtil {

    // Constants used for NBT tags
    public static final String BOUND_UUID = "BoundUUID";
    public static final String BOUND_NAME = "BoundName";

    /**
     * Binds an ItemStack to a player.
     *
     * @param itemStack    The ItemStack that should be bound
     * @param playerEntity The player to bind it to
     */
    public static void bind(ItemStack itemStack, PlayerEntity playerEntity) {
        bind(itemStack, playerEntity.getUuid(), playerEntity.getName().getString());
    }

    /**
     * Binds an ItemStack to a UUID and name.
     *
     * @param itemStack The ItemStack that should be bound
     * @param uuid      The UUID to bind it to
     * @param name      The name to bind it to
     */
    public static void bind(ItemStack itemStack, UUID uuid, String name) {
        final NbtCompound tag = itemStack.getOrCreateNbt();
        tag.putUuid(BOUND_UUID, uuid);
        tag.putString(BOUND_NAME, name);
    }

    /**
     * Unbinds an ItemStack
     *
     * @param itemStack The ItemStack that should be unbound
     */
    public static void unbind(ItemStack itemStack) {
        final NbtCompound tag = itemStack.getOrCreateNbt();
        tag.remove(BOUND_UUID);
        tag.remove(BOUND_NAME);
    }

    /**
     * Transfers the binding from one ItemStack to another.
     * This is used for the binding crafting recipe.
     *
     * @param from The source (Taglock Kit)
     * @param to   The target (Poppet)
     */
    public static void transfer(ItemStack from, ItemStack to) {
        if (!isBound(from)) return;
        bind(to, from.getNbt().getUuid(BOUND_UUID), from.getNbt().getString(BOUND_NAME));
    }

    /**
     * This method checks if the name of the bound player has changed.
     * If the bound player is online and his name has changed then the NBT tag will be updated.
     *
     * @param stack The bound ItemStack
     * @param world The world instance used to check
     */
    public static void checkForNameUpdate(ItemStack stack, World world) {
        if (world != null && isBound(stack)) {
            PlayerEntity player = world.getPlayerByUuid(getBoundUUID(stack));
            if (player != null) {
                final String playerName = player.getName().getString();
                if (!playerName.equals(getBoundName(stack))) {
                    stack.getNbt().putString(BOUND_NAME, playerName);
                }
            }
        }
    }

    /**
     * Retrieves the player that is bound to the item.
     * If the ItemStack is not bound or the player is not online it will return null.
     *
     * @param stack The bound ItemStack
     * @param world The world to retrieve the player
     * @return The bound player or null
     */
    public static PlayerEntity getBoundPlayer(ItemStack stack, World world) {
        if (isBound(stack)) {
            return world.getPlayerByUuid(getBoundUUID(stack));
        }
        return null;
    }

    /**
     * Checks if the given ItemStack has been bound to a player.
     *
     * @param stack The ItemStack to check
     * @return true if the ItemStack is bound, else false
     */
    public static boolean isBound(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().containsUuid(BOUND_UUID) && stack.getNbt().getUuid(BOUND_UUID) != null;
    }

    /**
     * Retrieves the UUID of the bound player.
     * If the ItemStack is not bound it will return null.
     *
     * @param stack The bound ItemStack
     * @return The UUID of the bound player or null
     */
    public static UUID getBoundUUID(ItemStack stack) {
        if (isBound(stack))
            return stack.getNbt().getUuid(BOUND_UUID);
        else
            return null;
    }

    /**
     * Retrieves the name of the bound player.
     * If the ItemStack is not bound it will return null.
     *
     * @param stack The bound ItemStack
     * @return The name of the bound player or null
     */
    public static String getBoundName(ItemStack stack) {
        if (isBound(stack))
            return stack.getNbt().getString(BOUND_NAME);
        else
            return null;
    }
}
