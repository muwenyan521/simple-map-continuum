package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ChunkStore {
    private final int capacity;
    private final LinkedHashMap<Key, ChunkSnapshot> snapshots = new LinkedHashMap<>(16, 0.75f, true);
    private long storeRevision;

    public ChunkStore(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    public synchronized ChunkMutation ingest(ChunkSnapshot incoming) {
        Objects.requireNonNull(incoming, "incoming");
        Key key = new Key(incoming.dimension(), incoming.position());
        ChunkSnapshot existing = snapshots.get(key);
        if (existing != null) {
            if (incoming.revision() < existing.revision()) {
                return new ChunkMutation(MutationKind.STALE, storeRevision, existing, Optional.of(existing));
            }
            if (incoming.revision() == existing.revision()) {
                MutationKind kind = Arrays.equals(incoming.payload(), existing.payload())
                        ? MutationKind.DUPLICATE : MutationKind.CONFLICT;
                return new ChunkMutation(kind, storeRevision, existing, Optional.of(existing));
            }
        }
        snapshots.put(key, incoming);
        storeRevision = Math.addExact(storeRevision, 1);
        while (snapshots.size() > capacity) snapshots.remove(snapshots.keySet().iterator().next());
        return new ChunkMutation(MutationKind.APPLIED, storeRevision, incoming, Optional.ofNullable(existing));
    }

    public synchronized Optional<ChunkSnapshot> snapshot(DimensionId dimension, ChunkPos position) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(position, "position");
        return Optional.ofNullable(snapshots.get(new Key(dimension, position)));
    }

    public synchronized List<ChunkSnapshot> snapshots() {
        List<ChunkSnapshot> result = new ArrayList<>(snapshots.values());
        result.sort(Comparator.comparing((ChunkSnapshot value) -> value.dimension().value())
                .thenComparingInt(value -> value.position().x())
                .thenComparingInt(value -> value.position().z()));
        return List.copyOf(result);
    }

    public synchronized int size() { return snapshots.size(); }
    public int capacity() { return capacity; }
    public synchronized long revision() { return storeRevision; }

    public synchronized boolean evict(DimensionId dimension, ChunkPos position) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(position, "position");
        return snapshots.remove(new Key(dimension, position)) != null;
    }

    private record Key(DimensionId dimension, ChunkPos position) { }
}
