package tritium.rendering.ui.widgets;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.settings.ClientSettings;

import java.awt.*;

/**
 * @author IzumiiKonata
 * Date: 2025/10/8 19:13
 */
public class IconWidget extends AbstractWidget<IconWidget> {

    @Getter
    @Setter
    private String icon;

    public CFontRenderer fr;

    public IconWidget(String icon, CFontRenderer fr, double x, double y, double width, double height) {
        this.icon = icon;
        this.fr = fr;
        this.setBounds(x, y, width, height);
    }

    float alphaAnim = 0f, alphaAnim2 = 0f;
    public double fontOffsetX = 0, fontOffsetY = 0;

    boolean run = false;

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        GlStateManager.disableAlpha();

        int alpha = (int) (this.getAlpha() * 255);

        double size = this.getWidth() * .5;

        if (alphaAnim != 0f) {
            int a = Math.min((int) (alphaAnim * 255), alpha);
            roundedRect(this.getX() + this.getWidth() * 0.5 - size, this.getY() + this.getHeight() * 0.5 - size, size * 2, size * 2, size - 0.5, ClientSettings.THEME.getValue() == ThemeManager.Theme.Light ? new Color(0, 0, 0, a) : new Color(255, 255, 255, a));
        }

        if (alphaAnim2 != 0f) {
            int a = Math.min((int) (alphaAnim2 * 255), alpha);
            roundedRect(this.getX() + this.getWidth() * 0.5 - size, this.getY() + this.getHeight() * 0.5 - size, size * 2, size * 2, size - 0.5, ClientSettings.THEME.getValue() == ThemeManager.Theme.Light ? new Color(0, 0, 0, a) : new Color(255, 255, 255, a));
        }

        if (run) {
            alphaAnim2 = Interpolations.interpBezier(alphaAnim2, 40 * RenderSystem.DIVIDE_BY_255, 0.2f);

            if (Math.abs(alphaAnim2 - 40 * RenderSystem.DIVIDE_BY_255) < 0.05f) {
                run = false;
            }
        } else {
            alphaAnim2 = Interpolations.interpBezier(alphaAnim2, 0, 0.2f);
        }

        if (this.isHovering()) {
            alphaAnim = Interpolations.interpBezier(alphaAnim, 40 * RenderSystem.DIVIDE_BY_255, 0.2f);
        } else {
            alphaAnim = Interpolations.interpBezier(alphaAnim, 0, 0.2f);
        }

        int w = fr.getStringWidth(icon);
        double h = fr.getFontHeight();

        fr.drawString(icon, this.getX() + this.getWidth() * 0.5 - w * 0.5 + fontOffsetX, this.getY() + this.getHeight() * 0.5 - h * 0.5 + fontOffsetY, ThemeManager.get(ThemeManager.ThemeColor.Text, alpha));
    }

    @Override
    public boolean onMouseClicked(double relativeX, double relativeY, int mouseButton) {

        this.run = true;

        return super.onMouseClicked(relativeX, relativeY, mouseButton);
    }
}
