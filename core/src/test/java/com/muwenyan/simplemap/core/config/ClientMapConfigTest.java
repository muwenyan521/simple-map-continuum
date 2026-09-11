package com.muwenyan.simplemap.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientMapConfigTest {
    @Test
    void defaultsComposeAllClientDomains() {
        ClientMapConfig config = ClientMapConfig.defaults();
        assertEquals(16, config.map().renderDistance());
        assertEquals(128, config.minimap().sizePixels());
        assertEquals(8, config.cave().maxLayers());
    }
}
