package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.TileKey;
import com.muwenyan.simplemap.platform.RegionCacheService;
import com.muwenyan.simplemap.platform.port.PersistencePort;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class RegionFilePersistencePort implements PersistencePort {
    private final RegionCacheService cache;

    public RegionFilePersistencePort(Path root) {
        cache = new RegionCacheService(Objects.requireNonNull(root, "root"));
    }

    @Override
    public Optional<CaveTile> read(TileKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.empty();
    }

    @Override
    public void write(CaveTile tile) {
        throw new UnsupportedOperationException("cave tile persistence requires a cave backend");
    }

    @Override
    public Optional<MapRegion> readRegion(DimensionId dimension, int regionX, int regionZ) {
        try {
            return cache.read(dimension, regionX, regionZ);
        } catch (IOException exception) {
            throw new IllegalStateException("cannot read region cache", exception);
        }
    }

    @Override
    public void writeRegion(MapRegion region) {
        try {
            cache.write(region);
        } catch (IOException exception) {
            throw new IllegalStateException("cannot write region cache", exception);
        }
    }
}
