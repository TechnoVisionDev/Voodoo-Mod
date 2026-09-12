package com.technovision.voodoo.items;
import com.technovision.voodoo.util.BindingUtil;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.function.Consumer;
public class TaglockKitItem extends Item {
    public TaglockKitItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || BindingUtil.isBound(stack)) return InteractionResult.PASS;
        if (!level.isClientSide()) BindingUtil.bind(stack, player);
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof Player target) || BindingUtil.isBound(stack)) return InteractionResult.PASS;
        if (!user.level().isClientSide()) BindingUtil.bind(stack, target);
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock) || BindingUtil.isBound(context.getItemInHand())) return InteractionResult.PASS;
        if (state.getValue(BedBlock.PART) != BedPart.HEAD) pos = pos.relative(state.getValue(BedBlock.FACING));
        final var head = pos;
        if (level.getServer() != null) level.getServer().getPlayerList().getPlayers().stream()
            .sorted(java.util.Comparator.comparing(net.minecraft.server.level.ServerPlayer::getSleepTimer))
            .filter(p -> p.getRespawnConfig() != null && p.getRespawnConfig().respawnData().dimension().equals(level.dimension()) && p.getRespawnConfig().respawnData().pos().equals(head))
            .findFirst().ifPresent(p -> BindingUtil.bind(context.getItemInHand(), p));
        return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept((BindingUtil.isBound(stack) ? Component.translatable("text.voodoo.taglock_kit.bound", BindingUtil.getBoundName(stack)) : Component.translatable("text.voodoo.taglock_kit.not_bound")).withStyle(ChatFormatting.GRAY));
    }
}
