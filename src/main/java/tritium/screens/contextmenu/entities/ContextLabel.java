package tritium.screens.contextmenu.entities;

import org.lwjglx.input.Mouse;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.entities.impl.ScrollText;
import tritium.rendering.font.CFontRenderer;
import tritium.screens.ClickGui;
import tritium.screens.contextmenu.ContextEntity;
import tritium.screens.contextmenu.ContextMenu;

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
        }

    }

}
