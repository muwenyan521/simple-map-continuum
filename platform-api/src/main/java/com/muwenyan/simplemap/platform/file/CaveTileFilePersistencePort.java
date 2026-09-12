package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.TileKey;
import com.muwenyan.simplemap.core.persistence.CaveTileCodec;
import com.muwenyan.simplemap.platform.port.PersistencePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public final class CaveTileFilePersistencePort implements PersistencePort {
    private final Path root;

    public CaveTileFilePersistencePort(Path root) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
    }

    @Override
    public Optional<CaveTile> read(long epoch, TileKey key) {
        if (epoch < 0) throw new IllegalArgumentException("epoch must be non-negative");
        Path path = path(epoch, Objects.requireNonNull(key, "key"));
        if (!Files.isRegularFile(path)) return Optional.empty();
        try {
            CaveTile tile = CaveTileCodec.decode(Files.readAllBytes(path));
            if (tile.epoch() != epoch || !tile.key().equals(key)) {
                throw new IllegalStateException("cave tile namespace mismatch");
            }
            return Optional.of(tile);
        } catch (IOException exception) {
            throw new IllegalStateException("cannot read cave tile", exception);
        }
    }

    @Override
    public void write(CaveTile tile) {
        Objects.requireNonNull(tile, "tile");
        try {
            Path target = path(tile.epoch(), tile.key());
            Files.createDirectories(target.getParent());
            if (Files.exists(target)) {
                Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
            boolean moved = false;
            try {
                Files.write(temporary, CaveTileCodec.encode(tile));
                CaveTileCodec.decode(Files.readAllBytes(temporary));
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
                moved = true;
            } finally {
                if (!moved) Files.deleteIfExists(temporary);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("cannot write cave tile", exception);
        }
    }

    public Path path(long epoch, TileKey key) {
        if (epoch < 0) throw new IllegalArgumentException("epoch must be non-negative");
        return pathForNamespace(epoch, Objects.requireNonNull(key, "key"));
    }

    private Path pathForNamespace(long epoch, TileKey key) {
        String dimension = key.dimension().value().replace(':', '_').replace('/', '_');
        return root.resolve(dimension).resolve("e" + epoch).resolve("m" + key.lod())
                .resolve("t." + key.x() + "." + key.z() + ".cvr");
    }
}
