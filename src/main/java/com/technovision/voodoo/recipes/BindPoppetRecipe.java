package com.technovision.voodoo.recipes;
import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.items.PoppetItem;
import com.technovision.voodoo.registry.*;
import com.technovision.voodoo.util.BindingUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public class BindPoppetRecipe extends CustomRecipe {
    private ItemStack[] ingredients(CraftingInput input) {
        ItemStack taglock = ItemStack.EMPTY, poppet = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            var stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.TAGLOCK_KIT) && BindingUtil.isBound(stack) && taglock.isEmpty()) taglock = stack;
            else if (stack.getItem() instanceof PoppetItem item && item.getPoppetType() != Poppet.PoppetType.BLANK && !BindingUtil.isBound(stack) && poppet.isEmpty()) poppet = stack;
            else return null;
        }
        return taglock.isEmpty() || poppet.isEmpty() ? null : new ItemStack[]{taglock, poppet};
    }
    @Override public boolean matches(CraftingInput input, Level level) { return ingredients(input) != null; }
    @Override public ItemStack assemble(CraftingInput input) {
        var items = ingredients(input);
        if (items == null) return ItemStack.EMPTY;
        ItemStack result = items[1].copyWithCount(1);
        BindingUtil.transfer(items[0], result);
        return result;
    }
    @Override public RecipeSerializer<BindPoppetRecipe> getSerializer() { return ModRecipes.BIND_POPPET_RECIPE; }
}
