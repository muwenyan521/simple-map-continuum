package com.muwenyan.simplemap.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class Fabric1201Recipes {
    public static final RecipeSerializer<Fabric1201CopyMapBookRecipe> COPY = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation("simplemap", "copy_book_map"),
            new SimpleCraftingRecipeSerializer<>((id, category) -> new Fabric1201CopyMapBookRecipe(id, category)));
    public static final RecipeSerializer<Fabric1201MergeMapBooksRecipe> MERGE = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation("simplemap", "merge_book_maps"),
            new SimpleCraftingRecipeSerializer<>((id, category) -> new Fabric1201MergeMapBooksRecipe(id, category)));
    private Fabric1201Recipes() { }
    public static void register() { }
}
