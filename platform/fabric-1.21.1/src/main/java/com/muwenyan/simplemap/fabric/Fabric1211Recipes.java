package com.muwenyan.simplemap.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class Fabric1211Recipes {
    public static final RecipeSerializer<Fabric1211CopyMapBookRecipe> COPY = register("copy_book_map",
            new SimpleCraftingRecipeSerializer<>(Fabric1211CopyMapBookRecipe::new));
    public static final RecipeSerializer<Fabric1211MergeMapBooksRecipe> MERGE = register("merge_book_maps",
            new SimpleCraftingRecipeSerializer<>(Fabric1211MergeMapBooksRecipe::new));

    private Fabric1211Recipes() { }

    public static void register() { }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeSerializer<T> register(
            String id, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                ResourceLocation.fromNamespaceAndPath("simplemap", id), serializer);
    }
}
