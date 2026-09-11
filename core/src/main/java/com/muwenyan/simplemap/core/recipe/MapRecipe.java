package com.muwenyan.simplemap.core.recipe;

import java.util.List;
import java.util.Objects;

public record MapRecipe(String id, List<String> ingredients, String result, int count) {
    public MapRecipe {
        id = normalize(id, "id");
        result = normalize(result, "result");
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients"));
        if (ingredients.isEmpty() || ingredients.stream().anyMatch(value -> value == null || value.isBlank())
                || count < 1 || count > 64) {
            throw new IllegalArgumentException("invalid recipe");
        }
    }

    private static String normalize(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.length() > 128) throw new IllegalArgumentException("invalid " + name);
        return normalized;
    }
}
