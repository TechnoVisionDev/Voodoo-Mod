package com.technovision.voodoo.util;
import com.technovision.voodoo.items.PoppetItem;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public interface ImplementedInventory extends WorldlyContainer {
    NonNullList<ItemStack> getItems();
    @Override default int getContainerSize() { return getItems().size(); }
    @Override default boolean isEmpty() { return getItems().stream().allMatch(ItemStack::isEmpty); }
    @Override default ItemStack getItem(int slot) { return getItems().get(slot); }
    @Override default ItemStack removeItem(int slot, int count) { var result = ContainerHelper.removeItem(getItems(), slot, count); if (!result.isEmpty()) setChanged(); return result; }
    @Override default ItemStack removeItemNoUpdate(int slot) { var result = ContainerHelper.takeItem(getItems(), slot); setChanged(); return result; }
    @Override default void setItem(int slot, ItemStack stack) { getItems().set(slot, stack); stack.limitSize(getMaxStackSize(stack)); setChanged(); }
    @Override default void clearContent() { getItems().clear(); setChanged(); }
    @Override default int[] getSlotsForFace(Direction side) { return java.util.stream.IntStream.range(0, getContainerSize()).toArray(); }
    @Override default boolean canPlaceItem(int slot, ItemStack stack) { return stack.getItem() instanceof PoppetItem; }
    @Override default boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return canPlaceItem(slot, stack); }
    @Override default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }
}
