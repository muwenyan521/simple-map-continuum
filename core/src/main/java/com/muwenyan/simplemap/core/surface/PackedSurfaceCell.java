package com.muwenyan.simplemap.core.surface;

public final class PackedSurfaceCell {
    public static final short EMPTY_Y = Short.MIN_VALUE;
    public static final short NO_BLOCK = -1;
    public static final byte NO_BIOME = -1;
    public static final long EMPTY = pack(EMPTY_Y, NO_BLOCK, NO_BIOME, (byte) 0, EMPTY_Y);

    private PackedSurfaceCell() { }

    public static long pack(short topY, short blockId, byte biomeId, byte flags, short floorY) {
        return ((long) topY & 0xffffL) | (((long) blockId & 0xffffL) << 16)
                | (((long) biomeId & 0xffL) << 32) | (((long) flags & 0xffL) << 40)
                | (((long) floorY & 0xffffL) << 48);
    }

    public static short topY(long packed) { return (short) packed; }
    public static short blockId(long packed) { return (short) (packed >>> 16); }
    public static byte biomeId(long packed) { return (byte) (packed >>> 32); }
    public static byte flags(long packed) { return (byte) (packed >>> 40); }
    public static short floorY(long packed) { return (short) (packed >>> 48); }
    public static boolean empty(long packed) { return topY(packed) == EMPTY_Y; }
    public static int light(long packed) { return flags(packed) & 0x0f; }
    public static boolean glowing(long packed) { return (flags(packed) & 0x10) != 0; }
    public static boolean fluid(long packed) { return (flags(packed) & 0x20) != 0; }
    public static boolean flower(long packed) { return (flags(packed) & 0x40) != 0; }
    public static boolean leaves(long packed) { return (flags(packed) & 0x80) != 0; }
    public static int waterDepth(long packed) {
        return fluid(packed) && !glowing(packed) ? Math.max(0, topY(packed) - floorY(packed)) : 0;
    }
    public static int reliefY(long packed) {
        return fluid(packed) && !glowing(packed) ? floorY(packed) : topY(packed);
    }
}
