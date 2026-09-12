package com.muwenyan.simplemap.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class Fabric1211Items {
    public static final Item EMPTY_MAP_BOOK = register("empty_map_book", false);
    public static final Item MAP_BOOK = register("map_book", true);

    private Fabric1211Items() { }

    public static void register() { }

    private static Item register(String id, boolean written) {
        return Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath("simplemap", id), new Fabric1211MapBookItem(new Item.Properties().stacksTo(1), written));
    }
}
