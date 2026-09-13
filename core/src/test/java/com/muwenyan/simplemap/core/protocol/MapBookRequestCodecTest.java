package com.muwenyan.simplemap.core.protocol;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookRequestCodecTest {
    @Test
    void roundTripsBookAndOperation() throws Exception {
        MapBookRequest request = new MapBookRequest(UUID.randomUUID(), MapBookOperation.LEARN);
        assertEquals(request, MapBookRequestCodec.decode(MapBookRequestCodec.encode(request)));
    }

    @Test
    void rejectsInvalidLengthAndOperation() {
        assertThrows(ProtocolException.class, () -> MapBookRequestCodec.decode(new byte[16]));
        byte[] invalid = new byte[17];
        invalid[16] = 99;
        assertThrows(ProtocolException.class, () -> MapBookRequestCodec.decode(invalid));
    }
}
