package com.muwenyan.simplemap.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForge1211Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "simplemap");
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<NeoForge1211CopyMapBookRecipe>> COPY =
            SERIALIZERS.register("copy_book_map", () -> new SimpleCraftingRecipeSerializer<>(NeoForge1211CopyMapBookRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<NeoForge1211MergeMapBooksRecipe>> MERGE =
            SERIALIZERS.register("merge_book_maps", () -> new SimpleCraftingRecipeSerializer<>(NeoForge1211MergeMapBooksRecipe::new));
    private NeoForge1211Recipes() { }
}
