package tritium.rendering.font;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import tritium.rendering.RGBA;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.other.MemoryTracker;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class TextureAtlas {
    
    private static final int ATLAS_SIZE = 2048;
    private static final int PADDING = 2;
    
    private int textureId;
    private int currentX = PADDING;
    private int currentY = PADDING;
    private int currentRowHeight = 0;
    
    private final List<AtlasRegion> regions = new ArrayList<>();
    
    public TextureAtlas() {
        this.init();
    }

    public void init() {
        MultiThreadingUtil.runOnMainThread(() -> {
            this.textureId = GL11.glGenTextures();
            GlStateManager.bindTexture(textureId);

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

            RenderSystem.linearFilter();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_ALPHA,
                    ATLAS_SIZE, ATLAS_SIZE, 0,
                    GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        });
    }
    
    public AtlasRegion upload(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (currentX + width + PADDING > ATLAS_SIZE) {
            currentX = PADDING;
            currentY += currentRowHeight + PADDING;
            currentRowHeight = 0;
        }
        
        if (currentY + height + PADDING > ATLAS_SIZE) {
            return null;
        }

        ByteBuffer buffer = imageToBuffer(image);
        
        GlStateManager.bindTexture(textureId);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 
                            currentX, currentY, 
                            width, height, 
                            GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, buffer);

        MemoryTracker.memFree(buffer);
        
        float u0 = (float) currentX / ATLAS_SIZE;
        float v0 = (float) currentY / ATLAS_SIZE;
        float u1 = (float) (currentX + width) / ATLAS_SIZE;
        float v1 = (float) (currentY + height) / ATLAS_SIZE;
        
        AtlasRegion region = new AtlasRegion(u0, v0, u1, v1, width, height);
        regions.add(region);
        
        currentX += width + PADDING;
        currentRowHeight = Math.max(currentRowHeight, height);
        
        return region;
    }
    
    private ByteBuffer imageToBuffer(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryTracker.memAlloc(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                buffer.put((byte) alpha);
            }
        }

        buffer.flip();
        return buffer;
    }
    
    public int getTextureId() {
        return textureId;
    }
    
    public void destroy() {
        GlStateManager.deleteTexture(textureId);
    }
    
    public static class AtlasRegion {
        public final float u0, v0, u1, v1;
        public final int width, height;
        
        public AtlasRegion(float u0, float v0, float u1, float v1, int width, int height) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.width = width;
            this.height = height;
        }
    }
}