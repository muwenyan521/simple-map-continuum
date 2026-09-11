package com.muwenyan.simplemap.core.surface;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.Objects;

public final class SurfaceRegionAssembler {
    private SurfaceRegionAssembler() {
    }

    public static boolean apply(MapRegion region, SurfaceSample sample) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(sample, "sample");
        ChunkPos origin = region.origin();
        int localX = sample.chunk().x() - origin.x();
        int localZ = sample.chunk().z() - origin.z();
        if (localX < 0 || localX >= 32 || localZ < 0 || localZ >= 32) {
            return false;
        }
        region.apply(localX, localZ, sample.toCell());
        return true;
    }
}
