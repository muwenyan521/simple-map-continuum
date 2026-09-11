package com.muwenyan.simplemap.core.model;

public record RegionPos(int x, int z) {
    public static RegionPos fromChunk(ChunkPos chunk) { return new RegionPos(chunk.x() >> 5, chunk.z() >> 5); }
}
