package com.muwenyan.simplemap.forge;

import com.muwenyan.simplemap.platform.MapProtocolEndpoint;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

public final class Forge1201Network {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("simplemap", "protocol"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static final MapProtocolEndpoint ENDPOINT = new MapProtocolEndpoint();
    private Forge1201Network() { }
    public static MapProtocolEndpoint endpoint() { return ENDPOINT; }
    public static void observe(com.muwenyan.simplemap.platform.MapClientController controller) {
        ENDPOINT.setObserver(frame -> { if (frame.type() == MapBookMessageType.WAYPOINT_SYNC) controller.applyWaypointSync(ENDPOINT.lastWaypointSync().orElseThrow()); });
    }

    public static void register() {
        CHANNEL.registerMessage(0, FrameMessage.class, Forge1201Network::encode, Forge1201Network::decode,
                (message, context) -> { context.get().enqueueWork(() -> ENDPOINT.receiveSafely(message.payload)); context.get().setPacketHandled(true); });
    }

    public static void sendToServer(byte[] payload) {
        CHANNEL.sendToServer(new FrameMessage(payload));
    }
    public static void sendToPlayer(ServerPlayer player, byte[] payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new FrameMessage(payload));
    }

    private static void encode(FrameMessage message, FriendlyByteBuf buffer) { buffer.writeByteArray(message.payload); }
    private static FrameMessage decode(FriendlyByteBuf buffer) { return new FrameMessage(buffer.readByteArray(4 * 1024 * 1024)); }
    public record FrameMessage(byte[] payload) { }
}
