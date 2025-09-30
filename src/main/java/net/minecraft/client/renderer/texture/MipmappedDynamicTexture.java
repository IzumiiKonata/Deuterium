package net.minecraft.client.renderer.texture;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryUtil;
import tritium.rendering.async.AsyncGLContext;
import tritium.utils.other.DevUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.max;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageResize.STBIR_FLAG_ALPHA_PREMULTIPLIED;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * @author IzumiiKonata
 * Date: 2025/1/23 10:58
 */
public class MipmappedDynamicTexture extends DynamicTexture {

    public MipmappedDynamicTexture(BufferedImage bufferedImage) {
        super(bufferedImage);
    }

    public MipmappedDynamicTexture(BufferedImage bufferedImage, boolean clearable) {
        super(bufferedImage, clearable);
    }

    public MipmappedDynamicTexture(BufferedImage bufferedImage, boolean clearable, boolean linear) {
        super(bufferedImage, clearable, linear);
    }

    public MipmappedDynamicTexture(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
    }

    @Override
    public void allocateTexture(int textureWidth, int textureHeight) {
        this.allocateTextureImpl(this.calculateMaxMipmapLevels(textureWidth, textureHeight), textureWidth, textureHeight);
    }

    public int calculateMaxMipmapLevels(int width, int height) {
        int mipmapLevel = 0;

        while (width > 1 || height > 1) {
            width = Math.max(1, width >> 1);
            height = Math.max(1, height >> 1);
            mipmapLevel++;
        }

        return mipmapLevel;
    }

    @SneakyThrows
    public synchronized void updateDynamicTexture() {

        synchronized (AsyncGLContext.MULTITHREADING_LOCK) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getGlTextureId());
            GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName = this.getGlTextureId();
            TextureUtil.setTextureBlurMipmap(isLinear(), true);
            TextureUtil.setTextureClamped(false);

            Minecraft mc = Minecraft.getMinecraft();

            if (mc.checkGLError("Dynamic Texture @ updateDynamicTexture @ pre")) {
                DevUtils.printCurrentInvokeStack();
            }

            if (this.dynamicTextureData.length < 4194304) {

                int padd = 0;

                if (this.dynamicTextureData.length % 4 != 0) {
                    padd = (4 - this.dynamicTextureData.length % 4);
                }

                ByteBuffer byteBuffer = memAlloc((this.dynamicTextureData.length + padd) << 2);
                IntBuffer buffer = byteBuffer.asIntBuffer();
                buffer.clear();
                buffer.put(this.dynamicTextureData, 0, this.dynamicTextureData.length);
                buffer.flip();

                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

                ByteBuffer input_pixels = byteBuffer;
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

                if (mc.checkGLError("Dynamic Texture @ updateDynamicTexture @ direct subImage2D")) {
                    DevUtils.printCurrentInvokeStack();
                }

            } else {

                // creates a 16 MB buffer
                IntBuffer dataBuffer = MemoryUtil.memAllocInt(4194304);

                int i = 4194304 / width;
                int j;

                int[] aint = this.dynamicTextureData;

                if (mc.gameSettings.anaglyph) {
                    aint = TextureUtil.updateAnaglyph(this.dynamicTextureData);
                }

                for (int k = 0; k < width * height; k += width * j) {
                    int l = k / width;
                    j = Math.min(i, height - l);
                    int i1 = width * j;

                    dataBuffer.clear();
                    dataBuffer.put(aint, k, i1);
                    dataBuffer.flip();

                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, l, width, j, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer);
                }

                if (mc.checkGLError("Dynamic Texture @ updateDynamicTexture @ indirect subImage2D")) {
                    DevUtils.printCurrentInvokeStack();
                }

                MemoryUtil.memFree(dataBuffer);

            }

            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            GL11.glFlush();

            if (this.clearable) {
                this.dynamicTextureData = new int[0];
            }
        }
    }
}
