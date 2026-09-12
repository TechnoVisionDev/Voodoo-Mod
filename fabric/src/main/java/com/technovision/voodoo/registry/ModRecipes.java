package com.technovision.voodoo.registry;
import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.recipes.BindPoppetRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
public class ModRecipes {
    private static final BindPoppetRecipe INSTANCE = new BindPoppetRecipe();
    public static final RecipeSerializer<BindPoppetRecipe> BIND_POPPET_RECIPE = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Voodoo.id("bind_poppet"), new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE)));
    public static void registerRecipes() {}
}
