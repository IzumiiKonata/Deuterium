package tritium.screens.contextmenu;

import net.minecraft.client.Minecraft;
import org.lwjglx.input.Mouse;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.ThemeManager;
import tritium.rendering.Stencil;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;

import tritium.screens.ClickGui;
import tritium.settings.ClientSettings;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ContextMenu implements SharedRenderingConstants {

    public double posX, posY;
    public boolean closing = false;

    final List<ContextEntity> entities = new ArrayList<>();

    public boolean lmbPressed = false;

    public ContextMenu(double x, double y, List<ContextEntity> entities) {
        this.posX = x;
        this.posY = y;
        this.entities.addAll(entities);
    }

    public boolean shouldClose = false;

    Animation alphaAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(150L));

    public void render(double mouseX, double mouseY) {

        double entityWidth = this.getEntityWidth();
        double entityHeight = this.getEntityHeight();

        if (posY + entityHeight * entities.size() > RenderSystem.getHeight()) {
            posY = RenderSystem.getHeight() - entityHeight * entities.size();
        }

        double offsetX = posX;
        double offsetY = posY;

        alphaAnimation.run(1);

        float alpha = (float) alphaAnimation.getValue();
        int iAlpha = (int) (alpha * 255);

        roundedRect(posX, posY, entityWidth, entityHeight * entities.size(), 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, iAlpha));

        this.roundedOutline(posX, posY, entityWidth, entityHeight * entities.size(), 6, 1, ThemeManager.getAsColor(ThemeManager.ThemeColor.Text, (int) (60 * alpha)));

        for (int i = 0; i < entities.size(); i++) {
            ContextEntity entity = entities.get(i);

            if (isHovered(mouseX, mouseY, offsetX, offsetY, entityWidth, entityHeight) || entity.shouldBeSelected(mouseX, mouseY)) {
                entity.selectAlphaAnimation.run(1);
            } else {
                entity.selectAlphaAnimation.run(0);
            }

            int alp = (int) (60 * alpha * entity.selectAlphaAnimation.getValue());
            int i1 = ClientSettings.THEME.getValue() == ThemeManager.Theme.Light ? hexColor(0, 0, 0, alp) : hexColor(255, 255, 255, alp);

            if (alp > 2) {

                if (i != 0 && i != entities.size() - 1) {
                    Rect.draw(offsetX, offsetY, entityWidth, entityHeight, i1, Rect.RectType.EXPAND);
                } else {

                    Stencil.write();
                    Rect.draw(offsetX, offsetY, entityWidth, entityHeight, -1, Rect.RectType.EXPAND);
                    Stencil.erase();
                    roundedRect(posX, posY, entityWidth, entityHeight * entities.size(), 6, new Color(i1, true));
                    Stencil.dispose();
                }
            }

            entity.render(this, offsetX, offsetY, entityWidth, entityHeight, mouseX, mouseY, alpha, iAlpha);

            offsetY += entityHeight;

        }

        if (!this.isHovered(mouseX, mouseY) && Mouse.isButtonDown(0)) {

            for (ContextEntity entity : entities) {
                if (!entity.shouldClose(mouseX, mouseY)) {
                    return;
                }
            }

            shouldClose = true;
        }

        if (!Mouse.isButtonDown(0) && lmbPressed) {
            lmbPressed = false;
        }

    }

    public boolean isHovered(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY, posX, posY, this.getEntityWidth(), this.getEntityHeight() * entities.size());
    }

    private double getEntityWidth() {
        return 100;
    }

    private double getEntityHeight() {
        return 20;
    }

}
