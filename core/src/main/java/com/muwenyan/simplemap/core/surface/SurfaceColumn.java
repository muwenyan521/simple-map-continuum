package com.muwenyan.simplemap.core.surface;

public record SurfaceColumn(int x, int z, int y, int argb, int biomeArgb, boolean fluid, boolean opaque) {
    public SurfaceColumn(int x, int z, int y, int argb, boolean fluid, boolean opaque) {
        this(x, z, y, argb, argb, fluid, opaque);
    }

    public SurfaceColumn {
        if ((argb >>> 24) == 0 || (biomeArgb >>> 24) == 0) {
            throw new IllegalArgumentException("surface colors must be visible");
        }
    }
}
