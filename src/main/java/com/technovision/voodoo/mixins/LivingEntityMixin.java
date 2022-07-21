package com.technovision.voodoo.mixins;

import com.technovision.voodoo.events.DamageReceivedEvent;
import com.technovision.voodoo.events.VoodooEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for LivingEntity methods
 *
 * @author TechnoVisions.
 */
@Mixin(ServerPlayerEntity.class)
public class LivingEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) ((Object)this);
        boolean cancelEvent = VoodooEvents.onDamageReceivedEvent(new DamageReceivedEvent(player, source, amount));
        if (cancelEvent) cir.setReturnValue(false);
    }
}
