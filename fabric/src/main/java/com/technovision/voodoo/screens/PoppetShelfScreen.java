package com.technovision.voodoo.screens;
import com.technovision.voodoo.Voodoo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class PoppetShelfScreen extends AbstractContainerScreen<PoppetShelfScreenHandler> {
    public PoppetShelfScreen(PoppetShelfScreenHandler menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Voodoo.id("textures/gui/poppet_shelf.png"), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}
