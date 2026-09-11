package com.muwenyan.simplemap.core.recipe;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeRegistryTest {
    @Test
    void registersRecipesByStableId() {
        RecipeRegistry registry = new RecipeRegistry();
        MapRecipe recipe = new MapRecipe("simplemap:map_book", List.of("minecraft:paper", "minecraft:compass"),
                "simplemap:map_book", 1);
        registry.register(recipe);
        assertEquals(recipe, registry.find(recipe.id()).orElseThrow());
        assertThrows(IllegalStateException.class, () -> registry.register(recipe));
    }
}
