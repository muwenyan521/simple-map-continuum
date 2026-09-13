package com.muwenyan.simplemap.core.surface;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SurfaceSampler {
    private SurfaceSampler() {
    }

    public static Optional<SurfaceSample> sample(ChunkPos chunk, List<SurfaceColumn> columns, long revision) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(columns, "columns");
        if (columns.isEmpty() || revision < 0) {
            return Optional.empty();
        }
        SurfaceColumn selected = columns.stream()
                .filter(SurfaceColumn::opaque)
                .max(Comparator.comparingInt(SurfaceColumn::y)
                        .thenComparingInt(SurfaceColumn::x)
                        .thenComparingInt(SurfaceColumn::z))
                .orElseGet(() -> columns.stream().max(Comparator.comparingInt(SurfaceColumn::y)).orElseThrow());
        int floor = columns.stream()
                .filter(column -> column.x() == selected.x() && column.z() == selected.z() && column.opaque())
                .mapToInt(SurfaceColumn::y)
                .min().orElse(selected.y());
        return Optional.of(new SurfaceSample(chunk, selected.y(), floor,
                SurfaceTint.apply(selected.argb(), selected.biomeArgb()), selected.fluid(), true, revision));
    }
}
