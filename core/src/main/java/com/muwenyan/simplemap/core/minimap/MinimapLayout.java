package com.muwenyan.simplemap.core.minimap;

public final class MinimapLayout {
    private MinimapLayout() { }

    public static ScreenRect place(MinimapConfig config, int screenWidth, int screenHeight, int margin) {
        if (config == null || screenWidth < 1 || screenHeight < 1 || margin < 0) {
            throw new IllegalArgumentException("invalid minimap layout");
        }
        int size = config.sizePixels();
        if (size + margin * 2 > screenWidth || size + margin * 2 > screenHeight) {
            throw new IllegalArgumentException("minimap does not fit screen");
        }
        int x = switch (config.anchor()) {
            case TOP_LEFT, BOTTOM_LEFT -> margin;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - margin - size;
        };
        int y = switch (config.anchor()) {
            case TOP_LEFT, TOP_RIGHT -> margin;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - margin - size;
        };
        return new ScreenRect(x, y, size, size);
    }

    public record ScreenRect(int x, int y, int width, int height) {
        public ScreenRect {
            if (x < 0 || y < 0 || width < 1 || height < 1) {
                throw new IllegalArgumentException("invalid screen rectangle");
            }
        }
    }
}
