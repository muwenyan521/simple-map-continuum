package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.platform.MapProtocolEndpoint;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class Fabric1211Network {
    private static final MapProtocolEndpoint ENDPOINT = new MapProtocolEndpoint();
    private Fabric1211Network() { }
    public static MapProtocolEndpoint endpoint() { return ENDPOINT; }
    public static void register() {
        PayloadTypeRegistry.playC2S().register(FramePayload.TYPE, FramePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FramePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ENDPOINT.receiveSafely(payload.data());
            });
        });
    }
    public static void registerClient() {
        PayloadTypeRegistry.playS2C().register(FramePayload.TYPE, FramePayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(FramePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ENDPOINT.receiveSafely(payload.data())));
    }

    public record FramePayload(byte[] data) implements CustomPacketPayload {
        public static final Type<FramePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("simplemap", "protocol"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FramePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.byteArray(4 * 1024 * 1024), FramePayload::data, FramePayload::new);
        public FramePayload { data = data.clone(); }
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
