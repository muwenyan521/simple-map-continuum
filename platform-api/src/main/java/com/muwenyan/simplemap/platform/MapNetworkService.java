package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.MapPacketRouter;
import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import com.muwenyan.simplemap.core.protocol.HandshakeSession;
import com.muwenyan.simplemap.core.protocol.MapBookHello;
import com.muwenyan.simplemap.platform.port.NetworkPort;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class MapNetworkService {
    private final NetworkPort transport;
    private final MapPacketRouter router;

    public MapNetworkService(NetworkPort transport, MapPacketRouter router) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.router = Objects.requireNonNull(router, "router");
    }

    public CompletableFuture<Void> send(MapBookFrame frame) {
        Objects.requireNonNull(frame, "frame");
        try {
            return transport.send(FrameCodec.encode(frame));
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    public void receive(byte[] payload) throws ProtocolException {
        router.dispatch(payload);
    }

    public MapBookHello negotiate(HandshakeSession session, MapBookHello remote) throws ProtocolException {
        Objects.requireNonNull(session, "session").sendHello();
        return session.acceptHello(Objects.requireNonNull(remote, "remote"));
    }
}
