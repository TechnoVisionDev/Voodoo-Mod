package com.technovision.voodoo.client;

import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

import static vazkii.patchouli.client.base.ClientTicker.partialTicks;

/**
 * Handles the rendering of poppets in the poppet shelf block.
 * Must be registered on the client side only!
 *
 * @author TechnoVision
 */
public class PoppetShelfRenderer implements BlockEntityRendererFactory<PoppetShelfBlockEntity> {

    @Override
    public BlockEntityRenderer<PoppetShelfBlockEntity> create(Context ctx) {
        return (entity, tickDelta, matrices, vertexConsumers, light, overlay) -> {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = entity.getItems().get(i);
                if (!stack.isEmpty()) {
                    matrices.push();
                    double offset = Math.sin((entity.getWorld().getTime() + partialTicks) / 8) / 32;
                    matrices.translate((i % 3) / 5D + 0.3, 0.9 + offset, (i / 3) / 5D + 0.3);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.getWorld().getTime() + tickDelta));
                    matrices.scale(0.4f, 0.4f, 0.4f);
                    MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);
                    matrices.pop();
                }
            }
        };
    }
}
