package com.muwenyan.simplemap.core.model;

import java.util.Objects;

public record CaveTile(TileKey key, long epoch, byte[] pixels) {
    public CaveTile {
        key = Objects.requireNonNull(key, "key");
        if (epoch < 0) throw new IllegalArgumentException("epoch must be non-negative");
        pixels = Objects.requireNonNull(pixels, "pixels").clone();
    }
    @Override public byte[] pixels() { return pixels.clone(); }
}
