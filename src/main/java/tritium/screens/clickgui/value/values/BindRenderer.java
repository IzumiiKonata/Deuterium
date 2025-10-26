package tritium.screens.clickgui.value.values;

import tritium.management.FontManager;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.screens.ClickGui;
import tritium.settings.BindSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:20
 */
public class BindRenderer extends AbstractWidget<BindRenderer> {

    private final BindSetting setting;

    public BindRenderer(BindSetting setting) {
        this.setting = setting;
        double height = FontManager.pf14.getHeight() + 4;
        this.setBounds(142, height);

        LabelWidget label = new LabelWidget(() -> setting.getName().get(), FontManager.pf14);
        label.setBeforeRenderCallback(() -> {
            label.setColor(ClickGui.getColor(20));
        });
        label.setPosition(0, height * .5 - FontManager.pf14.getHeight() * .5);
        this.addChild(label);
    }

    @Override
    public double getHeight() {

        this.setHidden(!setting.shouldRender());

        if (!setting.shouldRender())
            return 0;
        return super.getHeight();
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {

    }
}
