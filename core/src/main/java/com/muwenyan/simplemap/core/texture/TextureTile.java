package com.muwenyan.simplemap.core.texture;

import java.util.Objects;

public record TextureTile(String id, int width, int height, byte[] pixels) {
    public TextureTile {
        id = Objects.requireNonNull(id, "id");
        if (id.isBlank() || width < 1 || height < 1 || pixels == null
                || pixels.length != Math.multiplyExact(Math.multiplyExact(width, height), 4)) {
            throw new IllegalArgumentException("invalid texture tile");
        }
        pixels = pixels.clone();
    }

    @Override
    public byte[] pixels() {
        return pixels.clone();
    }
}
