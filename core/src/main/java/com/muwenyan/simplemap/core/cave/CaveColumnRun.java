package com.muwenyan.simplemap.core.cave;

public record CaveColumnRun(int topY, int floorY, int argb, int light, boolean fluid, boolean emissive) {
    public CaveColumnRun {
        if (floorY > topY || (argb >>> 24) == 0 || light < 0 || light > 15) {
            throw new IllegalArgumentException("invalid cave column run");
        }
    }

    public CaveLayer layer() {
        return new CaveLayer(topY, floorY, argb, light, fluid, emissive);
    }
}
