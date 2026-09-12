package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.platform.MapProtocolEndpoint;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class Fabric1201Network {
    private static final ResourceLocation CHANNEL = new ResourceLocation("simplemap", "protocol");
    private static final MapProtocolEndpoint ENDPOINT = new MapProtocolEndpoint();
    private Fabric1201Network() { }
    public static MapProtocolEndpoint endpoint() { return ENDPOINT; }
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buffer, responseSender) -> {
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.readBytes(payload);
            server.execute(() -> { try { ENDPOINT.receive(payload); } catch (com.muwenyan.simplemap.core.protocol.ProtocolException ignored) { } });
        });
    }
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buffer, responseSender) -> {
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.readBytes(payload);
            client.execute(() -> { try { ENDPOINT.receive(payload); } catch (com.muwenyan.simplemap.core.protocol.ProtocolException ignored) { } });
        });
    }
}
