package tritium.rendering.rendersystem;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Location;
import net.minecraft.util.Timer;
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
    private static final int[] DISPLAY_LISTS_2D = new int[4];
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

        return Minecraft.getMinecraft().displayWidth / 2.0;
    }

    public static double getHeight() {

        if (!ClientSettings.FIXED_SCALE.getValue()) {
            return ScaledResolution.get().getScaledHeight_double();
        }

        return Minecraft.getMinecraft().displayHeight * .5;
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

        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }


//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
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
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, top, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(left, top, 0.0).color(sr, sg, sb, sa).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
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
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, top, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(left, top, 0.0).color(er, eg, eb, ea).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
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
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, top, 0.0).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(left, top, 0.0).color(sr, sg, sb, sa).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawBorderedRect(double x, double y, double x2, double y2, double l1, int col1, int col2) {
        drawRect(x, y, x2, y2, col2);
        float f = (col1 >> 24 & 0xFF) * 0.003921568627451F;
        float f2 = (col1 >> 16 & 0xFF) * 0.003921568627451F;
        float f3 = (col1 >> 8 & 0xFF) * 0.003921568627451F;
        float f4 = (col1 & 0xFF) * 0.003921568627451F;
        glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        glEnable(2848);
        GL11.glPushMatrix();
        GlStateManager.color(f2, f3, f4, f);
        GL11.glLineWidth((float) l1);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
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

    public static void drawHorizontalLine(double x, double y, double x1, double y1, float width, int color) {
        float var11 = (float) (color >> 24 & 255) * 0.003921568627451F;
        float var12 = (float) (color >> 16 & 255) * 0.003921568627451F;
        float var13 = (float) (color >> 8 & 255) * 0.003921568627451F;
        float var14 = (float) (color & 255) * 0.003921568627451F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        GL11.glPushMatrix();
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        GL11.glPopMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

    public static float getRedFormInt(int color) {
        return (color >> 16 & 255) * DIVIDE_BY_255;
    }

    public static float getGreenFormInt(int color) {
        return (color >> 8 & 255) * DIVIDE_BY_255;
    }

    public static float getBlueFormInt(int color) {
        return (color & 255) * DIVIDE_BY_255;
    }

    public static float getAlphaFormInt(int color) {
        return (color >> 24 & 255) * DIVIDE_BY_255;
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

    public static void drawEntityESP(double x, double y, double z, double width, double height, float red, float green,
                                     float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWdith) {
        GL11.glPushMatrix();
        glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GlStateManager.color(red, green, blue, alpha);
        RenderSystem.drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
//        GL11.glLineWidth((float) lineWdith);
//        GlStateManager.color((float) lineRed, (float) lineGreen, (float) lineBlue, (float) lineAlpha);
//        RenderUtil
//                .drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        glEnable(3553);
        glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
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

    public static void quickDrawRect(final float x, final float y, final float x2, final float y2) {
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);

        GL11.glEnd();
    }

    public static void initDisplayList() {
        for (int i = 0; i < DISPLAY_LISTS_2D.length; i++) {
            DISPLAY_LISTS_2D[i] = GL11.glGenLists(1);
        }

        GL11.glNewList(DISPLAY_LISTS_2D[0], GL11.GL_COMPILE);

        quickDrawRect(-7F, 2F, -4F, 3F);
        quickDrawRect(4F, 2F, 7F, 3F);
        quickDrawRect(-7F, 0.5F, -6F, 3F);
        quickDrawRect(6F, 0.5F, 7F, 3F);

        GL11.glEndList();

        GL11.glNewList(DISPLAY_LISTS_2D[1], GL11.GL_COMPILE);

        quickDrawRect(-7F, 3F, -4F, 3.3F);
        quickDrawRect(4F, 3F, 7F, 3.3F);
        quickDrawRect(-7.3F, 0.5F, -7F, 3.3F);
        quickDrawRect(7F, 0.5F, 7.3F, 3.3F);

        GL11.glEndList();

        GL11.glNewList(DISPLAY_LISTS_2D[2], GL11.GL_COMPILE);

        quickDrawRect(4F, -20F, 7F, -19F);
        quickDrawRect(-7F, -20F, -4F, -19F);
        quickDrawRect(6F, -20F, 7F, -17.5F);
        quickDrawRect(-7F, -20F, -6F, -17.5F);

        GL11.glEndList();

        GL11.glNewList(DISPLAY_LISTS_2D[3], GL11.GL_COMPILE);

        quickDrawRect(7F, -20F, 7.3F, -17.5F);
        quickDrawRect(-7.3F, -20F, -7F, -17.5F);
        quickDrawRect(4F, -20.3F, 7.3F, -20F);
        quickDrawRect(-7.3F, -20.3F, -4F, -20F);

        GL11.glEndList();
    }

    public static void draw2D(final BlockPos blockPos, final int color, final int backgroundColor) {
        final RenderManager renderManager = mc.getRenderManager();

        final double posX = (blockPos.getX() + 0.5) - renderManager.renderPosX;
        final double posY = blockPos.getY() - renderManager.renderPosY;
        final double posZ = (blockPos.getZ() + 0.5) - renderManager.renderPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, posZ);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        GlStateManager.scale(-0.1D, -0.1D, 0.1D);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.depthMask(true);

        color(color);

        GL11.glCallList(DISPLAY_LISTS_2D[0]);

        color(backgroundColor);

        GL11.glCallList(DISPLAY_LISTS_2D[1]);

        GlStateManager.translate(0, 9, 0);

        color(color);

        GL11.glCallList(DISPLAY_LISTS_2D[2]);

        color(backgroundColor);

        GL11.glCallList(DISPLAY_LISTS_2D[3]);

        // Stop render
        glEnable(GL11.GL_DEPTH_TEST);
        glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GlStateManager.popMatrix();
    }

    public static void drawBlockBox(BlockPos blockPos, int color, final boolean outline) {
        GlStateManager.pushAttrib();
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        final double x = blockPos.getX() - renderManager.renderPosX;
        final double y = blockPos.getY() - renderManager.renderPosY;
        final double z = blockPos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();

        if (block != null) {
            final EntityPlayer player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        color(color);
        drawFilledBox(axisAlignedBB);

        if (outline) {
            GL11.glLineWidth(1F);
            glEnable(GL11.GL_LINE_SMOOTH);
            color(color);

            drawSelectionBoundingBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
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

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();

        tessellator.draw();
    }

    public static class RenderingTimes {
        public static long screen, render2D, render3D;
    }

    public static void circle(double x, double y, double r, int color) {
        arc(x, y, 0.0f, 360.0f, r, color);
    }

    public static void circle(double n, double n2, double n3, final Color color) {
        arc(n, n2, 0.0f, 360.0f, n3, color);
    }

    public static void circle2(double n, double n2, double n3, int n4) {
        arc2(n, n2, 0.0f, 360.0f, n3, n4);
    }

    public static void circle2(double n, double n2, double n3, final Color color) {
        arc2(n, n2, 0.0f, 360.0f, n3, color);
    }

    public static void arc(double x, double y, double n3, double angle, double r, int color) {
        arcEllipse(x, y, n3, angle, r, r, color);
    }

    public static void arc(double n, double n2, double n3, double n4, double n5, final Color color) {
        arcEllipse(n, n2, n3, n4, n5, n5, color);
    }

    public static void arc2(double n, double n2, double n3, double n4, double n5, int n6) {
        arcEllipse2(n, n2, n3, n4, n5, n5, n6);
    }

    public static void arc2(double n, double n2, double n3, double n4, double n5, final Color color) {
        arcEllipse2(n, n2, n3, n4, n5, n5, color);
    }

    public static void arcEllipse(double x, double y, double n3, double n4, double n5, double n6, Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GlStateManager.color(0.0f, 0.0f, 0.0f, 0.0f);
        if (n3 > n4) {
            double n7 = n4;
            n4 = n3;
            n3 = n7;
        }
        Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() * 0.003921568627451F, color.getGreen() * 0.003921568627451F, color.getBlue() * 0.003921568627451F, 1f);
        glEnable(2848);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(3);
        double n8 = n4;
        while (n8 >= n3) {
            GL11.glVertex2d(x + Math.cos(n8 * 3.141592653589793 / 180.0) * (n5 * 1.001f), y + Math.sin(n8 * 3.141592653589793 / 180.0) * (n6 * 1.001f));
            n8 -= 4.0f;
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.color(color.getRed() * 0.003921568627451F, color.getGreen() * 0.003921568627451F, color.getBlue() * 0.003921568627451F, color.getAlpha() * 0.003921568627451F);
        GL11.glBegin(6);
        double n9 = n4;
        while (n9 >= n3) {
            GL11.glVertex2d(x + Math.cos(n9 * 3.141592653589793 / 180.0) * n5, y + Math.sin(n9 * 3.141592653589793 / 180.0) * n6);
            n9 -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void arcEllipse(double x, double y, double n3, double angle, double radiusHorizontal, double radiusVertical, int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GlStateManager.color(0.0f, 0.0f, 0.0f, 1.0f);
        if (n3 > angle) {
            double n8 = angle;
            angle = n3;
            n3 = n8;
        }
        float p_color_3_ = (color >> 24 & 0xFF) * 0.003921568627451F;
        float p_color_0_ = (color >> 16 & 0xFF) * 0.003921568627451F;
        float p_color_1_ = (color >> 8 & 0xFF) * 0.003921568627451F;
        float p_color_2_ = (color & 0xFF) * 0.003921568627451F;
        Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, p_color_3_);
        glEnable(2848);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(3);
        double n9 = angle;
        while (n9 >= n3) {
            GL11.glVertex2d(x + Math.cos(n9 * 3.141592653589793 / 180.0) * (radiusHorizontal * 1.001f), y + Math.sin(n9 * 3.141592653589793 / 180.0) * (radiusVertical * 1.001f));
            n9 -= 4.0f;
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, p_color_3_);
        GL11.glBegin(6);
        double n10 = angle;
        while (n10 >= n3) {
            GL11.glVertex2d(x + Math.cos(n10 * 3.141592653589793 / 180.0) * radiusHorizontal, y + Math.sin(n10 * 3.141592653589793 / 180.0) * radiusVertical);
            n10 -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void arcEllipse2(double n, double n2, double n3, double n4, double n5, double n6, Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GlStateManager.color(0.0f, 0.0f, 0.0f, 0.0f);
        if (n3 > n4) {
            double n7 = n4;
            n4 = n3;
            n3 = n7;
        }
        Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() * 0.003921568627451F, color.getGreen() * 0.003921568627451F, color.getBlue() * 0.003921568627451F, color.getAlpha() * 0.003921568627451F);
        if (color.getAlpha() > 0.9f) {
            glEnable(2848);
            GL11.glLineWidth(1.0f);
            GL11.glBegin(3);
            double n8 = n4;
            while (n8 >= n3) {
                GL11.glVertex2d(n + Math.cos(n8 * 3.141592653589793 / 180.0) * (n5 * 1.001f), n2 + Math.sin(n8 * 3.141592653589793 / 180.0) * (n6 * 1.001f));
                n8 -= 4.0f;
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        double n9 = n4;
        while (n9 >= n3) {
            GL11.glVertex2d(n + Math.cos(n9 * 3.141592653589793 / 180.0) * n5, n2 + Math.sin(n9 * 3.141592653589793 / 180.0) * n6);
            n9 -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void arcEllipse2(double n, double n2, double n3, double n4, double n5, double n6, int n7) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GlStateManager.color(0.0f, 0.0f, 0.0f, 0.0f);
        if (n3 > n4) {
            double n8 = n4;
            n4 = n3;
            n3 = n8;
        }
        float p_color_3_ = (n7 >> 24 & 0xFF) * 0.003921568627451F;
        float p_color_0_ = (n7 >> 16 & 0xFF) * 0.003921568627451F;
        float p_color_1_ = (n7 >> 8 & 0xFF) * 0.003921568627451F;
        float p_color_2_ = (n7 & 0xFF) * 0.003921568627451F;
        Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, p_color_3_);
        if (p_color_3_ > 0.9f) {
            glEnable(2848);
            GL11.glLineWidth(1.0f);
            GL11.glBegin(3);
            double n9 = n4;
            while (n9 >= n3) {
                GL11.glVertex2d(n + (float) Math.cos(n9 * 3.141592653589793 / 180.0) * (n5 * 1.001f), n2 + (float) Math.sin(n9 * 3.141592653589793 / 180.0) * (n6 * 1.001f));
                n9 -= 4.0f;
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        double n10 = n4;
        while (n10 >= n3) {
            GL11.glVertex2d(n + (float) Math.cos(n10 * 3.141592653589793 / 180.0) * n5, n2 + (float) Math.sin(n10 * 3.141592653589793 / 180.0) * n6);
            n10 -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
