package com.muwenyan.simplemap.core.lod;

public record LodLevel(int level, int scale) {
    public LodLevel { if (level < 0 || level > 30 || scale <= 0 || (scale & (scale - 1)) != 0) throw new IllegalArgumentException("invalid lod"); }
    public static LodLevel of(int level) { return new LodLevel(level, 1 << level); }
}
