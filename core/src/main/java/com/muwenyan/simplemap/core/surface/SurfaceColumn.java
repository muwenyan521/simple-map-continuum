package com.muwenyan.simplemap.core.surface;

public record SurfaceColumn(int x, int z, int y, int argb, boolean fluid, boolean opaque) {
    public SurfaceColumn {
        if ((argb >>> 24) == 0) {
            throw new IllegalArgumentException("surface color must be visible");
        }
    }
}
