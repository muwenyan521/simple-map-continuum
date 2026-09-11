package com.muwenyan.simplemap.core.texture;

import java.util.List;

public record TextureAtlasLayout(int width, int height, List<TextureRegion> regions) {
    public TextureAtlasLayout {
        if (width < 1 || height < 1 || regions == null) {
            throw new IllegalArgumentException("invalid atlas layout");
        }
        regions = List.copyOf(regions);
    }
}
