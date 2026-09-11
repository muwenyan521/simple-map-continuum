package com.muwenyan.simplemap.core.map;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MapRegion {
    private final DimensionId dimension;
    private final ChunkPos origin;
    private final MapCell[] cells;
    private long revision;

    public MapRegion(DimensionId dimension, ChunkPos origin, int width, int height) {
        this.dimension = Objects.requireNonNull(dimension, "dimension");
        this.origin = Objects.requireNonNull(origin, "origin");
        if (width != 32 || height != 32) throw new IllegalArgumentException("regions are 32x32 chunks");
        this.cells = new MapCell[width * height];
    }
    public DimensionId dimension() { return dimension; }
    public ChunkPos origin() { return origin; }
    public long revision() { return revision; }
    public void apply(int x, int z, MapCell cell) {
        if (cell == null || x < 0 || x >= 32 || z < 0 || z >= 32) throw new IllegalArgumentException("cell");
        cells[z * 32 + x] = cell;
        revision = Math.addExact(revision, 1);
    }
    public MapCell cell(int x, int z) {
        if (x < 0 || x >= 32 || z < 0 || z >= 32) throw new IllegalArgumentException("cell");
        return cells[z * 32 + x];
    }
    public List<MapCell> completedCells() {
        List<MapCell> result = new ArrayList<>();
        for (MapCell cell : cells) if (cell != null && cell.complete()) result.add(cell);
        return List.copyOf(result);
    }
}
