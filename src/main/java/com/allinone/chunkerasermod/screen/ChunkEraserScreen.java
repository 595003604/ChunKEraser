package com.allinone.chunkerasermod.screen;

import com.allinone.chunkerasermod.ChunkEraser;
import com.allinone.chunkerasermod.block.ChunkEraserBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class ChunkEraserScreen extends AbstractContainerScreen<ChunkEraserMenu>{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ChunkEraser.MODID, "textures/gui/chunk_eraser_gui.png");

    private Button button_active;
    private Button button_direction;
    private Button button_range_add;
    private Button button_range_sub;
    private Button button_speed_add;
    private Button button_speed_sub;
    private Button button_bedrock;
    private Button button_is_placing;
    private Button button_placing_block;

    public ChunkEraserScreen(ChunkEraserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 223;
        this.imageHeight = 256;
    }

    @Override
    protected void init() {
        //客户端执行
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        button_active = this.addRenderableWidget(Button.builder(Component.literal("启动开关"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_ACTIVE_ID);
                    }
                })
                .bounds(x + 110, y + 115, 80, 40)
                .build());

        button_direction = this.addRenderableWidget(Button.builder(Component.literal("方向"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_DIRECTION_ID);
                    }
                })
                .bounds(x + 65, y + 25, 18, 18)
                .build());

        button_range_add = this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_RANGE_ADD_ID);
                    }
                })
                .bounds(x + 65, y + 47, 18, 9)
                .tooltip(Tooltip.create(Component.literal("增加范围，以机器所在区块为中心")))
                .build());
        button_range_sub = this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_RANGE_SUB_ID);
                    }
                })
                .bounds(button_range_add.getX(), button_range_add.getY() + 10, 18, 9)
                .tooltip(Tooltip.create(Component.literal("减少范围，以机器所在区块为中心")))
                .build());

        button_speed_add = this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_SPEED_ADD_ID);
                    }
                })
                .bounds(x + 65, y + 70, 18, 9)
                .tooltip(Tooltip.create(Component.literal("增加速度")))
                .build());
        button_speed_sub = this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_SPEED_SUB_ID);
                    }
                })
                .bounds(button_speed_add.getX(), button_speed_add.getY() + 10, 18, 9)
                .tooltip(Tooltip.create(Component.literal("减少速度")))
                .build());

        button_bedrock = this.addRenderableWidget(Button.builder(Component.literal("破基岩"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_BEDROCK_ID);
                    }
                })
                .bounds(x + 65, y + 93, 18, 18)
                .tooltip(Tooltip.create(Component.literal("是否破基岩")))
                .build());

        button_is_placing = this.addRenderableWidget(Button.builder(Component.literal("放置模式"), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_IS_PLACING_ID);
                    }
                })
                .bounds(x + 65, y + 115, 18, 18)
                .tooltip(Tooltip.create(Component.literal("是否为放置方块模式，此模式下机器将在机器上方或下方放置一层方块")))
                .build());

        button_placing_block = this.addRenderableWidget(Button.builder(Component.literal(""), (button) -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ChunkEraserMenu.BUTTON_PLACING_BLOCK_ID);
                    }
                })
                .bounds(x + 65, y + 127, 18, 18)
                .tooltip(Tooltip.create(Component.literal("选择放置的方块")))
                .build());


    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 绑定GUI纹理
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        // 计算GUI绘制位置（居中）
        int x = this.leftPos;
        int y = this.topPos;
        // 绘制GUI背景
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        guiGraphics.drawString(this.font, "方向：",
                button_direction.getX() - this.leftPos - 25, button_direction.getY() - this.topPos + 5,4210752, false);

        guiGraphics.drawString(this.font, "范围：",
                button_range_add.getX() - this.leftPos - 25, button_range_add.getY() - this.topPos + 5,4210752, false);
        guiGraphics.drawString(this.font, (menu.blockEntity.range * 2 + 1) + " × " + (menu.blockEntity.range * 2 + 1) + "区块",
                button_range_add.getX() - this.leftPos + 25, button_range_add.getY() - this.topPos + 5,4210752, false);

        guiGraphics.drawString(this.font, "速度：",
                button_speed_add.getX() - this.leftPos - 25, button_speed_add.getY() - this.topPos + 5,4210752, false);
        guiGraphics.drawString(this.font,  (menu.blockEntity.opsPerTick) + " (" + (menu.blockEntity.opsPerTick) * (menu.blockEntity.range * 2 + 1) * (menu.blockEntity.range * 2 + 1)  + "方块/tick)",
                button_speed_add.getX() - this.leftPos + 25, button_speed_add.getY() - this.topPos + 5,4210752, false);

        guiGraphics.drawString(this.font, "破基岩：",
                button_bedrock.getX() - this.leftPos - 34, button_bedrock.getY() - this.topPos + 5,4210752, false);

        guiGraphics.drawString(this.font, "放置模式：",
                button_is_placing.getX() - this.leftPos - 43, button_is_placing.getY() - this.topPos + 5,4210752, false);

        if (mouseX >= this.leftPos + 45 && mouseX <= this.leftPos + 65 && mouseY >= this.topPos && mouseY <= this.topPos + 20) {
            List<Component> tooltipLines = List.of(
                    Component.literal("此机器用于区块清除与地形平整"),
                    Component.literal("清除地形时不会掉落物品，请检查目标区域是否有重要建筑").withStyle(ChatFormatting.RED),
                    Component.literal("清除时会跳过未加载的方块，请确认区块已加载").withStyle(ChatFormatting.GRAY),
                    Component.literal("工作中改变方向和范围设置，会重置清除进度").withStyle(ChatFormatting.GRAY),
                    Component.literal("当运行到世界高度上下限时，机器会自动停止").withStyle(ChatFormatting.GRAY),
                    Component.literal("放置方块模式下，机器将在设置方向上放置一层方块").withStyle(ChatFormatting.GRAY)
            );
            guiGraphics.renderComponentTooltip(this.font, tooltipLines, mouseX - this.leftPos , mouseY - this.topPos);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        button_active.setTooltip(Tooltip.create(Component.literal(menu.blockEntity.getBlockState().getValue(ChunkEraserBlock.ACTIVE) ? "工作中~点击停止" : "点击启动机器")));

        button_direction.setTooltip(Tooltip.create(Component.literal(menu.blockEntity.workDirection ? "清除机器下方区域，直至基岩层" : "清除机器上方区域，直至高度上限")));
        button_direction.setMessage(Component.literal(menu.blockEntity.workDirection ? "⇩" : "⇧"));

        button_range_add.active = menu.blockEntity.range < 20;
        button_range_sub.active = menu.blockEntity.range > 0;

        button_speed_add.active = menu.blockEntity.opsPerTick < 120;
        button_speed_sub.active = menu.blockEntity.opsPerTick > 5;

        button_bedrock.setMessage(Component.literal(menu.blockEntity.canDestroyBedrock ? "✓" : "×"));
        button_is_placing.setMessage(Component.literal(menu.blockEntity.isPlacing ? "✓" : "×"));

        guiGraphics.renderItem(new ItemStack(Items.SMOOTH_STONE, 1), 200, 100);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 获取鼠标下的槽位
        Slot slot = this.getSlotUnderMouse();

        // 只有当：按住Shift + 鼠标左键(0) + 鼠标下有槽位 + 槽位里有东西
        if (slot != null && hasShiftDown() && button == 0 && slot.hasItem()) {
            // 模拟发送一次 Quick Move (Shift+点击) 的包给服务器
            this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, slot.index, 0, ClickType.QUICK_MOVE, this.minecraft.player);
            // 这里不返回 true，允许后续逻辑继续处理（虽然通常这样就够了）
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
