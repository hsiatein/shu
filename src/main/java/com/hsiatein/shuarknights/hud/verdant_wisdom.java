package com.hsiatein.shuarknights.hud;


import com.hsiatein.shuarknights.item.yucong;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import com.hsiatein.shuarknights.shuarknights;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraft.client.gui.GuiGraphics;

import static java.lang.Math.round;
import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class verdant_wisdom implements IGuiOverlay {
    private static final ResourceLocation ICON = fromNamespaceAndPath(shuarknights.MODID,"textures/hud/verdant_wisdom.png");
    public static int CHARGES = 0;
    public static final int MAX_CHARGE =3;
    public static int SP = 0;
    public static final int MAX_SP =4;
    public static boolean DISPLAY = false;
    public static final int DX = 0;
    public static final int DY =0;
    public static void recoverSP(ItemStack heldItem){
        DISPLAY=heldItem.getItem() instanceof yucong;
        if (CHARGES<MAX_CHARGE){
            SP++;
        }
        if (SP>=20*MAX_SP){
            SP=0;
            CHARGES++;
        }
    }
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if(!DISPLAY) return;
        // 设置 shader 和颜色
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ICON);

        // 图标位置
        int iconX = (int) round(screenWidth * 0.9)+DX;
        int iconY = (int) round(screenHeight * 0.5)+DY;

        // 绘制图标
        guiGraphics.blit(ICON, iconX, iconY, 0, 0, 32, 32, 32, 32);

        // 计算蒙版高度
        int maxWisdom = MAX_SP*20;
        int maskHeight = (int) (32 * (1 - (SP / (float) maxWisdom)));

        // 绘制绿色半透明蒙版
        if (maskHeight < 32) {
            guiGraphics.fill(iconX, iconY + maskHeight, iconX + 32, iconY + 32, 0x8000FF00);
        }

        if(CHARGES <1 || MAX_CHARGE==1) return;
        // 绘制小圆圈
        int circleRadius = 4;
        int circleX = iconX+1; // 圆心位置
        int circleY = iconY+1; // 圆心位置
        guiGraphics.fill(circleX - circleRadius, circleY - circleRadius, circleX + circleRadius-1, circleY + circleRadius-1, 0xFF404040); // 灰色背景

        // 绘制数字
        String chargeText = String.valueOf(CHARGES);
        guiGraphics.drawString(Minecraft.getInstance().font, chargeText, circleX-3, circleY-4, 0xFFFFFFFF); // 白色字体

    }
}
