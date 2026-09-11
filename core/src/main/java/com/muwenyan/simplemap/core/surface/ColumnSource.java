package com.muwenyan.simplemap.core.surface;

import java.util.Optional;

@FunctionalInterface
public interface ColumnSource {
    Optional<SurfaceColumn> sample(int localX, int localZ);
}
