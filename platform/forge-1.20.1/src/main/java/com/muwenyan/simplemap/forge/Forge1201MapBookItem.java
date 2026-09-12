package com.muwenyan.simplemap.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class Forge1201MapBookItem extends Item {
    private final boolean written;
    public Forge1201MapBookItem(Properties properties, boolean written) { super(properties); this.written = written; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (written && !stack.hasTag()) return InteractionResultHolder.fail(stack);
        if (level.isClientSide) Minecraft.getInstance().setScreen(new Forge1201MapScreen(new MapForge1201Bootstrap()));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
