package com.technovision.voodoo.items;
import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.util.BindingUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.function.Consumer;
public class PoppetItem extends Item {
    protected final Poppet.PoppetType poppetType;
    public PoppetItem(Poppet.PoppetType type, Properties properties) { super(properties); this.poppetType = type; }
    public Poppet.PoppetType getPoppetType() { return poppetType; }
    @Override public boolean isFoil(ItemStack stack) { return poppetType == Poppet.PoppetType.VOODOO_PROTECTION || poppetType == Poppet.PoppetType.REFLECTOR; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (poppetType == Poppet.PoppetType.BLANK) return;
        tooltip.accept((BindingUtil.isBound(stack) ? Component.translatable("text.voodoo.poppet.bound", BindingUtil.getBoundName(stack)) : Component.translatable("text.voodoo.poppet.not_bound")).withStyle(ChatFormatting.GRAY));
    }
}
