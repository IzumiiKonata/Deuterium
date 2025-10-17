package tritium.screens.ncm;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shaders;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.BaseScreen;
import tritium.screens.ncm.panels.PlaylistsPanel;

import java.util.Collections;

/**
 * @author IzumiiKonata
 * Date: 2025/10/16 19:47
 */
public class NCMScreen extends BaseScreen {

    @Getter
    private static NCMScreen instance = new NCMScreen();

    float alpha = 0f;
    boolean closing = false;

    Panel basePanel = new Panel();

    @Getter
    PlaylistsPanel playlistsPanel;

    RectWidget currentPanelBg = new RectWidget();

    NCMPanel prevAnimatingPanel = null;
    NCMPanel currentPanel = null;
    float curPanelAlphaAnimation = 0f;

    @Override
    public void initGui() {

        alpha = 0f;
        closing = false;

        this.basePanel.getChildren().clear();

        this.layout();

        this.playlistsPanel = new PlaylistsPanel();
        this.basePanel.addChild(this.playlistsPanel);

        this.basePanel.addChild(this.currentPanelBg);

        this.currentPanelBg.setBeforeRenderCallback(() -> {
            this.currentPanelBg.setBounds(playlistsPanel.getWidth(), 0, this.currentPanelBg.getParentWidth() - playlistsPanel.getWidth(), this.getPanelHeight() * 0.93);
            this.currentPanelBg.setColor(getColor(ColorType.GENERIC_BACKGROUND));
        });
    }

    private void layout() {
        this.basePanel.setBounds(this.getPanelWidth(), this.getPanelHeight());

        RectWidget bg = new RectWidget();
        this.basePanel.addChild(bg);

        this.basePanel.setBeforeRenderCallback(() -> {
            this.basePanel.center();
        });
    }

    public double getSpacing() {
        return 16.0;
    }

    public double getPanelWidth() {
        return RenderSystem.getWidth() - this.getSpacing() * 2;
    }

    public double getPanelHeight() {
        return RenderSystem.getHeight() - this.getSpacing() * 2;
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        if (closing && alpha <= 0.02f)
            mc.displayGuiScreen(null);

        alpha = Interpolations.interpBezier(alpha, closing ? 0f : 1f, 0.4f);

        Shaders.GAUSSIAN_BLUR_SHADER.run(Collections.singletonList(() -> {
            Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), -1);
        }), alpha);

        int dWheel = Mouse.getDWheel2();

        GlStateManager.pushMatrix();
        this.scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 0.9 + (alpha * 0.1));
        this.basePanel.setAlpha(alpha);
        this.basePanel.renderWidget(mouseX, mouseY, dWheel);

        float alphaInterpolateSpeed = 0.4f;
        if (this.prevAnimatingPanel != null) {
            this.prevAnimatingPanel.setAlpha(Interpolations.interpBezier(this.prevAnimatingPanel.getAlpha(), 0f, alphaInterpolateSpeed));
            this.prevAnimatingPanel.setBounds(this.currentPanelBg.getX(), this.currentPanelBg.getY(), this.currentPanelBg.getWidth(), this.currentPanelBg.getHeight());

            GlStateManager.pushMatrix();
            this.scaleAtPos(this.currentPanelBg.getX() + this.currentPanelBg.getWidth() * .5, this.currentPanelBg.getY() + this.currentPanelBg.getHeight() * .5, 0.9 + (this.prevAnimatingPanel.getAlpha() * 0.1));
            this.prevAnimatingPanel.renderWidget(mouseX, mouseY, dWheel);
            GlStateManager.popMatrix();

            if (this.prevAnimatingPanel.getAlpha() <= 0.02f)
                this.prevAnimatingPanel = null;
        } else if (this.currentPanel != null) {
            curPanelAlphaAnimation = Interpolations.interpBezier(curPanelAlphaAnimation, 1f, alphaInterpolateSpeed);
            this.currentPanel.setAlpha(Math.min(this.basePanel.getAlpha(), curPanelAlphaAnimation));
            this.currentPanel.setBounds(this.currentPanelBg.getX(), this.currentPanelBg.getY(), this.currentPanelBg.getWidth(), this.currentPanelBg.getHeight());

            StencilClipManager.beginClip(() -> {
                Rect.draw(this.currentPanelBg.getX(), this.currentPanelBg.getY(), this.currentPanelBg.getWidth(), this.currentPanelBg.getHeight(), -1);
            });

            GlStateManager.pushMatrix();
            this.scaleAtPos(this.currentPanelBg.getX() + this.currentPanelBg.getWidth() * .5, this.currentPanelBg.getY() + this.currentPanelBg.getHeight() * .5, 1.1 - (curPanelAlphaAnimation * 0.1));

            this.currentPanel.renderWidget(mouseX, mouseY, dWheel);
            GlStateManager.popMatrix();

            StencilClipManager.endClip();
        }

        GlStateManager.popMatrix();
    }

    public void setCurrentPanel(NCMPanel panel) {
        this.prevAnimatingPanel = this.currentPanel;
        this.currentPanel = panel;
        if (panel != null) {
            this.currentPanel.onInit();
            this.currentPanel.setAlpha(0);
            this.curPanelAlphaAnimation = 0f;
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE)
            closing = true;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.basePanel.onMouseClickReceived(mouseX, mouseY, mouseButton);
//        this.playlistsPanel.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

    public enum ColorType {

        GENERIC_BACKGROUND,
        PRIMARY_TEXT,
        SECONDARY_TEXT;

    }

    public static int getColor(ColorType type) {

        switch (type) {
            case GENERIC_BACKGROUND:
                return 0x1E1E1E;
            case PRIMARY_TEXT:
                return 0xFFFFFF;
            case SECONDARY_TEXT:
                return 0x4B4B4B;
        }

        return 0;
    }
}
