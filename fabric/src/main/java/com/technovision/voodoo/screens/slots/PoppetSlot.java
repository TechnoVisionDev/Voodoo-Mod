package com.technovision.voodoo.screens.slots;

import com.technovision.voodoo.items.PoppetItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class PoppetSlot extends Slot {

    public PoppetSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof PoppetItem;
    }
}
