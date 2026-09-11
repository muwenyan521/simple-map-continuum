package com.muwenyan.simplemap.core.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class AtomicArchiveStore {
    private AtomicArchiveStore() { }

    public static void write(Path target, ArchiveFormat format, byte[] payload) throws IOException {
        byte[] encoded = ArchiveCodec.encode(format, payload);
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        boolean replaced = false;
        try {
            Files.write(temporary, encoded, StandardOpenOption.TRUNCATE_EXISTING);
            ArchiveCodec.decode(format, Files.readAllBytes(temporary));
            if (Files.exists(target)) {
                Path backup = target.resolveSibling(target.getFileName() + ".bak");
                Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException atomicFailure) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
            replaced = true;
        } finally {
            if (!replaced) Files.deleteIfExists(temporary);
        }
    }

    public static byte[] read(Path source, ArchiveFormat format) throws IOException {
        return ArchiveCodec.decode(format, Files.readAllBytes(source));
    }
}
