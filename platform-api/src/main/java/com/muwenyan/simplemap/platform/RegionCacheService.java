package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.persistence.ArchiveFormat;
import com.muwenyan.simplemap.core.persistence.AtomicArchiveStore;
import com.muwenyan.simplemap.core.persistence.RegionArchiveCodec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class RegionCacheService {
    private final Path root;

    public RegionCacheService(Path root) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
    }

    public Path path(DimensionId dimension, int regionX, int regionZ) {
        Objects.requireNonNull(dimension, "dimension");
        String safe = dimension.value().replace(':', '_').replace('/', '_');
        return root.resolve(safe).resolve("r." + regionX + "." + regionZ + ".smap");
    }

    public void write(MapRegion region) throws IOException {
        Objects.requireNonNull(region, "region");
        RegionPos regionPosition = RegionPos.fromChunk(region.origin());
        Path target = path(region.dimension(), regionPosition.x(), regionPosition.z());
        AtomicArchiveStore.write(target, ArchiveFormat.SMAP, RegionArchiveCodec.encode(region, ArchiveFormat.SMAP));
    }

    public Optional<MapRegion> read(DimensionId dimension, int regionX, int regionZ) throws IOException {
        Path target = path(dimension, regionX, regionZ);
        if (!Files.isRegularFile(target)) return Optional.empty();
        MapRegion region = RegionArchiveCodec.decode(AtomicArchiveStore.read(target, ArchiveFormat.SMAP), ArchiveFormat.SMAP);
        RegionPos actualRegion = RegionPos.fromChunk(region.origin());
        if (!actualRegion.equals(new RegionPos(regionX, regionZ))) {
            throw new IOException("region origin does not match cache key");
        }
        return Optional.of(region);
    }

    public boolean invalidate(DimensionId dimension, int regionX, int regionZ) throws IOException {
        Path target = path(dimension, regionX, regionZ);
        if (!Files.exists(target)) return false;
        Path backup = target.resolveSibling(target.getFileName() + ".invalid");
        Files.move(target, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return true;
    }
}
