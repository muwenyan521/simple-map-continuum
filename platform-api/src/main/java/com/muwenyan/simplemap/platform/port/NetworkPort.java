package com.muwenyan.simplemap.platform.port;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface NetworkPort {
    CompletableFuture<Void> send(byte[] payload);

    default void requirePayload(byte[] payload) {
        Objects.requireNonNull(payload, "payload");
    }
}
