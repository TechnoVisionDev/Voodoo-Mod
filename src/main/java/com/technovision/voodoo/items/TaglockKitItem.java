package com.technovision.voodoo.items;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.registry.ModItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static com.technovision.voodoo.util.BindingUtil.*;

/**
 * Taglock item that can be used to collect player blood samples.
 *
 * @author TechnoVision
 */
public class TaglockKitItem extends Item {

    public TaglockKitItem() {
        super(new FabricItemSettings().group(Voodoo.ITEM_GROUP).maxCount(8));
    }

    /**
     * Binds the taglock to the player holding it.
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.getWorld().isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if (isBound(stack)) return new TypedActionResult<>(ActionResult.PASS, stack);
            if (user.isSneaking()) {
                if (!stack.hasNbt()) {
                    stack.setNbt(new NbtCompound());
                }
                if (!isBound(stack)) {
                    bind(stack, user);
                    return new TypedActionResult<>(ActionResult.SUCCESS, stack);
                }
            }
        }
        return super.use(world, user, hand);
    }

    /**
     * Binds the taglock to the person who owns the bed clicked on.
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity user = context.getPlayer();
        if (!world.isClient() && user != null && user.isSneaking() && state.getBlock() instanceof BedBlock) {
            MinecraftServer server = world.getServer();
            if (state.get(BedBlock.PART) != BedPart.HEAD) {
                pos = pos.offset(state.get(BedBlock.FACING));
            }

            BlockPos finalPos = pos;
            server.getPlayerManager().getPlayerList().stream()
                    .sorted(Comparator.comparing(ServerPlayerEntity::getSleepTimer))
                    .filter(p -> finalPos.equals(p.getSpawnPointPosition()))
                    .findFirst()
                    .ifPresent(serverPlayerEntity -> bind(context.getStack(), serverPlayerEntity));
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    /**
     * Binds the taglock to the player clicked on.
     */
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getWorld().isClient()) return ActionResult.PASS;
        if (stack.getItem() != ModItems.TAGLOCK_KIT.getDefaultStack().getItem()) return ActionResult.PASS;
        if (entity instanceof PlayerEntity player) {
            if (isBound(stack)) return ActionResult.PASS;
            bind(stack, player);
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    /**
     * Updates item tooltip to display player name.
     * If no player is bound, will display as empty.
     */
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (isBound(stack)) {
            checkForNameUpdate(stack, world);
            tooltip.add(Text.translatable("text.voodoo.taglock_kit.bound", getBoundName(stack)).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        } else {
            tooltip.add(Text.translatable("text.voodoo.taglock_kit.not_bound").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        }
    }
}
