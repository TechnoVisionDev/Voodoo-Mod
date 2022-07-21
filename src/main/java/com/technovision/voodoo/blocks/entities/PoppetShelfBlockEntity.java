package com.technovision.voodoo.blocks.entities;

import com.technovision.voodoo.registry.ModBlockEntities;
import com.technovision.voodoo.screens.PoppetShelfScreenHandler;
import com.technovision.voodoo.util.ImplementedInventory;
import com.technovision.voodoo.util.PoppetUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PoppetShelfBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private UUID ownerUuid;
    private String ownerName;
    private boolean inventoryTouched;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public PoppetShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POPPET_SHELF_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PoppetShelfBlockEntity entity) {
        if (!world.isClient() && entity.inventoryTouched) {
            entity.inventoryTouched = false;
            entity.markDirty();
        }
    }

    public void inventoryTouched() {
        this.inventoryTouched = true;
        PoppetUtil.invalidateShelfCache(PoppetShelfBlockEntity.this);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        if (this.ownerUuid != ownerUuid) {
            PoppetUtil.removePoppetShelf(this.ownerUuid, this);
            this.ownerUuid = ownerUuid;
            PoppetUtil.addPoppetShelf(this.ownerUuid, this);
            this.markDirty();
        }
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        this.markDirty();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        MutableText component;
        if (this.getOwnerName() == null) {
            component = Text.translatable("text.voodoo.poppet.not_bound");
        } else {
            final PlayerEntity player = world.getPlayerByUuid(this.getOwnerUuid());
            if (player != null && !this.getOwnerName().equals(player.getName().getString()))
                this.setOwnerName(player.getName().getString());
            component = Text.literal(this.getOwnerName());
        }
        return Text.translatable("screen.voodoo.poppet_shelf", component);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new PoppetShelfScreenHandler(syncId, inv, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        if (ownerUuid != null)
            nbt.putUuid("owner_uuid", ownerUuid);
        if (ownerName != null)
            nbt.putString("owner_name", ownerName);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        PoppetUtil.removePoppetShelf(this.ownerUuid, this);
        if (nbt.containsUuid("owner_uuid"))
            this.ownerUuid = nbt.getUuid("owner_uuid");
        if (nbt.contains("owner_name"))
            this.ownerName = nbt.getString("owner_name");
        PoppetUtil.addPoppetShelf(this.ownerUuid, this);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        PoppetUtil.removePoppetShelf(this.ownerUuid, this);
    }

    @Override
    public void onClose(PlayerEntity player) {
        PoppetShelfBlockEntity.this.inventoryTouched();
    }
}
