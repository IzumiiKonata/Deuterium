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
        label
                .setClickable(false)
                .setPosition(0, height * .5 - FontManager.pf14.getHeight() * .5)
                .setBeforeRenderCallback(() -> {
                    label.setColor(ClickGui.getColor(20));
                });
        this.addChild(label);

        RoundedRectWidget rect = new RoundedRectWidget() {
            final CheckRenderer cr = new CheckRenderer();

            @Override
            public void onRender(double mouseX, double mouseY) {
                super.onRender(mouseX, mouseY);

                cr.render(this.getX(), this.getY(), this.getWidth(), 2, setting.getValue(), reAlpha(ClickGui.getColor(20), this.getAlpha()));
            }
        };

        rect
            .setShouldSetMouseCursor(true)
            .setBounds(this.getWidth() - height, 0, height, height)
            .setBeforeRenderCallback(() -> {
                rect.setColor(rect.isHovering() ? ClickGui.getColor(22) : ClickGui.getColor(21));
            })
            .setRadius(2)
            .setOnClickCallback((relativeX, relativeY, mouseButton) -> {
                if (mouseButton == 0) {
                    setting.toggle();
                }
                return true;
            });

        this.addChild(rect);
    }

    @Override
    public double getHeight() {

        this.setHidden(!setting.shouldRender());

        if (!setting.shouldRender())
            return 0;
        return super.getHeight();
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

    }
}
