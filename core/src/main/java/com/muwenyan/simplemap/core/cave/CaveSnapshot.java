package com.muwenyan.simplemap.core.cave;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.List;
import java.util.Objects;

public record CaveSnapshot(ChunkPos chunk, long revision, boolean scanned, boolean fullHeight, List<List<CaveColumnRun>> columns) {
    public CaveSnapshot {
        chunk = Objects.requireNonNull(chunk, "chunk");
        if (revision < 0 || columns == null || columns.size() != 256) {
            throw new IllegalArgumentException("invalid cave snapshot");
        }
        columns = columns.stream().map(List::copyOf).toList();
    }

    public int runCount() {
        return columns.stream().mapToInt(List::size).sum();
    }
}
