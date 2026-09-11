package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.model.ColorMode;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.MapMode;
import java.util.Objects;

public record CacheNamespace(DimensionId dimension, MapMode mode, ColorMode colorMode, int epoch) {
    public CacheNamespace {
        dimension = Objects.requireNonNull(dimension, "dimension");
        mode = Objects.requireNonNull(mode, "mode");
        colorMode = Objects.requireNonNull(colorMode, "colorMode");
        if (epoch < 0 || epoch > 255) {
            throw new IllegalArgumentException("epoch must be between 0 and 255");
        }
    }

    public String folderName() {
        String dimensionName = dimension.value().replace(':', '_').replace('/', '_');
        return dimensionName + "/" + mode.name().toLowerCase() + "/" + colorMode.name().toLowerCase()
                + "/e" + epoch;
    }

    public String regionFile(int regionX, int regionZ) {
        return folderName() + "/r." + regionX + "." + regionZ + ".cache";
    }
}
