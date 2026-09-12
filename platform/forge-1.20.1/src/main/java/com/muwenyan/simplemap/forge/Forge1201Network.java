package com.muwenyan.simplemap.forge;

import com.muwenyan.simplemap.platform.MapProtocolEndpoint;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Forge1201Network {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("simplemap", "protocol"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static final MapProtocolEndpoint ENDPOINT = new MapProtocolEndpoint();
    private Forge1201Network() { }

    public static void register() {
        CHANNEL.registerMessage(0, FrameMessage.class, Forge1201Network::encode, Forge1201Network::decode,
                (message, context) -> { context.get().enqueueWork(() -> { try { ENDPOINT.receive(message.payload); } catch (com.muwenyan.simplemap.core.protocol.ProtocolException ignored) { } }); context.get().setPacketHandled(true); });
    }

    private static void encode(FrameMessage message, FriendlyByteBuf buffer) { buffer.writeByteArray(message.payload); }
    private static FrameMessage decode(FriendlyByteBuf buffer) { return new FrameMessage(buffer.readByteArray(4 * 1024 * 1024)); }
    public record FrameMessage(byte[] payload) { }
}
