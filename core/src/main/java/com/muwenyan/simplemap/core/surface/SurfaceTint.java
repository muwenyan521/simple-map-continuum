package com.muwenyan.simplemap.core.surface;

public final class SurfaceTint {
    private SurfaceTint() { }

    public static int apply(int baseArgb, int biomeArgb) {
        if (baseArgb == biomeArgb) return baseArgb;
        int alpha = (baseArgb >>> 24) & 255;
        int red = ((baseArgb >>> 16 & 255) * (biomeArgb >>> 16 & 255) + 127) / 255;
        int green = ((baseArgb >>> 8 & 255) * (biomeArgb >>> 8 & 255) + 127) / 255;
        int blue = ((baseArgb & 255) * (biomeArgb & 255) + 127) / 255;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
}
