package tech.konata.phosphate.screens.clickgui.settingrenderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.CheckRenderer;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.settings.BooleanSetting;

import java.awt.*;
import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2023/12/30
 */
public class BooleanRenderer extends SettingRenderer<BooleanSetting> {

    public BooleanRenderer(BooleanSetting settingIn) {
        super(settingIn);
    }

    final Animation anim = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(200));

    CheckRenderer checkRenderer = new CheckRenderer();

    @Override
    public double render(double mouseX, double mouseY, int dWheel) {

        CFontRenderer fr = FontManager.pf20;
        double rSize = fr.getHeight() + 2, rX = x;

        double checkBoxY = y + (fr.getHeight() + 8) * 0.5 - rSize * 0.5;

        if (anim.getValue() != 255)
            this.roundedRect(rX, checkBoxY, rSize, rSize, 3, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        this.roundedRectAccentColor(rX, checkBoxY, rSize, rSize, 3, (int) anim.run(this.setting.getValue() ? 255 : 0));

        checkRenderer.render(rX, checkBoxY - 0.5, rSize, 2.25, this.setting.getValue());

        fr.drawString(this.setting.getName().get(), x + rSize + 4, y + (fr.getHeight() + 8) * 0.5 - fr.getHeight() * 0.5 - 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        return fr.getHeight() + 8;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        double rSize = FontManager.pf20.getHeight() + 2, rX = x;

        if (isHovered(mouseX, mouseY, rX, y + (FontManager.pf20.getHeight() + 8) * 0.5 - rSize * 0.5, rSize, rSize))
            this.setting.toggle();

    }
}
