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
    public final int width, height;
    public final char value;

    public float u0, v0, u1, v1;
    public boolean uploaded = false;


    public void setAtlasRegion(TextureAtlas.AtlasRegion region) {
        this.u0 = region.u0;
        this.v0 = region.v0;
        this.u1 = region.u1;
        this.v1 = region.v1;
        this.uploaded = true;
    }
}