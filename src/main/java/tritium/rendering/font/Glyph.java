package tritium.rendering.font;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

@RequiredArgsConstructor
public class Glyph {

    public final int width, height;
    public final char value;
    public int textureId = -1;

    public int vboId = -1;
    public boolean cached = true;

    private static final java.util.Map<GlyphCache.GlyphSize, Integer> VBO_CACHE = new java.util.HashMap<>();

    public void init() {
        if (true)
            return;
        GlyphCache.GlyphSize gs = new GlyphCache.GlyphSize(this.width, this.height);

        // 检查缓存
        Integer cachedVbo = VBO_CACHE.get(gs);
        if (cachedVbo != null) {
            vboId = cachedVbo;
            cached = true;
            return;
        }

        vboId = GL15.glGenBuffers();
        cached = false;

        float w = this.width;
        float h = this.height;

        // position (2) + texCoord (2) = 4 floats per vertex, 6 vertices (2 triangles)
        float[] vertices = {
                0, h,  0, 1,  // bottom-left
                w, h,  1, 1,  // bottom-right
                w, 0,  1, 0,  // top-right

                w, 0,  1, 0,  // top-right
                0, 0,  0, 0,  // top-left
                0, h,  0, 1   // bottom-left
        };

        FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        VBO_CACHE.put(gs, vboId);
    }

    public float render(float xOffset, float yOffset, float r2, float g2, float b2, float a) {
        if (this.value != ' ' && this.vboId != -1 && textureId != -1) {
            GlStateManager.bindTexture(textureId);
            GlStateManager.color(r2, g2, b2, a);
            GlStateManager.pushMatrix();
            GlStateManager.translate(xOffset, yOffset, 0);

            // 使用 VBO 渲染
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

            GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, 0);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 16, 8);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            GlStateManager.popMatrix();
        }

        return this.width;
    }

    public void cleanup() {
        if (vboId != -1 && !cached) {
            GL15.glDeleteBuffers(vboId);
        }
        if (textureId != -1) {
            GlStateManager.deleteTexture(textureId);
        }
    }
}