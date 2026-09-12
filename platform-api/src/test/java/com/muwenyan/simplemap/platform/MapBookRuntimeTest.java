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
}
