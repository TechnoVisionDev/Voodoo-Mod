package com.technovision.voodoo.items;
import com.technovision.voodoo.*;
import com.technovision.voodoo.registry.ModItems;
import com.technovision.voodoo.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public class VoodooPoppetItem extends PoppetItem {
    public VoodooPoppetItem(Properties properties) { super(Poppet.PoppetType.VOODOO, properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) { player.startUsingItem(hand); return InteractionResult.CONSUME; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.BOW; }
    @Override public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (level.isClientSide() || remaining > 71980 || !(user instanceof ServerPlayer player)) return false;
        if (!(BindingUtil.getBoundPlayer(stack, level) instanceof ServerPlayer target)) return false;
        ItemStack offhand = user.getOffhandItem();
        if (offhand.is(ModItems.NEEDLE)) {
            offhand.shrink(1);
            if (target.hurtServer(target.level(), new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.NEEDLE, stack, player), 2))
                PoppetUtil.damageStack(stack, 1, player);
        } else {
            Poppet protection = PoppetUtil.getPlayerPoppet(target, Poppet.PoppetType.VOODOO_PROTECTION);
            if (protection != null) { PoppetUtil.useVoodooProtectionPuppet(stack, user); protection.use(); }
            else {
                PoppetUtil.damageStack(stack, 2, player);
                target.push(player.getRandom().nextDouble() + 0.5, player.getRandom().nextDouble(), player.getRandom().nextDouble() + 0.5);
                target.hurtMarked = true;
            }
        }
        return true;
    }
}
