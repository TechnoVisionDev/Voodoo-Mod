package com.technovision.voodoo.mixins;

import com.technovision.voodoo.items.entities.PoppetItemEntity;
import com.technovision.voodoo.items.VoodooPoppetItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for PlayerEntity methods.
 *
 * @author TechnoVision
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    /**
     * Fires when an item is dropped by a player.
     * Checks for Voodoo poppet item and drops custom item entity instead.
     */
    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value="TAIL"), cancellable = true)
    public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (stack.getItem() instanceof VoodooPoppetItem) {
            if (stack.isEmpty()) {
                cir.setReturnValue(null);
            } else {
                PlayerEntity player = (PlayerEntity) (Object) this;
                if (player.world.isClient) {
                    player.swingHand(Hand.MAIN_HAND);
                }
                double d = player.getEyeY() - 0.30000001192092896;
                PoppetItemEntity itemEntity = new PoppetItemEntity(player.world, player.getX(), d, player.getZ(), stack);
                itemEntity.setPickupDelay(40);

                float f;
                float g;
                if (throwRandomly) {
                    f = player.getRandom().nextFloat() * 0.5F;
                    g = player.getRandom().nextFloat() * 6.2831855F;
                    itemEntity.setVelocity((double) (-MathHelper.sin(g) * f), 0.20000000298023224, (double) (MathHelper.cos(g) * f));
                } else {
                    f = 0.3F;
                    g = MathHelper.sin(player.getPitch() * 0.017453292F);
                    float h = MathHelper.cos(player.getPitch() * 0.017453292F);
                    float i = MathHelper.sin(player.getYaw() * 0.017453292F);
                    float j = MathHelper.cos(player.getYaw() * 0.017453292F);
                    float k = player.getRandom().nextFloat() * 6.2831855F;
                    float l = 0.02F * player.getRandom().nextFloat();
                    itemEntity.setVelocity((double) (-i * h * 0.3F) + Math.cos((double) k) * (double) l, (double) (-g * 0.3F + 0.1F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.1F), (double) (j * h * 0.3F) + Math.sin((double) k) * (double) l);
                }
                cir.setReturnValue(itemEntity);
            }
        }
    }
}
