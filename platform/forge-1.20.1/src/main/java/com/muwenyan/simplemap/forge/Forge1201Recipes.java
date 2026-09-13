package com.muwenyan.simplemap.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Forge1201Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "simplemap");
    public static final RegistryObject<RecipeSerializer<Forge1201CopyMapBookRecipe>> COPY = SERIALIZERS.register(
            "copy_book_map", () -> new SimpleCraftingRecipeSerializer<>(Forge1201CopyMapBookRecipe::new));
    public static final RegistryObject<RecipeSerializer<Forge1201MergeMapBooksRecipe>> MERGE = SERIALIZERS.register(
            "merge_book_maps", () -> new SimpleCraftingRecipeSerializer<>(Forge1201MergeMapBooksRecipe::new));
    private Forge1201Recipes() { }
}
