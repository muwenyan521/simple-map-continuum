package com.muwenyan.simplemap.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

@Mod("simplemap")
public final class Forge1201Entrypoint {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "simplemap");
    public static final RegistryObject<Item> EMPTY_MAP_BOOK = ITEMS.register("empty_map_book", () -> new Forge1201MapBookItem(new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> MAP_BOOK = ITEMS.register("map_book", () -> new Forge1201MapBookItem(new Item.Properties().stacksTo(1), true));

    public Forge1201Entrypoint() {
        ITEMS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus().addListener(Forge1201Entrypoint::addCreative);
        new MapForge1201Bootstrap().descriptor();
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(EMPTY_MAP_BOOK);
            event.accept(MAP_BOOK);
        }
    }
}
