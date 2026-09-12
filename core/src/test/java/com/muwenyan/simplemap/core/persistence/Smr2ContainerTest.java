package com.muwenyan.simplemap.core.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Smr2ContainerTest {
    @TempDir Path temporary;

    @Test
    void recoversDamagedTailAndKeepsNewestRevision() throws Exception {
        Path path = temporary.resolve("r.0.0.smr2");
        Smr2Header header = new Smr2Header(42, 0, 0, 1);
        Smr2Container.append(path, header, new Smr2Record(Smr2RecordType.SURFACE_SOURCE, 7, 1, 1, new byte[]{1}));
        Smr2Container.append(path, header, new Smr2Record(Smr2RecordType.SURFACE_SOURCE, 7, 2, 0, new byte[]{2}));
        Files.write(path, new byte[]{0, 1, 2}, java.nio.file.StandardOpenOption.APPEND);

        Smr2ScanResult damaged = Smr2Container.scan(path, header);
        assertTrue(damaged.damagedTail());
        assertArrayEquals(new byte[]{2}, damaged.latest().values().iterator().next().payload());

        Smr2Container.append(path, header, new Smr2Record(Smr2RecordType.CAVE_ARCHIVE, 9, 1, 0, new byte[]{9}));
        assertFalse(Smr2Container.scan(path, header).damagedTail());
        assertEquals(2, Smr2Container.scan(path, header).latest().size());
    }

    @Test
    void rejectsWrongWorldIdentityAndCompactsWithBackup() throws Exception {
        Path path = temporary.resolve("r.-1.3.smr2");
        Smr2Header header = new Smr2Header(5, -1, 3, 1);
        Smr2Container.append(path, header, new Smr2Record(Smr2RecordType.CAVE_ARCHIVE, 1, 1, 1, new byte[]{1}));
        Smr2Container.append(path, header, new Smr2Record(Smr2RecordType.CAVE_ARCHIVE, 1, 1, 2, new byte[]{2}));

        assertThrows(ArchiveException.class, () -> Smr2Container.scan(path, new Smr2Header(6, -1, 3, 1)));
        Smr2Container.compact(path, header);
        assertTrue(Files.isRegularFile(path.resolveSibling("r.-1.3.smr2.bak")));
        assertEquals(1, Smr2Container.scan(path, header).latest().size());
    }
}
