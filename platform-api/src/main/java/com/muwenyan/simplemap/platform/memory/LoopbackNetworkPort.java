package com.muwenyan.simplemap.platform.memory;

import com.muwenyan.simplemap.platform.port.NetworkPort;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class LoopbackNetworkPort implements NetworkPort {
    private final Consumer<byte[]> receiver;

    public LoopbackNetworkPort(Consumer<byte[]> receiver) {
        this.receiver = Objects.requireNonNull(receiver, "receiver");
    }

    @Override
    public CompletableFuture<Void> send(byte[] payload) {
        requirePayload(payload);
        byte[] copy = payload.clone();
        receiver.accept(copy);
        return CompletableFuture.completedFuture(null);
    }
}
