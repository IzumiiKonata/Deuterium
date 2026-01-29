package tritium.screens;

import lombok.Getter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.management.FontManager;
import tritium.rendering.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.logging.ConsoleOutputRedirector;

import java.util.Queue;

/**
 * @author IzumiiKonata
 * @since 2024/8/31 12:42
 */
public class LogScreen extends BaseScreen {

    @Getter
    private static final LogScreen instance = new LogScreen();

    int scroll = 0;

    @Override
    public void initGui() {
        scroll = 0;

        CFontRenderer fr = FontManager.pf18;

        Queue<String> list = ConsoleOutputRedirector.SYSTEM_OUT;

        double spacing = 8;
        double subSpacing = 4;

        double height = RenderSystem.getHeight() - spacing * 2 - subSpacing * 3 - FontManager.pf40.getHeight() - subSpacing * 2;

        int max = (int) (height / (4 + fr.getHeight()));

        if (list.size() * (4 + fr.getHeight()) > height) {
            scroll = list.size() - 1 - max;
        }

    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        double spacing = 8;

        Rect.draw(spacing, spacing, RenderSystem.getWidth() - spacing * 2, RenderSystem.getHeight() - spacing * 2, hexColor(0, 0, 0, 160), Rect.RectType.EXPAND);

        double subSpacing = 4;
        FontManager.pf40.drawString("日志", spacing + subSpacing, spacing + subSpacing, -1);

        double posX = spacing + subSpacing, posY = spacing + subSpacing + FontManager.pf40.getHeight();
        double width = RenderSystem.getWidth() - spacing * 2 - subSpacing * 2;
        double height = RenderSystem.getHeight() - spacing * 2 - subSpacing * 3 - FontManager.pf40.getHeight();
//        Rect.draw(posX, posY, width, height, -1, Rect.RectType.EXPAND);

        posX += subSpacing;
//        posY += subSpacing * 0.5;

        width -= subSpacing * 2;
        height -= subSpacing * 2;

        int dWheel2 = Mouse.getDWheel();

        int step = 4;

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            step *= 2;
        }

        if (dWheel2 > 0) {
            scroll -= step;
        }

        if (dWheel2 < 0) {
            scroll += step;
        }


//        Rect.draw(posX, posY, width, height, 0xff0090ff, Rect.RectType.EXPAND);

        CFontRenderer fr = FontManager.pf18;

        Queue<String> queue = ConsoleOutputRedirector.SYSTEM_OUT;

        int size = queue.size();
        scroll = Math.max(0, Math.min(size - 1, scroll));

        int max = (int) (height / (4 + fr.getHeight()));

        int index = 0;
        int drawn = 0;

        for (String s : queue) {

            if (index < scroll) {
                index++;
                continue;
            }

            if (drawn >= max) {
                break;
            }

            fr.drawString(s, posX, posY, -1);
            posY += 4 + fr.getHeight();

            index++;
            drawn++;
        }

    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {

        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        }

        if (keyCode == Keyboard.KEY_UP) {
            this.scroll = 0;
        }

        if (keyCode == Keyboard.KEY_DOWN) {
            this.scroll = ConsoleOutputRedirector.SYSTEM_OUT.size() - 2;
        }

    }
}
