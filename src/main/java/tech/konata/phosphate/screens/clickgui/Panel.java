package tech.konata.phosphate.screens.clickgui;

import lombok.Getter;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
@Getter
public abstract class Panel implements SharedRenderingConstants {

    private final String internalName;

    private final Localizable name;

    public double posX, posY, width, height;


    public Panel(String internalName) {
        this.internalName = internalName;

        this.name = Localizable.of("panel." + internalName.toLowerCase() + ".name");
    }

    public abstract void init();

    public abstract void onSwitchedTo();

    public abstract void draw(double mouseX, double mouseY, int dWheel);

    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {

    }

    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {

    }

}
