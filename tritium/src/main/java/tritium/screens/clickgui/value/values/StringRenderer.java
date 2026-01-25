package tritium.screens.clickgui.value.values;

import org.lwjgl.input.Keyboard;
import tritium.management.FontManager;
import tritium.rendering.entities.impl.TextField;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.rendering.ui.widgets.TextFieldWidget;
import tritium.screens.ClickGui;
import tritium.settings.LabelSetting;
import tritium.settings.StringSetting;

import java.util.Objects;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:20
 */
public class StringRenderer extends AbstractWidget<StringRenderer> {

    private final StringSetting setting;

    public StringRenderer(StringSetting setting) {
        this.setting = setting;
        double height = FontManager.pf14.getHeight() + 4;
        this.setBounds(142, height * 2);

        LabelWidget label = new LabelWidget(() -> setting.getName().get(), FontManager.pf14);
        label.setBeforeRenderCallback(() -> {
            label.setColor(ClickGui.getColor(20));
        });
        label.setPosition(0, height * .5 - FontManager.pf14.getHeight() * .5);
        this.addChild(label);

        RectWidget rw = new RectWidget();
        this.addChild(rw);

        rw.setBeforeRenderCallback(() -> {
            rw.setPosition(0, height);
            rw.setBounds(this.getWidth(), height);
            rw.setColor(ClickGui.getColor(24));
        });

        TextFieldWidget tfw = new TextFieldWidget(FontManager.pf14);
        rw.addChild(tfw);

        tfw.setDisabledTextColor(0xFF808080);
        tfw.drawUnderline(false);

        tfw.setBeforeRenderCallback(() -> {
            tfw.setMargin(2);
            tfw.setColor(RenderSystem.reAlpha(ClickGui.getColor(20), this.getAlpha()));
            if (!Objects.equals(setting.getValue(), tfw.getText()))
                tfw.setText(setting.getValue());
        });

        tfw.setTextChangedCallback(setting::setValue);

        tfw.setOnKeyTypedCallback((character, keyCode) -> {

            if (tfw.isFocused()) {

                if (keyCode == Keyboard.KEY_ESCAPE) {
                    tfw.setFocused(false);
                    return true;
                }

            }

            return false;
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
    public void onRender(double mouseX, double mouseY) {

    }
}
