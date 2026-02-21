package tritium.rendering.entities.clickable.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;

import java.awt.*;

public class ClickableIcon extends ClickEntity {

    @Getter
    @Setter
    private String icon;

    private CFontRenderer fr;

    public ClickableIcon(String icon, CFontRenderer fr, double x, double y, double width, double height,
                         Runnable click, Runnable hold, Runnable focus, Runnable release, Runnable onBlur) {
        super(x, y, width, height, MouseBounds.CallType.Expand, click, hold, focus, release, onBlur);
        this.icon = icon;
        this.fr = fr;
    }

    public ClickableIcon(String icon, double x, double y, double width, double height,
                         Runnable click, Runnable hold, Runnable focus, Runnable release, Runnable onBlur) {
        this(icon, FontManager.icon30, x, y, width, height, click, hold, focus, release, onBlur);
    }

    float alphaAnim = 0f, alphaAnim2 = 0f;

    boolean run = false;

    public void draw(double mouseX, double mouseY) {

        GlStateManager.disableAlpha();

        double size = 12;

        if (alphaAnim != 0f)
            roundedRect(this.getX() + this.getWidth() * 0.5 - size, this.getY() + this.getHeight() * 0.5 - size, size * 2, size * 2, size - 0.5, ClientSettings.THEME.getValue() == ThemeManager.Theme.Light ? new Color(0, 0, 0, (int) (alphaAnim * 255)) : new Color(255, 255, 255, (int) (alphaAnim * 255)));

        if (alphaAnim2 != 0f)
            roundedRect(this.getX() + this.getWidth() * 0.5 - size, this.getY() + this.getHeight() * 0.5 - size, size * 2, size * 2, size - 0.5, ClientSettings.THEME.getValue() == ThemeManager.Theme.Light ? new Color(0, 0, 0, (int) (alphaAnim2 * 255)) : new Color(255, 255, 255, (int) (alphaAnim2 * 255)));

//        RenderSystem.circle(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, smallFr ? 8 : 12, GlobalSettings.THEME.getValue() == ThemeManager.Theme.Light ? hexColor(0, 0, 0, (int) (alphaAnim * 255)) : hexColor(255, 255, 255, (int) (alphaAnim * 255)));
//        RenderSystem.circle(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, smallFr ? 8 : 12, GlobalSettings.THEME.getValue() == ThemeManager.Theme.Light ? hexColor(0, 0, 0, (int) (alphaAnim2 * 255)) : hexColor(255, 255, 255, (int) (alphaAnim2 * 255)));

        if (run) {
            alphaAnim2 = Interpolations.interpolate(alphaAnim2, 40 * RenderSystem.DIVIDE_BY_255, 0.2f);

            if (Math.abs(alphaAnim2 - 40 * RenderSystem.DIVIDE_BY_255) < 0.05f) {
                run = false;
            }
        } else {
            alphaAnim2 = Interpolations.interpolate(alphaAnim2, 0, 0.2f);
        }

        if (isInBounds(mouseX, mouseY)) {
            alphaAnim = Interpolations.interpolate(alphaAnim, 40 * RenderSystem.DIVIDE_BY_255, 0.2f);
        } else {
            alphaAnim = Interpolations.interpolate(alphaAnim, 0, 0.2f);
        }

        int w = fr.getStringWidth(icon);
        double h = fr.getHeight();

        fr.drawString(icon, this.getX() + this.getWidth() * 0.5 - w * 0.5, this.getY() + this.getHeight() * 0.5 - h * 0.5, ThemeManager.getActual(ThemeManager.ThemeColor.Text));

        super.tick(mouseX, mouseY);
    }

    @Override
    public void onClick() {
        run = true;
    }
}
