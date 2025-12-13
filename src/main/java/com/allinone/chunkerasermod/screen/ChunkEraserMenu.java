package com.allinone.chunkerasermod.screen;

import com.allinone.chunkerasermod.entity.ChunkEraserBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ChunkEraserMenu extends AbstractContainerMenu {
    public static final int BUTTON_ACTIVE_ID = 0;
    public static final int BUTTON_DIRECTION_ID = 1;
    public static final int BUTTON_RANGE_ADD_ID = 2;
    public static final int BUTTON_RANGE_SUB_ID = 3;
    public static final int BUTTON_SPEED_ADD_ID = 4;
    public static final int BUTTON_SPEED_SUB_ID = 5;

    public final Level level;
    public final ChunkEraserBlockEntity blockEntity;

    // 服务端入口 由 BlockEntity.createMenu 调用
    public ChunkEraserMenu(int containerId, Inventory playerInventory, ChunkEraserBlockEntity blockEntity) {
        super(ModMenus.CHUNK_ERASER.get(), containerId);
        this.level = playerInventory.player.level();
        this.blockEntity = blockEntity;
    }

    // 客户端入口 (网络包调用) 由 MenuScreens 注册时调用
    public ChunkEraserMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                (ChunkEraserBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_ACTIVE_ID) {
            blockEntity.cycleActive();
            return true;
        }
        if (id == BUTTON_DIRECTION_ID) {
            blockEntity.changeDirection();
            return true;
        }
        if (id == BUTTON_RANGE_ADD_ID || id == BUTTON_RANGE_SUB_ID) {
            blockEntity.changeRange(id);
            return true;
        }
        if (id == BUTTON_SPEED_ADD_ID || id == BUTTON_SPEED_SUB_ID) {
            blockEntity.changeOPT(id);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }
}
