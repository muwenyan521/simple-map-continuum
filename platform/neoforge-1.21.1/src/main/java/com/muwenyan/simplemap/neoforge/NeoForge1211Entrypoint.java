package com.muwenyan.simplemap.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.Item;

@Mod("simplemap")
public final class NeoForge1211Entrypoint {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("simplemap");
    public static final DeferredItem<Item> EMPTY_MAP_BOOK = ITEMS.registerSimpleItem("empty_map_book", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MAP_BOOK = ITEMS.registerSimpleItem("map_book", new Item.Properties().stacksTo(1));

    public NeoForge1211Entrypoint() {
        ITEMS.register(net.neoforged.fml.ModLoadingContext.get().getActiveContainer().getEventBus());
        new MapNeoForge1211Bootstrap().descriptor();
    }
}
