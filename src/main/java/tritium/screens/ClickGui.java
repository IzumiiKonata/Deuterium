package tritium.screens;

import lombok.Getter;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
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
        this.windows.add(playlistsWindow);
        this.windows.add(musicsWindow);

        this.windows.forEach(Window::init);
    }

    @Override
    public void initGui() {
        this.closing = false;
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        this.alpha = Interpolations.interpBezier(this.alpha, this.closing ? .0f : 1f, 0.3f);

        if (this.alpha <= .05f && this.closing)
            mc.displayGuiScreen(null);

        this.dWheel = Mouse.getDWheel2();

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
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.closing = true;
        }

        this.windows.forEach(window -> window.keyTyped(typedChar, keyCode));
    }

    public static int getColor(int type) {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();
        switch (theme) {
            case Dark:
                switch (type) {
                    case 0:
                        return RenderSystem.hexColor(21, 21, 21);// Menu
                    case 1:
                        return RenderSystem.hexColor(255, 255, 255);// Top Title
                    case 2:
                        return RenderSystem.hexColor(21, 21, 21);// Top
                    case 3:
                        return RenderSystem.hexColor(28, 28, 28);// Main
                    case 4:
                        return RenderSystem.hexColor(200, 200, 200);// Menu Icon off
                    case 5:
                        return RenderSystem.hexColor(255, 255, 255);// Menu Icon on
                    case 6:
                        return RenderSystem.hexColor(28, 28, 28);// Menu Frame
                    case 7:
                        return RenderSystem.hexColor(60, 81, 249);// Menu Chosen Frame
                    case 8:
                        return RenderSystem.hexColor(21, 21, 21);// ModuleList rect
                    case 9:
                        return RenderSystem.hexColor(255, 255, 255);// ModuleList text
                    case 10:
                        return RenderSystem.hexColor(37, 38, 43);// ModuleList rect focus
                    case 11:
                        return RenderSystem.hexColor(24, 24, 24);// ModuleList setting
                    case 12:
                        return RenderSystem.hexColor(37, 38, 43);// ModuleList setting focus
                    case 13:
                        return RenderSystem.hexColor(21, 21, 21);// Module On Hover Rect
                    case 14:
                        return RenderSystem.hexColor(255, 255, 255);// Module On Hover Text
                    case 15:
                        return RenderSystem.hexColor(32, 32, 32);// Value List Menu
                    case 16:
                        return RenderSystem.hexColor(255, 255, 255);// ModuleList settings
                    case 17:
                        return RenderSystem.hexColor(255, 255, 255);// Value List Back
                    case 18:
                        return RenderSystem.hexColor(0, 111, 255);// Value List back focus
                    case 19:
                        return RenderSystem.hexColor(255, 255, 255);// Value List title
                    case 20:
                        return RenderSystem.hexColor(255, 255, 255);// Value List Label
                    case 21:
                        return RenderSystem.hexColor(38, 38, 38);// Boolean Value rect
                    case 22:
                        return RenderSystem.hexColor(44, 44, 44);// Boolean Value focus
                    case 23:
                        return RenderSystem.hexColor(38, 38, 38);// Enum Value rect
                    case 24:
                        return RenderSystem.hexColor(44, 44, 44);// Enum Value focus
                    case 25:
                        return RenderSystem.hexColor(52, 52, 52);// Number Value rect
                    case 26:
                        return RenderSystem.hexColor(255, 255, 255);// Number Value rounded
                    case 27:
                        return RenderSystem.hexColor(0, 111, 255);// Number Value rect value
                    case 28:
                        return RenderSystem.hexColor(32, 32, 32);// Command Box Rect
                    case 29:
                        return RenderSystem.hexColor(255, 255, 255);// Command Box Text
                }
            case Light:
                switch (type) {
                    case 0:
                        return RenderSystem.hexColor(236, 240, 241);// Menu
                    case 1:
                        return RenderSystem.hexColor(23, 32, 42);// Top Title
                    case 2:
                        return RenderSystem.hexColor(236, 240, 241);// Top
                    case 3:
                        return RenderSystem.hexColor(240, 243, 244);// Main
                    case 4:
                        return RenderSystem.hexColor(28, 40, 51);// Menu Icon off
                    case 5:
                        return RenderSystem.hexColor(240, 243, 244);// Menu Icon on
                    case 6:
                        return RenderSystem.hexColor(244, 246, 247);// Menu Frame
                    case 7:
                        return RenderSystem.hexColor(52, 73, 94);// Menu Chosen Frame
                    case 8:
                        return RenderSystem.hexColor(234, 236, 238);// ModuleList rect
                    case 9:
                        return RenderSystem.hexColor(23, 32, 42);// ModuleList text
                    case 10:
                        return RenderSystem.hexColor(244, 246, 246);// ModuleList rect focus
                    case 11:
                        return RenderSystem.hexColor(213, 216, 220);// ModuleList setting
                    case 12:
                        return RenderSystem.hexColor(214, 219, 223);// ModuleList setting focus
                    case 13:
                        return RenderSystem.hexColor(234, 236, 238);// Module On Hover Rect
                    case 14:
                        return RenderSystem.hexColor(23, 32, 42);// Module On Hover Text
                    case 15:
                        return RenderSystem.hexColor(251, 252, 252);// Value List Menu
                    case 16:
                        return RenderSystem.hexColor(23, 32, 42);// ModuleList settings
                    case 17:
                        return RenderSystem.hexColor(23, 32, 42);// Value List Back
                    case 18:
                        return RenderSystem.hexColor(0, 111, 255);// Value List back focus
                    case 19:
                        return RenderSystem.hexColor(23, 32, 42);// Value List title
                    case 20:
                        return RenderSystem.hexColor(23, 32, 42);// Value List Label
                    case 21:
                        return RenderSystem.hexColor(234, 237, 237);// Boolean Value rect
                    case 22:
                        return RenderSystem.hexColor(242, 243, 244);// Boolean Value focus
                    case 23:
                        return RenderSystem.hexColor(242, 244, 244);// Enum Value rect
                    case 24:
                        return RenderSystem.hexColor(244, 246, 246);// Enum Value focus
                    case 25:
                        return RenderSystem.hexColor(213, 219, 219);// Number Value rect
                    case 26:
                        return RenderSystem.hexColor(174, 182, 191);// Number Value rounded
                    case 27:
                        return RenderSystem.hexColor(0, 111, 255);// Number Value rect value
                    case 28:
                        return RenderSystem.hexColor(251, 252, 252);// Command Box Rect
                    case 29:
                        return RenderSystem.hexColor(23, 32, 42);// Command Box Text
                }
        }
        return 0;
    }
}
