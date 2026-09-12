package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.platform.port.ConfigPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public final class FileConfigPort implements ConfigPort {
    private final Path path;

    public FileConfigPort(Path path) { this.path = Objects.requireNonNull(path, "path").toAbsolutePath().normalize(); }

    @Override
    public Optional<String> read() {
        if (!Files.isRegularFile(path)) return Optional.empty();
        try { return Optional.of(Files.readString(path)); }
        catch (IOException exception) { throw new IllegalStateException("cannot read map config", exception); }
    }

    @Override
    public void write(String payload) {
        Objects.requireNonNull(payload, "payload");
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
            boolean moved = false;
            try {
                Files.writeString(temporary, payload);
                if (!payload.equals(Files.readString(temporary))) throw new IOException("config verification failed");
                try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (java.nio.file.AtomicMoveNotSupportedException exception) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
                moved = true;
            } finally {
                if (!moved) Files.deleteIfExists(temporary);
            }
        } catch (IOException exception) { throw new IllegalStateException("cannot write map config", exception); }
    }
}
