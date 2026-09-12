package com.muwenyan.simplemap.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.Item;

@Mod("simplemap")
public final class NeoForge1211Entrypoint {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("simplemap");
    public static final DeferredItem<Item> EMPTY_MAP_BOOK = ITEMS.register("empty_map_book", () -> new NeoForge1211MapBookItem(new Item.Properties().stacksTo(1), false));
    public static final DeferredItem<Item> MAP_BOOK = ITEMS.register("map_book", () -> new NeoForge1211MapBookItem(new Item.Properties().stacksTo(1), true));

    public NeoForge1211Entrypoint() {
        ITEMS.register(net.neoforged.fml.ModLoadingContext.get().getActiveContainer().getEventBus());
        new MapNeoForge1211Bootstrap().descriptor();
    }
}
