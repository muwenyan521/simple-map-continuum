package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.cave.CaveColumnRun;
import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaveRegionArchiveTest {
    @Test
    void roundTripsCvr1Records() throws Exception {
        List<List<CaveColumnRun>> columns = new ArrayList<>();
        for (int i = 0; i < 256; i++) columns.add(List.of());
        CaveSnapshot snapshot = new CaveSnapshot(new ChunkPos(3, -2), 4, true, false, columns);
        Map<ChunkPos, CaveSnapshot> decoded = CaveRegionArchive.decode(
                CaveRegionArchive.encode(Map.of(snapshot.chunk(), snapshot)));
        assertEquals(snapshot.revision(), decoded.get(snapshot.chunk()).revision());
    }
}
