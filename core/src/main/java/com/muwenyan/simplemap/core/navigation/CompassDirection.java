package com.muwenyan.simplemap.core.navigation;

public enum CompassDirection {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public static CompassDirection fromYaw(float yawDegrees) {
        if (!Float.isFinite(yawDegrees)) {
            throw new IllegalArgumentException("yaw must be finite");
        }
        int index = Math.floorMod(Math.round(yawDegrees / 90f), 4);
        return values()[index];
    }
}
