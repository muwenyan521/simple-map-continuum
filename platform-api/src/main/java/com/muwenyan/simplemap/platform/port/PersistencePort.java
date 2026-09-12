package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.TileKey;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Optional;

public interface PersistencePort {
    Optional<CaveTile> read(long epoch, TileKey key);

    void write(CaveTile tile);

    default Optional<MapRegion> readRegion(DimensionId dimension, int regionX, int regionZ) {
        return Optional.empty();
    }

    default void writeRegion(MapRegion region) {
        throw new UnsupportedOperationException("region persistence is not available");
    }
}
