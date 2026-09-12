package com.technovision.voodoo.client;
import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import java.util.*;
public class PoppetShelfRenderer implements BlockEntityRenderer<PoppetShelfBlockEntity, PoppetShelfRenderer.State> {
    public static class State extends BlockEntityRenderState { public final List<ItemStackRenderState> items = new ArrayList<>(); public float time; }
    private final ItemModelResolver resolver;
    public PoppetShelfRenderer(BlockEntityRendererProvider.Context context) { resolver = context.itemModelResolver(); }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(PoppetShelfBlockEntity entity, State state, float delta, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, delta, camera, overlay);
        state.time = (entity.getLevel().getGameTime() % 100000) + delta;
        state.items.clear();
        for (int i = 0; i < 9; i++) {
            var item = new ItemStackRenderState();
            resolver.updateForTopItem(item, entity.getItems().get(i), ItemDisplayContext.GROUND, entity.getLevel(), null, i);
            state.items.add(item);
        }
    }
    @Override public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.items.size(); i++) {
            var item = state.items.get(i); if (item.isEmpty()) continue;
            poses.pushPose();
            poses.translate((i % 3) / 5.0 + .3, .9 + Math.sin(state.time / 8) / 32, (i / 3) / 5.0 + .3);
            poses.mulPose(Axis.YP.rotationDegrees(state.time)); poses.scale(.4f, .4f, .4f);
            item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
    }
}
