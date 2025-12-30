package tritium.rendering.rendersystem;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;
import tritium.rendering.RGBA;
import tritium.rendering.Rect;
import tritium.settings.ClientSettings;
import tritium.utils.res.skin.PlayerSkinTextureCache;

import java.awt.*;
import java.util.UUID;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author IzumiiKonata
 * @since 4/15/2023 8:47 PM
 */
public class RenderSystem {
    public static final Object ASYNC_LOCK = new Object();
    public static final float DIVIDE_BY_255 = 0.003921568627451F;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static PlayerSkinTextureCache playerSkinTextureCache;
    @Getter
    @Setter
    private static double frameDeltaTime = 0;


    public static void setBlurMipmapDirect(boolean blur, boolean mipmap) {
        int minFilter = -1;
        int magFilter = -1;

        if (blur) {
            minFilter = mipmap ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR;
            magFilter = GL11.GL_LINEAR;
        } else {
            minFilter = mipmap ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST;
            magFilter = GL11.GL_NEAREST;
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
    }

    public static void linearFilter() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    public static void nearestFilter() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public static double getScaleFactor() {
        ScaledResolution scaledResolution = ScaledResolution.get();
        int scale = scaledResolution.getScaleFactor();

        return scale / 2.0;
    }

    public static double getWidth() {

        if (!ClientSettings.FIXED_SCALE.getValue()) {
            return ScaledResolution.get().getScaledWidth_double();
        }

        return getFixedWidth() * .5;
    }

    public static double getHeight() {

        if (!ClientSettings.FIXED_SCALE.getValue()) {
            return ScaledResolution.get().getScaledHeight_double();
        }

        return getFixedHeight() * .5;
    }

    public static double getFixedWidth() {
        return Math.min(mc.displayWidth, 1920);
    }

    public static double getFixedHeight() {
        double scaleFactor = mc.displayWidth / getFixedWidth();
        return mc.displayHeight / scaleFactor;
    }

    public static void color(int color) {
        float f = (color >> 24 & 255) * DIVIDE_BY_255;
        float f1 = (color >> 16 & 255) * DIVIDE_BY_255;
        float f2 = (color >> 8 & 255) * DIVIDE_BY_255;
        float f3 = (color & 255) * DIVIDE_BY_255;
        GlStateManager.color(f1, f2, f3, f);
//        GlStateManager.color(f1, f2, f3, f);
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {

        if (left > right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top > bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }


//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        boolean texture2DEnabled = GlStateManager.isTexture2DEnabled();
        if (texture2DEnabled)
            GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.color(color);

        GL11.glBegin(GL_TRIANGLE_STRIP);

        GL11.glVertex2d(left, bottom);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(right, top);

        GL11.glEnd();

//        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
//        worldrenderer.pos(left, bottom, 0.0D).endVertex();
//        worldrenderer.pos(right, bottom, 0.0D).endVertex();
//        worldrenderer.pos(right, top, 0.0D).endVertex();
//        worldrenderer.pos(left, top, 0.0D).endVertex();
//
//        tessellator.draw();
//        if (texture2DEnabled)
//            GlStateManager.enableTexture2D();
//        GlStateManager.enableTexture2D();
//        GlStateManager.disableBlend();

//        RenderSystem.resetColor();
    }

    public static void resetColor() {
        RenderSystem.color(-1);
    }

    public static void drawGradientRectLeftToRight(final double left, final double top, final double right, final double bottom, final int startColor, final int endColor) {
        final float sa = (startColor >> 24 & 0xFF) * 0.003921568627451F;
        final float sr = (startColor >> 16 & 0xFF) * 0.003921568627451F;
        final float sg = (startColor >> 8 & 0xFF) * 0.003921568627451F;
        final float sb = (startColor & 0xFF) * 0.003921568627451F;
        final float ea = (endColor >> 24 & 0xFF) * 0.003921568627451F;
        final float er = (endColor >> 16 & 0xFF) * 0.003921568627451F;
        final float eg = (endColor >> 8 & 0xFF) * 0.003921568627451F;
        final float eb = (endColor & 0xFF) * 0.003921568627451F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, top, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(left, top, 0.0).color(sr, sg, sb, sa).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRectBottomToTop(final double left, final double top, final double right, final double bottom, final int startColor, final int endColor) {
        final float sa = (startColor >> 24 & 0xFF) * 0.003921568627451F;
        final float sr = (startColor >> 16 & 0xFF) * 0.003921568627451F;
        final float sg = (startColor >> 8 & 0xFF) * 0.003921568627451F;
        final float sb = (startColor & 0xFF) * 0.003921568627451F;
        final float ea = (endColor >> 24 & 0xFF) * 0.003921568627451F;
        final float er = (endColor >> 16 & 0xFF) * 0.003921568627451F;
        final float eg = (endColor >> 8 & 0xFF) * 0.003921568627451F;
        final float eb = (endColor & 0xFF) * 0.003921568627451F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, top, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(left, top, 0.0).color(er, eg, eb, ea).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRectTopToBottom(final double left, final double top, final double right, final double bottom, final int startColor, final int endColor) {
        final float sa = (startColor >> 24 & 0xFF) * 0.003921568627451F;
        final float sr = (startColor >> 16 & 0xFF) * 0.003921568627451F;
        final float sg = (startColor >> 8 & 0xFF) * 0.003921568627451F;
        final float sb = (startColor & 0xFF) * 0.003921568627451F;
        final float ea = (endColor >> 24 & 0xFF) * 0.003921568627451F;
        final float er = (endColor >> 16 & 0xFF) * 0.003921568627451F;
        final float eg = (endColor >> 8 & 0xFF) * 0.003921568627451F;
        final float eb = (endColor & 0xFF) * 0.003921568627451F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, top, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(left, top, 0.0).color(sr, sg, sb, sa).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static boolean isHovered(double mouseX, double mouseY, double startX, double startY, double width, double height) {

        if (width < 0) {
            width = -width;
            startX -= width;
        }

        if (height < 0) {
            height = -height;
            startY -= height;
        }

        return mouseX >= startX && mouseY >= startY && mouseX <= startX + width && mouseY <= startY + height;
    }

    public static boolean isHovered(double mouseX, double mouseY, double startX, double startY, double width, double height, double shrink) {
        return RenderSystem.isHovered(mouseX, mouseY, startX + shrink, startY + shrink, width - shrink * 2, height - shrink * 2);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null) {
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else if (framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
        }
        return framebuffer;
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null) {
            return new Framebuffer(width, height, true);
        } else if (framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            framebuffer.createBindFramebuffer(width, height);
        }
        return framebuffer;
    }

    public static Framebuffer createFrameBufferNoDepth(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, false);
        }
        return framebuffer;
    }

    public static Framebuffer createDownScaledFrameBuffer(Framebuffer framebuffer, double factor) {
        if (framebuffer == null || framebuffer.framebufferWidth != (int) (mc.displayWidth * factor) || framebuffer.framebufferHeight != (int) (mc.displayHeight * factor)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer((int) (mc.displayWidth * factor), (int) (mc.displayHeight * factor), false);
        }
        return framebuffer;
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static void bindTexture(int textureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    public static double getMouseX() {
        return Mouse.getX() * RenderSystem.getScaleFactor();
    }

    public static double getMouseY() {
        return Mouse.getY() * RenderSystem.getScaleFactor();
    }

    public static void translateAndScale(double posX, double posY, double scale) {

        GlStateManager.translate(posX, posY, 0);
        GlStateManager.scale(scale, scale, 2);
        GlStateManager.translate(-posX, -posY, 0);

    }

    public static void refreshSkinCache() {
        if (mc == null)
            mc = Minecraft.getMinecraft();
        playerSkinTextureCache = new PlayerSkinTextureCache(mc.getSkinManager(), mc.getSessionService());
    }

    public static void drawPlayerHead(GameProfile gameProfile, double x, double y, double size) {
        Location resourceLocation = playerSkinTextureCache.getSkinTexture(gameProfile, (l, b) -> {
        });
        RenderSystem.drawPlayerHead(resourceLocation, x, y, size);
    }

    public static void drawPlayerHead(String username, double x, double y, double size) {
        Location resourceLocation = playerSkinTextureCache.getSkinTexture(username, (l, b) -> {
        });
        RenderSystem.drawPlayerHead(resourceLocation, x, y, size);
    }

    public static void drawLocalPlayerHead(double x, double y, double size) {

        Render<Entity> ero = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(Minecraft.getMinecraft().getRenderViewEntity());

        Location texture = ero.getTexture(Minecraft.getMinecraft().getRenderViewEntity());

        RenderSystem.drawPlayerHead(texture, x, y, size);

    }

    public static void drawPlayerHead(UUID uuid, double x, double y, double size) {
        Location resourceLocation = playerSkinTextureCache.getSkinTexture(uuid, (l, b) -> {
        });
        RenderSystem.drawPlayerHead(resourceLocation, x, y, size);
    }

    public static void drawPlayerHead(Location resourceLocation, double x, double y, double size) {
        if (resourceLocation == null) {
            resourceLocation = DefaultPlayerSkin.getDefaultSkin(UUID.randomUUID());
        }
        RenderSystem.resetColor();
        GlStateManager.enableAlpha();
        mc.getTextureManager().bindTexture(resourceLocation);
        Gui.drawScaledCustomSizeModalRect(x, y, 8.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
        Gui.drawScaledCustomSizeModalRect(x, y, 40.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
//        Gui.drawScaledCustomSizeModalRect(x, y, 40.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);

    }

    public static void drawOutLine(double x, double y, double width, double height, double thickness, int color) {
//        RenderSystem.color(color);

        Rect.draw(x - thickness, y - thickness, width + thickness * 2, thickness, color, Rect.RectType.EXPAND);
        Rect.draw(x - thickness, y - thickness, thickness, height + thickness, color, Rect.RectType.EXPAND);
        Rect.draw(x + width, y - thickness, thickness, height + thickness, color, Rect.RectType.EXPAND);
        Rect.draw(x - thickness, y + height, width + thickness * 2, thickness, color, Rect.RectType.EXPAND);


    }

    public static void doScissor(double x, double y, double width, double height) {
        doScissor((int) x, (int) y, (int) width, (int) height);
    }

    public static void doScissor(double x, double y, double width, double height, double shrink) {
        doScissor(x - shrink, y - shrink, width + shrink * 2, height + shrink * 2);
    }

    public static volatile boolean forceDisableScissor = false;

    public static void doScissor(int x, int y, int width, int height) {

        if (forceDisableScissor) {
            return;
        }

//        x += 1;
//        y += 1;
//        width += 1;
//        height += 1;
        glEnable(GL11.GL_SCISSOR_TEST);

        if (ClientSettings.FIXED_SCALE.getValue()) {
            x = x * 2;
            y = (int) ((RenderSystem.getHeight() - y) * 2);
            width = width * 2;
            height = height * 2;
        } else {
            final ScaledResolution sr = mc.scaledResolution;
            final double scale = sr.getScaleFactor();

            y = sr.getScaledHeight() - y;

            x *= scale;
            y *= scale;
            width *= scale;
            height *= scale;
        }

        GL11.glScissor(x, y - height, width, height);
    }

    public static void endScissor() {

        if (forceDisableScissor) {
            return;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static Color getOppositeColor(Color colorIn) {
        return new Color(255 - colorIn.getRed(), 255 - colorIn.getGreen(), 255 - colorIn.getBlue(), colorIn.getAlpha());
    }

    public static int getOppositeColorHex(int colorHex) {
        return getOppositeColor(new Color(colorHex, true)).getRGB();
    }

    public static int cRange(int c) {
        if (c < 0) {
            c = 0;
        }

        if (c > 255) {
            c = 255;
        }

        return c;
    }

    public static int reAlpha(int color, float alpha) {
        if (alpha > 1) {
            alpha = 1;
        }

        if (alpha < 0) {
            alpha = 0;
        }
        return RGBA.color((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, (int) (alpha * 255));
    }

    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

}
