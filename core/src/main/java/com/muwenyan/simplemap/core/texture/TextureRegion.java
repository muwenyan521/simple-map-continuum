package com.muwenyan.simplemap.core.texture;

public record TextureRegion(String id, int x, int y, int width, int height) {
    public TextureRegion {
        if (id == null || id.isBlank() || x < 0 || y < 0 || width < 1 || height < 1) {
            throw new IllegalArgumentException("invalid texture region");
        }
    }
}
