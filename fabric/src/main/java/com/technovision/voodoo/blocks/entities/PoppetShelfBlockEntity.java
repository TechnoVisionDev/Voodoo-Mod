package com.technovision.voodoo.blocks.entities;
import com.technovision.voodoo.registry.ModBlockEntities;
import com.technovision.voodoo.screens.PoppetShelfScreenHandler;
import com.technovision.voodoo.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import java.util.*;
public class PoppetShelfBlockEntity extends BlockEntity implements MenuProvider, ImplementedInventory {
    private UUID ownerUuid;
    private String ownerName;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
    public PoppetShelfBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.POPPET_SHELF_ENTITY, pos, state); }
    @Override public NonNullList<ItemStack> getItems() { return inventory; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerUuid(UUID uuid) { ownerUuid = uuid; PoppetUtil.addPoppetShelf(uuid, this); setChanged(); }
    public void setOwnerName(String name) { ownerName = name; setChanged(); }
    public void inventoryTouched() { setChanged(); }
    @Override public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override public void setLevel(Level level) { super.setLevel(level); PoppetUtil.addPoppetShelf(ownerUuid, this); }
    @Override public void clearRemoved() { super.clearRemoved(); PoppetUtil.addPoppetShelf(ownerUuid, this); }
    @Override public void setRemoved() { PoppetUtil.removePoppetShelf(ownerUuid, this); super.setRemoved(); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState newState) {
        if (level != null && !level.isClientSide()) { Containers.dropContents(level, worldPosition, this); level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock()); }
        super.preRemoveSideEffects(pos, newState);
    }
    @Override public Component getDisplayName() {
        if (ownerUuid != null && level != null) {
            Player owner = level.getPlayerByUUID(ownerUuid);
            if (owner != null && !owner.getName().getString().equals(ownerName)) setOwnerName(owner.getName().getString());
        }
        return Component.translatable("screen.voodoo.poppet_shelf", ownerName == null ? Component.translatable("text.voodoo.poppet.not_bound") : Component.literal(ownerName));
    }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new PoppetShelfScreenHandler(id, inventory, this); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); ContainerHelper.saveAllItems(output, inventory);
        if (ownerUuid != null) output.store("owner_uuid", UUIDUtil.CODEC, ownerUuid);
        if (ownerName != null) output.putString("owner_name", ownerName);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); inventory.clear(); ContainerHelper.loadAllItems(input, inventory);
        ownerUuid = input.read("owner_uuid", UUIDUtil.CODEC).orElse(null);
        ownerName = input.getString("owner_name").orElse(null);
        PoppetUtil.addPoppetShelf(ownerUuid, this);
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
}
