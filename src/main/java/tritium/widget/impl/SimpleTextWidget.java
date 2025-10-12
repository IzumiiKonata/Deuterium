package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import tritium.interfaces.IFontRenderer;
import tritium.management.FontManager;
import tritium.settings.ClientSettings;
import tritium.widget.Widget;
import tritium.interfaces.SharedRenderingConstants;

/**
 * @author IzumiiKonata
 * @since 2024/9/29 18:53
 */
public abstract class SimpleTextWidget extends Widget {

    public SimpleTextWidget(String name) {
        super(name);
    }

    @Override
    public void onRender(boolean editing) {
        IFontRenderer fr = FontManager.pf16;

        String text = this.getText();

        double spacing = 4;

        SharedRenderingConstants.NORMAL.add(() -> {
            GlStateManager.pushMatrix();
            this.doScale();
            this.renderStyledBackground(this.getX(), this.getY(), spacing * 2 + fr.getStringWidth(text), spacing * (2) + fr.getHeight(), 8);
            fr.drawString(text, this.getX() + spacing, this.getY() + spacing, -1);
            GlStateManager.popMatrix();
        });

        this.setWidth(spacing * 2 + fr.getStringWidth(text));
        this.setHeight(spacing * 2 + fr.getHeight());
    }

    public abstract String getText();

}
