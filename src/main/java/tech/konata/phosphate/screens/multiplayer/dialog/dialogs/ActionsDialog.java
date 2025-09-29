package tech.konata.phosphate.screens.multiplayer.dialog.dialogs;

import net.minecraft.util.Tuple;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.screens.dialog.Dialog;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ActionsDialog extends Dialog implements SharedRenderingConstants {

    private final String title, content;

    private double openCloseScale = 1.1;

    private final List<Tuple<String, Runnable>> buttons;

    public ActionsDialog(String title, String content, List<Tuple<String, Runnable>> actions) {

        this.title = title;
        this.content = content;

        this.buttons = actions;
    }

    @Override
    public void render(double mouseX, double mouseY) {
        super.drawBackgroundMask();
        this.openCloseScale = Interpolations.interpBezier(this.openCloseScale, this.isClosing() ? 1.1 : 1, 0.3);

        CFontRenderer titleRenderer = FontManager.pf28bold;
        CFontRenderer contentRenderer = FontManager.pf18;

        int intAlpha = (int) (this.alpha * 255);

        double width = Math.max(250, titleRenderer.getStringWidth(this.title) + 60);

        String[] strings = contentRenderer.fitWidth(this.content, width - 60);

        double height = 80 + contentRenderer.getHeight() * strings.length;
        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(ShaderRenderType.OVERLAY, Collections.singletonList(() -> {
            this.doGlPreTransforms(this.openCloseScale);
            this.roundedRect(x, y, width, height, 6, Color.WHITE);
            this.disposeTransforms();
        }));

        this.doGlPreTransforms(this.openCloseScale);

        this.roundedRect(x, y, width, height, 6, new Color(0, 0, 0, (int) (intAlpha * 0.3)));

        titleRenderer.drawString(this.title, x + 20, y + 17, RenderSystem.hexColor(233, 233, 233, intAlpha));

        contentRenderer.drawString(String.join("\n", strings), x + 20, y + 24 + titleRenderer.getHeight(), RenderSystem.hexColor(233, 233, 233, Math.min(160, intAlpha)));

        double buttonsSpacing = 15;
        double buttonsX = x + width - buttonsSpacing - contentRenderer.getStringWidth(buttons.get(0).getFirst());
        double buttonsY = y + height - 22;

        for (Tuple<String, Runnable> button : this.buttons) {

            if (RenderSystem.isHovered(mouseX, mouseY, buttonsX, buttonsY, contentRenderer.getStringWidth(button.getFirst()), contentRenderer.getHeight(), -5)) {
                this.roundedRect(buttonsX, buttonsY, contentRenderer.getStringWidth(button.getFirst()), contentRenderer.getHeight(), 2, 5, new Color(23, 23, 23, (int) (intAlpha * 0.4)));

                if (Mouse.isButtonDown(0) && !previousMouse) {
                    previousMouse = true;
                    button.getSecond().run();
                    this.close();
                }

            }

            contentRenderer.drawString(button.getFirst(), buttonsX, buttonsY, RenderSystem.hexColor(233, 233, 233, intAlpha));

            buttonsX = buttonsX - (contentRenderer.getStringWidth(button.getFirst()) + buttonsSpacing);
        }

        this.disposeTransforms();

        if (!Mouse.isButtonDown(0) && previousMouse) {
            previousMouse = false;
        }
    }

    public static Tuple<String, Runnable> buildAction(String label, Runnable onClick) {
        return new Tuple<>(label, onClick);
    }
}
