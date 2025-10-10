package tritium.screens;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.entities.RenderableEntity;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.screens.dialog.Dialog;
import tritium.settings.ClientSettings;
import tritium.utils.cursor.CursorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public class BaseScreen extends GuiScreen implements SharedRenderingConstants {

    final List<RenderableEntity> widgets = new ArrayList<>();

    public boolean lmbPressed = false, rmbPressed = false;

    int clickMoveTicks = 0;
    long lastClick = 0L;

    @Getter
    @Setter
    public Dialog dialog = null;

    public void drawScreen(double mouseX, double mouseY) {

    }

    public void onKeyTyped(char typedChar, int keyCode) {

    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {


    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {

    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {

    }

    public void clearAllWidgets() {

        for (RenderableEntity w : this.widgets) {
            this.clear(w);
        }

    }

    private void clear(RenderableEntity ent) {

        if (ent.hasChild()) {

            for (RenderableEntity w : ent.getContainer()) {
                this.clear(w);
            }

        }

        ent.getContainer().clear();

    }

    private void handleKeyTyped(char typedChar, int keyCode) {

        if (this.dialog != null) {
            this.dialog.keyTyped(typedChar, keyCode);
            return;
        }

        this.widgets.forEach(w -> w.keyTyped(typedChar, keyCode));

        this.onKeyTyped(typedChar, keyCode);
    }

    private void handleMouseClicked(double mouseX, double mouseY, int mouseButton) throws IOException {

        if (this.dialog != null) {
            this.dialog.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        this.widgets.forEach(w -> w.mouseClicked(mouseX, mouseY, mouseButton));

        clickMoveTicks = 0;
        lastClick = System.currentTimeMillis();

        this.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void handleMouseReleased(double mouseX, double mouseY, int mouseButton) {

        if (this.dialog != null) {
            this.dialog.mouseReleased(mouseX, mouseY, mouseButton);
            return;
        }

        this.widgets.forEach(w -> w.mouseReleased(mouseX, mouseY, mouseButton));

        if (mouseButton == 0) {
            this.lmbPressed = false;
        }

        if (mouseButton == 1) {
            this.rmbPressed = false;
        }

        this.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public void handleMouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {

//        if (this.dialog != null) {
//            this.dialog.handleMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
//            return;
//        }

        this.widgets.forEach(w -> w.handleMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick));

        this.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    public void renderLast(double mouseX, double mouseY) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawScreen((double) mouseX, (double) mouseY, partialTicks);
    }

    private long overrideMouseCursor = CursorUtils.ARROW;

    public void setCursor(long cursor) {
        overrideMouseCursor = cursor;
    }

    public final void drawScreen(double mX, double mY, float partialTicks) {

        overrideMouseCursor = CursorUtils.ARROW;

        boolean fixedScale = ClientSettings.FIXED_SCALE.getValue();

        double mouseX = fixedScale ? mX * RenderSystem.getScaleFactor() : mX;
        double mouseY = fixedScale ? mY * RenderSystem.getScaleFactor() : mY;

        RenderSystem.resetColor();

        if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {

            if (clickMoveTicks > 1) {
                this.handleMouseClickMove(mouseX, mouseY, Mouse.isButtonDown(0) ? 0 : 1, System.currentTimeMillis() - lastClick);
            }

            clickMoveTicks++;
        }

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        if (!Mouse.isButtonDown(1) && rmbPressed)
            rmbPressed = false;

        this.drawScreen(mouseX, mouseY);

        this.widgets.forEach(w -> w.draw(mouseX, mouseY));

        this.renderLast(mouseX, mouseY);

        if (this.dialog != null) {
            this.dialog.onRender(mouseX, mouseY);

            if (this.dialog.canClose())
                this.dialog = null;
        }

        CursorUtils.setCursor(overrideMouseCursor);

    }

    @Override
    @Deprecated
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.handleKeyTyped(typedChar, keyCode);
    }

    @Override
    @Deprecated
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.handleMouseClicked(ClientSettings.FIXED_SCALE.getValue() ? mouseX * RenderSystem.getScaleFactor() : mouseX, ClientSettings.FIXED_SCALE.getValue() ? mouseY * RenderSystem.getScaleFactor() : mouseY, mouseButton);
    }

    @Override
    @Deprecated
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.handleMouseReleased(ClientSettings.FIXED_SCALE.getValue() ? mouseX * RenderSystem.getScaleFactor() : mouseX, ClientSettings.FIXED_SCALE.getValue() ? mouseY * RenderSystem.getScaleFactor() : mouseY, mouseButton);
    }

}
