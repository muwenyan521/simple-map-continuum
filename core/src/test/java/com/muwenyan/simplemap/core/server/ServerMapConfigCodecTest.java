package com.muwenyan.simplemap.core.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerMapConfigCodecTest {
    @Test
    void roundTripAndStrictUnknownKeyHandling() {
        ServerMapConfig config = new ServerMapConfig(false, false, true, 10, 2048);
        assertEquals(config, ServerMapConfigCodec.decode(ServerMapConfigCodec.encode(config)));
        assertThrows(IllegalArgumentException.class, () -> ServerMapConfigCodec.decode(
                ServerMapConfigCodec.encode(config) + "extra=true\n"));
    }
}
