package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CenterOutChunkPlanner {
    private static final Comparator<PlannedChunk> ORDER = Comparator
            .comparingLong(PlannedChunk::distanceSquared)
            .thenComparingInt(chunk -> chunk.position().x())
            .thenComparingInt(chunk -> chunk.position().z());

    private CenterOutChunkPlanner() { }

    public static List<ChunkPos> plan(ChunkDemand demand) {
        if (demand == null) throw new NullPointerException("demand");
        List<PlannedChunk> candidates = new ArrayList<>(demand.maxChunks());
        int radius = demand.radius();
        ChunkPos center = demand.center();
        for (int ring = 0; ring <= radius && candidates.size() < demand.maxChunks(); ring++) {
            appendRing(candidates, center, ring);
        }
        candidates.sort(ORDER);
        candidates.subList(demand.maxChunks(), candidates.size()).clear();
        return candidates.stream().map(PlannedChunk::position).toList();
    }

    private static void appendRing(List<PlannedChunk> output, ChunkPos center, int ring) {
        if (ring == 0) {
            output.add(new PlannedChunk(center, 0));
            return;
        }
        long distance = ring;
        for (long offset = -distance; offset <= distance; offset++) {
            add(output, center, offset, -distance);
            add(output, center, offset, distance);
        }
        for (long offset = -distance + 1; offset < distance; offset++) {
            add(output, center, -distance, offset);
            add(output, center, distance, offset);
        }
    }

    private static void add(List<PlannedChunk> output, ChunkPos center, long dx, long dz) {
        output.add(new PlannedChunk(new ChunkPos(Math.toIntExact(center.x() + dx),
                Math.toIntExact(center.z() + dz)), dx * dx + dz * dz));
    }

    private record PlannedChunk(ChunkPos position, long distanceSquared) { }
}
