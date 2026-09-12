package com.muwenyan.simplemap.fabric;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
}
