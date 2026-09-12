package com.muwenyan.simplemap.neoforge;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public final class NeoForge1211MapBookItem extends Item {
    private final boolean written;
    public NeoForge1211MapBookItem(Properties properties, boolean written) { super(properties); this.written = written; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
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
}
