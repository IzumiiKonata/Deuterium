package net.minecraft.client.renderer.texture;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryUtil;
import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.utils.other.DevUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;

@Getter
public class DynamicTexture extends AbstractTexture {
    public int[] dynamicTextureData;

    /**
     * width of this icon in pixels
     */
    protected int width;

    /**
     * height of this icon in pixels
     */
    protected int height;

    @Getter
    @Setter
    protected boolean clearable = true;

    public DynamicTexture(BufferedImage bufferedImage) {
        this(bufferedImage.getWidth(), bufferedImage.getHeight());
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
        this.updateDynamicTexture();
    }

    public DynamicTexture(BufferedImage bufferedImage, boolean clearable) {
        this(bufferedImage.getWidth(), bufferedImage.getHeight());
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
        this.clearable = clearable;
        this.updateDynamicTexture();
    }

    public DynamicTexture(BufferedImage bufferedImage, boolean clearable, boolean linear) {
        this(bufferedImage.getWidth(), bufferedImage.getHeight());
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
        this.clearable = clearable;
        this.linear = linear;
        this.updateDynamicTexture();
    }

    public DynamicTexture(int textureWidth, int textureHeight) {
        this.width = textureWidth;
        this.height = textureHeight;
        this.dynamicTextureData = new int[textureWidth * textureHeight];
        this.allocateTexture(textureWidth, textureHeight);
    }

    public void allocateTexture(int textureWidth, int textureHeight) {
        this.allocateTextureImpl(0, textureWidth, textureHeight);
    }

    public void allocateTextureImpl(int levels, int width, int height) {

        this.deleteTexture();
        this.bindTexture();

        if (levels >= 0) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, levels);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, (float) levels);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
        }

        for (int i = 0; i <= levels; ++i) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
        }

    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
    }

    public void updateDynamicTexture(BufferedImage img) {

        if (img == null) {
            System.err.println("img == null?!");
            DevUtils.printCurrentInvokeStack();
            return;
        }

        if (this.dynamicTextureData.length != img.getWidth() * img.getHeight()) {
            this.width = img.getWidth();
            this.height = img.getHeight();
            this.dynamicTextureData = new int[img.getWidth() * img.getHeight()];
            this.deleteTexture();
            this.glTextureId = -1;
            TextureUtil.allocateTexture(this.getGlTextureId(), img.getWidth(), img.getHeight());
        }

        img.getRGB(0, 0, img.getWidth(), img.getHeight(), this.dynamicTextureData, 0, img.getWidth());

        Minecraft mc = Minecraft.getMinecraft();

        TextureUtil.allocateTexture(this.getGlTextureId(), img.getWidth(), img.getHeight());

        if (mc.checkGLError("Dynamic Texture @ allocateTexture")) {
            DevUtils.printCurrentInvokeStack();
        }

        this.updateDynamicTexture();

    }

    @Getter
    @Setter
    protected boolean linear = true;

    @SneakyThrows
    public synchronized void updateDynamicTexture() {

//        synchronized (AsyncGLContext.MULTITHREADING_LOCK) {
            this.bindTexture();
            TextureUtil.setTextureBlurMipmap(isLinear(), false);
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

                IntBuffer buffer = MemoryUtil.memAllocInt(this.dynamicTextureData.length + padd);
                buffer.clear();
                buffer.put(this.dynamicTextureData, 0, this.dynamicTextureData.length);
                buffer.flip();

                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

                MemoryUtil.memFree(buffer);

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

            if (this.clearable) {
                this.dynamicTextureData = new int[0];
            }
//        }
    }

    public int[] getTextureData() {
        return this.dynamicTextureData;
    }
}
