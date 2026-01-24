package tritium.widget;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import tritium.utils.i18n.Localizable;
import tritium.module.Module;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;

import tritium.settings.Setting;
import tritium.widget.direction.HorizontalDirection;
import tritium.widget.direction.VerticalDirection;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:52 AM
 */
public abstract class Widget extends Module {

    public Minecraft mc = Minecraft.getMinecraft();

    private double x = 0.1, y = 0.1;

    @Setter
    private double width = -1, height = -1;

    public double getWidth() {
        return this.width * scaleFactor;
    }

    public double getHeight() {
        return this.height * scaleFactor;
    }

    @Getter
    @Setter
    private double moveX, moveY;

    public double resizeX, resizeY;

    @Getter
    @Setter
    private boolean movable = true;

    @Getter
    private boolean resizable = false;

    @Getter
    @Setter
    private boolean scalable = true;

    public double scaleFactor = 1.0;

    public double defaultWidth, defaultHeight;

    public float hoveredAlpha = 0;

    @Getter
    private boolean lockResizeRatio = false;

    @Getter
    private double ratio = 1;

    public void setLockResizeRatio(boolean lockResizeRatio, double ratio) {
        this.lockResizeRatio = lockResizeRatio;
        this.ratio = ratio;
    }

    public HorizontalDirection horizontalDirection = HorizontalDirection.None;
    public VerticalDirection verticalDirection = VerticalDirection.None;

    public Widget(String internalName) {
        super(internalName, Category.WIDGET);
        String lowerCase = internalName.toLowerCase();

        this.name = Localizable.of("widget." + lowerCase + ".name");
        this.description = Localizable.of("widget." + lowerCase + ".desc");

//        WidgetsManager.getWidgets().add(this);

    }

    public void doScale() {
        GlStateManager.translate(this.getX(), this.getY(), 0);
        GlStateManager.scale(this.scaleFactor, this.scaleFactor, 1);
        GlStateManager.translate(-this.getX(), -this.getY(), 0);
    }

    public void renderStyledBackground(double x, double y, double width, double height, double radius) {

        Rect.draw(x, y, width, height, hexColor(0, 0, 0, this.getFadeAlpha()), Rect.RectType.EXPAND);
//
//        BLOOM.add(() -> {
//
//            if (bFlagScale) {
//                GlStateManager.pushMatrix();
//
//                GlStateManager.translate(x + width * 0.5, y + height * 0.5, 0);
//                GlStateManager.scale(centerScale, centerScale, 1);
//                GlStateManager.translate(-(x + width * 0.5), -(y + height * 0.5), 0);
//
//            }
//
//            if (style == GlobalSettings.HudStyle.Regular || style == GlobalSettings.HudStyle.Outline || style == GlobalSettings.HudStyle.Simple) {
//                this.roundedRect(x, y, width, height, radius + 1, -0.2, new Color(0, 0, 0, 200));
//            } else if (style == GlobalSettings.HudStyle.Glow || style == GlobalSettings.HudStyle.SimpleGlow) {
//                this.roundedRectAccentColor(x, y, width, height, radius, -0.5, 255);
//            }
//
//            if (bFlagScale) {
//                GlStateManager.popMatrix();
//            }
//
//        });
    }

    public int getFadeAlpha() {
        return 110;
    }

    public double getX() {
        return RenderSystem.getWidth() * x;
    }

    public double getY() {
        return RenderSystem.getHeight() * y;
    }

    public void setX(double x) {
        this.x = x / RenderSystem.getWidth();
    }

    public void setY(double y) {
        this.y = y / RenderSystem.getHeight();
    }

    public void setResizable(boolean bl, double defaultWidth, double defaultHeight) {

        this.resizable = bl;

        if (bl) {
            this.defaultWidth = defaultWidth;
            this.defaultHeight = defaultHeight;

            if (this.width == -1 || this.height == -1) {
                this.width = defaultWidth;
                this.height = defaultHeight;
            }
        }

    }

    public abstract void onRender(boolean editing);


    public void onResized(double lastWidth, double lastHeight) {

    }

    public void addSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.settings.add(setting);
            setting.onInit(this);
            setting.onInit();

        }
    }

    public void loadConfig(JsonObject directory) {
        directory.entrySet().forEach(data -> {
            String key = data.getKey();
            JsonElement value = data.getValue();
            switch (key) {
                case "Enabled":
                    if (!(this.isEnabled() && value.getAsBoolean())
                            && !(!this.isEnabled() && !value.getAsBoolean())) {
                        this.setEnabled(value.getAsBoolean());
                    }
                    break;
                case "PosX":
                    this.setX(value.getAsDouble());
                    break;
                case "PosY":
                    this.setY(value.getAsDouble());
                    break;
                case "Width":
                    this.setWidth(value.getAsDouble());
                    break;
                case "Height":
                    this.setHeight(value.getAsDouble());
                    break;
                case "HDirection":
                    this.horizontalDirection = HorizontalDirection.valueOf(value.getAsString());
                    break;
                case "VDirection":
                    this.verticalDirection = VerticalDirection.valueOf(value.getAsString());
                    break;
                case "ScaleFactor":
                    this.scaleFactor = value.getAsDouble();
                    break;
                default:
                    Setting<?> val = this.find(key);
                    if (val != null) {
                        val.loadValue(value.getAsString());
                    } else {
                        System.err.println("Setting \"" + key + "\" @ Widget \"" + this.getInternalName() + "\" can not be found!");
                        for (Setting<?> setting : this.settings) {
                            System.err.println("    " + setting.getInternalName());
                        }
                    }
                    break;
            }
        });
    }

    public JsonObject saveConfig() {
        JsonObject directory = new JsonObject();
        directory.addProperty("Enabled", this.isEnabled());
        directory.addProperty("PosX", this.getX());
        directory.addProperty("PosY", this.getY());
        directory.addProperty("Width", this.getWidth());
        directory.addProperty("Height", this.getHeight());
        directory.addProperty("HDirection", this.horizontalDirection.name());
        directory.addProperty("VDirection", this.verticalDirection.name());
        if (this.isScalable()) {
            directory.addProperty("ScaleFactor", this.scaleFactor);
        }
        this.settings.forEach(val -> directory.addProperty(val.getInternalName(), val.getValueForConfig()));

        return directory;
    }

}
