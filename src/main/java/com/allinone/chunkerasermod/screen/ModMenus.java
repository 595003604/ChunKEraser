package com.allinone.chunkerasermod.screen;

import com.allinone.chunkerasermod.ChunkEraser;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ChunkEraser.MODID);

    public static final Supplier<MenuType<ChunkEraserMenu>> CHUNK_ERASER=
            MENUS.register("chunk_eraser", () -> IMenuTypeExtension.create(ChunkEraserMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
