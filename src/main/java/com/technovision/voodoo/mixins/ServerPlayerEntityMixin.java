package com.technovision.voodoo.mixins;
import com.technovision.voodoo.events.*;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void voodoo$damage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (VoodooEvents.onDamageReceivedEvent(new DamageReceivedEvent((ServerPlayer)(Object)this, source, amount))) cir.setReturnValue(false);
    }
}
