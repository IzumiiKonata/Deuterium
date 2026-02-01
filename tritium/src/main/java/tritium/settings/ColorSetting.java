package tritium.settings;

import tritium.bridge.settings.ColorValueWrapper;
import tritium.rendering.RGBA;
import tritium.rendering.HSBColor;
import tritium.utils.i18n.Localizable;
import tritium.module.Module;
import tritium.widget.Widget;

import java.awt.*;
import java.util.function.Supplier;

public class ColorSetting extends Setting<Color> {

    private final int chromaCount = 0;
    public BooleanSetting rainbow = new BooleanSetting(this.getInternalName() + " Rainbow", false, this.getShouldRender()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.rainbow.name"));
        }

        @Override
        public void onToggle() {
            if (chroma.getValue()) {
                chroma.setValue(false);
            }
        }

    };
    public BooleanSetting chroma = new BooleanSetting(this.getInternalName() + " Chroma", false, this.getShouldRender()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.chroma.name"));
        }

        @Override
        public void onToggle() {
            if (rainbow.getValue()) {
                rainbow.setValue(false);
            }
        }

    };
    public NumberSetting<Float> rainbowSpeed = new NumberSetting<>(
            this.getInternalName() + " RainbowSpeed",
            3f, 1f, 10f, 0.1f, () -> this.rainbow.getValue() && this.getShouldRender().get()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.rainbowspeed.name"));
        }
    };
    public NumberSetting<Float> chromaSpeed = new NumberSetting<>(
            this.getInternalName() + " ChromaSpeed",
            3f, 1f, 15f, 0.1f, () -> this.chroma.getValue() && this.getShouldRender().get()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.chromaspeed.name"));
        }
    };
    public NumberSetting<Long> chromaValue = new NumberSetting<>(
            this.getInternalName() + " ChromaValue",
            150L, 1L, 200L, 1L, () -> this.chroma.getValue() && this.getShouldRender().get()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.chromavalue.name"));
        }
    };

    @Override
    protected void createValueWrapper() {
        this.wrapper = new ColorValueWrapper(this);
    }

    public ColorSetting(String label, HSBColor value) {
        super(label, value);
    }

    public ColorSetting(String label, HSBColor value, Supplier<Boolean> show) {
        super(label, value, show);
    }

    public void testIfChanged() {

    }

    public void onValueChanged(HSBColor value) {

    }

    @Override
    public Color buildDefaultValue(Color value) {
        return ((HSBColor) value).clone();
    }

    @Override
    public void loadValue(String input) {
        String[] split = input.split(":");
        if (split.length < 4)
            return;
        this.value = new HSBColor(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]),
                Integer.parseInt(split[3]));

        if (split.length < 6)
            return;
        this.rainbow.loadValue(split[4]);
        this.chroma.loadValue(split[5]);
    }

    public int getRGB() {
        return this.getValue().getColor().getRGB();
    }

    public int getRGB(int count) {
        return this.getValue(count).getColor().getRGB();
    }

    public int getRGB(double count, int alpha) {
        Color color = this.getValue(count).getColor();

        int alp = Math.min(alpha, color.getAlpha());

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return RGBA.color(red, green, blue, alp);
    }

    @Override
    public void onInit(Module module) {
        module.addSettings(this.rainbow, this.rainbowSpeed, this.chroma, this.chromaSpeed, this.chromaValue);
    }

    @Override
    public void onInit(Widget module) {
        module.addSettings(this.rainbow, this.rainbowSpeed, this.chroma, this.chromaSpeed, this.chromaValue);
    }

    public HSBColor getValue(double count) {
        if (this.chroma.getValue()) {
            float speed = this.chromaSpeed.getValue();
            long value = this.chromaValue.getValue();
            double v = (1 - speed / (this.chromaSpeed.getMaximum() + 0.1)) * 6000;
            float hue = (float) ((System.currentTimeMillis() + (count * value)) % (int) v);
            hue /= (int) v;
            ((HSBColor) super.getValue()).setHue(hue);
        } else if (this.rainbow.getValue()) {
            float speed = this.rainbowSpeed.getValue();
            double v = (1 - speed / (this.rainbowSpeed.getMaximum() + 0.1)) * 6000;
            float hue = System.currentTimeMillis() % (int) v;
            hue /= (int) v;
            ((HSBColor) super.getValue()).setHue(hue);
        }

        return ((HSBColor) super.getValue());
    }

    @Override
    public HSBColor getValue() {
        return this.getValue(0);
    }

    @Override
    public String getValueForConfig() {
        return this.getValue() + ":" + this.rainbow.getValue() + ":" + this.chroma.getValue();
    }

    public void draw(float mouseX, float mouseY, double positionX, double positionY) {

    }

}
