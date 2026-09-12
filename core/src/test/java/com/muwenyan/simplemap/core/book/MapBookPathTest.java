package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookPathTest {
    @Test
    void buildsStableVanillaAndModdedRegionPaths() {
        Path path = MapBookPath.region(Path.of("world"), UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new DimensionId("mod:sky/void"), new RegionPos(-2, 3));
        assertEquals(Path.of("world/simplemap_books/00000000-0000-0000-0000-000000000001/mod_sky_void/r.-2.3.smdat"), path);
        assertThrows(IllegalArgumentException.class, () -> MapBookPath.dimensionFolder(new DimensionId("..")));
    }
}
