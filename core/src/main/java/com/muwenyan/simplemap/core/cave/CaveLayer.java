package com.muwenyan.simplemap.core.cave;

public record CaveLayer(int topY, int floorY, int argb, int light, boolean fluid, boolean emissive) {
    public CaveLayer {
        if (floorY > topY || (argb >>> 24) == 0 || light < 0 || light > 15) {
            throw new IllegalArgumentException("invalid cave layer");
        }
    }

    public int shadedArgb(CaveLightMode mode) {
        if (mode == null) {
            throw new NullPointerException("mode");
        }
        if (mode == CaveLightMode.RAW || (mode == CaveLightMode.EMISSIVE && emissive)) {
            return argb;
        }
        double factor = switch (mode) {
            case BRIGHT -> 0.45d + light / 15d * 0.55d;
            case DIM -> 0.2d + light / 15d * 0.35d;
            case EMISSIVE -> 0.25d + light / 15d * 0.45d;
            case RAW -> 1d;
        };
        int alpha = (argb >>> 24) & 255;
        int red = scale((argb >>> 16) & 255, factor);
        int green = scale((argb >>> 8) & 255, factor);
        int blue = scale(argb & 255, factor);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int scale(int value, double factor) {
        return (int) Math.max(0, Math.min(255, Math.round(value * factor)));
    }
}
