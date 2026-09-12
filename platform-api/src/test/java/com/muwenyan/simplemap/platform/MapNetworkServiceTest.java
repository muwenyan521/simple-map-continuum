package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.HandshakeSession;
import com.muwenyan.simplemap.core.protocol.MapBookHello;
import com.muwenyan.simplemap.core.protocol.MapPacketRouter;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import com.muwenyan.simplemap.platform.memory.LoopbackNetworkPort;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapNetworkServiceTest {
    @Test
    void negotiatesSharedArchiveAndCapability() throws ProtocolException {
        MapNetworkService service = new MapNetworkService(new LoopbackNetworkPort(payload -> { }), new MapPacketRouter());
        MapBookHello local = new MapBookHello(1, 2, 1024, Set.of("SMAP"), Set.of("WAYPOINTS"));
        MapBookHello remote = new MapBookHello(1, 1, 512, Set.of("SMAP"), Set.of("WAYPOINTS"));
        assertEquals(512, service.negotiate(new HandshakeSession(local), remote).maxArchiveBytes());
    }
}
