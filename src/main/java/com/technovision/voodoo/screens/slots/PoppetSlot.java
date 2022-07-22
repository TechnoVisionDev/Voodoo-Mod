package com.technovision.voodoo.screens.slots;

import com.technovision.voodoo.items.PoppetItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class PoppetSlot extends Slot {

    public PoppetSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof PoppetItem;
    }
}
