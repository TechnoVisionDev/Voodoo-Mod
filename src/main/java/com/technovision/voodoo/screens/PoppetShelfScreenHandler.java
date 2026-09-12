package com.technovision.voodoo.screens;

import com.technovision.voodoo.registry.ModScreens;
import com.technovision.voodoo.screens.slots.PoppetSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class PoppetShelfScreenHandler extends AbstractContainerMenu {

    private final Container inventory;

    public PoppetShelfScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(9));
    }

    public PoppetShelfScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
        super(ModScreens.POPPET_SHELF_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, 9);
        this.inventory = inventory;
        inventory.startOpen(playerInventory.player);

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
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override public void removed(Player player) { super.removed(player); inventory.stopOpen(player); }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
