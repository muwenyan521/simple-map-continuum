package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.TileKey;
import com.muwenyan.simplemap.core.persistence.CaveTileCodec;
import com.muwenyan.simplemap.platform.port.PersistencePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class CaveTileFilePersistencePort implements PersistencePort {
    private final Path root;

    public CaveTileFilePersistencePort(Path root) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
    }

    @Override
    public Optional<CaveTile> read(TileKey key) {
        Path path = path(Objects.requireNonNull(key, "key"));
        if (!Files.isRegularFile(path)) return Optional.empty();
        try {
            return Optional.of(CaveTileCodec.decode(Files.readAllBytes(path)));
        } catch (IOException exception) {
            throw new IllegalStateException("cannot read cave tile", exception);
        }
    }

    @Override
    public void write(CaveTile tile) {
        Objects.requireNonNull(tile, "tile");
        try {
            Path target = path(tile.key());
            Files.createDirectories(target.getParent());
            if (Files.exists(target)) {
                Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            Files.write(target, CaveTileCodec.encode(tile));
        } catch (IOException exception) {
            throw new IllegalStateException("cannot write cave tile", exception);
        }
    }

    private Path path(TileKey key) {
        String dimension = key.dimension().value().replace(':', '_').replace('/', '_');
        return root.resolve(dimension).resolve("m" + key.lod()).resolve("t." + key.x() + "." + key.z() + ".cvr");
    }
}
