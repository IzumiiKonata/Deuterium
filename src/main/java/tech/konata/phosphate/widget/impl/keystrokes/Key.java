package tech.konata.phosphate.widget.impl.keystrokes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjglx.input.Keyboard;
import tech.konata.phosphate.interfaces.IFontRenderer;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.TexturedShadow;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.GlobalSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static tech.konata.phosphate.interfaces.SharedRenderingConstants.*;

public class Key implements SharedRenderingConstants {

    private final KeyBinding key;
    private final double xOffset, yOffset, width, height;

    private float pressedAlpha = 0;

    private float vR = 0, vG = 0, vB = 0;

    List<Circle> circles = new ArrayList<>();

    public Key(KeyBinding key, double xOffset, double yOffset, double width, double height) {
        this.key = key;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    public void render(double x, double y) {

        boolean bFlagVanilla = GlobalSettings.HUD_STYLE.getValue() == GlobalSettings.HudStyle.Vanilla;

        if (!bFlagVanilla) {
            if (this.key.getKeyCode() > 0 && this.key.isPressed()) {
                this.circles.add(new Circle());
            }

            BLUR.add(() -> {
                Rect.draw(x + xOffset, y + yOffset, width, height, -1, Rect.RectType.EXPAND);
            });
        }

        NORMAL.add(() -> {

            if (!bFlagVanilla) {
                this.pressedAlpha = Interpolations.interpBezier(this.pressedAlpha, this.key.pressed ? 80 * RenderSystem.DIVIDE_BY_255 : 0, 0.2f);

                Rect.draw(x + xOffset, y + yOffset, width, height, ThemeManager.get(ThemeManager.ThemeColor.Surface, 50), Rect.RectType.EXPAND);

                Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.hexColor(255, 255, 255, (int) (this.pressedAlpha * 255)), Rect.RectType.EXPAND);

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
            } else {

                float speed = 0.4f;

                if (this.key.pressed) {
                    this.vR = Interpolations.interpBezier(this.vR, 1, speed);
                    this.vG = Interpolations.interpBezier(this.vG, 1, speed);
                    this.vB = Interpolations.interpBezier(this.vB, 1, speed);
                } else {
                    this.vR = Interpolations.interpBezier(this.vR, 0, speed);
                    this.vG = Interpolations.interpBezier(this.vG, 0, speed);
                    this.vB = Interpolations.interpBezier(this.vB, 0, speed);
                }

                Rect.draw(x + xOffset, y + yOffset, width, height, hexColor((int) (this.vR * 255), (int) (this.vG * 255), (int) (this.vB * 255), 120), Rect.RectType.EXPAND);

//                Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.hexColor(255, 255, 255, (int) (this.pressedAlpha * 255)), Rect.RectType.EXPAND);

            }

            IFontRenderer fontRenderer = bFlagVanilla ? Minecraft.getMinecraft().fontRendererObj : FontManager.pf18;

            if (this.key.getKeyCode() != 57 && this.key.getKeyCode() > 0) {
                fontRenderer.drawString(this.getKeyName(), x + xOffset + 4, y + yOffset + 3 + (bFlagVanilla ? 1 : 0), hexColor(255 - (int) (this.vR * 255), 255 - (int) (this.vG * 255), 255 - (int) (this.vB * 255)));
            }

            if (this.key.getKeyCode() <= -99) {
                fontRenderer.drawCenteredString(this.getKeyName(), x + xOffset + width / 2 + (bFlagVanilla ? 1 : 0), y + yOffset + height / 2 - fontRenderer.getHeight() / 2.0 + (bFlagVanilla ? 1 : 0), hexColor(255 - (int) (this.vR * 255), 255 - (int) (this.vG * 255), 255 - (int) (this.vB * 255)));
            }

        });

    }

    public int getCPS() {
        return this.key.getKeyCode() == -100 ? CPSUtils.left.get() : CPSUtils.right.get();
    }

    private String getKeyName() {

        if (this.key.getKeyCode() <= -99) {

            int cps = this.getCPS();
            if (cps != 0) {
                return cps + " CPS";
            }

            return this.key.getKeyCode() == -100 ? "LMB" : "RMB";
        }

        return Keyboard.getKeyName(this.key.getKeyCode());
    }
}
