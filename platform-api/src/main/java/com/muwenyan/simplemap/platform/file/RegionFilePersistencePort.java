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
    private final CaveTileFilePersistencePort caves;

    public RegionFilePersistencePort(Path root) {
        Path normalized = Objects.requireNonNull(root, "root");
        cache = new RegionCacheService(normalized);
        caves = new CaveTileFilePersistencePort(normalized);
    }

    @Override
    public Optional<CaveTile> read(long epoch, TileKey key) {
        return caves.read(epoch, Objects.requireNonNull(key, "key"));
    }

    @Override
    public void write(CaveTile tile) {
        caves.write(Objects.requireNonNull(tile, "tile"));
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
