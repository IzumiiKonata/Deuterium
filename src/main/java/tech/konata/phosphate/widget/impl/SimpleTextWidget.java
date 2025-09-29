package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import tech.konata.phosphate.interfaces.IFontRenderer;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.widget.Widget;

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
        boolean bFlagVanilla = GlobalSettings.HUD_STYLE.getValue() == GlobalSettings.HudStyle.Vanilla;
        IFontRenderer fr = bFlagVanilla ? mc.fontRendererObj : FontManager.pf25;

        String text = this.getText();

        double spacing = 6;

        NORMAL.add(() -> {
            GlStateManager.pushMatrix();
            this.doScale();
            this.renderStyledBackground(this.getX(), this.getY(), spacing * 2 + fr.getStringWidth(text), spacing * (2) + fr.getHeight(), 8);
            fr.drawString(text, this.getX() + spacing, this.getY() + spacing + (bFlagVanilla ? 1 : 0), -1);
            GlStateManager.popMatrix();
        });

        this.setWidth(spacing * 2 + fr.getStringWidth(text));
        this.setHeight(spacing * 2 + fr.getHeight());
    }

    public abstract String getText();

}
