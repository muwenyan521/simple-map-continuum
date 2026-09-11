package com.muwenyan.simplemap.core;

import com.muwenyan.simplemap.core.model.*;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.core.session.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreContractsTest {
    @Test void snapshotsDefensivelyCopyPayload() {
        byte[] bytes = {1, 2};
        ChunkSnapshot snapshot = new ChunkSnapshot(new DimensionId("minecraft:overworld"), new ChunkPos(1, -2), 3, bytes);
        bytes[0] = 9;
        assertEquals(1, snapshot.payload()[0]);
        byte[] returned = snapshot.payload(); returned[1] = 8;
        assertEquals(2, snapshot.payload()[1]);
    }

    @Test void sessionRejectsCancelledAndStaleTasks() {
        Session session = new Session(7);
        assertTrue(session.accepts(7));
        assertFalse(session.accepts(8));
        session.cancel();
        assertFalse(session.accepts(7));
        assertThrows(com.muwenyan.simplemap.core.session.CancellationToken.CancellationException.class,
                () -> session.token().throwIfCancelled());
    }

    @Test void renderFrameIsImmutableAndCoordinatesAreStable() {
        DimensionId dim = new DimensionId("minecraft:overworld");
        TileKey key = new TileKey(dim, RegionPos.fromChunk(new ChunkPos(-1, -33)), 2, 0, 0);
        MapRenderFrame frame = new MapRenderFrame(dim, 1, 2,
                List.of(new com.muwenyan.simplemap.core.render.RenderTile(key, 2, 128, 128)));
        assertThrows(UnsupportedOperationException.class, () -> frame.tiles().clear());
        assertEquals(new RegionPos(-1, -2), RegionPos.fromChunk(new ChunkPos(-1, -33)));
    }
}
