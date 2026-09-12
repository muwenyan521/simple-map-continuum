package com.muwenyan.simplemap.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientMapConfigCodecTest {
    @Test
    void roundTripsComposedClientConfig() {
        ClientMapConfig source = ClientMapConfig.defaults();
        ClientMapConfig decoded = ClientMapConfigCodec.decode(ClientMapConfigCodec.encode(source));
        assertEquals(source.map(), decoded.map());
        assertEquals(source.minimap(), decoded.minimap());
        assertEquals(source.cave(), decoded.cave());
        assertEquals(source.features(), decoded.features());
    }
}
