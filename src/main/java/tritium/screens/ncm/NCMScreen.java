package tritium.screens.ncm;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.management.FontManager;
import tritium.ncm.OptionsUtil;
import tritium.ncm.music.CloudMusic;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.BaseScreen;
import tritium.screens.clickgui.music.LoginRenderer;
import tritium.screens.ncm.panels.ControlsBar;
import tritium.screens.ncm.panels.HomePanel;
import tritium.screens.ncm.panels.NavigateBar;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.util.ArrayList;
import java.util.List;

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
    NavigateBar playlistsPanel;

    RectWidget currentPanelBg = new RectWidget();

    float prevAnimatingPanelAlpha = 0f;
    NCMPanel prevAnimatingPanel = null;
    NCMPanel currentPanel = null;
    float curPanelAlphaAnimation = 0f;

    @Getter
    ControlsBar controlsBar;

    public MusicLyricsPanel musicLyricsPanel = null;

    public NCMScreen() {

    }

    @Override
    public void initGui() {
        alpha = 0f;
        closing = false;

        if (this.playlistsPanel == null) {
            this.layout();

            // only when logged in
            if (CloudMusic.profile != null)
                this.setCurrentPanel(new HomePanel());
        }

        if (CloudMusic.profile != null && CloudMusic.playLists != null && this.playlistsPanel.getPlaylistPanel().getChildren().stream().noneMatch(c -> c instanceof NavigateBar.PlaylistItem item && !item.getIcon().equals("A"))) {
            this.layout();
            this.setCurrentPanel(new HomePanel());
        }

        if (this.musicLyricsPanel != null)
            this.musicLyricsPanel.onInit();

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void layout() {
        this.basePanel.getChildren().clear();

        RectWidget bg = new RectWidget();
        this.basePanel.addChild(bg);

        this.basePanel.setBeforeRenderCallback(() -> {
            this.basePanel.center();
        });

        this.playlistsPanel = new NavigateBar();
        this.basePanel.addChild(this.playlistsPanel);

        this.basePanel.addChild(this.currentPanelBg);

        this.currentPanelBg.setBeforeRenderCallback(() -> {
            this.currentPanelBg.setBounds(playlistsPanel.getWidth(), 0, this.currentPanelBg.getParentWidth() - playlistsPanel.getWidth(), this.getPanelHeight() * 0.93);
            this.currentPanelBg.setColor(getColor(ColorType.GENERIC_BACKGROUND));
        });

        this.controlsBar = new ControlsBar();
        this.controlsBar.onInit();
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

//        Shaders.GAUSSIAN_BLUR_SHADER.run(Collections.singletonList(() -> {
//            Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), hexColor(1, 1, 1, alpha));
//        }));

        int dWheel = Mouse.getDWheel();

        GlStateManager.pushMatrix();
        this.scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 0.9 + (alpha * 0.1));

        this.basePanel.setBounds(this.getPanelWidth(), this.getPanelHeight());

        if (this.musicLyricsPanel == null || this.musicLyricsPanel.alpha <= .9f) {
            this.basePanel.setAlpha(alpha);
            this.basePanel.renderWidget(mouseX, mouseY, dWheel);

            float alphaInterpolateSpeed = 0.4f;
            if (this.prevAnimatingPanel != null) {
                this.prevAnimatingPanel.setAlpha(this.prevAnimatingPanelAlpha = Interpolations.interpBezier(this.prevAnimatingPanelAlpha, 0f, alphaInterpolateSpeed));
                this.prevAnimatingPanel.setBounds(this.currentPanelBg.getX(), this.currentPanelBg.getY(), this.currentPanelBg.getWidth(), this.currentPanelBg.getHeight());

                GlStateManager.pushMatrix();
                this.scaleAtPos(this.currentPanelBg.getX() + this.currentPanelBg.getWidth() * .5, this.currentPanelBg.getY() + this.currentPanelBg.getHeight() * .5, 0.9 + (this.prevAnimatingPanel.getAlpha() * 0.1));
                this.prevAnimatingPanel.renderWidget(mouseX, mouseY, dWheel);
                GlStateManager.popMatrix();

                if (this.prevAnimatingPanelAlpha <= 0.02f)
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

            this.controlsBar.setAlpha(alpha);
            this.controlsBar.setBounds(this.currentPanelBg.getX(), this.currentPanelBg.getY() + this.currentPanelBg.getHeight(), this.currentPanelBg.getWidth(), this.getPanelHeight() - this.currentPanelBg.getHeight());
            this.controlsBar.renderWidget(mouseX, mouseY, dWheel);
        }

        if (this.musicLyricsPanel != null) {
            StencilClipManager.beginClip(() -> {
                Rect.draw(basePanel.getX(), basePanel.getY(), basePanel.getWidth(), basePanel.getHeight(), -1);
            });
            Rect.draw(basePanel.getX(), basePanel.getY(), basePanel.getWidth(), basePanel.getHeight(), getColor(ColorType.GENERIC_BACKGROUND) | ((int) (this.musicLyricsPanel.alpha * 255)) << 24);
            this.musicLyricsPanel.onRender(mouseX, mouseY, basePanel.getX(), basePanel.getY(), basePanel.getWidth(), basePanel.getHeight(), dWheel);
            StencilClipManager.endClip();

            if (this.musicLyricsPanel.shouldClose())
                this.musicLyricsPanel = null;
        }

        boolean loggedIn = !OptionsUtil.getCookie().isEmpty();

        if (!loggedIn && this.loginRenderer == null) {
            this.loginRenderer = new LoginRenderer();
        }

        if (this.loginRenderer != null) {
            this.loginRenderer.render(mouseX, mouseY, basePanel.getX(), basePanel.getY(), basePanel.getWidth(), basePanel.getHeight(), basePanel.getAlpha());

            if (this.loginRenderer.canClose() && !OptionsUtil.getCookie().isEmpty()) {
                this.loginRenderer = null;
                MultiThreadingUtil.runAsync(() -> {
                    CloudMusic.loadNCM(OptionsUtil.getCookie());

                    MultiThreadingUtil.runOnMainThread(() -> {
                        this.layout();

                        if (CloudMusic.profile != null)
                            this.setCurrentPanel(new HomePanel());
                    });
                });
            }
        }

        this.renderDownloadingPanel();

        GlStateManager.popMatrix();
    }

    public boolean downloading = false;
    public double downloadProgress = 0;
    public String downloadSpeed = "0 b/s";
    float downloadPanelAlpha = 0.0f;

    private void renderDownloadingPanel() {
//        this.downloading = true;
        this.downloadPanelAlpha = Interpolations.interpBezier(this.downloadPanelAlpha, this.downloading ? 1f : 0f, 0.3f);

        if (this.downloadPanelAlpha <= 0.02f)
            return;

        double downloadPanelWidth = 240;
        double downloadPanelHeight = 60;
        double progressBarWidth = downloadPanelWidth - 16;
        double progressBarHeight = 8;

        double offsetY = 8 + -(8 + downloadPanelHeight) * (1 - downloadPanelAlpha);
        Rect.draw(RenderSystem.getWidth() * .5 - downloadPanelWidth * .5, offsetY, downloadPanelWidth, downloadPanelHeight, RenderSystem.reAlpha(0x202020, downloadPanelAlpha * alpha));
        FontManager.pf34bold.drawCenteredString("Downloading...", RenderSystem.getWidth() * .5, offsetY + 8, hexColor(1, 1, 1, downloadPanelAlpha * alpha));
        FontManager.pf25bold.drawCenteredString(String.valueOf(downloadSpeed), RenderSystem.getWidth() * .5, offsetY + 8 + FontManager.pf34bold.getHeight(), hexColor(1, 1, 1, downloadPanelAlpha * alpha));
        roundedRect(RenderSystem.getWidth() * .5 - progressBarWidth * .5, offsetY + downloadPanelHeight - 8 - progressBarHeight, progressBarWidth, progressBarHeight, 3, hexColor(1, 1, 1, .5f * downloadPanelAlpha * alpha));

        StencilClipManager.beginClip(() -> Rect.draw(RenderSystem.getWidth() * .5 - progressBarWidth * .5, offsetY + downloadPanelHeight - 8 - progressBarHeight, progressBarWidth * downloadProgress, progressBarHeight, -1));
        roundedRect(RenderSystem.getWidth() * .5 - progressBarWidth * .5, offsetY + downloadPanelHeight - 8 - progressBarHeight, progressBarWidth, progressBarHeight, 3, hexColor(1, 1, 1, downloadPanelAlpha * alpha));
        StencilClipManager.endClip();
    }

    public LoginRenderer loginRenderer = null;

    int currentActionPointer = 0;
    List<Runnable> actions = new ArrayList<>();

    public void setCurrentPanel(NCMPanel panel) {
        this.innerSetCurrentPanel(panel, true);

        if (panel != null) {
            Runnable action = () -> this.innerSetCurrentPanel(panel, false);

            if (actions.isEmpty()) {
                currentActionPointer = 0;
                actions.add(action);
            } else {
                ++ currentActionPointer;

                while (actions.size() > currentActionPointer + 1)
                    actions.remove(actions.size() - 1);

                if (currentActionPointer < actions.size()) {
                    actions.set(currentActionPointer, action);
                } else {
                    actions.add(action);
                }
            }
        }
    }

    private void innerSetCurrentPanel(NCMPanel panel, boolean shouldCallInit) {
        this.prevAnimatingPanel = this.currentPanel;
        this.prevAnimatingPanelAlpha = 1.0f;
        this.currentPanel = panel;
        if (panel != null) {
            if (shouldCallInit)
                this.currentPanel.onInit();
            this.currentPanel.setAlpha(0);
            this.curPanelAlphaAnimation = 0f;
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {

        if (this.basePanel.onKeyTypedReceived(typedChar, keyCode)) {
            return;
        }

        if (this.currentPanel != null && this.currentPanel.onKeyTypedReceived(typedChar, keyCode)) {
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {

            if (this.musicLyricsPanel != null)
                this.musicLyricsPanel.close();
            else
                closing = true;

        }

        if (keyCode == Keyboard.KEY_SPACE && CloudMusic.currentlyPlaying != null && CloudMusic.player != null && !CloudMusic.player.isFinished()) {
            if (CloudMusic.player.isPausing())
                CloudMusic.player.unpause();
            else
                CloudMusic.player.pause();
        }

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (musicLyricsPanel == null) {
            this.basePanel.onMouseClickReceived(mouseX, mouseY, mouseButton);

            if (this.currentPanel != null)
                this.currentPanel.onMouseClickReceived(mouseX, mouseY, mouseButton);

            this.controlsBar.onMouseClickReceived(mouseX, mouseY, mouseButton);

            // forward
            if (mouseButton == 4) {

                // is last
                if (currentActionPointer >= actions.size() - 1) {
                    currentActionPointer = actions.size() - 1;
                    return;
                } else {
                    currentActionPointer ++;
                    actions.get(currentActionPointer).run();
                }

            }
            // go back
            else if (mouseButton == 3) {
                if (currentActionPointer > 0) {
                    -- currentActionPointer;
                    actions.get(currentActionPointer).run();
                }
            }

        } else {
            this.musicLyricsPanel.mouseClicked(mouseX, mouseY, mouseButton);
        }

    }

    public enum ColorType {

        GENERIC_BACKGROUND,
        ELEMENT_BACKGROUND,
        ELEMENT_HOVER,
        PRIMARY_TEXT,
        SECONDARY_TEXT;

    }

    public static int getColor(ColorType type) {

        switch (type) {
            case GENERIC_BACKGROUND:
                return 0x1E1E1E;
            case ELEMENT_BACKGROUND:
                return 0x232323;
            case ELEMENT_HOVER:
                return 0x353535;
            case PRIMARY_TEXT:
                return 0xFFFFFF;
            case SECONDARY_TEXT:
                return 0x6B6B6B;
        }

        return 0;
    }
}
