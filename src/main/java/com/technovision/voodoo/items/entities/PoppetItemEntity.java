package com.technovision.voodoo.items.entities;

import com.technovision.voodoo.VoodooDamageSource;
import com.technovision.voodoo.util.BindingUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Poppet entity that is attached to voodoo poppets.
 *
 * @author TechnoVision
 */
public class PoppetItemEntity extends ItemEntity {

    private int tick = 0;
    private int lastFireTick = -20;

    public PoppetItemEntity(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
        this.setPickupDelay(40);
    }

    /**
     * Detects fire damage to the poppet.
     * If the poppet is bound to a player the bound player will be set on fire.
     *
     * @param source The source of the damage
     * @param amount The amount of damage that should be inflicted
     * @return If the damage to this entity should be canceled
     */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isFire()) {
            if (this.tick - this.lastFireTick >= 20) {
                this.lastFireTick = this.tick;
                PlayerEntity boundPlayer = BindingUtil.getBoundPlayer(getStack(), world);
                if (boundPlayer != null) {
                    boundPlayer.setOnFireFor(2);
                    boundPlayer.damage(new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.FIRE, getStack(), this), 1);
                    this.getStack().damage(2, boundPlayer, (e) -> {
                        this.remove(RemovalReason.KILLED);
                    });
                }
            }
            return false;
        }
        return super.damage(source, amount);
    }

    /**
     * This method is used to detect if the poppet is in water.
     * If the poppet is in water and is bound the bound player will start to drown, as if they were in water themselves.
     */
    @Override
    public void tick() {
        super.tick();
        tick++;
        if (this.world.isClient()) return;
        if (!this.isInsideWaterOrBubbleColumn()) return;
        if (!BindingUtil.isBound(this.getStack())) return;
        final PlayerEntity boundPlayer = BindingUtil.getBoundPlayer(this.getStack(), this.world);
        if (boundPlayer == null) return;
        if (boundPlayer.isInvulnerable()) return;
        if (boundPlayer.canBreatheInWater() || boundPlayer.hasStatusEffect(StatusEffects.WATER_BREATHING)) return;
        this.decreaseAirSupply(boundPlayer);
        if (boundPlayer.getAir() > -40) return;
        boundPlayer.setAir(0);
        if (boundPlayer.damage(new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.WATER, getStack(), this), 2.0F)) {
            this.getStack().damage(2, boundPlayer, (e) -> boundPlayer.sendToolBreakStatus(boundPlayer.getActiveHand()));
        }
    }

    /**
     * Private helper method to decrease the air supply of a player.
     *
     * @param player The player
     */
    private void decreaseAirSupply(PlayerEntity player) {
        final int airSupply = player.getAir();
        int respiration = EnchantmentHelper.getRespiration(player);
        player.setAir(respiration > 0 && this.random.nextInt(respiration + 1) > 0 ? airSupply : airSupply - 5);
    }
}
