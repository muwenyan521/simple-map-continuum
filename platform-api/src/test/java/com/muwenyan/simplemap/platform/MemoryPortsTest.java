package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.core.session.CancellationToken;
import com.muwenyan.simplemap.platform.memory.DirectSchedulerPort;
import com.muwenyan.simplemap.platform.memory.LoopbackNetworkPort;
import com.muwenyan.simplemap.platform.memory.MemoryConfigPort;
import com.muwenyan.simplemap.platform.memory.MemoryRenderPort;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemoryPortsTest {
    @Test
    void portsPreserveBoundaries() throws Exception {
        MemoryConfigPort config = new MemoryConfigPort();
        MapRuntime runtime = new MapRuntime((dimension, position) -> java.util.Optional.empty(), frame -> { },
                new com.muwenyan.simplemap.core.navigation.NavigationState(
                        new com.muwenyan.simplemap.core.navigation.MapViewport(0, 0, 1)), MapConfig.defaults());
        runtime.saveConfig(config);
        assertEquals(MapConfig.defaults(), com.muwenyan.simplemap.core.config.MapConfigCodec.decode(config.read().orElseThrow()));
        MemoryRenderPort render = new MemoryRenderPort();
        render.publish(new MapRenderFrame(new DimensionId("minecraft:overworld"), 1, 1, List.of()));
        assertEquals(1, render.latest().orElseThrow().generation());
        AtomicInteger received = new AtomicInteger();
        new LoopbackNetworkPort(bytes -> received.set(bytes.length)).send(new byte[]{1, 2}).get();
        assertEquals(2, received.get());
    }

    @Test
    void schedulerChecksCancellationBeforeAndAfterTask() {
        CancellationToken cancelled = () -> true;
        DirectSchedulerPort scheduler = new DirectSchedulerPort(Runnable::run);
        assertThrows(Exception.class, () -> scheduler.submit(() -> 1, cancelled).join());
    }
}
