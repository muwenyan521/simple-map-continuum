package com.muwenyan.simplemap.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.muwenyan.simplemap.core.model.DimensionId;
import net.minecraft.commands.arguments.UuidArgument;

@Mod("simplemap")
public final class NeoForge1211Entrypoint {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("simplemap");
    public static final DeferredItem<Item> EMPTY_MAP_BOOK = ITEMS.register("empty_map_book", () -> new NeoForge1211MapBookItem(new Item.Properties().stacksTo(1), false));
    public static final DeferredItem<Item> MAP_BOOK = ITEMS.register("map_book", () -> new NeoForge1211MapBookItem(new Item.Properties().stacksTo(1), true));

    public NeoForge1211Entrypoint() {
        ITEMS.register(net.neoforged.fml.ModLoadingContext.get().getActiveContainer().getEventBus());
        net.neoforged.fml.ModLoadingContext.get().getActiveContainer().getEventBus().addListener(NeoForge1211Entrypoint::addCreative);
        NeoForge.EVENT_BUS.addListener(NeoForge1211Entrypoint::registerCommands);
        new MapNeoForge1211Bootstrap().descriptor();
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(EMPTY_MAP_BOOK);
            event.accept(MAP_BOOK);
        }
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        var list = Commands.literal("list").executes(context -> {
            bindServerStorage(context.getSource());
            return new MapNeoForge1211Bootstrap().serverController().visibleWaypoints(new DimensionId(context.getSource().getLevel().dimension().location().toString())).size();
        });
        var z = Commands.argument("z", IntegerArgumentType.integer()).executes(context -> {
            var source = context.getSource();
            bindServerStorage(source);
            String command = "waypoint add " + StringArgumentType.getString(context, "name") + " "
                    + IntegerArgumentType.getInteger(context, "x") + " "
                    + IntegerArgumentType.getInteger(context, "y") + " "
                    + IntegerArgumentType.getInteger(context, "z");
            return new MapNeoForge1211Bootstrap().serverController().executeWaypointCommand(
                    source.getEntityOrException().getUUID(),
                    new DimensionId(source.getLevel().dimension().location().toString()), command).size();
        });
        var y = Commands.argument("y", IntegerArgumentType.integer()).then(z);
        var x = Commands.argument("x", IntegerArgumentType.integer()).then(y);
        var name = Commands.argument("name", StringArgumentType.word()).then(x);
        var waypoint = Commands.literal("waypoint").then(list).then(Commands.literal("add").then(name))
                .then(Commands.literal("remove").then(Commands.argument("id", UuidArgument.uuid()).executes(context -> executeWaypoint(context.getSource(), "waypoint remove " + UuidArgument.getUuid(context, "id")))))
                .then(Commands.literal("follow").then(Commands.argument("id", UuidArgument.uuid()).executes(context -> executeWaypoint(context.getSource(), "waypoint follow " + UuidArgument.getUuid(context, "id")))));
        event.getDispatcher().register(Commands.literal("simplemap").then(waypoint));
    }

    private static void bindServerStorage(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context) {
        bindServerStorage(context.getSource());
    }

    private static void bindServerStorage(net.minecraft.commands.CommandSourceStack source) {
        var root = source.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        new MapNeoForge1211Bootstrap().serverController().bindWaypointStorage(root);
        new MapNeoForge1211Bootstrap().serverController().bindBookStorage(root);
    }

    private static int executeWaypoint(net.minecraft.commands.CommandSourceStack source, String command) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        bindServerStorage(source);
        return new MapNeoForge1211Bootstrap().serverController().executeWaypointCommand(
                source.getEntityOrException().getUUID(),
                new DimensionId(source.getLevel().dimension().location().toString()), command).size();
    }
}
