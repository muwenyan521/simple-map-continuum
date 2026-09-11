package com.muwenyan.simplemap.core.color;

public enum ColorProfile {
    BALANCED(1.0, 1.0, 1.0), VIBRANT(1.12, 1.04, 1.0), NATURAL(0.94, 1.0, 1.08), CONTRAST(1.2, 1.2, 1.2);
    private final double red, green, blue;
    ColorProfile(double red, double green, double blue) { this.red = red; this.green = green; this.blue = blue; }
    public int apply(int argb) {
        int a=(argb>>>24)&255, r=(argb>>>16)&255, g=(argb>>>8)&255, b=argb&255;
        return a<<24 | scale(r, red)<<16 | scale(g, green)<<8 | scale(b, blue);
    }
    private static int scale(int value, double factor) { return (int)Math.max(0, Math.min(255, Math.round(value*factor))); }
}
