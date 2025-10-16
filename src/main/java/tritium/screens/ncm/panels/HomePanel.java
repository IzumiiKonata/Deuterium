package tritium.screens.ncm.panels;

import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ncm.NCMPanel;
import tritium.screens.ncm.NCMScreen;

/**
 * @author IzumiiKonata
 * Date: 2025/10/16 19:50
 */
public class HomePanel extends NCMPanel {

    @Override
    public void onInit() {
        this.getChildren().clear();

        this.layout();
    }

    public double getScaleFactor() {
        return 1.5;
    }

    public double getPanelWidth() {
        return 108 * getScaleFactor();
    }

    public double getPanelHeight() {
        return 234 * getScaleFactor();
    }

    private void layout() {
        this.setBounds(this.getPanelWidth(), this.getPanelHeight());

        RectWidget bg = new RectWidget();
        this.addChild(bg);

        this.setBeforeRenderCallback(() -> {
            this.center();

            bg.setMargin(0);
            bg.setColor(this.getColor(NCMScreen.ColorType.GENERIC_BACKGROUND));
        });

        this.genNavigateBar();
    }

    private void genNavigateBar() {
        Panel navigateBar = new Panel();

        navigateBar.setBounds(this.getPanelWidth(), this.getPanelHeight() * 0.069);

        RectWidget bg = new RectWidget();
        navigateBar.addChild(bg);

        navigateBar.setBeforeRenderCallback(() -> {
            navigateBar.setPosition(0, this.getPanelHeight() - navigateBar.getHeight());

            bg.setMargin(0);
            bg.setColor(this.getColor(NCMScreen.ColorType.GENERIC_BACKGROUND));
        });

        this.addChild(navigateBar);
    }

}
