package com.muwenyan.simplemap.platform.memory;

import com.muwenyan.simplemap.core.session.CancellationToken;
import com.muwenyan.simplemap.platform.port.SchedulerPort;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class DirectSchedulerPort implements SchedulerPort {
    private final Executor executor;

    public DirectSchedulerPort(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public <T> CompletableFuture<T> submit(Supplier<T> task, CancellationToken token) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(token, "token");
        return CompletableFuture.supplyAsync(() -> {
            token.throwIfCancelled();
            T result = task.get();
            token.throwIfCancelled();
            return result;
        }, executor);
    }
}
