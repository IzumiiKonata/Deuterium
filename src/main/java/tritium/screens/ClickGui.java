package tritium.screens;

import lombok.Getter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.management.ThemeManager;
import tritium.rendering.ARGB;
import tritium.rendering.animation.Interpolations;
import tritium.screens.clickgui.category.CategoriesWindow;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.module.ModuleListWindow;
import tritium.screens.clickgui.music.MusicsWindow;
import tritium.screens.clickgui.music.PlaylistsWindow;
import tritium.screens.clickgui.value.ValueWindow;
import tritium.settings.ClientSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 12:03
 */
public class ClickGui extends BaseScreen {

    @Getter
    private static final ClickGui instance = new ClickGui();

    @Getter
    int dWheel = 0;

    List<Window> windows = new ArrayList<>();

    @Getter
    CategoriesWindow categoriesWindow = new CategoriesWindow();

    @Getter
    ModuleListWindow moduleListWindow = new ModuleListWindow();

    @Getter
    ValueWindow valueWindow = new ValueWindow();

    @Getter
    PlaylistsWindow playlistsWindow = new PlaylistsWindow();

    @Getter
    MusicsWindow musicsWindow = new MusicsWindow();

    float alpha = .0f;
    boolean closing = false;

    public ClickGui() {
        this.windows.clear();

        this.windows.add(categoriesWindow);
        this.windows.add(moduleListWindow);
        this.windows.add(valueWindow);
//        this.windows.add(playlistsWindow);
//        this.windows.add(musicsWindow);

        this.windows.forEach(Window::init);
    }

    @Override
    public void initGui() {
        this.closing = false;

//        categoriesWindow.init();
//        playlistsWindow.init();
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        this.alpha = Interpolations.interpBezier(this.alpha, this.closing ? .0f : 1f, 0.3f);

        if (this.alpha <= .05f && this.closing)
            mc.displayGuiScreen(null);

        this.dWheel = Mouse.getDWheel();

        this.windows.forEach(window -> {
            window.setAlpha(this.alpha);
            window.render(mouseX, mouseY);
        });

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.windows.forEach(window -> window.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        for (Window window : this.windows) {
            if (window.keyTyped(typedChar, keyCode)) {
                return;
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.closing = true;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static int getColor(int type) {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();
        switch (theme) {
            case Dark:
                switch (type) {
                    case 0:
                        return ARGB.color(21, 21, 21);// Menu
                    case 1:
                        return ARGB.color(255, 255, 255);// Top Title
                    case 2:
                        return ARGB.color(21, 21, 21);// Top
                    case 3:
                        return ARGB.color(28, 28, 28);// Main
                    case 4:
                        return ARGB.color(200, 200, 200);// Menu Icon off
                    case 5:
                        return ARGB.color(255, 255, 255);// Menu Icon on
                    case 6:
                        return ARGB.color(28, 28, 28);// Menu Frame
                    case 7:
                        return ARGB.color(60, 81, 249);// Menu Chosen Frame
                    case 8:
                        return ARGB.color(21, 21, 21);// ModuleList rect
                    case 9:
                        return ARGB.color(255, 255, 255);// ModuleList text
                    case 10:
                        return ARGB.color(37, 38, 43);// ModuleList rect focus
                    case 11:
                        return ARGB.color(24, 24, 24);// ModuleList setting
                    case 12:
                        return ARGB.color(37, 38, 43);// ModuleList setting focus
                    case 13:
                        return ARGB.color(21, 21, 21);// Module On Hover Rect
                    case 14:
                        return ARGB.color(255, 255, 255);// Module On Hover Text
                    case 15:
                        return ARGB.color(32, 32, 32);// Value List Menu
                    case 16:
                        return ARGB.color(255, 255, 255);// ModuleList settings
                    case 17:
                        return ARGB.color(255, 255, 255);// Value List Back
                    case 18:
                        return ARGB.color(0, 111, 255);// Value List back focus
                    case 19:
                        return ARGB.color(255, 255, 255);// Value List title
                    case 20:
                        return ARGB.color(255, 255, 255);// Value List Label
                    case 21:
                        return ARGB.color(38, 38, 38);// Boolean Value rect
                    case 22:
                        return ARGB.color(44, 44, 44);// Boolean Value focus
                    case 23:
                        return ARGB.color(38, 38, 38);// Enum Value rect
                    case 24:
                        return ARGB.color(44, 44, 44);// Enum Value focus
                    case 25:
                        return ARGB.color(52, 52, 52);// Number Value rect
                    case 26:
                        return ARGB.color(255, 255, 255);// Number Value rounded
                    case 27:
                        return ARGB.color(0, 111, 255);// Number Value rect value
                    case 28:
                        return ARGB.color(32, 32, 32);// Command Box Rect
                    case 29:
                        return ARGB.color(255, 255, 255);// Command Box Text
                }
            case Light:
                switch (type) {
                    case 0:
                        return ARGB.color(236, 240, 241);// Menu
                    case 1:
                        return ARGB.color(23, 32, 42);// Top Title
                    case 2:
                        return ARGB.color(236, 240, 241);// Top
                    case 3:
                        return ARGB.color(240, 243, 244);// Main
                    case 4:
                        return ARGB.color(28, 40, 51);// Menu Icon off
                    case 5:
                        return ARGB.color(240, 243, 244);// Menu Icon on
                    case 6:
                        return ARGB.color(244, 246, 247);// Menu Frame
                    case 7:
                        return ARGB.color(52, 73, 94);// Menu Chosen Frame
                    case 8:
                        return ARGB.color(234, 236, 238);// ModuleList rect
                    case 9:
                        return ARGB.color(23, 32, 42);// ModuleList text
                    case 10:
                        return ARGB.color(244, 246, 246);// ModuleList rect focus
                    case 11:
                        return ARGB.color(213, 216, 220);// ModuleList setting
                    case 12:
                        return ARGB.color(214, 219, 223);// ModuleList setting focus
                    case 13:
                        return ARGB.color(234, 236, 238);// Module On Hover Rect
                    case 14:
                        return ARGB.color(23, 32, 42);// Module On Hover Text
                    case 15:
                        return ARGB.color(251, 252, 252);// Value List Menu
                    case 16:
                        return ARGB.color(23, 32, 42);// ModuleList settings
                    case 17:
                        return ARGB.color(23, 32, 42);// Value List Back
                    case 18:
                        return ARGB.color(0, 111, 255);// Value List back focus
                    case 19:
                        return ARGB.color(23, 32, 42);// Value List title
                    case 20:
                        return ARGB.color(23, 32, 42);// Value List Label
                    case 21:
                        return ARGB.color(234, 237, 237);// Boolean Value rect
                    case 22:
                        return ARGB.color(242, 243, 244);// Boolean Value focus
                    case 23:
                        return ARGB.color(242, 244, 244);// Enum Value rect
                    case 24:
                        return ARGB.color(244, 246, 246);// Enum Value focus
                    case 25:
                        return ARGB.color(213, 219, 219);// Number Value rect
                    case 26:
                        return ARGB.color(174, 182, 191);// Number Value rounded
                    case 27:
                        return ARGB.color(0, 111, 255);// Number Value rect value
                    case 28:
                        return ARGB.color(251, 252, 252);// Command Box Rect
                    case 29:
                        return ARGB.color(23, 32, 42);// Command Box Text
                }
        }
        return 0;
    }
}
