package com.muwenyan.simplemap.core.style;

import com.muwenyan.simplemap.core.color.ColorProfile;
import java.util.Objects;

public final class SurfaceColorizer {
    private SurfaceColorizer() {
    }

    public static int colorize(int baseArgb, int blockId, int height, int neighborHeight,
                               int fluidDepth, MapStyle style) {
        Objects.requireNonNull(style, "style");
        int color = style.blockOverrides().getOrDefault(blockId, baseArgb);
        color = style.colorProfile().apply(color);
        double factor = 1d;
        if (style.relief() != ReliefMode.OFF) {
            int delta = height - neighborHeight;
            double strength = style.relief() == ReliefMode.THREE_D ? 0.04d : 0.02d;
            factor += Math.max(-0.35d, Math.min(0.35d, delta * strength));
        }
        if (style.water() == WaterShading.DEPTH && fluidDepth > 0) {
            factor *= Math.max(0.35d, 1d - fluidDepth * 0.04d);
        }
        return scale(color, factor);
    }

    private static int scale(int argb, double factor) {
        int alpha = (argb >>> 24) & 255;
        int red = channel((argb >>> 16) & 255, factor);
        int green = channel((argb >>> 8) & 255, factor);
        int blue = channel(argb & 255, factor);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int channel(int value, double factor) {
        return (int) Math.max(0, Math.min(255, Math.round(value * factor)));
    }
}
