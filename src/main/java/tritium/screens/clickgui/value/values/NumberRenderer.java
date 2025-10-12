package tritium.screens.clickgui.value.values;

import org.lwjglx.input.Mouse;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.settings.NumberSetting;

import java.awt.*;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 18:15
 */
public class NumberRenderer extends AbstractWidget<NumberRenderer> {

    private NumberSetting<?> setting;

    double sliderWidth = 0;

    public NumberRenderer(NumberSetting<?> setting) {
        this.setting = setting;
        double height = FontManager.pf14.getHeight() + 4 + 8;
        this.setBounds(142, height);

        LabelWidget label = new LabelWidget(() -> setting.getName().get(), FontManager.pf14);
        label.setBeforeRenderCallback(() -> {
            label.setColor(ClickGui.getColor(20));
        });
        label.setPosition(0, (FontManager.pf14.getHeight() + 4) * .5 - FontManager.pf14.getHeight() * .5);
        this.addChild(label);

        LabelWidget valueLabel = new LabelWidget(setting::getStringForRender, FontManager.pf14);
        valueLabel.setBeforeRenderCallback(() -> {
            valueLabel.setPosition(this.getWidth() - FontManager.pf14.getWidth(setting.getStringForRender()), (FontManager.pf14.getHeight() + 4) * .5 - FontManager.pf14.getHeight() * .5);
            valueLabel.setColor(ClickGui.getColor(20));
        });

        this.addChild(valueLabel);

        RectWidget background = new RectWidget() {

            boolean clicking = false;

            {
                this.setOnClickCallback((x, y, i) -> {
                    if (i == 0)
                        clicking = true;
                    return true;
                });
            }

            @Override
            public void onRender(double mouseX, double mouseY, int dWheel) {
                super.onRender(mouseX, mouseY, dWheel);

                if (!Mouse.isButtonDown(0) && clicking)
                    clicking = false;

                if (this.isHovered(mouseX, mouseY, this.getX() - 2, this.getY() - 2, this.getWidth() + 4, this.getHeight() + 4) && clicking) {
                    double render = setting.getMinimum().doubleValue();
                    double max = setting.getMaximum().doubleValue();
                    double inc = setting.getIncrement().doubleValue();
                    double valAbs = mouseX - this.getX();
                    double perc = valAbs / NumberRenderer.this.getWidth();
                    perc = Math.min(Math.max(0.0D, perc), 1.0D);
                    double valRel = (max - render) * perc;
                    double val = render + valRel;
                    val = (double) Math.round(val * (1.0D / inc)) / (1.0D / inc);

                    setting.loadValue(String.valueOf(val));
                }
            }
        };

        background.setBounds(this.getWidth(), 2);
        background.setPosition(0, FontManager.pf14.getHeight() + 8);
        background.setColor(ClickGui.getColor(25));
        background.setBeforeRenderCallback(() -> {
            background.setColor(ClickGui.getColor(25));
        });

        this.addChild(background);

        RectWidget slider = new RectWidget() {
            @Override
            public void onRender(double mouseX, double mouseY, int dWheel) {
                super.onRender(mouseX, mouseY, dWheel);

                double r = 8.5;
                NumberRenderer.this.roundedRect(this.getX() + this.getWidth() - r * .5, this.getY() + this.getHeight() * .5 - r * .5, r, r, r * .5 - 1, RenderSystem.reAlpha(ClickGui.getColor(0), this.getAlpha()));
                r = 8;
                NumberRenderer.this.roundedRect(this.getX() + this.getWidth() - r * .5, this.getY() + this.getHeight() * .5 - r * .5, r, r, r * .5 - 1, RenderSystem.reAlpha(ClickGui.getColor(26), this.getAlpha()));
            }
        };

        slider.setBounds(this.getWidth(), 2);
        background.addChild(slider);
        slider.setClickable(false);

        slider.setBeforeRenderCallback(() -> {
            sliderWidth = Interpolations.interpBezier(sliderWidth, this.getWidth() * (setting.getValue().doubleValue() - setting.getMinimum().doubleValue()) / (setting.getMaximum().doubleValue() - setting.getMinimum().doubleValue()), 0.2);
            slider.setBounds(sliderWidth, 2);
            slider.setColor(ClickGui.getColor(27));
        });

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
