package com.muwenyan.simplemap.fabric;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import com.muwenyan.simplemap.core.book.MapBookStatus;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import com.muwenyan.simplemap.platform.MapBookRuntime;

public final class Fabric1211MapBookItem extends Item {
    private final boolean written;
    public Fabric1211MapBookItem(Properties properties, boolean written) { super(properties); this.written = written; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!written && readState(stack).status() == MapBookStatus.EMPTY && !level.isClientSide() && level.getServer() != null) {
            try {
                var root = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
                var book = new MapBookRuntime(root).create(player.getUUID());
                writeState(stack, MapBookItemState.written(book, "Map Book of " + player.getName().getString()));
            } catch (java.io.IOException exception) {
                return InteractionResultHolder.fail(stack);
            }
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (written && !hasBookId(data)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static boolean hasBookId(CustomData data) {
        if (data == null) return false;
        try { java.util.UUID.fromString(data.copyTag().getString("MapBookID")); return true; }
        catch (IllegalArgumentException exception) { return false; }
    }

    public static MapBookItemState readState(ItemStack stack) {
        CustomData data = stack == null ? null : stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return MapBookItemState.empty();
        var tag = data.copyTag();
        String id = tag.getString("MapBookID");
        if (id.isEmpty()) return MapBookItemState.empty();
        try { return new MapBookItemState(MapBookStatus.WRITTEN, UUID.fromString(id), tag.contains("MapBookTitle") ? tag.getString("MapBookTitle") : "Map Book"); }
        catch (IllegalArgumentException exception) { return MapBookItemState.empty(); }
    }

    public static void writeState(ItemStack stack, MapBookItemState state) {
        if (stack == null || state == null) throw new IllegalArgumentException("stack/state");
        var tag = new net.minecraft.nbt.CompoundTag();
        if (state.status() != MapBookStatus.EMPTY) {
            tag.putString("MapBookID", state.id().orElseThrow().toString());
            tag.putString("MapBookTitle", state.title());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (!written || level.isClientSide() || level.getServer() == null) return;
        try {
            var runtime = new MapBookRuntime(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            var tag = data == null ? null : data.copyTag();
            if (tag != null && tag.contains("MapBookPendingMerge")) {
                var result = runtime.mergeItems(state(tag, "MapBookMergeLeft"), state(tag, "MapBookMergeRight"),
                        player.getUUID(), "Merged Map Book");
                writeState(stack, result);
            } else if (tag != null && "COPY".equals(tag.getString("MapBookPendingAction"))) {
                var result = runtime.copyItem(readState(stack), MapBookItemState.empty(), player.getUUID(), player.getUUID());
                writeState(stack, result);
            } else if (readState(stack).status() == MapBookStatus.EMPTY) {
                var book = runtime.create(player.getUUID());
                writeState(stack, MapBookItemState.written(book, "Map Book of " + player.getName().getString()));
            } else return;
        } catch (java.io.IOException ignored) { }
    }

    private static MapBookItemState state(net.minecraft.nbt.CompoundTag tag, String key) {
        return new MapBookItemState(MapBookStatus.WRITTEN, UUID.fromString(tag.getString(key)), "Map Book");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        var state = readState(stack);
        tooltip.add(Component.translatable(state.status() == MapBookStatus.EMPTY ? "tooltip.simplemap.empty_book" : "tooltip.simplemap.written_book", state.title()));
        state.id().ifPresent(id -> tooltip.add(Component.translatable("tooltip.simplemap.archive", id)));
    }
}
