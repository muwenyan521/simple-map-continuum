package com.muwenyan.simplemap.core.texture;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TextureAtlas(TextureAtlasLayout layout, byte[] pixels) {
    public TextureAtlas {
        layout = Objects.requireNonNull(layout, "layout");
        if (pixels == null || pixels.length != Math.multiplyExact(Math.multiplyExact(layout.width(), layout.height()), 4)) {
            throw new IllegalArgumentException("invalid atlas pixels");
        }
        pixels = pixels.clone();
    }

    @Override
    public byte[] pixels() {
        return pixels.clone();
    }

    public static TextureAtlas assemble(TextureAtlasLayout layout, List<TextureTile> tiles) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(tiles, "tiles");
        Map<String, TextureTile> byId = tiles.stream().collect(java.util.stream.Collectors.toMap(TextureTile::id, value -> value));
        byte[] atlas = new byte[Math.multiplyExact(Math.multiplyExact(layout.width(), layout.height()), 4)];
        for (TextureRegion region : layout.regions()) {
            TextureTile tile = byId.get(region.id());
            if (tile == null || tile.width() != region.width() || tile.height() != region.height()) {
                throw new IllegalArgumentException("tile does not match atlas region: " + region.id());
            }
            byte[] source = tile.pixels();
            for (int row = 0; row < region.height(); row++) {
                int sourceOffset = row * tile.width() * 4;
                int targetOffset = ((region.y() + row) * layout.width() + region.x()) * 4;
                System.arraycopy(source, sourceOffset, atlas, targetOffset, tile.width() * 4);
            }
        }
        return new TextureAtlas(layout, atlas);
    }
}
