package com.allinone.chunkerasermod.block;

import com.allinone.chunkerasermod.entity.ChunkEraserBlockEntity;
import com.allinone.chunkerasermod.entity.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ChunkEraserBlock extends BaseEntityBlock {
    public static final MapCodec<ChunkEraserBlock> CODEC = simpleCodec(ChunkEraserBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active"); //不允许大写

    public ChunkEraserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ChunkEraserBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(blockEntity, Component.translatable("menu.displayname.chunkeraser")), pos);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            super.onBlockStateChange(level, pos, oldState, state);
            return;
        }

        boolean oldVal = oldState.getValue(ACTIVE);
        boolean newVal = state.getValue(ACTIVE);

        if (oldVal != newVal) {
            float pitch = newVal ? 0.6f : 0.5f;
            level.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0f, pitch);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level instanceof ServerLevel serverLevel) {
            return createTickerHelper(blockEntityType, ModEntities.CHUNK_ERASER_BE.get(), ChunkEraserBlockEntity::tickServer);
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChunkEraserBlockEntity(blockPos, blockState);
    }
}
