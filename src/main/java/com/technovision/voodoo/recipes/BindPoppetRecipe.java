package com.technovision.voodoo.recipes;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.items.PoppetItem;
import com.technovision.voodoo.registry.ModItems;
import com.technovision.voodoo.registry.ModRecipes;
import com.technovision.voodoo.util.BindingUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Custom special recipe that binds a taglock to a poppet, passing NBT tag data.
 *
 * @author TechnoVision
 */
public class BindPoppetRecipe extends SpecialCraftingRecipe {

    public BindPoppetRecipe(Identifier id) {
        super(id);
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingInventory inventory) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.of();
        int size = inventory.getHeight() * inventory.getWidth();
        for (int i = 0; i < size; ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                itemStacks.add(itemStack);
            }
        }
        return itemStacks;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        DefaultedList<ItemStack> itemStacks = getRemainder(inventory);
        if (itemStacks.size() != 2) return false;
        ItemStack itemStack1 = itemStacks.get(0);
        ItemStack itemStack2 = itemStacks.get(1);
        if (itemStack2.getItem() == ModItems.TAGLOCK_KIT) {
            final ItemStack tmp = itemStack1;
            itemStack1 = itemStack2;
            itemStack2 = tmp;
        }
        return itemStack1.getItem() == ModItems.TAGLOCK_KIT &&
                BindingUtil.isBound(itemStack1) &&
                itemStack2.getItem() instanceof PoppetItem &&
                !itemStack2.getItem().equals(ModItems.poppetMap.get(Poppet.PoppetType.BLANK)) &&
                !BindingUtil.isBound(itemStack2);
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        DefaultedList<ItemStack> itemStacks = getRemainder(inventory);
        ItemStack itemStack1 = itemStacks.get(0);
        ItemStack itemStack2 = itemStacks.get(1);
        if (itemStack2.getItem() == ModItems.TAGLOCK_KIT) {
            final ItemStack tmp = itemStack1;
            itemStack1 = itemStack2;
            itemStack2 = tmp;
        }
        ItemStack boundPoppet = new ItemStack(itemStack2.getItem());
        BindingUtil.transfer(itemStack1, boundPoppet);
        return boundPoppet;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height > 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BIND_POPPET_RECIPE;
    }
}
