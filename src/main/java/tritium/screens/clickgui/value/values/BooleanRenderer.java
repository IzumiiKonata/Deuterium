package tritium.screens.clickgui.value.values;

import tritium.management.FontManager;
import tritium.rendering.CheckRenderer;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.rendering.ui.widgets.RoundedRectWidget;
import tritium.screens.ClickGui;
import tritium.settings.BooleanSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 17:52
 */
public class BooleanRenderer extends AbstractWidget<BooleanRenderer> {

    private final BooleanSetting setting;

    public BooleanRenderer(BooleanSetting setting) {
        this.setting = setting;
        double height = FontManager.pf14.getHeight() + 4;
        this.setBounds(142, height);

        LabelWidget label = new LabelWidget(() -> setting.getName().get(), FontManager.pf14);
        label.setBeforeRenderCallback(() -> {
            label.setColor(ClickGui.getColor(20));
        });
        label.setPosition(0, height * .5 - FontManager.pf14.getHeight() * .5);
        this.addChild(label);

        RoundedRectWidget rect = new RoundedRectWidget() {

            CheckRenderer cr = new CheckRenderer();

            @Override
            public void onRender(double mouseX, double mouseY, int dWheel) {
                super.onRender(mouseX, mouseY, dWheel);

                cr.render(this.getX(), this.getY(), this.getWidth(), 2, setting.getValue());
            }
        };

        rect.setBounds(this.getWidth() - height, 0, height, height);
        rect.setBeforeRenderCallback(() -> {
            rect.setColor(rect.isHovering() ? ClickGui.getColor(22) : ClickGui.getColor(21));
        });
        rect.setRadius(2);
        rect.setOnClickCallback((relativeX, relativeY, mouseButton) -> {
            if (mouseButton == 0) {
                setting.toggle();
            }
            return true;
        });

        this.addChild(rect);
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {

    }
}
