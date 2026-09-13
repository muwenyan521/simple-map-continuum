package com.muwenyan.simplemap.fabric;

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

public final class Fabric1201MergeMapBooksRecipe extends CustomRecipe {
    public Fabric1201MergeMapBooksRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }
    @Override public boolean matches(CraftingContainer input, Level level) { return MapBookCrafting.matchesMerge(states(input)); }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        List<MapBookItemState> states = states(input);
        if (!MapBookCrafting.matchesMerge(states)) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(Fabric1201Items.MAP_BOOK);
        CompoundTag tag = result.getOrCreateTag();
        tag.putBoolean("MapBookPendingMerge", true);
        tag.putString("MapBookMergeLeft", states.get(0).id().orElseThrow().toString());
        tag.putString("MapBookMergeRight", states.get(1).id().orElseThrow().toString());
        return result;
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() { return Fabric1201Recipes.MERGE; }
    private static List<MapBookItemState> states(Container input) {
        List<MapBookItemState> states = new ArrayList<>();
        for (int index = 0; index < input.getContainerSize(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) continue;
            if (stack.is(Fabric1201Items.MAP_BOOK)) states.add(Fabric1201MapBookItem.readState(stack));
            else return List.of();
        }
        return List.copyOf(states);
    }
}
