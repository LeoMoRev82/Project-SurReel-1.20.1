package com.leomorev.surreel.block.mineshaft_builder;

import com.leomorev.surreel.SurReelMain;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class MineshaftBuilderScreen extends AbstractContainerScreen<MineshaftBuilderMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(SurReelMain.MODID, "textures/gui/mineshaft_builder_gui.png");

	public MineshaftBuilderScreen(MineshaftBuilderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

    @Override
    protected void renderBg(GuiGraphics pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;


        pPoseStack.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if(menu.isLit()) {
            int k = this.menu.getLitProgress();
            pPoseStack.blit(TEXTURE, x + 26, y + 20 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        float sendPercent = this.menu.getCompletionPercent();
        pPoseStack.drawString(this.font, Component.literal(String.valueOf(sendPercent / 100)), x + 20, y + 60, 0, false);
        //font.draw(pPoseStack, Component.literal(String.valueOf(sendPercent / 100)),  x + 20,  y + 60, 4210752);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
	
	//@Override
    //public void render(@NotNull PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
    //    renderBackground(pPoseStack);
    //    super.render(pPoseStack, mouseX, mouseY, delta);
    //    renderTooltip(pPoseStack, mouseX, mouseY);
    //}
	
}
