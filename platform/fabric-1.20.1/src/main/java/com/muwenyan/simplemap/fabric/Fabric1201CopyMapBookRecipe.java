package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.core.book.MapBookCrafting;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import com.muwenyan.simplemap.core.book.MapBookStatus;
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

public final class Fabric1201CopyMapBookRecipe extends CustomRecipe {
    public Fabric1201CopyMapBookRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }
    @Override public boolean matches(CraftingContainer input, Level level) {
        List<MapBookItemState> states = states(input);
        return states.size() == input.getContainerSize() - emptySlots(input) && MapBookCrafting.matchesCopy(states);
    }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        List<MapBookItemState> states = states(input);
        if (!MapBookCrafting.matchesCopy(states)) return ItemStack.EMPTY;
        MapBookItemState written = states.stream().filter(s -> s.status() == MapBookStatus.WRITTEN).findFirst().orElseThrow();
        ItemStack result = new ItemStack(Fabric1201Items.MAP_BOOK);
        CompoundTag tag = result.getOrCreateTag();
        tag.putString("MapBookID", written.id().orElseThrow().toString());
        tag.putString("MapBookTitle", written.title());
        tag.putString("MapBookPendingAction", "COPY");
        return result;
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() { return Fabric1201Recipes.COPY; }
    private static int emptySlots(CraftingContainer input) { int count = 0; for (int i = 0; i < input.getContainerSize(); i++) if (input.getItem(i).isEmpty()) count++; return count; }
    private static List<MapBookItemState> states(Container input) {
        List<MapBookItemState> states = new ArrayList<>();
        for (int index = 0; index < input.getContainerSize(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) continue;
            if (stack.is(Fabric1201Items.MAP_BOOK)) states.add(Fabric1201MapBookItem.readState(stack));
            else if (stack.is(Fabric1201Items.EMPTY_MAP_BOOK)) states.add(MapBookItemState.empty());
            else return List.of();
        }
        return List.copyOf(states);
    }
}
