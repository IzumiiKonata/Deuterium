package tritium.widget.impl;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.boss.BossStatus;
import tritium.settings.BooleanSetting;
import tritium.widget.Widget;

/**
 * @author IzumiiKonata
 * Date: 2025/10/20 19:31
 */
public class BossBar extends Widget {

    public BooleanSetting hide = new BooleanSetting("Hide", false);

    public BossBar() {
        super("BossBar");
    }

    @Override
    public void onRender(boolean editing) {
        this.setWidth(182);
        this.setHeight(18);

        if (hide.getValue())
            return;

        String bossName = editing ? "Boss Name" : BossStatus.bossName;

        if (BossStatus.statusBarTime > 0 || editing) {
            --BossStatus.statusBarTime;
            int j = 182;
            int l = (int) (BossStatus.healthScale * (float) (j + 1));
            mc.getTextureManager().bindTexture(Gui.icons);
            this.drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 74, j, 5);
            this.drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 74, j, 5);

            if (l > 0) {
                this.drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 79, l, 5);
            }

            mc.fontRendererObj.drawStringWithShadow(bossName, this.getX() + this.getWidth() * .5 - mc.fontRendererObj.getStringWidth(bossName) * .5, this.getY() + 2, 16777215);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void drawTexturedModalRect(double x, double y, int textureX, int textureY, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0).tex((float) (textureX) * f, (float) (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0).tex((float) (textureX + width) * f, (float) (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0).tex((float) (textureX + width) * f, (float) (textureY) * f1).endVertex();
        worldrenderer.pos(x, y, 0).tex((float) (textureX) * f, (float) (textureY) * f1).endVertex();
        tessellator.draw();
    }
}
