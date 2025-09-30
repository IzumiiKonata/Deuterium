package tritium.rendering.entities.impl;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Location;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import tritium.rendering.rendersystem.RenderSystem;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.Math.max;
import static org.lwjgl.nanovg.NanoSVG.nsvgDeleteRasterizer;
import static org.lwjgl.nanovg.NanoSVG.nsvgRasterize;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImageResize.STBIR_FLAG_ALPHA_PREMULTIPLIED;
import static org.lwjgl.system.MemoryUtil.*;

public class SVGImage {

    public static final Map<Location, SVGEntity> map = new HashMap<>();

    public static void draw(Location svgLocation, double x, double y, double width, double height) {

        if (map.get(svgLocation) == null) {
            SVGImage.load(svgLocation);
        }

        SVGEntity svgEntity = map.get(svgLocation);

        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//        OpenGlHelper.glBlendFunc();
        GlStateManager.enableTexture2D();

        GlStateManager.bindTexture(svgEntity.texID);

        Image.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

    }

    public static void drawKeepState(Location svgLocation, double x, double y, double width, double height) {

        if (map.get(svgLocation) == null) {
            SVGImage.load(svgLocation);
        }

        SVGEntity svgEntity = map.get(svgLocation);

        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.enableBlend();
        GlStateManager.bindTexture(svgEntity.texID);

        Image.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

    }

    public static void draw(Location svgLocation, double x, double y, double width, double height, int color) {

        if (map.get(svgLocation) == null) {
            SVGImage.load(svgLocation);
        }

        SVGEntity svgEntity = map.get(svgLocation);

        RenderSystem.resetColor();

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(svgEntity.texID);

        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height, color);

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

    }

    public static void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight, int color) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;

        int a = (color >> 24 & 255);
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).color(r, g, b, a).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).color(r, g, b, a).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).color(r, g, b, a).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).color(r, g, b, a).endVertex();
        tessellator.draw();
    }


    @SneakyThrows
    private static void load(Location svgLocation) {
        InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(svgLocation).getInputStream();

        ByteBuffer buffer = memAlloc(128 * 1024);

        try (ReadableByteChannel rbc = Channels.newChannel(inputStream)) {
            int c;
            while ((c = rbc.read(buffer)) != -1) {
                if (c == 0) {
                    buffer = memRealloc(buffer, (buffer.capacity() * 3) >> 1);
                }
            }
        }

        buffer.put((byte) 0);
        buffer.flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            NSVGImage img = NanoSVG.nsvgParse(buffer, stack.ASCII("px"), 96.0f);
            SVGEntity ent = new SVGEntity(img);
            ent.load((int) ent.img.width(), (int) ent.img.height());
            map.put(svgLocation, ent);
        } finally {
            memFree(buffer);
        }
    }

    public static void premultiplyAlpha(ByteBuffer image, int w, int h, int stride) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte) round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte) round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte) round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }

    private static class SVGEntity {

        public final NSVGImage img;
        public int texID = -1;

        public SVGEntity(NSVGImage img) {
            this.img = img;
        }

        public void load(int width, int height) {
            if (texID != -1) {
                throw new IllegalStateException("load() called twice!");
            }

            long rast = NanoSVG.nsvgCreateRasterizer();
            if (rast == NULL) {
                throw new IllegalStateException("Failed to create SVG rasterizer.");
            }

            ByteBuffer image = memAlloc(width * height * 4);

            nsvgRasterize(rast, img, 0, 0, 1.0f, image, width, height, width * 4);

            nsvgDeleteRasterizer(rast);

            texID = GL11.glGenTextures();

            glBindTexture(GL_TEXTURE_2D, texID);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            premultiplyAlpha(image, width, height, width * 4);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            ByteBuffer input_pixels = image;
            int input_w = width;
            int input_h = height;
            int mipmapLevel = 0;
            while (1 < input_w || 1 < input_h) {
                int output_w = max(1, input_w >> 1);
                int output_h = max(1, input_h >> 1);

                ByteBuffer output_pixels = memAlloc(width * height * 4);

                STBImageResize.stbir_resize_uint8_srgb(
                        input_pixels, input_w, input_h, input_w * 4,
                        output_pixels, output_w, output_h, output_w * 4,
                        4, 3, STBIR_FLAG_ALPHA_PREMULTIPLIED
                );

                memFree(input_pixels);

                glTexImage2D(GL_TEXTURE_2D, ++mipmapLevel, GL_RGBA, output_w, output_h, 0, GL_RGBA, GL_UNSIGNED_BYTE, output_pixels);

                input_pixels = output_pixels;
                input_w = output_w;
                input_h = output_h;
            }
            memFree(input_pixels);
        }

    }

}
