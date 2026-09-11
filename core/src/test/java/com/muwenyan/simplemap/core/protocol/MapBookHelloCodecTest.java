package com.muwenyan.simplemap.core.protocol;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookHelloCodecTest {
    @Test
    void roundTripPreservesNegotiationFields() throws Exception {
        MapBookHello hello = new MapBookHello(1, 2, 1024 * 1024,
                Set.of("SMAP", "SMR2"), Set.of("SAVE", "LEARN"));
        assertEquals(hello, MapBookHelloCodec.decode(MapBookHelloCodec.encode(hello)));
    }

    @Test
    void malformedWireIsRejected() throws Exception {
        MapBookHello hello = new MapBookHello(1, 0, 4096, Set.of("SMAP"), Set.of());
        byte[] wire = MapBookHelloCodec.encode(hello);
        byte[] truncated = java.util.Arrays.copyOf(wire, wire.length - 1);
        assertThrows(ProtocolException.class, () -> MapBookHelloCodec.decode(truncated));
    }
}
