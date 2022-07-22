package com.technovision.voodoo.blocks.entities;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.registry.ModBlockEntities;
import com.technovision.voodoo.screens.PoppetShelfScreenHandler;
import com.technovision.voodoo.util.ImplementedInventory;
import com.technovision.voodoo.util.PoppetUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class PoppetShelfBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private UUID ownerUuid;
    private String ownerName;
    private boolean inventoryTouched;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public PoppetShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POPPET_SHELF_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PoppetShelfBlockEntity entity) {
        if ((!world.isClient() && entity.inventoryTouched)) {
            entity.markDirty();
            Collection<ServerPlayerEntity> viewers = PlayerLookup.tracking(entity);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            for (ItemStack stack : entity.getItems()) {
                buf.writeItemStack(stack);
            }
            entity.inventoryTouched = false;
            viewers.forEach(player -> ServerPlayNetworking.send(player, new Identifier(Voodoo.MOD_ID, "update"), buf));
        }
    }


    public void inventoryTouched() {
        this.inventoryTouched = true;
        PoppetUtil.invalidateShelfCache(PoppetShelfBlockEntity.this);
    }

    public void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
        this.inventoryTouched = true;
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
        this.inventoryTouched();
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
        this.inventoryTouched();
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        PoppetUtil.removePoppetShelf(this.ownerUuid, this);
    }


}
