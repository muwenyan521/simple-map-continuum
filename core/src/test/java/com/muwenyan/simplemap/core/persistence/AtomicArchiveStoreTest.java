package com.muwenyan.simplemap.core.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtomicArchiveStoreTest {
    @TempDir
    Path temporary;

    @Test
    void replacingArchiveLeavesRecoverableBackup() throws Exception {
        Path target = temporary.resolve("region.smap");
        AtomicArchiveStore.write(target, ArchiveFormat.SMAP, new byte[]{1, 2});
        byte[] old = Files.readAllBytes(target);
        AtomicArchiveStore.write(target, ArchiveFormat.SMAP, new byte[]{3, 4});
        Path backup = temporary.resolve("region.smap.bak");
        assertTrue(Files.exists(backup));
        assertArrayEquals(old, Files.readAllBytes(backup));
        assertArrayEquals(new byte[]{3, 4}, AtomicArchiveStore.read(target, ArchiveFormat.SMAP));
    }
}
