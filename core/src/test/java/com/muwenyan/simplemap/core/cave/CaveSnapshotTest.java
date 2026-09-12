package com.muwenyan.simplemap.core.cave;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaveSnapshotTest {
    @Test
    void storesAllColumnRunsWithStableCount() {
        List<List<CaveColumnRun>> columns = new ArrayList<>();
        for (int i = 0; i < 256; i++) columns.add(i == 0
                ? List.of(new CaveColumnRun(20, 10, 0xff334455, 5, false, false))
                : List.of());
        CaveSnapshot snapshot = new CaveSnapshot(new ChunkPos(1, 2), 7, true, false, columns);
        assertEquals(1, snapshot.runCount());
    }
}
