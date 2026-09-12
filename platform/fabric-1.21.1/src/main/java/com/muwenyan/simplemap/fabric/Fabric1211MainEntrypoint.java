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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public final class Fabric1211MainEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        Fabric1211Items.register();
        new MapFabric1211Bootstrap().descriptor();
        Fabric1211Network.register();
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                var bootstrap = new MapFabric1211Bootstrap();
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
            sender.sendPacket(new Fabric1211Network.FramePayload(
                    new MapFabric1211Bootstrap().serverController().encodeWaypointSyncFrame(java.util.UUID.randomUUID(), 0)));
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var list = Commands.literal("list").executes(context -> {
            var source = context.getSource();
            bindServerStorage(source);
            return new MapFabric1211Bootstrap().serverController().visibleWaypoints(
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
                                            return new MapFabric1211Bootstrap().serverController().executeWaypointCommand(
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
        new MapFabric1211Bootstrap().serverController().bindWaypointStorage(root);
        new MapFabric1211Bootstrap().serverController().bindBookStorage(root);
    }

    private static int executeWaypoint(CommandSourceStack source, String command) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        bindServerStorage(source);
        int result = new MapFabric1211Bootstrap().serverController().executeWaypointCommand(
                source.getEntityOrException().getUUID(),
                new DimensionId(source.getLevel().dimension().location().toString()), command).size();
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(source.getPlayerOrException(),
                new Fabric1211Network.FramePayload(new MapFabric1211Bootstrap().serverController()
                        .encodeWaypointSyncFrame(source.getEntityOrException().getUUID(), result)));
        return result;
    }
}
