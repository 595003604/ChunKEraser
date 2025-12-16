package com.allinone.chunkerasermod.component;

import com.allinone.chunkerasermod.ChunkEraser;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    // 1. 创建注册器
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ChunkEraser.MODID);

    // 2. 注册一个 Integer 类型的组件，用来存 "storedCount"
    // networkSynchronized 是必须的，否则客户端看不到数据（比如用于Tooltip显示）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_BLOCK_COUNT =
            DATA_COMPONENT_TYPES.register("stored_block_count", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(ExtraCodecs.NON_NEGATIVE_INT) // 存盘用的Codec，这里限制为非负整数
                            .networkSynchronized(ByteBufCodecs.VAR_INT) // 网络同步用的Codec
                            .build()
            );

    // 如果你还需要存那个 "storedItem" 的类型，可以再注册一个，或者直接用原版的 DataComponents.ITEM_NAME 等

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}

