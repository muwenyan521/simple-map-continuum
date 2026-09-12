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

public final class Fabric1201MainEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        Fabric1201Items.register();
        new MapFabric1201Bootstrap().descriptor();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var list = Commands.literal("list").executes(context -> {
            var source = context.getSource();
            return new MapFabric1201Bootstrap().clientController().visibleWaypoints(
                    new DimensionId(source.getLevel().dimension().location().toString())).size();
        });
        var add = Commands.literal("add").then(Commands.argument("name", StringArgumentType.word())
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            var source = context.getSource();
                                            String command = "waypoint add " + StringArgumentType.getString(context, "name") + " "
                                                    + IntegerArgumentType.getInteger(context, "x") + " "
                                                    + IntegerArgumentType.getInteger(context, "y") + " "
                                                    + IntegerArgumentType.getInteger(context, "z");
                                            return new MapFabric1201Bootstrap().clientController().executeWaypointCommand(
                                                    source.getEntityOrException().getUUID(),
                                                    new DimensionId(source.getLevel().dimension().location().toString()), command).size();
                                        })))));
        var waypoint = Commands.literal("waypoint").then(list).then(add)
                .then(Commands.literal("remove").then(Commands.argument("id", UuidArgument.uuid()).executes(context -> new MapFabric1201Bootstrap().clientController().executeWaypointCommand(
                        context.getSource().getEntityOrException().getUUID(), new DimensionId(context.getSource().getLevel().dimension().location().toString()),
                        "waypoint remove " + UuidArgument.getUuid(context, "id")).size())))
                .then(Commands.literal("follow").then(Commands.argument("id", UuidArgument.uuid()).executes(context -> new MapFabric1201Bootstrap().clientController().executeWaypointCommand(
                        context.getSource().getEntityOrException().getUUID(), new DimensionId(context.getSource().getLevel().dimension().location().toString()),
                        "waypoint follow " + UuidArgument.getUuid(context, "id")).size())));
        dispatcher.register(Commands.literal("simplemap").then(waypoint));
    }
}
