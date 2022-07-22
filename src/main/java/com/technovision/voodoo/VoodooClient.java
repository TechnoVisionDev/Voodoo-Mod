package com.technovision.voodoo;

import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.client.PoppetShelfRenderer;
import com.technovision.voodoo.registry.ModBlockEntities;
import com.technovision.voodoo.registry.ModScreens;
import com.technovision.voodoo.screens.PoppetShelfScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

/**
 * Client side initialization of Voodoo mod.
 * Used to receive client-side packets and register screens/renderers.
 *
 * @author TechnoVision
 */
public class VoodooClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.POPPET_SHELF_SCREEN_HANDLER, PoppetShelfScreen::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.POPPET_SHELF_ENTITY, new PoppetShelfRenderer());

        // Poppet Shelf Rendering Packets
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(Voodoo.MOD_ID, "update"), (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            DefaultedList<ItemStack> inv = DefaultedList.ofSize(9, ItemStack.EMPTY);
            for (int i = 0; i < 9; i++) {
                inv.set(i, buf.readItemStack());
            }
            client.execute(() -> {
                PoppetShelfBlockEntity blockEntity = (PoppetShelfBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                blockEntity.setInvStackList(inv);
            });
        });
    }
}
