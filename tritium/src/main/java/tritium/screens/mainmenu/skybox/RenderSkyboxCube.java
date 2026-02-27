package tritium.screens.mainmenu.skybox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Location;
import net.minecraft.util.Matrix4f;
import org.lwjgl.opengl.GL11;
import tritium.rendering.rendersystem.RenderSystem;

public class RenderSkyboxCube {
    private final Location[] locations = new Location[6];

    public RenderSkyboxCube(Location texture) {
        for (int i = 0; i < 6; ++i) {
            this.locations[i] = Location.of(texture.getResourceDomain(), texture.getResourcePath() + '_' + i + ".png");
        }
    }

    public void render(Minecraft mc, float pitch, float yaw, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.multMatrix(Matrix4f.perspective(85.0D, (float) RenderSystem.getWidth() / (float) RenderSystem.getHeight(), 0.05F, 10.0F));
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GlStateManager.pushMatrix();
//        float f = -0.5F / 256.0F;
//        float f1 = -0.5F / 256.0F;
//        GlStateManager.translate(f, f1, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);

        for (int k = 0; k < 6; ++k) {
            mc.getTextureManager().bindTexture(this.locations[k]);
            RenderSystem.nearestFilter();
            wr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            int l = Math.round(255.0F * alpha);

            if (k == 0) {
                wr.pos(-1.0D, -1.0D, 1.0D).tex(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, 1.0D, 1.0D).tex(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, 1.0D, 1.0D).tex(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, -1.0D, 1.0D).tex(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 1) {
                wr.pos(1.0D, -1.0D, 1.0D).tex(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, 1.0D, 1.0D).tex(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, 1.0D, -1.0D).tex(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, -1.0D, -1.0D).tex(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 2) {
                wr.pos(1.0D, -1.0D, -1.0D).tex(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, 1.0D, -1.0D).tex(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, 1.0D, -1.0D).tex(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, -1.0D, -1.0D).tex(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 3) {
                wr.pos(-1.0D, -1.0D, -1.0D).tex(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, 1.0D, -1.0D).tex(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, 1.0D, 1.0D).tex(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, -1.0D, 1.0D).tex(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 4) {
                wr.pos(-1.0D, -1.0D, -1.0D).tex(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, -1.0D, 1.0D).tex(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, -1.0D, 1.0D).tex(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, -1.0D, -1.0D).tex(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 5) {
                wr.pos(-1.0D, 1.0D, 1.0D).tex(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
                wr.pos(-1.0D, 1.0D, -1.0D).tex(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, 1.0D, -1.0D).tex(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
                wr.pos(1.0D, 1.0D, 1.0D).tex(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            tessellator.draw();
        }

        GlStateManager.popMatrix();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
    }

}
