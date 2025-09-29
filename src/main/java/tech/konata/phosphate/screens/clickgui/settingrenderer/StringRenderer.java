package tech.konata.phosphate.screens.clickgui.settingrenderer;

import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.entities.impl.TextField;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.settings.StringSetting;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 20:50
 */
public class StringRenderer extends SettingRenderer<StringSetting> {

    public StringRenderer(StringSetting settingIn) {
        super(settingIn);
        tf.setText(settingIn.getValue());
    }

    TextField tf = new TextField(0, 0, 0, 100, 10);

    @Override
    public double render(double mouseX, double mouseY, int dWheel) {

        CFontRenderer pf20 = FontManager.pf20;
        pf20.drawString(this.setting.getName().get(), x, y, ThemeManager.get(ThemeManager.ThemeColor.Text));

        roundedRect(x, y + 4 + pf20.getHeight(), this.width, 20, 4, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        tf.setPosition(x + 4, y + 9 + pf20.getHeight());
        tf.width = (float) this.width - 8;
        tf.height = 12;

        tf.setTextColor(ThemeManager.get(ThemeManager.ThemeColor.Text));
        tf.setDisabledTextColour(ThemeManager.get(ThemeManager.ThemeColor.Text, 200));

        if (tf.isFocused())
            tf.onTick();

        tf.drawTextBox((int) mouseX, (int) mouseY, true);

        this.setting.setValue(tf.getText());

        return 0;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        tf.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        tf.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        tf.textboxKeyTyped(typedChar, keyCode);
    }
}
