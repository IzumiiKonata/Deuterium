package tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Language;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.Localizer;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.SettingEntry;
import tech.konata.phosphate.settings.GlobalSettings;

import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/10/26 16:41
 */
public class LanguageEntry extends SettingEntry {


    public LanguageEntry() {
        super("Language");
        super.imgLocation = Location.of(Phosphate.NAME + "/textures/settings/language.svg");
    }

    boolean lmbPressed = false;

    @Override
    public void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {
        List<Language> languages = Localizer.getLanguages();

        double offsetY = posY;
        double langHeight = 40;
        double spacing = 4;

        double flagSpacing = 4;
        double flagHeight = langHeight - flagSpacing * 2;
        double flagWidth = flagHeight / 0.666015625;

        for (Language language : languages) {

            if (Localizer.getLANG() == language) {
                this.roundedRectAccentColor(posX, offsetY, width, langHeight, 6);
            } else {
                roundedRect(posX, offsetY, width, langHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));
            }

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            Location flagLocation = this.getFlagLocation(language);
            Minecraft.getMinecraft().getTextureManager().bindTexture(flagLocation);
            roundedRectTextured(posX + flagSpacing, offsetY + flagSpacing, flagWidth, flagHeight, 4);

            CFontRenderer frBold = FontManager.pf25bold;

            double textX = posX + flagSpacing * 3 + flagWidth;
            frBold.drawString(language.getName(), textX, offsetY + langHeight * 0.5 - frBold.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

            boolean hovered = isHovered(mouseX, mouseY, posX, offsetY, width, langHeight);

            if (hovered && Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;
                GlobalSettings.LANG.setValue(language.getName());
            }

            offsetY += langHeight + spacing;
        }

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;
    }

    private Location getFlagLocation(Language language) {

        if (language.getName().equals("中文 (简体)"))
            return Location.of(Phosphate.NAME + "/textures/flag/cn.png");

        if (language.getName().equals("English (US)"))
            return Location.of(Phosphate.NAME + "/textures/flag/us.png");

        return null;
    }

}
