package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.MapMode;

public final class MapModeController {
    private MapMode mode;

    public MapModeController(MapMode initial) {
        mode = initial == null ? MapMode.SURFACE : initial;
    }

    public synchronized MapMode mode() {
        return mode;
    }

    public synchronized MapMode toggle() {
        mode = mode == MapMode.SURFACE ? MapMode.CAVE : MapMode.SURFACE;
        return mode;
    }

    public synchronized void set(MapMode next) {
        mode = java.util.Objects.requireNonNull(next, "next");
    }
}
