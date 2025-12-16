package com.allinone.chunkerasermod.screen;

import com.allinone.chunkerasermod.entity.ChunkEraserBlockEntity;
import com.allinone.chunkerasermod.util.TabSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ChunkEraserMenu extends AbstractContainerMenu {
    public static final int BUTTON_ACTIVE_ID = 0;
    public static final int BUTTON_DIRECTION_ID = 1;
    public static final int BUTTON_RANGE_ADD_ID = 2;
    public static final int BUTTON_RANGE_SUB_ID = 3;
    public static final int BUTTON_SPEED_ADD_ID = 4;
    public static final int BUTTON_SPEED_SUB_ID = 5;
    public static final int BUTTON_BEDROCK_ID = 6;
    public static final int BUTTON_IS_PLACING_ID = 7;

    public final Level level;
    public final ChunkEraserBlockEntity blockEntity;

    public int selectTab = 0;

    // 服务端入口 由 BlockEntity.createMenu 调用
    public ChunkEraserMenu(int containerId, Inventory playerInventory, ChunkEraserBlockEntity blockEntity) {
        super(ModMenus.CHUNK_ERASER.get(), containerId);
        this.level = playerInventory.player.level();
        this.blockEntity = blockEntity;

        this.addSlot(new TabSlot(blockEntity.itemStackHandler, 0, 80, 35, this, 0) {
            @Override
            public int getMaxStackSize(ItemStack stack) {
                return Integer.MAX_VALUE;
            }
            @Override
            public int getMaxStackSize() {
                return Integer.MAX_VALUE;
            }
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BlockItem;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 45 + col * 18, 170 + row * 18));
            }
        }

        // 3. 添加玩家快捷栏槽位（标准位置：x=8, y=142；共9个）
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 45 + col * 18, 228));
        }
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
        if (id == BUTTON_BEDROCK_ID) {
            blockEntity.changeCanDestroyBedrock();
            return true;
        }
        if (id == BUTTON_IS_PLACING_ID) {
            blockEntity.changeIsPlacing();
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
