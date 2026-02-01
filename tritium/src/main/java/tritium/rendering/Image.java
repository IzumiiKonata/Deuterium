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
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;

public class Image {

    private static void renderDbgInfo(String str, double x, double y, double width, double height) {

        // show layout
        RenderSystem.drawOutLine(x, y, width, height, 0.5, 0x40FF0000);

        double lineLength = Math.min(8, Math.min(width * .25, height * .25));
        double lineSize = 1;
        int lineColor = 0x400090FF;
        // left top
        Rect.draw(x, y, lineLength, lineSize, lineColor);
        Rect.draw(x, y, lineSize, lineLength, lineColor);

        // right top
        Rect.draw(x + width - lineLength, y, lineLength, lineSize, lineColor);
        Rect.draw(x + width - lineSize, y, lineSize, lineLength, lineColor);

        // left bottom
        Rect.draw(x, y + height - lineLength, lineSize, lineLength, lineColor);
        Rect.draw(x, y + height - lineSize, lineLength, lineSize, lineColor);

        // right bottom
        Rect.draw(x + width - lineLength, y + height - lineSize, lineLength, lineSize, lineColor);
        Rect.draw(x + width - lineSize, y + height - lineLength, lineSize, lineLength, lineColor);
        
        CFontRenderer fr = FontManager.pf18;

        double sw = fr.getStringWidthD(str);
        double dbgX = Math.max(0, Math.min(RenderSystem.getWidth() - sw, x));
        double dbgY = Math.max(0, Math.min(RenderSystem.getHeight() - fr.getFontHeight(), y));
        fr.drawStringWithShadow(str, dbgX, dbgY, -1);
    }

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
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.linearFilter();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
        }
        GlStateManager.enableAlpha();
    }

    public static void drawNearest(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
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

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinearRotate90R(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
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

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinearRotate90L(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
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

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
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
        GlStateManager.enableTexture2D();
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

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
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
        GlStateManager.enableTexture2D();
        TextureUtils.bindTexture(img.getGlTextureId());
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, tWidth, tHeight, width, height);

        if (ClientSettings.DEBUG_MODE.getValue()) {
            renderDbgInfo("TexID: " + img.getGlTextureId(), x, y, width, height);
        }

//        GlStateManager.enableAlpha();

    }

    public static void draw(Location img, double x, double y, double width, double height, double tWidth, double tHeight, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.enableTexture2D();
        ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(img);
        if (textureObj != null && textureObj != TextureUtil.missingTexture) {
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, tWidth, tHeight, width, height);
        } else if (textureObj != TextureUtil.missingTexture) {
            textureObj = new SimpleTexture(img);
            Minecraft.getMinecraft().getTextureManager().loadTexture(img, textureObj);
        }

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
        }

        GlStateManager.enableAlpha();
        
    }

    public static void draw(int textureId, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(textureId);

        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        if (ClientSettings.DEBUG_MODE.getValue())
            renderDbgInfo("TexID: " + textureId, x, y, width, height);

        GlStateManager.enableAlpha();
        
    }

    public static void drawLinearFlippedY(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
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

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
        }

        GlStateManager.enableAlpha();
    }

    public static void drawLinearFlippedXAndY(Location img, double x, double y, double width, double height, Type type) {

        if (type == Type.Normal) {
            GlStateManager.color(1, 1, 1, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
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

        if (ClientSettings.DEBUG_MODE.getValue()) {
            int beginIndex = img.getResourcePath().indexOf("/");
            String str = img.getResourcePath().substring(beginIndex == -1 ? 0 : beginIndex + 1);
            renderDbgInfo(str, x, y, width, height);
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
        GlStateManager.enableTexture2D();
//        GlStateManager.bindTexture(textureId);

        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        GlStateManager.enableAlpha();
        
    }

    public static void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }

    public enum Type {
        NoColor, Normal
    }
}
