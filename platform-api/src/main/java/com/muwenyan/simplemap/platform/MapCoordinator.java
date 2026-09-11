package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.session.Session;
import com.muwenyan.simplemap.core.session.CancellationToken;
import com.muwenyan.simplemap.core.streaming.ChunkDemand;
import com.muwenyan.simplemap.core.streaming.ChunkMutation;
import com.muwenyan.simplemap.platform.port.SchedulerPort;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public final class MapCoordinator {
    private final MapRuntime runtime;
    private final SchedulerPort scheduler;
    private final AtomicLong generation = new AtomicLong();
    private Session active;

    public MapCoordinator(MapRuntime runtime, SchedulerPort scheduler) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    public synchronized CompletableFuture<List<ChunkMutation>> refresh(ChunkDemand demand) {
        Objects.requireNonNull(demand, "demand");
        if (active != null) {
            active.cancel();
        }
        Session session = new Session(generation.incrementAndGet());
        active = session;
        return scheduler.submit(() -> runtime.refresh(demand), session.token())
                .thenApply(result -> {
                    synchronized (this) {
                        if (!session.accepts(session.generation())) {
                            throw new CancellationToken.CancellationException();
                        }
                        return List.copyOf(result);
                    }
                });
    }

    public synchronized void cancel() {
        if (active != null) {
            active.cancel();
            active = null;
        }
    }

    public synchronized long generation() {
        return generation.get();
    }
}
