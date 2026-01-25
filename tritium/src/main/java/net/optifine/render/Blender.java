package net.optifine.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.src.Config;
import org.lwjgl.opengl.GL11;

public class Blender {
    public static final int BLEND_ALPHA = 0;
    public static final int BLEND_ADD = 1;
    public static final int BLEND_SUBSTRACT = 2;
    public static final int BLEND_MULTIPLY = 3;
    public static final int BLEND_DODGE = 4;
    public static final int BLEND_BURN = 5;
    public static final int BLEND_SCREEN = 6;
    public static final int BLEND_OVERLAY = 7;
    public static final int BLEND_REPLACE = 8;
    public static final int BLEND_DEFAULT = 1;

    public static int parseBlend(String str) {
        if (str == null) {
            return 1;
        } else {
            str = str.toLowerCase().trim();

            if (str.equals("alpha")) {
                return 0;
            } else if (str.equals("add")) {
                return 1;
            } else if (str.equals("subtract")) {
                return 2;
            } else if (str.equals("multiply")) {
                return 3;
            } else if (str.equals("dodge")) {
                return 4;
            } else if (str.equals("burn")) {
                return 5;
            } else if (str.equals("screen")) {
                return 6;
            } else if (str.equals("overlay")) {
                return 7;
            } else if (str.equals("replace")) {
                return 8;
            } else {
                Config.warn("Unknown blend: " + str);
                return 1;
            }
        }
    }

    public static void setupBlend(int blend, float brightness) {
        switch (blend) {
            case 0:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
                break;

            case 1:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
                break;

            case 2:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 3:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(brightness, brightness, brightness, brightness);
                break;

            case 4:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 5:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 6:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 7:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 8:
                GlStateManager.enableAlpha();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
        }

        GlStateManager.enableTexture2D();
    }

    public static void clearBlend(float rainBrightness) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, rainBrightness);
    }
}
