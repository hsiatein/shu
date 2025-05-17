package com.hsiatein.shuarknights.hud;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import com.hsiatein.shuarknights.shuarknights;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.gui.GuiGraphics;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class shu_skill_one implements IGuiOverlay {
    private static final ResourceLocation SHU_SKILL_ONE_ICON = fromNamespaceAndPath(shuarknights.MODID,"textures/hud/shu_skill_one.png");


    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        // 设置 shader 和颜色
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SHU_SKILL_ONE_ICON);

        int x = screenWidth/2;
        int y = screenHeight;

        // 绘制图标
        guiGraphics.blit(SHU_SKILL_ONE_ICON, x-90, y-50, 0, 0, 32, 32,32,32); // 16x16 图标，调整参数根据需要
    }
}
