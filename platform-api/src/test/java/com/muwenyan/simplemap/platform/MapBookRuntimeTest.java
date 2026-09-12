package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.RegionPos;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        org.junit.jupiter.api.Assertions.assertNotEquals(source.id(), copy.id());
    }
}
