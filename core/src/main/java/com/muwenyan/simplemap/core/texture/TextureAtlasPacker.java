package com.muwenyan.simplemap.core.texture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class TextureAtlasPacker {
    private TextureAtlasPacker() {
    }

    public static TextureAtlasLayout pack(List<TextureTile> tiles, int maxSize) {
        Objects.requireNonNull(tiles, "tiles");
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize");
        }
        List<TextureTile> ordered = new ArrayList<>(tiles);
        ordered.sort(Comparator.comparing(TextureTile::id));
        Set<String> ids = new HashSet<>();
        List<TextureRegion> regions = new ArrayList<>(ordered.size());
        int x = 0;
        int y = 0;
        int rowHeight = 0;
        int usedWidth = 0;
        for (TextureTile tile : ordered) {
            if (!ids.add(tile.id()) || tile.width() > maxSize || tile.height() > maxSize) {
                throw new IllegalArgumentException("tile cannot fit atlas");
            }
            if (x > 0 && x + tile.width() > maxSize) {
                x = 0;
                y += rowHeight;
                rowHeight = 0;
            }
            if (y + tile.height() > maxSize) {
                throw new IllegalArgumentException("atlas capacity exceeded");
            }
            regions.add(new TextureRegion(tile.id(), x, y, tile.width(), tile.height()));
            x += tile.width();
            rowHeight = Math.max(rowHeight, tile.height());
            usedWidth = Math.max(usedWidth, x);
        }
        return new TextureAtlasLayout(Math.max(1, usedWidth), Math.max(1, y + rowHeight), regions);
    }
}
