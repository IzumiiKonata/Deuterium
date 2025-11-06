package tritium.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Location;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;
import tritium.rendering.rendersystem.RenderSystem;

public class Image {

    public static void draw(Location img, double x, double y, double width, double height, Type type) {
        draw(img, x, y, width, height, width, height, type);
    }

    public static void draw(ITextureObject img, double x, double y, double width, double height, Type type) {
        draw(img, x, y, width, height, width, height, type);
    }

    public static void drawLinear(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }
        
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawNearest(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.nearestFilter();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinearRotate90R(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            drawModalRectWithCustomSizedTextureRotate90R(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinearRotate90L(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            drawModalRectWithCustomSizedTextureRotate90L(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawModalRectWithCustomSizedTextureRotate90R(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u) * f, (v) * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex((u) * f, (v + height) * f1).endVertex();
        tessellator.draw();
    }

    public static void drawModalRectWithCustomSizedTextureRotate90L(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex((u + width) * f, (v) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex((u) * f, (v) * f1).endVertex();
        tessellator.draw();
    }

    public static void drawLinearFlippedX(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            drawModalRectWithCustomSizedTextureFlippedX(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawSpecial(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinear(Location img, double x, double y, double width, double height, double tWidth, double tHeight, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, tWidth, tHeight, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();

    }

    public static void draw(ITextureObject img, double x, double y, double width, double height, double tWidth, double tHeight, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        TextureUtils.bindTexture(img.getGlTextureId());
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, tWidth, tHeight, width, height);

        GlStateManager.enableAlpha();

    }

    public static void draw(Location img, double x, double y, double width, double height, double tWidth, double tHeight, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, tWidth, tHeight, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
        
    }

    public static void draw(Location img, double x, double y, double width, double height, double tX, double tY, double tWidth, double tHeight, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture2(x, y, tX, tY, width, height, tWidth, tHeight);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();

    }

    public static void drawKeepBlend(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();

    }

    public static void drawKeepBlendLinear(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();

    }

    public static void drawKeepState(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

//        GlStateManager.enableBlend();
//        GlStateManager.disableAlpha();
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

//        GlStateManager.enableAlpha();

    }

    public static void draw(int textureId, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(textureId);

        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        GlStateManager.enableAlpha();
        
    }

    public static void drawFlipped(int textureId, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
//        GlStateManager.blendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(textureId);

        drawModalRectWithCustomSizedTextureFlippedY(x, y, 0, 0, width, height, width, height);

        GlStateManager.enableAlpha();

    }

    public static void drawLinearFlippedY(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            drawModalRectWithCustomSizedTextureFlippedY(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinearFlippedXAndY(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            drawModalRectWithCustomSizedTextureFlippedXAndY(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawModalRectWithCustomSizedTextureFlippedX(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u) * f, (v) * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex((u + width) * f, (v) * f1).endVertex();
        tessellator.draw();
    }

    public static void drawModalRectWithCustomSizedTextureFlippedY(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        tessellator.draw();
    }

    public static void drawModalRectWithCustomSizedTextureFlippedXAndY(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex((u + width) * f, (v) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u) * f, (v) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        tessellator.draw();
    }

    public static void draw(double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//        GlStateManager.bindTexture(textureId);

        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        GlStateManager.enableAlpha();
        
    }

    public static void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public enum Type {
        NoColor, Normal
    }
}
