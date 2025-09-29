package tech.konata.phosphate.screens.clickgui.panels.musicpanel.entities;

import org.lwjglx.input.Mouse;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextEntity;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextMenu;
import tech.konata.phosphate.utils.timing.Timer;

public class ContextLabel extends ContextEntity {

    final Runnable onClick;

    public ContextLabel(String label, Runnable onClick) {
        super(label);
        this.onClick = onClick;
    }

    ScrollText st = new ScrollText();

    @Override
    public void render(ContextMenu menu, double x, double y, double width, double height, double mouseX, double mouseY, float alpha, int iAlpha) {

        CFontRenderer fr = FontManager.pf20;

        st.render(fr, this.getLabel(), x + 4, y + height * 0.5 - fr.getHeight() * 0.5, width - 8, ThemeManager.get(ThemeManager.ThemeColor.Text, iAlpha));

        if (isHovered(mouseX, mouseY, x, y, width, height) && Mouse.isButtonDown(0) && !menu.lmbPressed) {
            menu.lmbPressed = true;

            this.onClick.run();
            ClickGui.getInstance().musicPanel.rightClickMenu = null;
            ClickGui.getInstance().musicPanel.lmbPressed = true;
            ClickGui.getInstance().musicPanel.clickResistTimer.reset();

        }

    }

}
