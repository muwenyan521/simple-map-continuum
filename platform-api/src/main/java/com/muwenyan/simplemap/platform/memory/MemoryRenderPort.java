package com.muwenyan.simplemap.platform.memory;

import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.platform.port.RenderPort;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MemoryRenderPort implements RenderPort {
    private final AtomicReference<MapRenderFrame> latest = new AtomicReference<>();

    @Override
    public void publish(MapRenderFrame frame) {
        latest.set(Objects.requireNonNull(frame, "frame"));
    }

    public Optional<MapRenderFrame> latest() {
        return Optional.ofNullable(latest.get());
    }
}
