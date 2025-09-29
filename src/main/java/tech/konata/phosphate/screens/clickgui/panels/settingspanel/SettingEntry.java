package tech.konata.phosphate.screens.clickgui.panels.settingspanel;

import lombok.Getter;
import net.minecraft.util.Location;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.screens.clickgui.panels.SettingsPanel;

/**
 * @author IzumiiKonata
 * @since 2024/10/26 16:37
 */
public abstract class SettingEntry implements SharedRenderingConstants {

    public final String internalName;

    @Getter
    private final Localizable name;

    @Getter
    protected Location imgLocation;

    @Getter
    private final SettingsPanel.RenderValues renderValues = new SettingsPanel.RenderValues();

    @Getter
    private final ScrollText st = new ScrollText();

    public SettingEntry(final String internalName) {
        this.internalName = internalName;
        this.name = Localizable.of("entry." + internalName.toLowerCase() + ".name");
    }

    public abstract void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel);

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
    }
}
