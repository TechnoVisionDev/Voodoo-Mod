package com.technovision.voodoo.items;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.VoodooDamageSource;
import com.technovision.voodoo.registry.ModItems;
import com.technovision.voodoo.util.PoppetUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.Random;

import static com.technovision.voodoo.Poppet.PoppetType.VOODOO;
import static com.technovision.voodoo.util.BindingUtil.getBoundPlayer;

public class VoodooPoppetItem extends PoppetItem {

    private static final Random random = new Random();

    public VoodooPoppetItem() {
        super(VOODOO);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient() && remainingUseTicks <= 72000 - 20 && user instanceof PlayerEntity player) {
            PlayerEntity boundPlayer = getBoundPlayer(stack, world);
            if (boundPlayer != null) {
                ItemStack offhand = user.getOffHandStack();
                if (!offhand.isEmpty() && offhand.getItem() == ModItems.NEEDLE) {
                    offhand.decrement(1);
                    if (boundPlayer.damage(new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.NEEDLE, stack, player), 2)) {
                        stack.damage(1, user, (e) -> {
                            player.sendToolBreakStatus(player.getActiveHand());
                        });
                    }
                } else {
                    Poppet voodooProtectionPoppet = PoppetUtil.getPlayerPoppet((ServerPlayerEntity) boundPlayer, Poppet.PoppetType.VOODOO_PROTECTION);

                    if (voodooProtectionPoppet != null) {
                        PoppetUtil.useVoodooProtectionPuppet(stack, user);
                        voodooProtectionPoppet.use();
                    } else {
                        stack.damage(2, user, (e) -> {
                            player.sendToolBreakStatus(player.getActiveHand());
                        });
                        double deltaX = random.nextDouble() + 0.5;
                        double deltaY = random.nextDouble();
                        double deltaZ = random.nextDouble() + 0.5;
                        boundPlayer.addVelocity(deltaX, deltaY, deltaZ);
                        boundPlayer.velocityModified = true;

                    }
                }
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
