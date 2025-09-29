package tech.konata.phosphate.rendering.background;

import lombok.Getter;
import lombok.var;
import org.lwjgl.system.Platform;
import tech.konata.phosphate.rendering.background.nativelib.DwmApiLib;
import tech.konata.phosphate.rendering.background.nativelib.NtDllLib;
import tech.konata.phosphate.settings.GlobalSettings;

import java.util.function.Consumer;

public class AcrylicBackground {

    @Getter
    static long windowHandle = 0;

    public static void setWindowHandle(long handle) throws IllegalStateException {
        windowHandle = handle;
    }

    private static final Consumer<Boolean> systemThemeChangeHandler = (dark) -> {
        final long handle = AcrylicBackground.getWindowHandle();
        DwmApiLib.setBoolWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE, dark);
    };

    public static final boolean IS_COMPATIBLE = Platform.get() == Platform.WINDOWS && NtDllLib.checkCompatibility();

    /**
     * Apply all Win11-Specific config to the game window.
     * Called once after the window is created.
     */
    public static void ApplyWin11Specific() {

        if (!IS_COMPATIBLE) {
            return;
        }

        long handle = AcrylicBackground.getWindowHandle();

        DwmApiLib.setBoolWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE,
                GlobalSettings.WINDOW_THEME_STYLE.getValue().getValue());

        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                GlobalSettings.WINDOW_BACKDROP_STYLE.getValue().getType());

        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_WINDOW_CORNER_PREFERENCE,
                GlobalSettings.WINDOW_CORNER_STYLE.getValue().getType());

        var borderHidden = GlobalSettings.HIDE_BORDER.getValue();
        var customBorder = GlobalSettings.CUSTOM_BORDER.getValue();

        if (borderHidden) {
            // Window border is hidden
            DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.DWMWA_COLOR_NONE);
        } else if (customBorder) {
            // Window border is visible and customized
            var borderRgb = GlobalSettings.BORDER_COLOR.getRGB();
            DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.rgb2ColorRef(borderRgb));
        } else {
            // Use default border color
            DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.DWMWA_COLOR_DEFAULT);
        }
    }

    private static int hexColor(int red, int green, int blue, int alpha) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static WindowsThemeDetector themeDetector;

    public static WindowsThemeDetector getThemeDetector() {
        if (themeDetector == null) {
            themeDetector = new WindowsThemeDetector();
        }

        return themeDetector;
    }

    static {

        if (IS_COMPATIBLE) {

            Thread thread = new Thread(() -> {

                while (true) {
                    if (GlobalSettings.HIDE_BORDER.getValue() || !GlobalSettings.CUSTOM_BORDER.getValue() || getWindowHandle() == 0L) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {

                        var borderRgb = GlobalSettings.BORDER_COLOR.getRGB();
                        DwmApiLib.setIntWA(getWindowHandle(), DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.rgb2ColorRef(borderRgb));

                        try {
                            Thread.sleep(20L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }, "Window Border Color Setter");

            thread.setUncaughtExceptionHandler((t, e) -> {
                e.printStackTrace();
            });

            thread.start();
        }

    }

}
