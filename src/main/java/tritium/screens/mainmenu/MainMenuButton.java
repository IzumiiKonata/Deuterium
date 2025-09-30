package tritium.screens.mainmenu;

import lombok.Getter;
import tritium.utils.i18n.Localizable;
import tritium.interfaces.SharedRenderingConstants;

/**
 * @author IzumiiKonata
 * @since 2024/11/12 20:57
 */
public class MainMenuButton implements SharedRenderingConstants {

    @Getter
    private final Localizable label;
    @Getter
    private final ClickHandler handler;

    public float hoveredAlpha = 0.0f;

    public MainMenuButton(Localizable label, ClickHandler clickHandler) {
        this.label = label;
        this.handler = clickHandler;
    }

    public void draw() {

    }

    public interface ClickHandler {

        void onClick(MainMenuButton button);

    }

}
