package com.muwenyan.simplemap.platform.memory;

import com.muwenyan.simplemap.platform.port.ConfigPort;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MemoryConfigPort implements ConfigPort {
    private final AtomicReference<String> value = new AtomicReference<>();

    @Override
    public Optional<String> read() {
        return Optional.ofNullable(value.get());
    }

    @Override
    public void write(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalArgumentException("encoded config");
        }
        value.set(encoded);
    }
}
