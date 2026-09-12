package com.technovision.voodoo.items;
import com.technovision.voodoo.*;
import com.technovision.voodoo.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public class VampiricPoppetItem extends PoppetItem {
    public VampiricPoppetItem(Properties properties) { super(Poppet.PoppetType.VAMPIRIC, properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) { player.startUsingItem(hand); return InteractionResult.CONSUME; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.DRINK; }
    @Override public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level.isClientSide() || (remaining - 1) % 20 != 0 || !(user instanceof ServerPlayer player)) return;
        if (!(BindingUtil.getBoundPlayer(stack, level) instanceof ServerPlayer target)) return;
        float amount = Math.min(3, Math.min(player.getMaxHealth() - player.getHealth(), target.getHealth() - 6));
        if (amount > 0 && target.hurtServer(target.level(), new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.VAMPIRIC, stack, player), amount)) {
            player.heal(amount); PoppetUtil.damageStack(stack, 1, player);
        }
    }
}
