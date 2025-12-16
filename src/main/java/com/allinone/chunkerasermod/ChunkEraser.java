package com.allinone.chunkerasermod;

import com.allinone.chunkerasermod.block.ChunkEraserBlock;
import com.allinone.chunkerasermod.component.ModDataComponents;
import com.allinone.chunkerasermod.entity.ModEntities;
import com.allinone.chunkerasermod.screen.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(ChunkEraser.MODID)
public class ChunkEraser {
    public static final String MODID = "chunkeraser";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> CHUNK_ERASER_BLOCK =
            BLOCKS.register("chunk_eraser", () -> new ChunkEraserBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(2.0f, 6.0f)
                    .lightLevel(blockState -> blockState.getValue(ChunkEraserBlock.ACTIVE) ? 15 : 0)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> CHUNK_ERASER_ITEM =
            ITEMS.register("chunk_eraser", () -> new BlockItem(CHUNK_ERASER_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("chunk_eraser_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.chunkeraser"))
            .icon(() -> new ItemStack(CHUNK_ERASER_BLOCK.get()))
            .displayItems((parameters, output) -> {
                output.accept(CHUNK_ERASER_BLOCK.get());
            }).build());

    public ChunkEraser(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        ModEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModDataComponents.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 这里的 Log 逻辑没问题，但确保 Config 类已经定义了这些字段
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Chunk Eraser mod loaded successfully!");
    }
}