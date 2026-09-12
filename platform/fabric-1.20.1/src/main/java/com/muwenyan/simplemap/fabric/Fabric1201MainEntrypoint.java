package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.UuidArgument;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class Fabric1201MainEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        Fabric1201Items.register();
        new MapFabric1201Bootstrap().descriptor();
        Fabric1201Network.register();
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                var bootstrap = new MapFabric1201Bootstrap();
                bindServerStorage(player.createCommandSourceStack());
                bootstrap.serverController().addDeathWaypoint(player.getUUID(),
                        new DimensionId(player.level().dimension().location().toString()),
                        new com.muwenyan.simplemap.core.model.BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ()));
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var source = handler.player.createCommandSourceStack();
            bindServerStorage(source);
            var bytes = new MapFabric1201Bootstrap().serverController().encodeWaypointSyncFrame(java.util.UUID.randomUUID(), 0);
            var buffer = PacketByteBufs.create(); buffer.writeBytes(bytes);
            sender.sendPacket(new net.minecraft.resources.ResourceLocation("simplemap", "protocol"), buffer);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var list = Commands.literal("list").executes(context -> {
            var source = context.getSource();
            bindServerStorage(source);
            return new MapFabric1201Bootstrap().serverController().visibleWaypoints(
                    new DimensionId(source.getLevel().dimension().location().toString())).size();
        });
        var add = Commands.literal("add").then(Commands.argument("name", StringArgumentType.word())
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            var source = context.getSource();
                                            bindServerStorage(source);
                                            String command = "waypoint add " + StringArgumentType.getString(context, "name") + " "
                                                    + IntegerArgumentType.getInteger(context, "x") + " "
                                                    + IntegerArgumentType.getInteger(context, "y") + " "
                                                    + IntegerArgumentType.getInteger(context, "z");
                                            return new MapFabric1201Bootstrap().serverController().executeWaypointCommand(
                                                    source.getEntityOrException().getUUID(),
                                                    new DimensionId(source.getLevel().dimension().location().toString()), command).size();
                                        })))));
        var waypoint = Commands.literal("waypoint").then(list).then(add)
                .then(Commands.literal("remove").then(Commands.argument("id", UuidArgument.uuid()).executes(context -> executeWaypoint(context.getSource(), "waypoint remove " + UuidArgument.getUuid(context, "id")))))
                .then(Commands.literal("follow").then(Commands.argument("id", UuidArgument.uuid()).executes(context -> executeWaypoint(context.getSource(), "waypoint follow " + UuidArgument.getUuid(context, "id")))));
        dispatcher.register(Commands.literal("simplemap").then(waypoint));
    }

    private static void bindServerStorage(CommandSourceStack source) {
        var root = source.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        new MapFabric1201Bootstrap().serverController().bindWaypointStorage(root);
        new MapFabric1201Bootstrap().serverController().bindBookStorage(root);
    }

    private static int executeWaypoint(CommandSourceStack source, String command) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        bindServerStorage(source);
        int result = new MapFabric1201Bootstrap().serverController().executeWaypointCommand(
                source.getEntityOrException().getUUID(),
                new DimensionId(source.getLevel().dimension().location().toString()), command).size();
        var packet = PacketByteBufs.create();
        packet.writeBytes(new MapFabric1201Bootstrap().serverController()
                .encodeWaypointSyncFrame(source.getEntityOrException().getUUID(), result));
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(source.getPlayerOrException(),
                new net.minecraft.resources.ResourceLocation("simplemap", "protocol"), packet);
        return result;
    }
}
