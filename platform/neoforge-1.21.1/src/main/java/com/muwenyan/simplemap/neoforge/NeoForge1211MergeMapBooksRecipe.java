package com.muwenyan.simplemap.neoforge;

import com.muwenyan.simplemap.core.book.MapBookCrafting;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

public final class NeoForge1211MergeMapBooksRecipe extends CustomRecipe {
    public NeoForge1211MergeMapBooksRecipe(CraftingBookCategory category) { super(category); }
    @Override public boolean matches(CraftingInput input, Level level) {
        List<MapBookItemState> states = states(input);
        return states.size() == input.ingredientCount() && MapBookCrafting.matchesMerge(states);
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<MapBookItemState> states = states(input);
        if (!MapBookCrafting.matchesMerge(states)) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(NeoForge1211Entrypoint.MAP_BOOK.get());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("MapBookPendingMerge", true);
        tag.putString("MapBookMergeLeft", states.get(0).id().orElseThrow().toString());
        tag.putString("MapBookMergeRight", states.get(1).id().orElseThrow().toString());
        result.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return result;
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() { return NeoForge1211Recipes.MERGE.get(); }
    private static List<MapBookItemState> states(CraftingInput input) {
        List<MapBookItemState> states = new ArrayList<>();
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) continue;
            if (stack.is(NeoForge1211Entrypoint.MAP_BOOK.get())) states.add(NeoForge1211MapBookItem.readState(stack));
            else return List.of();
        }
        return List.copyOf(states);
    }
}
