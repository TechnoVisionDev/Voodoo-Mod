package com.technovision.voodoo.items;

import com.technovision.voodoo.VoodooDamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import static com.technovision.voodoo.Poppet.PoppetType.VAMPIRIC;
import static com.technovision.voodoo.util.BindingUtil.getBoundPlayer;

public class VampiricPoppetItem extends PoppetItem {

    private static final int DRAINAGE_INTERVAL = 20;
    private static final int HEALTH_LIMIT = 6;
    private static final int HEALTH_PER_DRAIN = 3;

    public VampiricPoppetItem() {
        super(VAMPIRIC);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient()) return;
        if ((remainingUseTicks - 1) % DRAINAGE_INTERVAL != 0) return;
        if (!(user instanceof final ServerPlayerEntity player)) return;
        final float playerDifference = player.getMaxHealth() - player.getHealth();
        if (playerDifference == 0) return;
        final PlayerEntity boundPlayer = getBoundPlayer(stack, world);
        if (boundPlayer == null) return;
        final float boundDifference = boundPlayer.getHealth() - HEALTH_LIMIT;
        if (boundDifference <= 0) return;
        final float healthToTake = Math.min(Math.min(playerDifference, boundDifference), HEALTH_PER_DRAIN);
        if (boundPlayer.damage(new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.VAMPIRIC, stack, player), healthToTake)) {
            player.heal(healthToTake);
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }
}
