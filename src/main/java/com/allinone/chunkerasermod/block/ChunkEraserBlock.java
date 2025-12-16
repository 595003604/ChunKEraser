package com.allinone.chunkerasermod.block;

import com.allinone.chunkerasermod.entity.ChunkEraserBlockEntity;
import com.allinone.chunkerasermod.entity.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // 1. 获取即将被破坏的 BlockEntity
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof ChunkEraserBlockEntity eraser) {
            // 2. 创建掉落物 ItemStack
            ItemStack stack = new ItemStack(this);

            // 3. 将 BlockEntity 的数据保存到 Tag 中
            // 使用 saveWithoutMetadata 可以包含 id，或者只用 saveAdditional
            // 这里我们手动创建一个 Tag 并调用 saveAdditional，只保存我们需要的数据
            CompoundTag tag = new CompoundTag();
            eraser.saveAdditional(tag, builder.getLevel().registryAccess());

            // 4. 将 Tag 包装为 CustomData 并存入 ItemStack 的 BLOCK_ENTITY_DATA 组件
            // 只有当 Tag 不为空时才写入，避免产生空的组件
            if (!tag.isEmpty()) {
                stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
            }

            // 5. 返回包含这个 ItemStack 的列表
            return List.of(stack);
        }

        // 如果没有 BE（理论上不应该发生），走默认掉落
        return super.getDrops(state, builder);
    }

    // 创造模式中键（Pick Block）也复制数据
//     @Override
//    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, BlockState state) {
//        ItemStack stack = super.getCloneItemStack(level, pos, state);
//        BlockEntity be = level.getBlockEntity(pos);
//        if (be instanceof ChunkEraserBlockEntity eraser) {
//            CompoundTag tag = new CompoundTag();
//            eraser.saveAdditional(tag, level.registryAccess());
//            if (!tag.isEmpty()) {
//                stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
//            }
//        }
//        return stack;
//    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // 获取数据组件
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            // 读取 NBT
            CompoundTag tag = data.copyTag();
            if (tag.contains("StoredItemType") && tag.contains("StoredItemCount")) {
                String itemKey = tag.getString("StoredItemType");
                int count = tag.getInt("StoredItemCount");

                // 获取物品名称
                ResourceLocation rl = ResourceLocation.tryParse(itemKey);
                if (rl != null) {
                    Item item = BuiltInRegistries.ITEM.get(rl);
                    // 显示: "内含: 石头 x 12345"
                    tooltipComponents.add(Component.literal("§7内含: ")
                            .append(item.getName(new ItemStack(item)))
                            .append(" x " + count));
                }
            }
        }
    }
}
