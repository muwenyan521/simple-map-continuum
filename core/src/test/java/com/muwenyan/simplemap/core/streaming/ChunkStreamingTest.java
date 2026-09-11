package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ChunkStreamingTest {
    private static final DimensionId OVERWORLD = new DimensionId("minecraft:overworld");

    @Test void plannerIsBoundedCenterOutAndDeterministic() {
        ChunkDemand demand = new ChunkDemand(OVERWORLD, new ChunkPos(10, -4), 2, 7);
        List<ChunkPos> first = CenterOutChunkPlanner.plan(demand);
        assertEquals(7, first.size());
        assertEquals(new ChunkPos(10, -4), first.get(0));
        assertIterableEquals(first, CenterOutChunkPlanner.plan(demand));
        assertEquals(List.of(new ChunkPos(10, -4), new ChunkPos(9, -4), new ChunkPos(10, -5),
                new ChunkPos(10, -3), new ChunkPos(11, -4), new ChunkPos(9, -5), new ChunkPos(9, -3)), first);
    }

    @Test void plannerHandlesNegativeCoordinatesWithoutOverflow() {
        List<ChunkPos> result = CenterOutChunkPlanner.plan(new ChunkDemand(OVERWORLD,
                new ChunkPos(Integer.MIN_VALUE + 2, Integer.MAX_VALUE - 2), 1, 9));
        assertEquals(9, result.size());
        assertTrue(result.contains(new ChunkPos(Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 2)));
    }

    @Test void plannerStaysBoundedForLargeDemandAndRejectsCoordinateOverflow() {
        assertEquals(List.of(new ChunkPos(0, 0)), CenterOutChunkPlanner.plan(
                new ChunkDemand(OVERWORLD, new ChunkPos(0, 0), Integer.MAX_VALUE, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> new ChunkDemand(OVERWORLD, new ChunkPos(Integer.MAX_VALUE, 0), 1, 1));
    }

    @Test void storeRejectsStaleAndConflictingMutations() {
        ChunkStore store = new ChunkStore(2);
        ChunkSnapshot initial = snapshot(1, 3);
        ChunkMutation applied = store.ingest(initial);
        assertEquals(MutationKind.APPLIED, applied.kind());
        assertEquals(1, applied.storeRevision());
        assertEquals(MutationKind.DUPLICATE, store.ingest(snapshot(1, 3)).kind());
        assertEquals(MutationKind.CONFLICT, store.ingest(snapshot(1, 4)).kind());
        assertEquals(MutationKind.STALE, store.ingest(snapshot(0, 2)).kind());
        assertEquals(1, store.revision());
    }

    @Test void storeAdvancesRevisionAndEvictsLeastRecentlyUsed() {
        ChunkStore store = new ChunkStore(2);
        store.ingest(snapshot(1, 1));
        store.ingest(snapshotAt(new ChunkPos(1, 0), 1, 2));
        assertTrue(store.snapshot(OVERWORLD, new ChunkPos(0, 0)).isPresent());
        store.snapshot(OVERWORLD, new ChunkPos(0, 0));
        store.ingest(snapshotAt(new ChunkPos(2, 0), 1, 3));
        assertTrue(store.snapshot(OVERWORLD, new ChunkPos(0, 0)).isPresent());
        assertFalse(store.snapshot(OVERWORLD, new ChunkPos(1, 0)).isPresent());
        assertEquals(3, store.revision());
        assertEquals(2, store.size());
    }

    private static ChunkSnapshot snapshot(long revision, int payload) {
        return snapshotAt(new ChunkPos(0, 0), revision, payload);
    }

    private static ChunkSnapshot snapshotAt(ChunkPos position, long revision, int payload) {
        return new ChunkSnapshot(OVERWORLD, position, revision, new byte[] {(byte) payload});
    }
}
