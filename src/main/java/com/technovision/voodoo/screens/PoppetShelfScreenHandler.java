package com.technovision.voodoo.screens;

import com.technovision.voodoo.registry.ModScreens;
import com.technovision.voodoo.screens.slots.PoppetSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class PoppetShelfScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    public PoppetShelfScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public PoppetShelfScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreens.POPPET_SHELF_SCREEN_HANDLER, syncId);
        checkSize(inventory, 9);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new PoppetSlot(inventory, 0, 62, 17));
        this.addSlot(new PoppetSlot(inventory, 1, 80, 17));
        this.addSlot(new PoppetSlot(inventory, 2, 98, 17));
        this.addSlot(new PoppetSlot(inventory, 3, 62, 35));
        this.addSlot(new PoppetSlot(inventory, 4, 80, 35));
        this.addSlot(new PoppetSlot(inventory, 5, 98, 35));
        this.addSlot(new PoppetSlot(inventory, 6, 62, 53));
        this.addSlot(new PoppetSlot(inventory, 7, 80, 53));
        this.addSlot(new PoppetSlot(inventory, 8, 98, 53));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
