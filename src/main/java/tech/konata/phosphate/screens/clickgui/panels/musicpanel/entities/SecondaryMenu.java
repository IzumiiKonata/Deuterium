package tech.konata.phosphate.screens.clickgui.panels.musicpanel.entities;

import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextEntity;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextMenu;
import tech.konata.phosphate.settings.GlobalSettings;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SecondaryMenu extends ContextEntity {

    final List<ContextEntity> entities = new ArrayList<>();


    public SecondaryMenu(String label, List<ContextEntity> entities) {
        super(label);
        this.entities.addAll(entities);
    }

    double offsetX, posY, entityWidth, entityHeight;

    boolean open = false;

    ScrollText st = new ScrollText();

    Animation alphaAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(150L));

    @Override
    public void render(ContextMenu menu, double posX, double posY, double entityWidth, double entityHeight, double mouseX, double mouseY, float alpha, int iAlpha) {

        CFontRenderer fr = FontManager.pf20;

        st.render(fr, this.getLabel(), posX + 4, posY + entityHeight * 0.5 - fr.getHeight() * 0.5, entityWidth - 8, ThemeManager.get(ThemeManager.ThemeColor.Text, iAlpha));
//        fr.drawString("Open: " + open, posX + 4, posY + entityHeight * 0.5 + fr.getHeight() * 0.5, -1);

        if (isHovered(mouseX, mouseY, posX, posY, entityWidth, entityHeight)) {
            open = true;
        }

        if (posY + entityHeight * entities.size() > RenderSystem.getHeight()) {
            posY = RenderSystem.getHeight() - entityHeight * entities.size();
        }

        double offsetX = posX + entityWidth + 4;
        double offsetY = posY;

        alphaAnimation.run(open ? 1 : 0);

        alpha = (float) alphaAnimation.getValue();
        iAlpha = (int) (alpha * 255);

        if (alpha > 0.02f) {
            roundedRect(offsetX, posY, entityWidth, entityHeight * entities.size(), 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, iAlpha));

            this.roundedOutline(offsetX, posY, entityWidth, entityHeight * entities.size(), 6, 1, ThemeManager.getAsColor(ThemeManager.ThemeColor.Text, (int) (60 * alpha)));

            for (int i = 0; i < entities.size(); i++) {
                ContextEntity entity = entities.get(i);

                if (isHovered(mouseX, mouseY, offsetX, offsetY, entityWidth, entityHeight) || entity.shouldBeSelected(mouseX, mouseY)) {
                    entity.selectAlphaAnimation.run(1);
                } else {
                    entity.selectAlphaAnimation.run(0);
                }

                int alp = (int) (60 * alpha * entity.selectAlphaAnimation.getValue());
                int i1 = GlobalSettings.THEME.getValue() == ThemeManager.Theme.Light ? hexColor(0, 0, 0, alp) : hexColor(255, 255, 255, alp);

                if (alp > 2) {

                    if (i != 0 && i != entities.size() - 1) {
                        Rect.draw(offsetX, offsetY, entityWidth, entityHeight, i1, Rect.RectType.EXPAND);
                    } else {

                        Stencil.write();
                        Rect.draw(offsetX, offsetY, entityWidth, entityHeight, -1, Rect.RectType.EXPAND);
                        Stencil.erase();
                        roundedRect(offsetX, posY, entityWidth, entityHeight * entities.size(), 6, new Color(i1, true));
                        Stencil.dispose();
                    }
                }

                entity.render(menu, offsetX, offsetY, entityWidth, entityHeight, mouseX, mouseY, alpha, iAlpha);

                offsetY += entityHeight;

            }
        }

        if (!this.isHovered(mouseX, mouseY, offsetX - 8, posY, entityWidth + 16, entityHeight * entities.size())) {

            boolean allClose = true;
            for (ContextEntity entity : entities) {
                if (!entity.shouldClose(mouseX, mouseY)) {
                    allClose = false;
                }
            }

            if (allClose) {
                open = false;
            }
        }

        this.offsetX = offsetX;
        this.posY = posY;
        this.entityWidth = entityWidth;
        this.entityHeight = entityHeight;

    }

    @Override
    public boolean shouldClose(double mouseX, double mouseY) {

        boolean shouldThisClose = !this.isHovered(mouseX, mouseY, offsetX - 8, posY, entityWidth + 16, entityHeight * entities.size());

        if (shouldThisClose) {
            for (ContextEntity entity : entities) {
                if (!entity.shouldClose(mouseX, mouseY)) {
                    return false;
                }
            }
        }

        return shouldThisClose;
    }

    @Override
    public boolean shouldBeSelected(double mouseX, double mouseY) {

        boolean allClose = true;
        for (ContextEntity entity : entities) {
            if (!entity.shouldClose(mouseX, mouseY)) {
                allClose = false;
            }
        }

        return (open && this.isHovered(mouseX, mouseY, offsetX - 8, posY, entityWidth + 16, entityHeight * entities.size())) || !allClose;
    }
}
