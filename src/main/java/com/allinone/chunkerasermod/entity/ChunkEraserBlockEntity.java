package com.allinone.chunkerasermod.entity;

import com.allinone.chunkerasermod.ChunkEraser;
import com.allinone.chunkerasermod.block.ChunkEraserBlock;
import com.allinone.chunkerasermod.screen.ChunkEraserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = ChunkEraser.MODID)
public class ChunkEraserBlockEntity extends BlockEntity implements MenuProvider {
    public int opsPerTick = 30;
    public int range = 4;
    public boolean workDirection = true;
    public boolean canDestroyBedrock = false;
    public boolean isPlacing = false;

    public Item storedItem = Items.AIR;
    public Item placingItem = Items.AIR;
    public int storedCount = 0;

    private BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
    private final ChunkPos chunkPos;
    private final int MachineY;
    private final int chunkX;
    private final int chunkZ;
    private int currentX;
    private int currentY;
    private int currentZ;

    public final ItemStackHandler itemStackHandler = new ItemStackHandler(1) {
        // 【关键修复】这是对外宣告的上限，漏斗和管道会检查这个值
        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }


        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof BlockItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            storedItem = itemStackHandler.getStackInSlot(0).getItem();
            storedCount = itemStackHandler.getStackInSlot(0).getCount();
            setChangedAndSendBlockUpdated();
        }
    };

    public ChunkEraserBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModEntities.CHUNK_ERASER_BE.get(), pos, blockState);
        this.chunkPos = new ChunkPos(pos);
        this.chunkX = chunkPos.getMinBlockX();
        this.chunkZ = chunkPos.getMinBlockZ();
        this.MachineY = pos.getY();
        resetCursor();

        // itemStackHandler.setStackInSlot(0, new ItemStack(Items.STONE, Integer.MAX_VALUE));
    }

    public void cycleActive() {
        if (level != null && !level.isClientSide) {
            if (!isPlacing) {
                level.setBlockAndUpdate(getBlockPos(), this.getBlockState().cycle(ChunkEraserBlock.ACTIVE));
            } else {
                 // 模拟提取只会得到64的stack
                 int blockToPlacing = 256 * (range * 2 + 1) * (range * 2 + 1);
                 if (storedCount < blockToPlacing) {
                     sendMsgToNearestPlayer("方块数量不足以填满设定范围（需 " + blockToPlacing + " 个）");
                     return;
                 }
                 placingItem = storedItem;
                 itemStackHandler.setStackInSlot(0, new ItemStack(storedItem, storedCount - blockToPlacing));
                level.setBlockAndUpdate(getBlockPos(), this.getBlockState().cycle(ChunkEraserBlock.ACTIVE));
            }
        }
    }

    public void changeDirection() {
        if (level != null && !level.isClientSide) {
            this.workDirection = !this.workDirection;
            resetCursor();
            level.setBlockAndUpdate(getBlockPos(), this.getBlockState().setValue(ChunkEraserBlock.ACTIVE, false));
            setChangedAndSendBlockUpdated();
        }
    }

    public void changeRange(int id) {
        if (level != null && !level.isClientSide) {
            if (id == ChunkEraserMenu.BUTTON_RANGE_ADD_ID) range++;
            if (id == ChunkEraserMenu.BUTTON_RANGE_SUB_ID) range--;
            // 简单的边界检查是个好习惯
            if (range < 0) range = 0;

            resetCursor();
            level.setBlockAndUpdate(getBlockPos(), this.getBlockState().setValue(ChunkEraserBlock.ACTIVE, false));
            setChangedAndSendBlockUpdated();
        }
    }

    public void changeOPT(int id) {
        if (level != null && !level.isClientSide) {
            if (id == ChunkEraserMenu.BUTTON_SPEED_ADD_ID) opsPerTick += 5;
            if (id == ChunkEraserMenu.BUTTON_SPEED_SUB_ID) opsPerTick -= 5;
            if (opsPerTick < 1) opsPerTick = 1;
            setChangedAndSendBlockUpdated();
        }
    }

    public void changeCanDestroyBedrock() {
        if (level != null && !level.isClientSide) {
            canDestroyBedrock = !canDestroyBedrock;
            setChangedAndSendBlockUpdated();
        }
    }

    public void changeIsPlacing() {
        if (level != null && !level.isClientSide) {
            isPlacing = !isPlacing;
            resetCursor();
            level.setBlockAndUpdate(getBlockPos(), this.getBlockState().setValue(ChunkEraserBlock.ACTIVE, false));
            setChangedAndSendBlockUpdated();
        }
    }

    public void setChangedAndSendBlockUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void resetCursor() {
        this.currentX = 0;
        this.currentZ = 0;
        this.currentY = workDirection ? MachineY - 1 : MachineY + 1;
    }

    public static void tickServer(Level level, BlockPos blockPos, BlockState blockState, ChunkEraserBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockState.getValue(ChunkEraserBlock.ACTIVE)) {
            if (!blockEntity.isPlacing) {
                blockEntity.processChunkMining();
            } else {
                blockEntity.processChunkPlacing();
            }
        }
    }

    public void processChunkMining() {
        for (int i = 0; i < opsPerTick; i++) {
            if (currentY < -64 || currentY > 319) {
                // sendMsgToNearestPlayer("test");
                level.setBlockAndUpdate(getBlockPos(), this.getBlockState().setValue(ChunkEraserBlock.ACTIVE, false));
                break;
            }

            for (int chunkOffsetX = -range; chunkOffsetX <= range; chunkOffsetX++) {
                for (int chunkOffsetZ = -range; chunkOffsetZ <= range; chunkOffsetZ++) {
                    currentPos.set(currentX + chunkX + chunkOffsetX * 16, currentY, currentZ + chunkZ + chunkOffsetZ * 16);
                    mining(currentPos);
                }
            }

            advanceCursor();
        }
    }

    public void processChunkPlacing() {
        for (int i = 0; i < opsPerTick; i++) {
            if (currentY < MachineY - 1 || currentY > MachineY + 1 ) {
                if (level != null) {
                    // sendMsgToNearestPlayer("test");
                    level.setBlockAndUpdate(getBlockPos(), this.getBlockState().setValue(ChunkEraserBlock.ACTIVE, false));
                }
                break;
            }

            for (int chunkOffsetX = -range; chunkOffsetX <= range; chunkOffsetX++) {
                for (int chunkOffsetZ = -range; chunkOffsetZ <= range; chunkOffsetZ++) {
                    currentPos.set(currentX + chunkX + chunkOffsetX * 16, currentY, currentZ + chunkZ + chunkOffsetZ * 16);
                    if (!currentPos.equals(this.getBlockPos())) {
                        placing(currentPos);
                    }
                }
            }

            advanceCursor();
        }
    }

    private void advanceCursor() {
        currentX++;
        if (workDirection) {
            if (currentX > 15) {
                currentX = 0;
                currentZ++;
                if (currentZ > 15) {
                    currentZ = 0;
                    currentY--;
                    setChanged();
                }
            }
        } else {
            if (currentX > 15) {
                currentX = 0;
                currentZ++;
                if (currentZ > 15) {
                    currentZ = 0;
                    currentY++;
                    setChanged();
                }
            }
        }

    }

    private void mining(BlockPos.MutableBlockPos currentPos) {
        if (level == null || !level.isLoaded(currentPos)) return;
        BlockState blockState = level.getBlockState(currentPos);
        if (!blockState.isAir() && (canDestroyBedrock || blockState.getDestroySpeed(level, currentPos) >= 0)) {
            // getDestroySpeed返回的是硬度，基岩为-1，草为0， 流体为100
            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 18); // 通知客户端不通知邻居
        }
    }

    private void placing(BlockPos.MutableBlockPos currentPos) {
        if (level == null || !level.isLoaded(currentPos)) return;
        if (!(placingItem instanceof BlockItem blockItem)) return;
        level.setBlock(currentPos, blockItem.getBlock().defaultBlockState(), 18);
    }

    private void sendMsgToNearestPlayer(String msg) {
        Player p = null;
        if (level != null) {
            p = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 10, false);
        }
        if (p != null) p.sendSystemMessage(Component.literal(msg));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cY", currentY);
        tag.putInt("cX", currentX);
        tag.putInt("cZ", currentZ);
        tag.putInt("range", range);
        tag.putInt("opsPerTick", opsPerTick);
        tag.putBoolean("workDirection", workDirection);
        tag.putBoolean("canDestroyBedrock", canDestroyBedrock);
        tag.putBoolean("isPlacing", isPlacing);
        if (!itemStackHandler.getStackInSlot(0).isEmpty()) {
            storedItem = itemStackHandler.getStackInSlot(0).getItem();
            storedCount = itemStackHandler.getStackInSlot(0).getCount();
            String itemKey = BuiltInRegistries.ITEM.getKey(this.storedItem).toString();
            tag.putString("StoredItemType", itemKey);
            tag.putInt("StoredItemCount", this.storedCount);
        }
        if (isPlacing && placingItem != Items.AIR) {
            tag.putString("PlacingItemType", BuiltInRegistries.ITEM.getKey(placingItem).toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("cY")) {
            currentY = tag.getInt("cY");
            currentX = tag.getInt("cX");
            currentZ = tag.getInt("cZ");
            range = tag.getInt("range");
            opsPerTick = tag.getInt("opsPerTick");
            workDirection = tag.getBoolean("workDirection");
            canDestroyBedrock = tag.getBoolean("canDestroyBedrock");
            isPlacing = tag.getBoolean("isPlacing");

            if (tag.contains("StoredItemType")) {
                // 1. 读取注册名并转回 Item 对象
                String itemKey = tag.getString("StoredItemType");
                ResourceLocation rl = ResourceLocation.tryParse(itemKey);

                if (rl != null) {
                    this.storedItem = BuiltInRegistries.ITEM.get(rl);
                } else {
                    this.storedItem = Items.AIR; // 防止空指针或错误
                }

                // 2. 读取数量
                this.storedCount = tag.getInt("StoredItemCount");

                itemStackHandler.setStackInSlot(0, new ItemStack(storedItem, storedCount));
            }

            if (tag.contains("PlacingItemType")) {
                ResourceLocation rl = ResourceLocation.tryParse(tag.getString("PlacingItemType"));
                if (rl != null) {
                    this.placingItem = BuiltInRegistries.ITEM.get(rl);
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.displayname.chunkeraser");
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChunkEraserMenu(containerId, playerInventory, this);
    }

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModEntities.CHUNK_ERASER_BE.get(),
                (be, context) -> be.itemStackHandler);
    }
}
