package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import tritium.interfaces.IFontRenderer;
import tritium.management.FontManager;
import tritium.rendering.Stencil;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;

import tritium.settings.BooleanSetting;
import tritium.settings.ClientSettings;
import tritium.widget.Widget;
import tritium.interfaces.SharedRenderingConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/8/22 21:45
 */
public class PotionDisplay extends Widget {

    public PotionDisplay() {
        super("PotionDisplay");
    }

    public BooleanSetting highlight = new BooleanSetting("Time reminder", true);

    final PotionEffect pe = new PotionEffect(16, 1000);

    @Override
    public void onRender(boolean editing) {

        double x = this.getX(), y = this.getY();

        double width = 120;
        double height = 40;
        double spacing = 4;
        double verticalSpacing = 8;

        double texSize = height - spacing * 2;

        List<PotionEffect> potionList = new ArrayList<>(mc.thePlayer.getActivePotionEffects());

        if (editing && potionList.isEmpty()) {
            pe.setPotionDurationMax(true);
            potionList.add(pe);
        }

        for (PotionEffect effect : potionList) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

//            this.roundedRectAccentColor(x, y, width, height, 8, this.getFadeAlpha());

            double finalY = y;
            SharedRenderingConstants.NORMAL.add(() -> {

                GlStateManager.pushMatrix();
                this.doScale();

                this.renderStyledBackground(x, finalY, width, height, 8);

                if (!effect.getIsPotionDurationMax()) {

                    Rect.draw(x, finalY, width * ((double) effect.getDuration() / effect.totalDuration), height, hexColor(255, 255, 255, 80), Rect.RectType.EXPAND);

                }

                if (potion.hasStatusIcon()) {
                    int iconIndex = potion.getStatusIconIndex();
                    GlStateManager.enableBlend();
                    GlStateManager.disableAlpha();

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);

                    this.mc.getTextureManager().bindTexture(Location.of("textures/gui/container/inventory.png"));
                    this.drawTexturedModalRect(x + spacing, finalY + spacing, texSize, texSize, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18);
//                    GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                }

                String potionName = I18n.format(potion.getName());

                if (effect.getAmplifier() == 1) {
                    potionName = potionName + " " + I18n.format("enchantment.level.2");
                } else if (effect.getAmplifier() == 2) {
                    potionName = potionName + " " + I18n.format("enchantment.level.3");
                } else if (effect.getAmplifier() == 3) {
                    potionName = potionName + " " + I18n.format("enchantment.level.4");
                }

                IFontRenderer fr = true ? mc.fontRendererObj : FontManager.pf20bold;

                fr.drawString(potionName, x + spacing + texSize + spacing, finalY + spacing * 2, -1);
                String duration = Potion.getDurationString(effect);

                fr.drawString(duration, x + spacing + texSize + spacing, finalY + spacing * 2 + fr.getHeight() + spacing, hexColor(255, 255, 255, 180));


                if (highlight.getValue() && !effect.getIsPotionDurationMax()) {
                    int sec = effect.getDuration() / 20;
                    int min = sec / 60;
                    sec = sec % 60;

                    double offset = 2;

                    if (sec <= 15 && min == 0) {

                        float speed = 0.2f;

                        if (effect.fade) {
                            effect.hightlightAlpha = Interpolations.interpBezier(effect.hightlightAlpha, 0.0F, speed);

                            if (effect.hightlightAlpha < 0.02f) {
                                effect.fade = false;
                            }
                        } else {
                            effect.hightlightAlpha = Interpolations.interpBezier(effect.hightlightAlpha, 1.0F, speed);

                            if (effect.hightlightAlpha > 0.98f) {
                                effect.fade = true;
                            }
                        }

                        this.roundedOutline(x - offset, finalY - offset, width + offset * 2, height + offset * 2, true ? 3 : 11, 1.5, 1, new Color(1, 1, 1, effect.hightlightAlpha));
                    }
                }

                GlStateManager.popMatrix();
            });

            y += height + verticalSpacing;
        }

        this.setWidth(width);
        this.setHeight(y - this.getY() - verticalSpacing);

    }

    public void drawTexturedModalRect(double x, double y, double width, double height, double textureX, double textureY, double texWidth, double texHeight) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;

        float zLevel = 1.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, zLevel).tex((float) (textureX) * f, (float) (textureY + texHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, zLevel).tex((float) (textureX + texWidth) * f, (float) (textureY + texHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, zLevel).tex((float) (textureX + texWidth) * f, (float) (textureY) * f1).endVertex();
        worldrenderer.pos(x, y, zLevel).tex((float) (textureX) * f, (float) (textureY) * f1).endVertex();
        tessellator.draw();
    }

}
