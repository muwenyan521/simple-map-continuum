package com.muwenyan.simplemap.forge;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import com.muwenyan.simplemap.core.book.MapBookStatus;
import java.util.UUID;
import com.muwenyan.simplemap.platform.MapBookRuntime;

public final class Forge1201MapBookItem extends Item {
    private final boolean written;
    public Forge1201MapBookItem(Properties properties, boolean written) { super(properties); this.written = written; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!written && !hasBookId(stack) && !level.isClientSide() && level.getServer() != null) {
            try {
                var root = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
                var book = new MapBookRuntime(root).create(player.getUUID());
                writeState(stack, MapBookItemState.written(book, "Map Book of " + player.getName().getString()));
            } catch (java.io.IOException exception) {
                return InteractionResultHolder.fail(stack);
            }
        }
        if (written && !hasBookId(stack)) return InteractionResultHolder.fail(stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (!written || level.isClientSide() || level.getServer() == null || hasBookId(stack)) return;
        try {
            var book = new MapBookRuntime(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)).create(player.getUUID());
            writeState(stack, MapBookItemState.written(book, "Map Book of " + player.getName().getString()));
        } catch (java.io.IOException ignored) { }
    }

    private static boolean hasBookId(ItemStack stack) {
        if (!stack.hasTag()) return false;
        try { java.util.UUID.fromString(stack.getTag().getString("MapBookID")); return true; }
        catch (IllegalArgumentException exception) { return false; }
    }

    public static MapBookItemState readState(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return MapBookItemState.empty();
        var tag = stack.getTag();
        String id = tag.getString("MapBookID");
        if (id.isEmpty()) return MapBookItemState.empty();
        try { return new MapBookItemState(MapBookStatus.WRITTEN, UUID.fromString(id), tag.contains("MapBookTitle") ? tag.getString("MapBookTitle") : "Map Book"); }
        catch (IllegalArgumentException exception) { return MapBookItemState.empty(); }
    }

    public static void writeState(ItemStack stack, MapBookItemState state) {
        if (stack == null || state == null) throw new IllegalArgumentException("stack/state");
        if (state.status() == MapBookStatus.EMPTY) { stack.removeTagKey("MapBookID"); stack.removeTagKey("MapBookTitle"); return; }
        var tag = stack.getOrCreateTag();
        tag.putString("MapBookID", state.id().orElseThrow().toString());
        tag.putString("MapBookTitle", state.title());
    }
}
