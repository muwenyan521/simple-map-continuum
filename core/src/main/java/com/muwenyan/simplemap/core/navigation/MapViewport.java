package com.muwenyan.simplemap.core.navigation;

public record MapViewport(double centerX, double centerZ, double zoom) {
    public MapViewport {
        if (!Double.isFinite(centerX) || !Double.isFinite(centerZ) || !Double.isFinite(zoom)
                || zoom < 0.125 || zoom > 64.0) {
            throw new IllegalArgumentException("invalid viewport");
        }
    }

    public MapViewport pan(double deltaX, double deltaZ) {
        return new MapViewport(centerX + deltaX / zoom, centerZ + deltaZ / zoom, zoom);
    }

    public MapViewport zoomAt(double factor, double cursorX, double cursorZ) {
        if (!Double.isFinite(factor) || factor <= 0.0) throw new IllegalArgumentException("factor");
        double nextZoom = Math.max(0.125, Math.min(64.0, zoom * factor));
        double scale = 1.0 / zoom - 1.0 / nextZoom;
        return new MapViewport(centerX + cursorX * scale, centerZ + cursorZ * scale, nextZoom);
    }

    public MapViewport centered(double x, double z) { return new MapViewport(x, z, zoom); }
}
