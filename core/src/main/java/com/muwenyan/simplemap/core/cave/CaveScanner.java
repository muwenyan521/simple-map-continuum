package com.muwenyan.simplemap.core.cave;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CaveScanner {
    private CaveScanner() { }

    public static CaveSnapshot scan(ChunkPos chunk, CaveConfig config, ColumnSource source, long revision) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(source, "source");
        if (revision < 0) throw new IllegalArgumentException("revision must be non-negative");
        List<List<CaveColumnRun>> columns = new ArrayList<>(256);
        for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            columns.add(List.copyOf(source.sample(x, z)));
        }
        return new CaveSnapshot(chunk, revision, true, false, columns);
    }

    @FunctionalInterface
    public interface ColumnSource {
        List<CaveColumnRun> sample(int localX, int localZ);
    }
}
