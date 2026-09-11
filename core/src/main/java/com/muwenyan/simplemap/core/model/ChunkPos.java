package com.muwenyan.simplemap.core.model;

public record ChunkPos(int x, int z) {
    public long packed() { return ((long) x << 32) ^ (z & 0xffffffffL); }
}
