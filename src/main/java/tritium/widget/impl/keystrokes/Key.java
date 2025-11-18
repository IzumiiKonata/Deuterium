package tritium.widget.impl.keystrokes;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.LazyLoadBase;
import org.lwjgl.input.Keyboard;
import tritium.interfaces.IFontRenderer;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.rendering.ARGB;
import tritium.rendering.Stencil;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Key implements SharedRenderingConstants {

    private final KeyBinding key;
    private final double xOffset, yOffset, width, height;

    private float pressedAlpha = 0;

    private float vR = 0, vG = 0, vB = 0;

    List<Circle> circles = new ArrayList<>();

    private final LazyLoadBase<String> keyName;

    public Key(KeyBinding key, double xOffset, double yOffset, double width, double height) {
        this.key = key;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;

        this.keyName = LazyLoadBase.of(() -> {
            if (key.getKeyCode() <= -99) {
                return this.key.getKeyCode() == -100 ? "L" : "R";
            }

            return Keyboard.getKeyName(this.key.getKeyCode());
        });
    }

    public void render(double x, double y) {

        if (this.key.getKeyCode() > 0 && this.key.isPressed()) {
            this.circles.add(new Circle());
        }

        NORMAL.add(() -> {

            GlStateManager.pushMatrix();

            WidgetsManager.keyStrokes.doScale();

            this.pressedAlpha = Interpolations.interpBezier(this.pressedAlpha, this.key.pressed ? 120 * RenderSystem.DIVIDE_BY_255 : 0, 0.2f);

            Rect.draw(x + xOffset, y + yOffset, width, height, ARGB.color(0, 0, 0, 50), Rect.RectType.EXPAND);
            Rect.draw(x + xOffset, y + yOffset, width, height, ARGB.color(255, 255, 255, (int) (this.pressedAlpha * 255)), Rect.RectType.EXPAND);

            Stencil.write();
            Rect.draw(x + xOffset, y + yOffset, width, height, -1, Rect.RectType.EXPAND);
            Stencil.erase();
            Iterator<Circle> it = this.circles.iterator();

            while (it.hasNext()) {
                Circle circle = it.next();

                circle.length = Interpolations.interpBezier(circle.length, width * 1.1, 0.12);

                circle.draw(x + xOffset + width * 0.5, y + yOffset + height * 0.5);

                if (circle.length >= width * 0.7)
                    circle.alpha = Interpolations.interpBezier(circle.alpha, 0f, 0.12f);

                if (circle.length >= width * 0.95)
                    it.remove();
            }

            Stencil.dispose();

            IFontRenderer fontRenderer = ClientSettings.WIDGETS_USE_VANILLA_FONT_RENDERER.getValue() ? FontManager.vanilla : FontManager.pf18;

            if (this.key.getKeyCode() != 57) {
                boolean renderCPS = this.key.getKeyCode() <= -99 && WidgetsManager.keyStrokes.showCPS.getValue();
                fontRenderer.drawCenteredString(
                        this.getKeyName(),
                        x + xOffset + width * .5,
                        y + yOffset + height * .5 - fontRenderer.getHeight() / 2.0 - (renderCPS ? 4 : (fontRenderer == FontManager.vanilla ? 0 : .5)),
                        hexColor(255 - (int) (this.vR * 255), 255 - (int) (this.vG * 255), 255 - (int) (this.vB * 255))
                );

                if (renderCPS) {
                    IFontRenderer fr = ClientSettings.WIDGETS_USE_VANILLA_FONT_RENDERER.getValue() ? FontManager.vanilla : FontManager.pf12;
                    fr.drawCenteredString(
                            this.getCPS() + " CPS",
                            x + xOffset + width * .5,
                            y + yOffset + height * .5 - fr.getHeight() / 2.0 + 5,
                            fr == FontManager.vanilla ? .8 : 1,
                            hexColor(255 - (int) (this.vR * 255), 255 - (int) (this.vG * 255), 255 - (int) (this.vB * 255))
                    );
                }
            } else {
                Rect.draw(x + xOffset + width * .2, y + yOffset + height * .5 - .5, width * .6, 1, -1);
            }

            GlStateManager.popMatrix();
        });

    }

    public int getCPS() {
        return this.key.getKeyCode() == -100 ? CPSUtils.left.get() : CPSUtils.right.get();
    }

    private String getKeyName() {
        return this.keyName.getValue();
    }
}
