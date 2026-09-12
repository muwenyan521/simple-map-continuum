package com.muwenyan.simplemap.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;

@Mod("simplemap")
public final class Forge1201Entrypoint {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "simplemap");
    public static final RegistryObject<Item> EMPTY_MAP_BOOK = ITEMS.register("empty_map_book", () -> new Forge1201MapBookItem(new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> MAP_BOOK = ITEMS.register("map_book", () -> new Forge1201MapBookItem(new Item.Properties().stacksTo(1), true));

    public Forge1201Entrypoint() {
        ITEMS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        new MapForge1201Bootstrap().descriptor();
    }
}
