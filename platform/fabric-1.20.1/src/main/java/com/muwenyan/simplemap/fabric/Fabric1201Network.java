package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.platform.MapProtocolEndpoint;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class Fabric1201Network {
    private static final ResourceLocation CHANNEL = new ResourceLocation("simplemap", "protocol");
    private static final MapProtocolEndpoint ENDPOINT = new MapProtocolEndpoint();
    private Fabric1201Network() { }
    public static MapProtocolEndpoint endpoint() { return ENDPOINT; }
    public static void observe(com.muwenyan.simplemap.platform.MapClientController controller) {
        ENDPOINT.setObserver(frame -> { if (frame.type() == MapBookMessageType.WAYPOINT_SYNC) controller.applyWaypointSync(ENDPOINT.lastWaypointSync().orElseThrow()); });
    }
    public static void sendToServer(byte[] payload) { ClientPlayNetworking.send(CHANNEL, new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload))); }
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buffer, responseSender) -> {
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.readBytes(payload);
            server.execute(() -> ENDPOINT.receiveSafely(payload));
        });
    }
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buffer, responseSender) -> {
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.readBytes(payload);
            client.execute(() -> ENDPOINT.receiveSafely(payload));
        });
    }
}
