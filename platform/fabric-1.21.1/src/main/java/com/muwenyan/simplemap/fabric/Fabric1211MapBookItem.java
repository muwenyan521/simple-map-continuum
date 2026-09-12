package com.muwenyan.simplemap.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class Fabric1211MapBookItem extends Item {
    private final boolean written;
    public Fabric1211MapBookItem(Properties properties, boolean written) { super(properties); this.written = written; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) Minecraft.getInstance().setScreen(new Fabric1211MapScreen(new MapFabric1211Bootstrap()));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
