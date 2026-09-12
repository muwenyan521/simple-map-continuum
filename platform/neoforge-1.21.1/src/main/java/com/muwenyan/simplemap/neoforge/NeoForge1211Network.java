package com.muwenyan.simplemap.neoforge;

import com.muwenyan.simplemap.platform.MapProtocolEndpoint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NeoForge1211Network {
    private static final MapProtocolEndpoint ENDPOINT = new MapProtocolEndpoint();
    private NeoForge1211Network() { }
    public static MapProtocolEndpoint endpoint() { return ENDPOINT; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(FramePayload.TYPE, FramePayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ENDPOINT.receiveSafely(payload.data());
                }));
        event.registrar("1").playToClient(FramePayload.TYPE, FramePayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ENDPOINT.receiveSafely(payload.data());
                }));
    }

    public record FramePayload(byte[] data) implements CustomPacketPayload {
        public static final Type<FramePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("simplemap", "protocol"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FramePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.byteArray(4 * 1024 * 1024), FramePayload::data, FramePayload::new);
        public FramePayload { data = data.clone(); }
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
