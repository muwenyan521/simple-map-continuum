package com.muwenyan.simplemap.fabric;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class Fabric1201Items {
    public static final Item EMPTY_MAP_BOOK = register("empty_map_book");
    public static final Item MAP_BOOK = register("map_book");

    private Fabric1201Items() { }

    public static void register() { }

    private static Item register(String id) {
        return Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation("simplemap", id), new Item(new FabricItemSettings().stacksTo(1)));
    }
}
