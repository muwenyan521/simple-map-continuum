package com.muwenyan.simplemap.forge;

import com.muwenyan.simplemap.core.book.MapBookCrafting;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

public final class Forge1201MergeMapBooksRecipe extends CustomRecipe {
    public Forge1201MergeMapBooksRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }
    @Override public boolean matches(CraftingContainer input, Level level) { return MapBookCrafting.matchesMerge(states(input)); }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        List<MapBookItemState> states = states(input);
        if (!MapBookCrafting.matchesMerge(states)) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(Forge1201Entrypoint.MAP_BOOK.get());
        CompoundTag tag = result.getOrCreateTag();
        tag.putBoolean("MapBookPendingMerge", true);
        tag.putString("MapBookMergeLeft", states.get(0).id().orElseThrow().toString());
        tag.putString("MapBookMergeRight", states.get(1).id().orElseThrow().toString());
        return result;
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() { return Forge1201Recipes.MERGE.get(); }
    private static List<MapBookItemState> states(Container input) {
        List<MapBookItemState> states = new ArrayList<>();
        for (int index = 0; index < input.getContainerSize(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) continue;
            if (stack.is(Forge1201Entrypoint.MAP_BOOK.get())) states.add(Forge1201MapBookItem.readState(stack));
            else return List.of();
        }
        return List.copyOf(states);
    }
}
