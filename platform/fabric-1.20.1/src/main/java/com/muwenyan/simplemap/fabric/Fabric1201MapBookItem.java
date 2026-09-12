package com.muwenyan.simplemap.fabric;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import com.muwenyan.simplemap.core.book.MapBookStatus;
import java.util.UUID;

public final class Fabric1201MapBookItem extends Item {
    private final boolean written;
    public Fabric1201MapBookItem(Properties properties, boolean written) { super(properties); this.written = written; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (written && !hasBookId(stack)) return InteractionResultHolder.fail(stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
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
        try {
            return new MapBookItemState(MapBookStatus.WRITTEN, UUID.fromString(id),
                    tag.contains("MapBookTitle") ? tag.getString("MapBookTitle") : "Map Book");
        } catch (IllegalArgumentException exception) {
            return MapBookItemState.empty();
        }
    }

    public static void writeState(ItemStack stack, MapBookItemState state) {
        if (stack == null || state == null) throw new IllegalArgumentException("stack/state");
        if (state.status() == MapBookStatus.EMPTY) {
            stack.removeTagKey("MapBookID");
            stack.removeTagKey("MapBookTitle");
            return;
        }
        var tag = stack.getOrCreateTag();
        tag.putString("MapBookID", state.id().orElseThrow().toString());
        tag.putString("MapBookTitle", state.title());
    }
}
