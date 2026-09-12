package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.platform.file.MapBookFileService;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapBookFileServiceTest {
    @TempDir Path temporary;

    @Test
    void writesReadsAndBacksUpMapBook() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        book.save(owner, new RegionPos(0, 0), new byte[]{1});
        MapBookFileService service = new MapBookFileService(temporary);
        service.write(book);
        service.write(book);
        assertEquals(book.id(), service.read(book.id()).id());
        assertTrue(java.nio.file.Files.exists(service.path(book.id()).resolveSibling(book.id() + ".smbk.bak")));
    }

    @Test
    void recoversFromVerifiedBackupAfterPrimaryCorruption() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        book.save(owner, new RegionPos(0, 0), new byte[]{1});
        MapBookFileService service = new MapBookFileService(temporary);
        service.write(book);
        book.save(owner, new RegionPos(1, 0), new byte[]{2});
        service.write(book);
        java.nio.file.Files.write(service.path(book.id()), new byte[]{0});
        assertEquals(1, service.readRecovering(book.id()).snapshot().regions().size());
    }
}
