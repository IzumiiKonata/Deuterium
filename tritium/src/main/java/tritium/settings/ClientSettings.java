package tritium.settings;

import com.google.gson.JsonObject;
import ingameime.IngameIMEJNI;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.lwjglx.opengl.DisplayMode;
import tritium.Tritium;
import tritium.management.ConfigManager;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.Quality;
import tritium.rendering.ime.IngameIMERenderer;
import tritium.screens.ClickGui;
import tritium.utils.i18n.Localizable;
import tritium.management.Localizer;
import tritium.management.ThemeManager;
import tritium.module.Module;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_RAW_MOUSE_MOTION;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;

/**
 * @author IzumiiKonata
 * @since 2023/12/23
 */
public class ClientSettings {

    public static final StringModeSetting LANG = new StringModeSetting("Lang", "LOL", "LOL") {
        @Override
        public void onModeChanged(String before, String now) {
            Localizer.setLang(now);

            if (Minecraft.getMinecraft().currentScreen instanceof ClickGui) {
                // 重新排序一下
                ClickGui.getInstance().getModuleListWindow().refreshModules();
            }
        }
    };

    public static final ModeSetting<Quality> MUSIC_QUALITY = new ModeSetting<>("Quality", Quality.STANDARD) {
        @Override
        public void onModeChanged(Quality before, Quality now) {
            CloudMusic.quality = now;
        }
    };

    public static final BooleanSetting DEBUG_MODE = new BooleanSetting("Debug Info", false, () -> !Tritium.getInstance().isObfuscated());

    public static final ModeSetting<VideoPreset> VIDEO_PRESET = new ModeSetting<>("Video Preset", VideoPreset.Performance) {
        @Override
        public void onModeChanged(VideoPreset before, VideoPreset now) {

            if (now == VideoPreset.Performance) {
                RENDER2D_FRAMERATE.setValue(RENDER2D_FRAMERATE.getMaximum());
                SHADERS_FRAMERATE.setValue(60);
                GUIINGAME_CACHE.setValue(true);
            }

            if (now == VideoPreset.Balanced) {
                RENDER2D_FRAMERATE.setValue(RENDER2D_FRAMERATE.getMaximum());
                SHADERS_FRAMERATE.setValue(SHADERS_FRAMERATE.getMaximum());
                GUIINGAME_CACHE.setValue(true);
            }

            if (now == VideoPreset.Quality) {
                RENDER2D_FRAMERATE.setValue(RENDER2D_FRAMERATE.getMinimum());
                SHADERS_FRAMERATE.setValue(SHADERS_FRAMERATE.getMinimum());
                GUIINGAME_CACHE.setValue(false);
            }

        }
    };

    public static final BooleanSetting FRAME_PREDICT = new BooleanSetting("Frame Predict", false);
    public static final BooleanSetting MUSIC_TOAST = new BooleanSetting("Music Toast", true);
    public static final BooleanSetting RAW_INPUT = new BooleanSetting("Raw Input", false) {

        @Override
        public void onToggle() {

            if (this.getValue()) {

                if (!GLFW.glfwRawMouseMotionSupported()) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage("系统不支持原始鼠标输入!");
                    this.setValue(false);
                    return;
                }

                GLFW.glfwSetInputMode(Display.getWindow(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
            } else {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
            }

        }
    };

    public static final ModeSetting<ThemeManager.Theme> THEME = new ModeSetting<>("Theme", ThemeManager.Theme.Dark) {
        @Override
        public void onModeChanged(ThemeManager.Theme before, ThemeManager.Theme now) {
            ThemeManager.setTheme(now);

            if (!Tritium.getInstance().isClientLoaded()) {
                for (ThemeManager.ThemeColor t : ThemeManager.ThemeColor.values()) {
                    Color actual = new Color(ThemeManager.getActual(t));

                    t.r = actual.getRed();
                    t.g = actual.getGreen();
                    t.b = actual.getBlue();

                }
            }
        }
    };

    public static final BooleanSetting FIXED_SCALE = new BooleanSetting("Fixed Scale", true);
    public static final BooleanSetting NO_CLICK_DELAY = new BooleanSetting("No Click Delay", true);
    public static final BooleanSetting RENDER_SELF_NAME_TAG = new BooleanSetting("Render Player's Own Name Tag", true);
    public static final BooleanSetting NAME_TAG_BACKGROUND = new BooleanSetting("Name Tag Background", true);
    public static final BooleanSetting RENDER_GLOW = new BooleanSetting("Render Glow", true);
    public static final BooleanSetting RENDER_BLUR = new BooleanSetting("Render Blur", true);
    public static final BooleanSetting CENTER_INVENTORY = new BooleanSetting("Center the Inventory", true);
    public static final BooleanSetting IN_GAME_IME = new BooleanSetting("In game IME", true) {
        @Override
        public void onToggle() {

            if (this.getValue() && IngameIMEJNI.supported) {
                IngameIMERenderer.destroyInputCtx();
                IngameIMERenderer.createInputCtx();
            }

        }
    };
    public static final BooleanSetting WIDGETS_USE_VANILLA_FONT_RENDERER = new BooleanSetting("Use Vanilla Font for Widgets Rendering", false);

    public static StringModeSetting FULL_SCREEN_RESOLUTION = new StringModeSetting("Fullscreen Resolution", "None?!", "None?!");
    public static StringModeSetting FULL_SCREEN_REFRESH_RATE = new StringModeSetting("Fullscreen Refresh Rate", "Auto", "Auto", "60", "90", "120", "144", "165", "200", "240", "360") {
        @Override
        public String getNameForRender(String modeIn) {
            if (modeIn.equals("Auto")) {
                return Localizer.getInstance().translate("setting.auto.name");
            }

            return modeIn;
        }
    };

    public enum VideoPreset {
        Quality,
        Balanced,
        Performance
    }

    public static final BooleanSetting GUIINGAME_CACHE = new BooleanSetting("GuiIngame Cache", true, () -> false);

    public static final NumberSetting<Integer> RENDER2D_FRAMERATE = new NumberSetting<>("Render2D Framerate", 250, 0, 250, 10, () -> false) {

        final Localizable lUnlimited = Localizable.of("settings.unlimited.name");
        final Localizable lDesktopRefreshrate = Localizable.of("settings.desktop_refreshrate.name");

        @Override
        public String getStringForRender() {

            if (this.getValue() == 0)
                return lUnlimited.get();

            if (this.getValue() == this.getMaximum().intValue())
                return lDesktopRefreshrate.get();

            return super.getStringForRender();
        }

    };

    public static final NumberSetting<Integer> SHADERS_FRAMERATE = new NumberSetting<>("Shaders Framerate", 250, 0, 250, 10, () -> false) {

        final Localizable lUnlimited = Localizable.of("settings.unlimited.name");
        final Localizable lDesktopRefreshrate = Localizable.of("settings.desktop_refreshrate.name");

        @Override
        public String getStringForRender() {

            if (this.getValue() == 0)
                return lUnlimited.get();

            if (this.getValue() == this.getMaximum().intValue())
                return lDesktopRefreshrate.get();

            return super.getStringForRender();
        }

    };

    public static final Module settingsModule = new Module("Setting", Module.Category.SETTING);

    @Getter
    private static final List<Setting<?>> settings = new ArrayList<>();
    public static JsonObject config;

    @SneakyThrows
    public static void initialize() {

        ClientSettings.config = ConfigManager.preloadGlobalSettings();

        settings.clear();

        Localizer.loadLang();

        List<String> modes = new ArrayList<>();
        for (DisplayMode dMode : Display.getAvailableDisplayModes()) {
            if (dMode.getFrequency() == Display.getDesktopDisplayMode().getFrequency())
                modes.add(dMode.getWidth() + "x" + dMode.getHeight());
        }

        Collections.reverse(modes);

        FULL_SCREEN_RESOLUTION = new StringModeSetting("Fullscreen Resolution", modes.getFirst(), modes);

        DisplayMode ddm = Display.getDesktopDisplayMode();
        FULL_SCREEN_RESOLUTION.setMode(ddm.getWidth() + "x" + ddm.getHeight());

        for (Field field : ClientSettings.class.getDeclaredFields()) {
            field.setAccessible(true);

            if (Setting.class.isAssignableFrom(field.getType())) {
                Setting<?> setting = (Setting<?>) field.get(null);
                settings.add(setting);
            }
        }

        if (config != null) {
            config.entrySet().forEach(s -> {
                Setting<?> setting = ClientSettings.getSettingByName(s.getKey());

                if (setting != null)
                    setting.loadValue(s.getValue().getAsString());
                else
                    System.out.println(s.getKey());
            });
        }

        settingsModule.getSettings().clear();
        settingsModule.addSettings(settings.toArray(new Setting[0]));
    }

    public static Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (name.equalsIgnoreCase(setting.getInternalName()))
                return setting;
        }

        return null;
    }

}
