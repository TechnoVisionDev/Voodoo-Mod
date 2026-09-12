package com.technovision.voodoo.mixins;
import com.technovision.voodoo.VoodooDamageSource;
import com.technovision.voodoo.items.VoodooPoppetItem;
import com.technovision.voodoo.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Unique private int voodoo$lastFireTick = -20;
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void voodoo$fire(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity)(Object)this;
        if (!(self.getItem().getItem() instanceof VoodooPoppetItem) || !source.is(DamageTypeTags.IS_FIRE)) return;
        if (self.tickCount - voodoo$lastFireTick >= 20) {
            voodoo$lastFireTick = self.tickCount;
            if (BindingUtil.getBoundPlayer(self.getItem(), level) instanceof ServerPlayer target) {
                target.igniteForSeconds(2);
                target.hurtServer(target.level(), new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.FIRE, self.getItem(), self), 1);
                PoppetUtil.damageStack(self.getItem(), 2, target);
                if (self.getItem().isEmpty()) self.discard();
            }
        }
        cir.setReturnValue(false);
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void voodoo$water(CallbackInfo ci) {
        ItemEntity self = (ItemEntity)(Object)this;
        if (self.level().isClientSide() || self.isRemoved() || !(self.getItem().getItem() instanceof VoodooPoppetItem) || !self.isInWater()) return;
        if (!(BindingUtil.getBoundPlayer(self.getItem(), self.level()) instanceof ServerPlayer target) || target.isInvulnerable() || target.hasEffect(MobEffects.WATER_BREATHING)) return;
        double oxygen = target.getAttributeValue(Attributes.OXYGEN_BONUS);
        if (oxygen <= 0 || target.getRandom().nextDouble() >= oxygen / (oxygen + 1)) target.setAirSupply(target.getAirSupply() - 5);
        if (target.getAirSupply() > -40) return;
        target.setAirSupply(0);
        if (target.hurtServer(target.level(), new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.WATER, self.getItem(), self), 2)) PoppetUtil.damageStack(self.getItem(), 2, target);
        if (self.getItem().isEmpty()) self.discard();
    }
}
