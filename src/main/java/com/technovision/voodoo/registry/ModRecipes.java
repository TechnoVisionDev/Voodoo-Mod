package com.technovision.voodoo.registry;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.recipes.BindPoppetRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModRecipes {

    public static final SpecialRecipeSerializer<BindPoppetRecipe> BIND_POPPET_RECIPE = new SpecialRecipeSerializer<>(BindPoppetRecipe::new);

    public static void registerRecipes() {
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Voodoo.MOD_ID, "bind_poppet"), BIND_POPPET_RECIPE);
    }
}
