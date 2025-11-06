package tritium.rendering.font;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

@RequiredArgsConstructor
public class Glyph {
    
    public final float width, height;
    public final int imgWidth, imgHeight;
    public final char value;
    public int textureId = -1;
    
    public int callList = -1;
    public boolean cached = true;

    public void init() {

        GlyphCache.GlyphSize gs = new GlyphCache.GlyphSize(this.width, this.height, this.imgWidth, this.imgHeight);

        Integer cachedCallList = GlyphCache.get(gs);
        if (cachedCallList != null) {

//            System.out.println("GlyphCache 命中: Width: " + gs.width + ", Height: " + gs.height);

            callList = cachedCallList;
            cached = true;
            return;
        }

        callList = GLAllocation.generateDisplayLists(1);
        cached = false;
        GlyphCache.CALL_LIST_COUNTER.set(GlyphCache.CALL_LIST_COUNTER.get() + 1);

        GL11.glNewList(this.callList, GL11.GL_COMPILE);

        float w = this.width;
        float h = this.height;

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, (float) this.height / this.imgHeight);
        GL11.glVertex2f(0, h);
        GL11.glTexCoord2f((float) this.width / this.imgWidth, 0);
        GL11.glVertex2f(w, 0);
        GL11.glTexCoord2f((float) this.width / this.imgWidth, (float) this.height / this.imgHeight);
        GL11.glVertex2f(w, h);
        GL11.glEnd();

        GL11.glEndList();

        GlyphCache.put(gs, callList);

    }

    public float render(double xOffset, double yOffset, float r2, float g2, float b2, float a) {
        if (this.value != ' ' && this.callList != -1 && textureId != -1) {

            GlStateManager.bindTexture(textureId);

            GlStateManager.color(r2, g2, b2, a);
//            GlStateManager.pushMatrix();
            GlStateManager.translate(xOffset, yOffset, 0);
            GlStateManager.callList(this.callList);
            GlStateManager.translate(-xOffset, -yOffset, 0);
//            GlStateManager.popMatrix();


        }

        return this.width;
    }
    
}