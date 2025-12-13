package com.allinone.chunkerasermod.entity;

import com.allinone.chunkerasermod.ChunkEraser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ChunkEraser.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkEraserBlockEntity>> CHUNK_ERASER_BE =
            BLOCK_ENTITIES.register("chunk_eraser_be",
                    () -> BlockEntityType.Builder.of(
                            ChunkEraserBlockEntity::new, // 这里假设你已经有了这个类的构造函数
                            ChunkEraser.CHUNK_ERASER_BLOCK.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}