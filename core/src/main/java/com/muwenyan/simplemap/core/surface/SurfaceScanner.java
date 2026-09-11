package com.muwenyan.simplemap.core.surface;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SurfaceScanner {
    private static final int CHUNK_SIZE = 16;

    private SurfaceScanner() {
    }

    public static Optional<SurfaceSample> scan(ChunkPos chunk, ColumnSource source, long revision) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(source, "source");
        List<SurfaceColumn> columns = new ArrayList<>(CHUNK_SIZE * CHUNK_SIZE);
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                source.sample(x, z).ifPresent(columns::add);
            }
        }
        return SurfaceSampler.sample(chunk, columns, revision);
    }
}
