package com.muwenyan.simplemap.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class Fabric1211Items {
    public static final Item EMPTY_MAP_BOOK = register("empty_map_book");
    public static final Item MAP_BOOK = register("map_book");

    private Fabric1211Items() { }

    public static void register() { }

    private static Item register(String id) {
        return Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath("simplemap", id), new Item(new Item.Properties().stacksTo(1)));
    }
}
