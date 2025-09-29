package tech.konata.phosphate.settings;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.rendering.background.AcrylicBackground;
import tech.konata.phosphate.rendering.background.nativelib.DwmApiLib;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.Localizer;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.Quality;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.panels.MusicPanel;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2023/12/23
 */
public class GlobalSettings {

    public static final StringModeSetting LANG = new StringModeSetting("Lang", "LOL", "LOL") {
        @Override
        public void onModeChanged(String before, String now) {
            Localizer.setLang(now);
        }
    };

    public static final BooleanSetting DEBUG_MODE = new BooleanSetting("Debug Info", false, () -> !Phosphate.getInstance().isObfuscated());

    public static final ModeSetting<VideoPreset> VIDEO_PRESET = new ModeSetting<VideoPreset>("Video Preset", VideoPreset.Performance) {
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

    public static final ModeSetting<ThemeManager.Theme> THEME = new ModeSetting<ThemeManager.Theme>("Theme", ThemeManager.Theme.Light) {
        @Override
        public void onModeChanged(ThemeManager.Theme before, ThemeManager.Theme now) {
            ThemeManager.setTheme(now);

            if (!Phosphate.getInstance().isClientLoaded()) {
                for (ThemeManager.ThemeColor t : ThemeManager.ThemeColor.values()) {
                    Color actual = new Color(ThemeManager.getActual(t));

                    t.r = actual.getRed();
                    t.g = actual.getGreen();
                    t.b = actual.getBlue();

                }
            }
        }
    };

    public static final ModeSetting<HudStyle> HUD_STYLE = new ModeSetting<>("Hud Style", HudStyle.Regular, () -> false);

    public enum HudStyle {
        Regular,
//        Glow,
        Outline,
        Simple,
        Vanilla,
    }

    public static final StringModeSetting ACCENT_COLOR = new StringModeSetting("AccentColor", "Blue", ThemeManager.getAllAccentColorNames(), () -> false) {
        @Override
        public void onModeChanged(String before, String now) {
            ThemeManager.setAccentColor(ThemeManager.getAccentColorByName(now));
        }
    };

    public static final BooleanSetting FIXED_SCALE = new BooleanSetting("Fixed Scale", true);
    public static final BooleanSetting NO_CLICK_DELAY = new BooleanSetting("No Click Delay", true);
    public static final BooleanSetting RENDER_SELF_NAME_TAG = new BooleanSetting("Render Player's Own Name Tag", true);
    public static final BooleanSetting RENDER_GLOW = new BooleanSetting("Render Glow", true);
    public static final BooleanSetting RENDER_BLUR = new BooleanSetting("Render Blur", true);
    public static final BooleanSetting CENTER_INVENTORY = new BooleanSetting("Center the Inventory", true);

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

    public enum WindowThemeStyle {
        Auto,
        Light,
        Dark;

        public boolean getValue() {
            if (this == Auto)
                return AcrylicBackground.getThemeDetector().isDark();
            else if (this == Light)
                return false;
            else
                return true;
        }
    }

    public static final ModeSetting<WindowThemeStyle> WINDOW_THEME_STYLE = new ModeSetting<WindowThemeStyle>("Window Theme Style", WindowThemeStyle.Auto, () -> AcrylicBackground.IS_COMPATIBLE) {
        @Override
        public void onModeChanged(WindowThemeStyle before, WindowThemeStyle now) {

            if (before != now) {
                AcrylicBackground.ApplyWin11Specific();
            }

        }
    };

    public enum WindowBackdropStyle {
        Auto(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO),
        None(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_NONE),
        Mica(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_MAINWINDOW),
        Acrylic(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TRANSIENTWINDOW),
        Tabbed(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TABBEDWINDOW);

        @Getter
        private final DwmApiLib.DWM_SYSTEMBACKDROP_TYPE type;

        WindowBackdropStyle(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE type) {
            this.type = type;
        }
    }

    public static final ModeSetting<WindowBackdropStyle> WINDOW_BACKDROP_STYLE = new ModeSetting<WindowBackdropStyle>("Window Backdrop Style", WindowBackdropStyle.Acrylic, () -> AcrylicBackground.IS_COMPATIBLE) {
        @Override
        public void onModeChanged(WindowBackdropStyle before, WindowBackdropStyle now) {

            if (before != now) {
                AcrylicBackground.ApplyWin11Specific();
            }

        }
    };

    public enum WindowCornerStyle {
        Default(DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT),
        DONOTROUND(DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DONOTROUND),
        ROUND(DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_ROUND),
        ROUNDSMALL(DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_ROUNDSMALL);

        @Getter
        private final DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE type;

        WindowCornerStyle(DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE type) {
            this.type = type;
        }
    }

    public static final ModeSetting<WindowCornerStyle> WINDOW_CORNER_STYLE = new ModeSetting<WindowCornerStyle>("Window Corner Style", WindowCornerStyle.Default, () -> AcrylicBackground.IS_COMPATIBLE) {
        @Override
        public void onModeChanged(WindowCornerStyle before, WindowCornerStyle now) {

            if (before != now) {
                AcrylicBackground.ApplyWin11Specific();
            }

        }
    };

    public static final BooleanSetting HIDE_BORDER = new BooleanSetting("Hide Window Border", false, () -> AcrylicBackground.IS_COMPATIBLE) {
        @Override
        public void onToggle() {
            AcrylicBackground.ApplyWin11Specific();
        }
    };

    public static final BooleanSetting CUSTOM_BORDER = new BooleanSetting("Custom Window Border", false, () -> AcrylicBackground.IS_COMPATIBLE && !HIDE_BORDER.getValue()) {
        @Override
        public void onToggle() {
            AcrylicBackground.ApplyWin11Specific();
        }
    };

    public static final ColorSetting BORDER_COLOR = new ColorSetting("Border Color", new HSBColor(255, 255, 255, 255), () -> AcrylicBackground.IS_COMPATIBLE && !HIDE_BORDER.getValue() && CUSTOM_BORDER.getValue()) {
        @Override
        public void onValueChanged(HSBColor value) {
            AcrylicBackground.ApplyWin11Specific();
        }
    };

    public static final BooleanSetting BORDERLESS_WINDOW = new BooleanSetting("Borderless Window", false);
    public static final BooleanSetting GENSHIN_IMPACT_MODE = new BooleanSetting("怎么你也喜欢玩那个二字游戏吗", false, () -> false);

    public enum VideoPreset {
        Quality,
        Balanced,
        Performance
    }

    public static final StringSetting BOLD_FONT_RENDERER_PATH = new StringSetting("Bold Font Path", "") {
        @Override
        public boolean onValueChanged(String before, String after) {
            return new File(after).exists();
        }
    };

    public static final StringSetting REGULAR_FONT_RENDERER_PATH = new StringSetting("Regular Font Path", "") {
        @Override
        public boolean onValueChanged(String before, String after) {
            return new File(after).exists();
        }
    };

    public static final NumberSetting<Double> FONT_OFFSET_X = new NumberSetting<>("Font Offset X", 0.0, -4.0, 4.0, 0.5);
    public static final NumberSetting<Double> FONT_OFFSET_Y = new NumberSetting<>("Font Offset Y", -2.0, -4.0, 4.0, 0.5);

    public static final BooleanSetting GUIINGAME_CACHE = new BooleanSetting("GuiIngame Cache", true, () -> false);

    public static final NumberSetting<Integer> RENDER2D_FRAMERATE = new NumberSetting<Integer>("Render2D Framerate", 250, 0, 250, 10, () -> false) {

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

    public static final NumberSetting<Integer> SHADERS_FRAMERATE = new NumberSetting<Integer>("Shaders Framerate", 250, 0, 250, 10, () -> false) {

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

//    public static final BooleanSetting MUSIC_CACHING = new BooleanSetting("M_Music Caching", true, () -> ClickGui.getInstance().currentPanel instanceof MusicPanel);
    public static final BooleanSetting MUSIC_THEME = new BooleanSetting("M_Music Theme", true, () -> ClickGui.getInstance().currentPanel instanceof MusicPanel);

    public static final NumberSetting<Double> MIX_FACTOR = new NumberSetting<>("M_Mix Factor", 0.2, 0.1, 1.0, 0.01, () -> ClickGui.getInstance().currentPanel instanceof MusicPanel && MUSIC_THEME.getValue());

    public static final BooleanSetting PLAY_NOTIFY = new BooleanSetting("M_Play Notify", true, () -> ClickGui.getInstance().currentPanel instanceof MusicPanel);

    public static final NumberSetting<Double> PLAYBACK_SPEED = new NumberSetting<Double>("M_Playback Speed", 1.0, 0.1, 2.0, 0.05, () -> ClickGui.getInstance().currentPanel instanceof MusicPanel) {
        @Override
        public void onValueChanged(Double last, Double now) {

            if (CloudMusic.player != null) {
                CloudMusic.player.player.rate(now.floatValue());
            }

        }
    };
    public static final ModeSetting<Quality> MUSIC_QUALITY = new ModeSetting<>("M_Music Quality", Quality.STANDARD, () -> ClickGui.getInstance().currentPanel instanceof MusicPanel);

    public static final ModeSetting<EffectType> EFFECT_TYPE = new ModeSetting<EffectType>("M_Effect", EffectType.None) {
        @Override
        public void onModeChanged(EffectType before, EffectType now) {
            if (CloudMusic.player != null) {
                CloudMusic.player.setEffects();
            }
        }
    };

    public enum EffectType {
        None,
        Delay,
        Reverb
    }

    public static final NumberSetting<Float> REVERB_DAMP = new NumberSetting<Float>("M_Damp", 0.2f, 0.0f, 1.0f, 0.01f, () -> EFFECT_TYPE.getValue() == EffectType.Reverb) {
        @Override
        public void onValueChanged(Float last, Float now) {

            if (CloudMusic.player != null) {
                CloudMusic.player.getReverbEffect().damp(now);
            }

        }
    };
    public static final NumberSetting<Float> REVERB_ROOM = new NumberSetting<Float>("M_Room", 0.84f, 0.0f, 1.0f, 0.01f, () -> EFFECT_TYPE.getValue() == EffectType.Reverb) {
        @Override
        public void onValueChanged(Float last, Float now) {

            if (CloudMusic.player != null) {
                CloudMusic.player.getReverbEffect().room(now);
            }

        }
    };
    public static final NumberSetting<Float> REVERB_WET = new NumberSetting<Float>("M_Wet", 0.84f, 0.0f, 1.0f, 0.01f, () -> EFFECT_TYPE.getValue() == EffectType.Reverb) {
        @Override
        public void onValueChanged(Float last, Float now) {

            if (CloudMusic.player != null) {
                CloudMusic.player.getReverbEffect().wet(now);
            }

        }
    };

    public static final NumberSetting<Float> DELAY_TIME = new NumberSetting<Float>("M_Delay Time", 0.0f, 0.0f, 4.0f, 0.01f, () -> EFFECT_TYPE.getValue() == EffectType.Delay) {
        @Override
        public void onValueChanged(Float last, Float now) {

            if (CloudMusic.player != null) {
                CloudMusic.player.getDelayEffect().time(now);
            }

        }
    };

    public static final NumberSetting<Float> DELAY_FEEDBACK = new NumberSetting<Float>("M_Feedback", 0.0f, 0.0f, 1.0f, 0.01f, () -> EFFECT_TYPE.getValue() == EffectType.Delay) {
        @Override
        public void onValueChanged(Float last, Float now) {

            if (CloudMusic.player != null) {
                CloudMusic.player.getDelayEffect().feedback(now);
            }

        }
    };

    public static final NumberSetting<Integer> volume = new NumberSetting<Integer>("M_Volume", 30, 0, 100, 1, () -> false) {
        @Override
        public void onValueChanged(Integer last, Integer now) {
            if (CloudMusic.player == null) {
                return;
            }

            if (now < 0) {
                this.setValue(0);
            }

            if (now > 100) {
                this.setValue(100);
            }

            CloudMusic.player.setVolume(now.floatValue() / 100.0f);
        }
    };

    private static void setEq(int idx, double gain) {
//        if (CloudMusic.player != null && CloudMusic.player.player != null && CloudMusic.player.player.getStatus() != MediaPlayer.Status.DISPOSED) {
//            AudioEqualizer eq = CloudMusic.player.player.getAudioEqualizer();
//            eq.setEnabled(true);
//            eq.getBands().get(idx).setGain(gain);
//        }
    }

    public static final NumberSetting<Double> EQ_0 = new NumberSetting<Double>("EQ0", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(0, now);
        }
    };
    public static final NumberSetting<Double> EQ_1 = new NumberSetting<Double>("EQ1", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(1, now);
        }
    };
    public static final NumberSetting<Double> EQ_2 = new NumberSetting<Double>("EQ2", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(2, now);
        }
    };
    public static final NumberSetting<Double> EQ_3 = new NumberSetting<Double>("EQ3", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(3, now);
        }
    };
    public static final NumberSetting<Double> EQ_4 = new NumberSetting<Double>("EQ4", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(4, now);
        }
    };
    public static final NumberSetting<Double> EQ_5 = new NumberSetting<Double>("EQ5", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(5, now);
        }
    };
    public static final NumberSetting<Double> EQ_6 = new NumberSetting<Double>("EQ6", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(6, now);
        }
    };
    public static final NumberSetting<Double> EQ_7 = new NumberSetting<Double>("EQ7", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(7, now);
        }
    };
    public static final NumberSetting<Double> EQ_8 = new NumberSetting<Double>("EQ8", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(8, now);
        }
    };
    public static final NumberSetting<Double> EQ_9 = new NumberSetting<Double>("EQ9", 0.0, -24.0, 12.0, 0.1, () -> false) {
        @Override
        public void onValueChanged(Double last, Double now) {
            setEq(9, now);
        }
    };

    public static final Module dummyMusicModule = new Module("Setting", Module.Category.SETTING) {
        {
            super.setShouldRender(() -> false);
        }
    };


    public static final Module dummyModule = new Module("Setting", Module.Category.SETTING) {
        {
            super.setShouldRender(() -> false);
        }
    };


    @Getter
    private static final List<Setting<?>> settings = new ArrayList<>();
    public static JsonObject config;

    @SneakyThrows
    public static void initialize() {
        settings.clear();

        Localizer.loadLang();

        List<String> modes = new ArrayList<>();
        for (org.lwjglx.opengl.DisplayMode dMode : Display.getAvailableDisplayModes()) {
            if (dMode.getFrequency() == Display.getDesktopDisplayMode().getFrequency())
                modes.add(dMode.getWidth() + "x" + dMode.getHeight());
        }

        Collections.reverse(modes);

        FULL_SCREEN_RESOLUTION = new StringModeSetting("Fullscreen Resolution", modes.get(0), modes);

        DisplayMode ddm = Display.getDesktopDisplayMode();
        FULL_SCREEN_RESOLUTION.setMode(ddm.getWidth() + "x" + ddm.getHeight());

        for (Field field : GlobalSettings.class.getDeclaredFields()) {
            field.setAccessible(true);

            if (Setting.class.isAssignableFrom(field.getType())) {
                Setting<?> setting = (Setting<?>) field.get(null);
                settings.add(setting);
            }
        }

        if (config != null) {
            config.entrySet().forEach(s -> {
                Setting<?> setting = GlobalSettings.getSettingByName(s.getKey());

                if (setting != null)
                    setting.loadValue(s.getValue().getAsString());
                else
                    System.out.println(s.getKey());
            });
        }

        dummyModule.addSettings(settings.toArray(new Setting[0]));

        for (Setting<?> setting : settings) {
            if (setting.getInternalName().startsWith("M_")) {
                dummyMusicModule.addSettings(setting);
            }
        }

    }

    public static Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (name.equalsIgnoreCase(setting.getInternalName()))
                return setting;
        }

        return null;
    }

}
