package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class ChunkEventRouter {
    private final ChunkStore store;
    private Consumer<ChunkEvent> listener = ignored -> { };

    public ChunkEventRouter(ChunkStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public synchronized void onEvent(ChunkEvent event) {
        Objects.requireNonNull(event, "event");
        if (event.kind() == ChunkEventKind.UNLOADED) {
            store.evict(event.dimension(), event.position());
            listener.accept(event);
            return;
        }
        listener.accept(event);
    }

    public synchronized void listener(Consumer<ChunkEvent> next) {
        listener = Objects.requireNonNull(next, "next");
    }

    public Optional<com.muwenyan.simplemap.core.model.ChunkSnapshot> current(DimensionId dimension, ChunkPos position) {
        return store.snapshot(dimension, position);
    }
}
