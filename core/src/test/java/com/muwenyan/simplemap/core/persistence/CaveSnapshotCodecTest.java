package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.cave.CaveColumnRun;
import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaveSnapshotCodecTest {
    @Test
    void roundTripsCvt4ColumnsAndRejectsCorruption() throws Exception {
        List<List<CaveColumnRun>> columns = new ArrayList<>();
        for (int index = 0; index < 256; index++) {
            columns.add(index == 5
                    ? List.of(new CaveColumnRun(80, 64, 0xff223344, 12, true, false))
                    : List.of());
        }
        CaveSnapshot source = new CaveSnapshot(new ChunkPos(-3, 7), 11, true, true, columns);
        CaveSnapshot decoded = CaveSnapshotCodec.decode(CaveSnapshotCodec.encode(source));
        assertEquals(source.chunk(), decoded.chunk());
        assertEquals(source.revision(), decoded.revision());
        assertEquals(1, decoded.runCount());
        assertEquals(source.columns().get(5), decoded.columns().get(5));
        byte[] corrupted = CaveSnapshotCodec.encode(source);
        corrupted[corrupted.length - 5] ^= 1;
        assertThrows(ArchiveException.class, () -> CaveSnapshotCodec.decode(corrupted));
    }
}
