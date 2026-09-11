package com.muwenyan.simplemap.core.recipe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RecipeRegistry {
    private final Map<String, MapRecipe> recipes = new LinkedHashMap<>();

    public synchronized void register(MapRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipes.putIfAbsent(recipe.id(), recipe) != null) throw new IllegalStateException("duplicate recipe");
    }

    public synchronized Optional<MapRecipe> find(String id) {
        return Optional.ofNullable(recipes.get(Objects.requireNonNull(id, "id")));
    }

    public synchronized List<MapRecipe> all() {
        return List.copyOf(recipes.values());
    }
}
