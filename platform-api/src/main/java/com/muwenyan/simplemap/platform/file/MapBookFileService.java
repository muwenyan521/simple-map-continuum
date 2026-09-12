package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookArchiveCodec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

public final class MapBookFileService {
    private final Path root;

    public MapBookFileService(Path root) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
    }

    public Path path(UUID id) {
        return root.resolve("simplemap_books").resolve(Objects.requireNonNull(id, "id") + ".smbk");
    }

    public void write(MapBook book) throws IOException {
        Objects.requireNonNull(book, "book");
        Path target = path(book.id());
        Files.createDirectories(target.getParent());
        byte[] encoded = MapBookArchiveCodec.encode(book);
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            Files.write(temporary, encoded);
            MapBookArchiveCodec.decode(Files.readAllBytes(temporary));
            if (Files.exists(target)) {
                Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporary);
        }
    }

    public MapBook read(UUID id) throws IOException {
        Path target = path(id);
        return MapBookArchiveCodec.decode(Files.readAllBytes(target));
    }

    public MapBook readRecovering(UUID id) throws IOException {
        Objects.requireNonNull(id, "id");
        try {
            return read(id);
        } catch (IOException primary) {
            Path backup = path(id).resolveSibling(id + ".smbk.bak");
            if (!Files.isRegularFile(backup)) throw primary;
            try {
                return MapBookArchiveCodec.decode(Files.readAllBytes(backup));
            } catch (IOException recovery) {
                recovery.addSuppressed(primary);
                throw recovery;
            }
        }
    }
}
