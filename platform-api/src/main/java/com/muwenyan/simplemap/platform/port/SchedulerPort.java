package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.session.CancellationToken;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface SchedulerPort {
    <T> CompletableFuture<T> submit(Supplier<T> task, CancellationToken token);
}
