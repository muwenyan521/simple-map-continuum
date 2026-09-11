package com.muwenyan.simplemap.core.minimap;

public record MinimapConfig(
        boolean enabled,
        int sizePixels,
        double zoom,
        MinimapShape shape,
        MinimapAnchor anchor,
        boolean rotateWithPlayer,
        boolean showCoordinates) {
    public MinimapConfig {
        if (sizePixels < 64 || sizePixels > 1024) {
            throw new IllegalArgumentException("sizePixels must be between 64 and 1024");
        }
        if (!Double.isFinite(zoom) || zoom < 0.125d || zoom > 64d) {
            throw new IllegalArgumentException("zoom must be between 0.125 and 64");
        }
        if (shape == null || anchor == null) {
            throw new NullPointerException("shape/anchor");
        }
    }

    public static MinimapConfig defaults() {
        return new MinimapConfig(true, 128, 1d, MinimapShape.CIRCLE,
                MinimapAnchor.TOP_RIGHT, true, true);
    }
}
