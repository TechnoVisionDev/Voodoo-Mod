package com.technovision.voodoo.mixins;

import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class WorldMixin {

    @Inject(method = "addBlockEntity", at = @At("HEAD"))
    public void addBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        System.out.println("Test");
        if (blockEntity instanceof PoppetShelfBlockEntity poppetShelfBlockEntity) {
            System.out.println(poppetShelfBlockEntity.getOwnerName());
        }
    }
}
