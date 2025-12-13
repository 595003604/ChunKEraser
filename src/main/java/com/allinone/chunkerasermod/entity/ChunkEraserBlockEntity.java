package com.allinone.chunkerasermod.entity;

import com.allinone.chunkerasermod.block.ChunkEraserBlock;
import com.allinone.chunkerasermod.screen.ChunkEraserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ChunkEraserBlockEntity extends BlockEntity implements MenuProvider {
    public int opsPerTick = 30;
    public int range = 4;
    public boolean workDirection = true;

    private BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
    private final ChunkPos chunkPos;
    private final int MachineY;
    private final int chunkX;
    private final int chunkZ;
    private int currentX;
    private int currentY;
    private int currentZ;

    public ChunkEraserBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModEntities.CHUNK_ERASER_BE.get(), pos, blockState);
        this.chunkPos = new ChunkPos(pos);
        this.chunkX = chunkPos.getMinBlockX();
        this.chunkZ = chunkPos.getMinBlockZ();
        this.MachineY = pos.getY();
        resetCursor();
    }

    public void cycleActive() {
        if (level != null && !level.isClientSide) {
            level.setBlockAndUpdate(getBlockPos(), this.getBlockState().cycle(ChunkEraserBlock.ACTIVE));
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
            if (id == ChunkEraserMenu.BUTTON_RANGE_ADD_ID) {
                range++;
                resetCursor();
                setChangedAndSendBlockUpdated();
            }
            if (id == ChunkEraserMenu.BUTTON_RANGE_SUB_ID) {
                range--;
                resetCursor();
                setChangedAndSendBlockUpdated();
            }
        }
    }

    public void changeOPT(int id) {
        if (level != null && !level.isClientSide) {
            if (id == ChunkEraserMenu.BUTTON_SPEED_ADD_ID) {
                opsPerTick += 5;
                //resetCursor();
                setChangedAndSendBlockUpdated();
            }
            if (id == ChunkEraserMenu.BUTTON_SPEED_SUB_ID) {
                opsPerTick -= 5;
                //resetCursor();
                setChangedAndSendBlockUpdated();
            }
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
        if (level.isClientSide || !blockState.getValue(ChunkEraserBlock.ACTIVE)) return;
        blockEntity.processChunkMining();
    }

    public void processChunkMining() {
        for (int i = 0; i < opsPerTick; i++) {
            if (currentY < -64 || currentY > 319) {
                cycleActive();
                return;
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
        if (level != null && !level.isLoaded(currentPos)) return;
        BlockState blockState = level.getBlockState(currentPos);
        if (!blockState.isAir() && blockState.getDestroySpeed(level, currentPos) >= 0) {
            // getDestroySpeed返回的是硬度，基岩为-1，草为0， 流体为100
            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 18); // 通知客户端不通知邻居
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cY", currentY);
        tag.putInt("cX", currentX);
        tag.putInt("cZ", currentZ);
        tag.putInt("range", range);
        tag.putInt("opsPerTick", opsPerTick);
        tag.putBoolean("workDirection", workDirection);
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
}
