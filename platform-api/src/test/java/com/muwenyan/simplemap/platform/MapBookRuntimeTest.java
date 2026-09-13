package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MapBookRuntimeTest {
    @TempDir Path temporary;

    @Test
    void persistsAndCopiesBookWithNewIdentity() throws Exception {
        MapBookRuntime runtime = new MapBookRuntime(temporary);
        UUID owner = UUID.randomUUID();
        var source = runtime.create(owner);
        source.save(owner, new RegionPos(0, 0), new byte[]{1});
        runtime.save(source);
        var copy = runtime.copy(source, owner, owner);
        assertEquals(1, runtime.load(copy.id()).snapshot().regions().size());
        assertNotEquals(source.id(), copy.id());
    }

    @Test
    void copyItemCreatesWrittenStateAndPersistsArchive() throws Exception {
        MapBookRuntime runtime = new MapBookRuntime(temporary);
        UUID owner = UUID.randomUUID();
        var source = runtime.create(owner);
        source.save(owner, new RegionPos(0, 0), new byte[]{7});
        runtime.save(source);
        var state = runtime.copyItem(com.muwenyan.simplemap.core.book.MapBookItemState.written(source, "Atlas"),
                com.muwenyan.simplemap.core.book.MapBookItemState.empty(), owner, owner);
        assertEquals(com.muwenyan.simplemap.core.book.MapBookStatus.WRITTEN, state.status());
        assertEquals(1, runtime.load(state.id().orElseThrow()).snapshot().regions().size());
    }

    @Test
    void mergeItemsReturnsWrittenStateForPersistentBooks() throws Exception {
        MapBookRuntime runtime = new MapBookRuntime(temporary);
        UUID owner = UUID.randomUUID();
        var left = runtime.create(owner);
        left.save(owner, new RegionPos(0, 0), new byte[]{1});
        runtime.save(left);
        var right = runtime.create(owner);
        right.save(owner, new RegionPos(1, 0), new byte[]{2});
        runtime.save(right);
        var merged = runtime.mergeItems(MapBookItemState.written(left, "Atlas"),
                MapBookItemState.written(right, "Atlas"), owner, "Merged");
        assertEquals(com.muwenyan.simplemap.core.book.MapBookStatus.WRITTEN, merged.status());
        assertEquals(2, runtime.load(merged.id().orElseThrow()).snapshot().regions().size());
        org.junit.jupiter.api.Assertions.assertNotEquals(left.id(), merged.id().orElseThrow());
    }

    @Test
    void persistsPermissionChanges() throws Exception {
        MapBookRuntime runtime = new MapBookRuntime(temporary);
        UUID owner = UUID.randomUUID();
        UUID reader = UUID.randomUUID();
        var book = runtime.create(owner);
        runtime.grant(book.id(), owner, reader, com.muwenyan.simplemap.core.book.BookPermission.WRITE);
        var loaded = runtime.load(book.id());
        loaded.save(reader, new RegionPos(2, 2), new byte[]{3});
        assertEquals(1, loaded.snapshot().regions().size());
        runtime.revoke(book.id(), owner, reader);
        var revoked = runtime.load(book.id());
        org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class,
                () -> revoked.save(reader, new RegionPos(3, 3), new byte[]{4}));
    }

    @Test
    void archivedPermissionSurvivesReload() throws Exception {
        MapBookRuntime runtime = new MapBookRuntime(temporary);
        UUID owner = UUID.randomUUID();
        UUID reader = UUID.randomUUID();
        var book = runtime.create(owner);
        runtime.grant(book.id(), owner, reader, com.muwenyan.simplemap.core.book.BookPermission.READ);
        var restored = runtime.load(book.id());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> restored.snapshot());
        org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class,
                () -> restored.save(reader, new RegionPos(1, 1), new byte[]{1}));
    }

    @Test
    void actorReadAccessIsCheckedOnLoad() throws Exception {
        MapBookRuntime runtime = new MapBookRuntime(temporary);
        UUID owner = UUID.randomUUID();
        var book = runtime.create(owner);
        org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class,
                () -> runtime.load(book.id(), UUID.randomUUID()));
    }
}
