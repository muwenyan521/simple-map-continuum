package com.muwenyan.simplemap.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.world.Container;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.muwenyan.simplemap.core.model.DimensionId;
import net.minecraft.commands.arguments.UuidArgument;

@Mod("simplemap")
public final class Forge1201Entrypoint {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "simplemap");
    public static final RegistryObject<Item> EMPTY_MAP_BOOK = ITEMS.register("empty_map_book", () -> new Forge1201MapBookItem(new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> MAP_BOOK = ITEMS.register("map_book", () -> new Forge1201MapBookItem(new Item.Properties().stacksTo(1), true));

    public Forge1201Entrypoint() {
        ITEMS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus().addListener(Forge1201Entrypoint::addCreative);
        MinecraftForge.EVENT_BUS.addListener(Forge1201Entrypoint::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(Forge1201Entrypoint::onCrafted);
        MinecraftForge.EVENT_BUS.addListener(Forge1201Entrypoint::onLogin);
        new MapForge1201Bootstrap().descriptor();
        Forge1201Network.register();
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
            return new MapForge1201Bootstrap().serverController().visibleWaypoints(new DimensionId(context.getSource().getLevel().dimension().location().toString())).size();
        });
        var z = Commands.argument("z", IntegerArgumentType.integer()).executes(context -> {
            var source = context.getSource();
            bindServerStorage(source);
            String command = "waypoint add " + StringArgumentType.getString(context, "name") + " "
                    + IntegerArgumentType.getInteger(context, "x") + " "
                    + IntegerArgumentType.getInteger(context, "y") + " "
                    + IntegerArgumentType.getInteger(context, "z");
            return new MapForge1201Bootstrap().serverController().executeWaypointCommand(
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

    private static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        Item crafted = event.getCrafting().getItem();
        if (crafted != MAP_BOOK.get()) return;
        Container inventory = event.getInventory();
        java.util.List<com.muwenyan.simplemap.core.book.MapBookItemState> states = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (stack.is(MAP_BOOK.get())) states.add(Forge1201MapBookItem.readState(stack));
            else if (stack.is(EMPTY_MAP_BOOK.get())) states.add(com.muwenyan.simplemap.core.book.MapBookItemState.empty());
        }
        try {
            var root = player.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            var runtime = new com.muwenyan.simplemap.platform.MapBookRuntime(root);
            var result = states.size() == 2 && states.stream().anyMatch(s -> s.status() == com.muwenyan.simplemap.core.book.MapBookStatus.EMPTY)
                    ? runtime.copyItem(states.get(0).status() == com.muwenyan.simplemap.core.book.MapBookStatus.WRITTEN ? states.get(0) : states.get(1),
                    com.muwenyan.simplemap.core.book.MapBookItemState.empty(), player.getUUID(), player.getUUID())
                    : runtime.mergeItems(states.get(0), states.get(1), player.getUUID(), "Merged Map Book");
            Forge1201MapBookItem.writeState(event.getCrafting(), result);
        } catch (java.io.IOException | RuntimeException ignored) { }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        bindServerStorage(player.createCommandSourceStack());
        Forge1201Network.sendToPlayer(player,
                new MapForge1201Bootstrap().serverController().encodeWaypointSyncFrame(java.util.UUID.randomUUID(), 0));
    }

    private static void bindServerStorage(CommandSourceStack source) {
        var root = source.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        new MapForge1201Bootstrap().serverController().bindWaypointStorage(root);
        new MapForge1201Bootstrap().serverController().bindBookStorage(root);
    }

    private static int executeWaypoint(CommandSourceStack source, String command) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        bindServerStorage(source);
        int result = new MapForge1201Bootstrap().serverController().executeWaypointCommand(
                source.getEntityOrException().getUUID(),
                new DimensionId(source.getLevel().dimension().location().toString()), command).size();
        if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            Forge1201Network.sendToPlayer(player, new MapForge1201Bootstrap().serverController()
                    .encodeWaypointSyncFrame(player.getUUID(), result));
        }
        return result;
    }
}
