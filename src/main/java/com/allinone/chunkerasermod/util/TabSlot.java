package com.allinone.chunkerasermod.util;

import com.allinone.chunkerasermod.screen.ChunkEraserMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class TabSlot extends SlotItemHandler {
    private final ChunkEraserMenu menu;
    private final int targetTab;

    public TabSlot(IItemHandler itemHandler, int index, int x, int y, ChunkEraserMenu menu, int targetTab) {
        super(itemHandler, index, x, y);
        this.menu = menu;
        this.targetTab = targetTab;
    }

    @Override
    public boolean isActive() {
        return this.targetTab == -1 || this.targetTab == this.menu.selectTab;
    }

//    @Override
//    public boolean mayPickup(Player player) {
//        return isActive() && super.mayPickup(player);
//    }
//
//    @Override
//    public boolean mayPlace(ItemStack stack) {
//        return isActive() && super.mayPlace(stack);
//    }
}