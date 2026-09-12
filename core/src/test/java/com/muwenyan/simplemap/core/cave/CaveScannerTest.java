package com.muwenyan.simplemap.core.cave;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaveScannerTest {
    @Test
    void scansAllColumnsWithRevision() {
        CaveSnapshot snapshot = CaveScanner.scan(new ChunkPos(2, -1), CaveConfig.defaults(),
                (x, z) -> (x == 0 && z == 0)
                        ? List.of(new CaveColumnRun(12, 4, 0xFF112233, 9, false, true))
                        : List.of(), 7);
        assertEquals(7, snapshot.revision());
        assertEquals(256, snapshot.columns().size());
        assertEquals(1, snapshot.columns().get(0).size());
        assertEquals(1, snapshot.runCount());
    }
}
