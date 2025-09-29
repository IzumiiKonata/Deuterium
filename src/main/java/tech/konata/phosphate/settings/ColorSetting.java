package tech.konata.phosphate.settings;

import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.widget.Widget;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorSetting extends Setting<HSBColor> {

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
    public NumberSetting<Float> rainbowSpeed = new NumberSetting<Float>(
            this.getInternalName() + " RainbowSpeed",
            3f, 1f, 10f, 0.1f, () -> this.rainbow.getValue() && this.getShouldRender().get()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.rainbowspeed.name"));
        }
    };
    public NumberSetting<Float> chromaSpeed = new NumberSetting<Float>(
            this.getInternalName() + " ChromaSpeed",
            3f, 1f, 15f, 0.1f, () -> this.chroma.getValue() && this.getShouldRender().get()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.chromaspeed.name"));
        }
    };
    public NumberSetting<Long> chromaValue = new NumberSetting<Long>(
            this.getInternalName() + " ChromaValue",
            150L, 1L, 200L, 1L, () -> this.chroma.getValue() && this.getShouldRender().get()) {
        @Override
        public void onInit() {
            super.setName(Localizable.of("settings.chromavalue.name"));
        }
    };


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
    public HSBColor buildDefaultValue(HSBColor value) {
        return value.clone();
    }

    @Override
    public void loadValue(String input) {
        String[] split = input.split(":");
        if (split.length < 4)
            return;
        this.value = new tech.konata.phosphate.rendering.HSBColor(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]),
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

        return RenderSystem.hexColor(color.getRed(), color.getGreen(), color.getBlue(), alp);
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
            float speed = this.chromaSpeed.getValue().floatValue();
            long value = this.chromaValue.getValue();
            double v = (1 - speed / (this.chromaSpeed.getMaximum() + 0.1)) * 6000;
            float hue = (float) ((System.currentTimeMillis() + (count * value)) % (int) v);
            hue /= (int) v;
            super.getValue().setHue(hue);
        } else if (this.rainbow.getValue()) {
            float speed = this.rainbowSpeed.getValue().floatValue();
            double v = (1 - speed / (this.rainbowSpeed.getMaximum() + 0.1)) * 6000;
            float hue = System.currentTimeMillis() % (int) v;
            hue /= (int) v;
            super.getValue().setHue(hue);
        }

        return super.getValue();
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

    @Override
    public void setValue(HSBColor value) {
        super.setValue(value);
    }

}
