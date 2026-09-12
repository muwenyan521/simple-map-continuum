package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.platform.file.CaveRegionFileService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaveRegionFileServiceTest {
    @TempDir Path temporary;

    @Test
    void writesValidatesReadsAndBacksUpCvrRegion() throws Exception {
        CaveRegionFileService service = new CaveRegionFileService(temporary);
        ChunkPos chunk = new ChunkPos(2, 3);
        java.util.ArrayList<List<com.muwenyan.simplemap.core.cave.CaveColumnRun>> columns = new java.util.ArrayList<>();
        for (int i = 0; i < 256; i++) columns.add(List.of());
        CaveSnapshot snapshot = new CaveSnapshot(chunk, 1, true, false, columns);
        service.write(8, 1, 0, 0, Map.of(chunk, snapshot));
        service.write(8, 1, 0, 0, Map.of(chunk, snapshot));
        assertEquals(1, service.read(8, 1, 0, 0).size());
        assertTrue(Files.exists(service.path(8, 1, 0, 0).resolveSibling("r.0.0.cvr.bak")));
    }
}
