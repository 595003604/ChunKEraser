package com.allinone.chunkerasermod;

import com.allinone.chunkerasermod.screen.ChunkEraserScreen;
import com.allinone.chunkerasermod.screen.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ChunkEraser.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ChunkEraser.MODID, value = Dist.CLIENT)
public class ChunkEraserClient {
    public ChunkEraserClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ChunkEraser.LOGGER.info("HELLO FROM CLIENT SETUP");
        ChunkEraser.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        // 绑定 MenuType 到对应的 Screen
        event.register(ModMenus.CHUNK_ERASER.get(), ChunkEraserScreen::new);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == ChunkEraser.CHUNK_ERASER_ITEM.get()) {
            event.getToolTip().add(Component.literal("此机器用于区块清除与地形平整！").withStyle(net.minecraft.ChatFormatting.GOLD));
        }
    }
}
