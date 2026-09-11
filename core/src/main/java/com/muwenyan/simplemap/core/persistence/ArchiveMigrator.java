package com.muwenyan.simplemap.core.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArchiveMigrator {
    private ArchiveMigrator() { }

    public static MigrationResult migrate(Path source, Path target, ArchiveFormat sourceFormat, ArchiveFormat targetFormat) throws IOException {
        byte[] payload = AtomicArchiveStore.read(source, sourceFormat);
        AtomicArchiveStore.write(target, targetFormat, payload);
        byte[] verified = AtomicArchiveStore.read(target, targetFormat);
        if (!java.util.Arrays.equals(payload, verified)) {
            throw new ArchiveException("migration verification mismatch");
        }
        return new MigrationResult(true, payload, sourceFormat.magic());
    }

    public static boolean isValid(Path path, ArchiveFormat format) {
        try { ArchiveCodec.decode(format, Files.readAllBytes(path)); return true; }
        catch (IOException | RuntimeException ignored) { return false; }
    }
}
