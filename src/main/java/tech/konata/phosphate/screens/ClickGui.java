package tech.konata.phosphate.screens;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.interfaces.SharedConstants;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.FramebufferCaching;
import tech.konata.phosphate.rendering.TextureReader;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.utils.cursor.CursorUtils;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.animation.MultipleEndpointAnimation;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.clickgui.ModuleSettings;
import tech.konata.phosphate.screens.clickgui.Panel;
import tech.konata.phosphate.screens.clickgui.panels.ModulesPanel;
import tech.konata.phosphate.screens.clickgui.panels.MusicPanel;
import tech.konata.phosphate.screens.clickgui.panels.OverviewPanel;
import tech.konata.phosphate.screens.clickgui.panels.SettingsPanel;
import tech.konata.phosphate.screens.dialog.impl.DialogHowToMoveWidgets;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.utils.timing.Timer;
import tech.konata.phosphate.widget.impl.MusicWidget;

import javax.xml.soap.Text;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;

/**
 * @author IzumiiKonata
 * @since 2023/12/23
 */
public class ClickGui extends BaseScreen {

    @Getter
    private static final ClickGui instance = new ClickGui();
//    final Animation openCloseAnimation = new Animation(Easing.BEZIER, Duration.ofMillis(250));

    public MultipleEndpointAnimation scaleAnimation;
    Animation panelAlphaAnimation = new Animation(Easing.LINEAR, Duration.ofMillis(150));

    final List<Panel> panels = new ArrayList<>();

    public final OverviewPanel overviewPanel = new OverviewPanel();

    public final ModulesPanel modulesPanel = new ModulesPanel();

    public final MusicPanel musicPanel = new MusicPanel();

    public final SettingsPanel settingsPanel = new SettingsPanel();

    public Panel currentPanel, previousPanel;

    boolean closing = false;

    public double posX = 100, posY = 100, width = 664, height = 422;

    public ModuleSettings settingsRenderer = null;

    boolean isFirstTime;

    public ClickGui() {

        this.panels.addAll(Arrays.asList(this.overviewPanel, this.modulesPanel, this.musicPanel, this.settingsPanel));

        this.panels.forEach(Panel::init);

        this.currentPanel = this.panels.get(0);

        isFirstTime = Phosphate.getInstance().getConfigManager().isFirstTime();
    }

    final Timer avoidDwheelTimer = new Timer();

    @Override
    public void initGui() {
        closing = false;

        scaleAnimation = new MultipleEndpointAnimation(Easing.EASE_IN_OUT_QUAD, Duration.ofMillis(200), 0.0)
                .addEndpoint(0.0, Duration.ofMillis(200))
                .addEndpoint(1.05, Duration.ofMillis(200))
//                .addEndpoint(0.95, Duration.ofMillis(300))
                .addEndpoint(1.0, Duration.ofMillis(100));

        this.scaleAnimation.reset();

        this.currentPanel.onSwitchedTo();
        avoidDwheelTimer.reset();

        if (isFirstTime) {
            isFirstTime = false;
//            this.setDialog(new DialogHowToMoveWidgets());
        }

//        for (Notification notification : NotificationManager.getNotifications()) {
//            if (notification.forever) {
//                notification.stayTime = 100L;
//                notification.forever = false;
//            }
//        }
//
//        NotificationManager.show("Test", "Test Notificationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", Notification.Type.INFO, 2000);
        bTest = true;
    }

    boolean bTest = false;

    final Random random = new Random();

    double size = 0.25;

    double texX = getNextValidPos(size)[0], texY = getNextValidPos(size)[1], destX = 0, destY = 0;

    private boolean isValid(double texSize, double x, double y) {
        return x >= 0 && y >= 0 && x + texSize <= 1 && y + texSize <= 1;
    }

    private double[] getNextValidPos(double texSize) {

        // [0, 1 - textureSize]

        float x = random.nextFloat();
        float y = random.nextFloat();

        while (!isValid(texSize, x, y)) {
            x = random.nextFloat();
            y = random.nextFloat();
        }

        return new double[] { x, y };
    }

    float musicBgAlpha = 0.0f;
    ITextureObject prevBg = null;
    Music prevMusic = null;

    int ovrDWheel = 0;

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        this.scaleAnimation.run(closing);
        this.panelAlphaAnimation.run(closing ? 0 : 1);
        this.scaleAtPos(posX + width * 0.5, posY + height * 0.5, this.scaleAnimation.run(closing));

        this.roundedRect(posX, posY, width, height, 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        Location musicCoverBlured = CloudMusic.currentlyPlaying == null ? null : musicPanel.getMusicCoverBlured(CloudMusic.currentlyPlaying);
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        ITextureObject texture = CloudMusic.currentlyPlaying == null ? null : textureManager.getTexture(musicCoverBlured);

        if (CloudMusic.currentlyPlaying != null && (texture != null || prevBg != null)) {

            if (CloudMusic.currentlyPlaying != prevMusic) {
                prevBg = prevMusic == null ? null : textureManager.getTexture(MusicWidget.getMusicCoverBlurred(prevMusic));
                prevMusic = CloudMusic.currentlyPlaying;
                musicBgAlpha = 0.0f;
            }

            if (prevBg != null && musicBgAlpha < 0.99f) {
                GlStateManager.bindTexture(prevBg.getGlTextureId());
                RenderSystem.linearFilter();
                this.roundedRectTextured(posX, posY, width, height, texX, texY, texX + size, texY + size, 10, 1);
            }

            if (texture != null) {
                this.musicBgAlpha = Interpolations.interpBezier(this.musicBgAlpha, 1.0f, prevBg == null ? 0.15f : 0.05f);
                GlStateManager.bindTexture(texture.getGlTextureId());
                RenderSystem.linearFilter();
                this.roundedRectTextured(posX, posY, width, height, texX, texY, texX + size, texY + size, 10, this.musicBgAlpha);
            }

            if (texX == destX && texY == destY) {
                double[] pos = this.getNextValidPos(size);
                destX = pos[0];
                destY = pos[1];
            } else {
                float speed = 0.000121875f;

                if (CloudMusic.currentlyPlaying != null && CloudMusic.player != null && CloudMusic.player.player.isPlaying()) {
                    texX = Interpolations.interpLinear((float) texX, (float) destX, speed);
                    texY = Interpolations.interpLinear((float) texY, (float) destY, speed);
                }
            }

//            RenderSystem.roundedRect(posX, posY, width, height, 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, (int) (alpha * 0.3f * 255)));

        } else if (texture != null) {
            this.musicBgAlpha = Interpolations.interpBezier(this.musicBgAlpha, 0.0f, 0.15f);
        }

        Stencil.write();
        Rect.draw(posX, posY, 33, height, -1, Rect.RectType.EXPAND);
        Stencil.erase();
        this.roundedRect(posX, posY, width, height, 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));
        Stencil.dispose();

        int dWheel = Mouse.getDWheel2();

        if (!avoidDwheelTimer.isDelayed(100))
            dWheel = 0;

        if (this.settingsRenderer != null) {
            ovrDWheel = dWheel;
            dWheel = 0;
        }

        this.renderWidgetButton(mouseX, mouseY);

        this.renderPanelList(mouseX, mouseY);

        this.renderPanel(mouseX, mouseY, dWheel);

        if (closing && this.scaleAnimation.isFinished(true))
            SharedConstants.mc.displayGuiScreen(null);

        if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            width = 664;
            height = 422;
            prevPanelAnim = -(width - 42);

            posX = posY = 100;
        }
    }

    public double prevPanelAnim = 0;

    public void renderPanel(double mouseX, double mouseY, int dWheel) {

        double panelWidth = 38, panelHeight = height, spacing = 4;

//        this.roundedRect(posX + panelWidth, posY + spacing, width - panelWidth - spacing, height - spacing * 2, 5, ThemeManager.getAsColor(ThemeManager.ThemeColor.BaseLighter, 160));
        prevPanelAnim = Interpolations.interpBezier(prevPanelAnim, -(width - panelWidth - spacing), 0.25f);

        if (this.previousPanel != null) {

            RenderSystem.doScissor((int) (posX + panelWidth), (int) (posY + spacing), (int) (width - panelWidth - spacing), (int) (height - spacing * 2));
            RenderSystem.forceDisableScissor = true;

            this.previousPanel.posX = (posX + panelWidth + prevPanelAnim);
            this.previousPanel.posY = (posY + spacing);
            this.previousPanel.width = (width - panelWidth - spacing);
            this.previousPanel.height = (height - spacing * 2);
            this.previousPanel.draw(-1, -1, 0);

//            Rect.draw((int) (posX + panelWidth), (int) (posY + spacing), 200, 300, 0xff0090ff, Rect.RectType.EXPAND);


        }

        this.currentPanel.posX = (posX + panelWidth + width - panelWidth - spacing + prevPanelAnim);
        this.currentPanel.posY = (posY + spacing);
        this.currentPanel.width = (width - panelWidth - spacing);
        this.currentPanel.height = (height - spacing * 2);

        this.currentPanel.draw(mouseX, mouseY, dWheel);

        if (this.previousPanel != null) {
            RenderSystem.forceDisableScissor = false;
            RenderSystem.endScissor();

            if (prevPanelAnim <= -(width - panelWidth - spacing) + 5) {
                previousPanel = null;
            }
        }

        if (this.settingsRenderer != null)
            this.settingsRenderer.render(posX + panelWidth, posY + spacing, width - panelWidth - spacing, height - spacing * 2, mouseX, mouseY, ovrDWheel);

    }

    public float rwbHoverAlpha = 0.0f;

    public void renderWidgetButton(double mouseX, double mouseY) {
        double size = 26;
        double offsetX = posX + 4, offsetY = posY + height - 4 - size;

        this.roundedRectAccentColor(offsetX, offsetY, size, size, 6, 160);
        this.roundedRect(offsetX, offsetY, size, size, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, 100));

        SVGImage.draw(Location.of(Phosphate.NAME + "/textures/clickgui/panel/edit.svg"), offsetX + 4, offsetY + 4, 18, 18, ThemeManager.get(ThemeManager.ThemeColor.Text));

        boolean hovered = isHovered(mouseX, mouseY, offsetX, offsetY, size, size);

        if (hovered) {
            rwbHoverAlpha = Interpolations.interpBezier(rwbHoverAlpha, 0.2f, 0.4f);

            if (Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;
                mc.displayGuiScreen(new MoveWidgetsScreen());
            }

        } else {
            rwbHoverAlpha = Interpolations.interpBezier(rwbHoverAlpha, 0.0f, 0.4f);
        }

        if (rwbHoverAlpha > 0.02) {
            this.roundedRect(offsetX, offsetY, size, size, 6, new Color(1, 1, 1, rwbHoverAlpha));
        }
    }

    public void renderPanelList(double mouseX, double mouseY) {

        double offsetX = posX + 8, offsetY = posY + 36;

        double selectorSpacing = 4;

        selectorAnim.run(this.panels.indexOf(currentPanel) * 28);

        this.roundedRectAccentColor(offsetX - selectorSpacing, offsetY - selectorSpacing + selectorAnim.getValue(), 26, selectorSpacing * 2 + 18, 5);

        for (Panel panel : this.panels) {

            SVGImage.draw(Location.of(Phosphate.NAME + "/textures/clickgui/panel/" + panel.getInternalName() + ".svg"), offsetX, offsetY, 18, 18, ThemeManager.get(ThemeManager.ThemeColor.Text));

            if (this.isHovered(mouseX, mouseY, offsetX - selectorSpacing, offsetY - selectorSpacing, 26, selectorSpacing * 2 + 18) && Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;

                if (panel != currentPanel)
                    prevPanelAnim = 0;

                if (panel != currentPanel) {
                    previousPanel = currentPanel;
                }

                currentPanel = panel;

                currentPanel.onSwitchedTo();

                if (this.settingsRenderer != null) {
                    this.settingsRenderer.closing = true;
                }
            }

            offsetY += 28;

        }

    }

    Animation selectorAnim = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(100));

    double moveX = 0, moveY = 0;
    double moveXW = 0, moveYW = 0;
    boolean dragging = false;

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (this.isHovered(mouseX, mouseY, posX, posY, width, 10) && mouseButton == 0) {
            moveX = mouseX - posX;
            moveY = mouseY - posY;
        }

        if (RenderSystem.isHovered(mouseX, mouseY, posX + width - 16, posY + height - 16, 24, 24) && this.getDialog() == null) {
            if (mouseButton == 0 && !dragging) {
                dragging = true;
            }
        }

        if (this.settingsRenderer != null) {
            this.settingsRenderer.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            this.currentPanel.mouseClicked(mouseX, mouseY, mouseButton);
        }


    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }

        if (mouseButton == 0 && dragging) {
            dragging = false;
        }

        if (this.settingsRenderer != null) {
            this.settingsRenderer.mouseReleased(mouseX, mouseY, mouseButton);
        } else {
            this.currentPanel.mouseReleased(mouseX, mouseY, mouseButton);
        }

    }

    @Override
    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {

        if (this.currentPanel == musicPanel && musicPanel.searchBox.isFocused())
            return;

        if (this.isHovered(mouseX, mouseY, posX, posY, width, 20) && mouseButton == 0) {

            if (moveX == 0 || moveY == 0) {
                moveX = mouseX - posX;
                moveY = mouseY - posY;
            }

            musicPanel.smoothSelectorY += mouseY - moveY - posY;
            settingsPanel.smoothSelectorY += mouseY - moveY - posY;

            posX = mouseX - moveX;
            posY = mouseY - moveY;
        }

        if (this.settingsRenderer != null) {
            this.settingsRenderer.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        } else {
            this.currentPanel.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        }

        if (RenderSystem.isHovered(mouseX, mouseY, posX + width - 16, posY + height - 16, 24, 24) && this.getDialog() == null) {
            this.setCursor(CursorUtils.RESIZE_NWSE);
        }

        if (dragging) {

            if (moveXW == 0 && moveYW == 0) {
                moveXW = mouseX - width;
                moveYW = mouseY - height;
            } else {
                if (mouseX - moveXW >= 664) {
                    prevPanelAnim -= mouseX - moveXW - width;
                    width = mouseX - moveXW;
                }

                if (mouseY - moveYW >= 422) {
                    height = mouseY - moveYW;
                }
            }
        } else if (moveXW != 0 || moveYW != 0) {
            moveXW = 0;
            moveYW = 0;
        }

    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {

        if (this.settingsRenderer != null) {
            this.settingsRenderer.onKeyTyped(typedChar, keyCode);
        } else {

            if (!this.currentPanel.keyTyped(typedChar, keyCode))
                if (keyCode == Keyboard.KEY_ESCAPE)
//                    closing = !closing;
                    closing = true;

        }
    }
}
