package com.muwenyan.simplemap.core.protocol;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HandshakeSessionTest {
    @Test
    void successfulHandshakeTransitionsToEstablished() throws Exception {
        MapBookHello local = new MapBookHello(1, 2, 4096, Set.of("SMAP"), Set.of("SAVE"));
        HandshakeSession session = new HandshakeSession(local);
        assertEquals(local, session.sendHello());
        session.acceptHello(new MapBookHello(1, 1, 2048, Set.of("SMAP"), Set.of("SAVE")));
        assertEquals(HandshakeState.ESTABLISHED, session.state());
        assertEquals(2048, session.negotiated().maxArchiveBytes());
    }

    @Test
    void invalidOrderFailsAndCannotRecover() throws Exception {
        HandshakeSession session = new HandshakeSession(
                new MapBookHello(1, 0, 4096, Set.of("SMAP"), Set.of()));
        assertThrows(ProtocolException.class, () -> session.acceptHello(session.local()));
        assertEquals(HandshakeState.FAILED, session.state());
        assertThrows(ProtocolException.class, session::sendHello);
    }
}
