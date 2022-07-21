package com.technovision.voodoo.items;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.Voodoo;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.technovision.voodoo.Poppet.PoppetType.*;
import static com.technovision.voodoo.util.BindingUtil.*;

public class PoppetItem extends Item {

    protected final Poppet.PoppetType poppetType;

    public PoppetItem(Poppet.PoppetType poppetType) {
        super(new FabricItemSettings().group(Voodoo.ITEM_GROUP).maxCount(1).maxDamage(poppetType.getDurability()));
        this.poppetType = poppetType;
    }

    public Poppet.PoppetType getPoppetType() {
        return poppetType;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (isBound(stack)) {
            checkForNameUpdate(stack, world);
            tooltip.add(Text.translatable("text.voodoo.poppet.bound", getBoundName(stack)).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        } else {
            tooltip.add(Text.translatable("text.voodoo.poppet.not_bound").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        }
    }

    @Override
    public boolean isDamageable() {
        return poppetType.hasDurability();
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return poppetType.getDurability();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return poppetType == VOODOO_PROTECTION || poppetType == REFLECTOR;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return hasGlint(stack) ? Rarity.RARE : poppetType == DEATH_PROTECTION ? Rarity.UNCOMMON : super.getRarity(stack);
    }
}
