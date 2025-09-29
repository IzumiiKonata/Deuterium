package tech.konata.phosphate.screens.clickgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL30;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.rendering.FramebufferCaching;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.panels.MusicPanel;
import tech.konata.phosphate.screens.clickgui.settingrenderer.ColorRenderer;
import tech.konata.phosphate.screens.clickgui.settingrenderer.NumberRenderer;
import tech.konata.phosphate.settings.*;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;

/**
 * @author IzumiiKonata
 * @since 2023/12/30
 */
public class ModuleSettings implements SharedRenderingConstants {

    public final Module module;

    private final List<SettingRenderer<?>> renders = new ArrayList<>();
    public boolean closing = false;

    public ModuleSettings(Module moduleIn) {
        this.module = moduleIn;

        for (Setting<?> setting : this.module.getSettings()) {
            this.renders.add(SettingRenderer.of(setting));
        }

    }

    double scroll = 0, ySmooth = 0;

    Localizable lReset = Localizable.of("panel.music.eqreset");

    boolean lmbPressed = false;

    List<Runnable> blur = new ArrayList<>();

    public void render(double x, double y, double width, double height, double mouseX, double mouseY, int dWheel) {

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        GlStateManager.disableAlpha();

        this.renderBackground();

        this.renderPanel(x, y, width, height, mouseX, mouseY, dWheel);

        if (closing && this.panelScaleAnimation.isFinished()) {

            if (ClickGui.getInstance().currentPanel instanceof MusicPanel) {

                if (ClickGui.getInstance().settingsRenderer != null) {
                    ClickGui.getInstance().settingsRenderer = null;
                }

            } else {
                ClickGui.getInstance().settingsRenderer = null;
            }

            if (ColorRenderer.floatingPane != null) {
                ColorRenderer.floatingPane = null;
            }

        }

        if (ColorRenderer.floatingPane != null) {
            ColorRenderer.floatingPane.renderPane(mouseX, mouseY);
        }

    }

    Framebuffer panelFb = null;

    Animation panelScaleAnimation = new Animation(Easing.EASE_IN_OUT_SINE, Duration.ofMillis(150), 0.9);
    Animation panelAlphaAnimation = new Animation(Easing.LINEAR, Duration.ofMillis(150));

    public void renderPanel(double x, double y, double width, double height, double mouseX, double mouseY, int dWheel) {
        double panelWidth = width * 0.9;
        double panelHeight = height * 0.7;

        double panelX = x + width * 0.5 - panelWidth * 0.5;
        double panelY = y + height * 0.52 - panelHeight * 0.5;

        this.panelScaleAnimation.run(closing ? 0.9 : 1);
        this.panelAlphaAnimation.run(closing ? 0 : 1);

        double yAdd = 5;
        if (dWheel > 0)
            ySmooth -= yAdd;
        else if (dWheel < 0)
            ySmooth += yAdd;

        ySmooth = Interpolations.interpBezier(ySmooth, 0, 0.1f);
        scroll = Interpolations.interpBezier(scroll, scroll + ySmooth, 0.6f);

        if (scroll < 0)
            scroll = Interpolations.interpBezier(scroll, 0, 0.2f);

        panelFb = RenderSystem.createFrameBuffer(panelFb);
        panelFb.bindFramebuffer(true);
        panelFb.framebufferClearNoBinding();

        FramebufferCaching.setOverridingFramebuffer(panelFb);

        this.roundedRect(panelX, panelY, panelWidth, panelHeight, 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, 255));

        FontManager.pf40.drawString(this.module.getName().get(), panelX + 8, panelY - 4 - FontManager.pf40.getHeight(), ThemeManager.get(ThemeManager.ThemeColor.Text, 255));

        double settingX = panelX + 8, settingY = panelY + 8 - scroll;
        double settingWidthSmall = (panelWidth - 24) * 0.5;
        double settingWidthLarge = panelWidth - 16;
        double settingHeightAccum = 0;

        Stencil.write(panelFb);
        this.roundedRect(panelX, panelY, panelWidth, panelHeight, 10, -2, new Color(0, 0, 0, 255));
        Stencil.erase();

        for (SettingRenderer<?> render : this.renders) {

            if (!render.setting.shouldRender())
                continue;

            boolean bLarge = render instanceof NumberRenderer;

            if (bLarge) {
                render.width = settingWidthLarge;
                settingX = panelX + 8;

                if (settingHeightAccum != 0) {
                    settingY += settingHeightAccum;
                    settingHeightAccum = 0;
                }

            } else {
                render.width = settingWidthSmall;
            }

            render.x = settingX;
            render.y = settingY;

            render.height = height;

            double renderHeight = render.render(mouseX, mouseY, dWheel);

            if (bLarge) {
                settingX = panelX + 8;
                settingY += renderHeight;
            } else {

                if (settingX > panelX + 10) {
                    settingX = panelX + 8;

                    if (settingHeightAccum != 0) {
                        settingY += Math.max(renderHeight + 8, settingHeightAccum);

                        settingHeightAccum = 0;
                    } else {
                        settingY += renderHeight + 8;
                    }

                } else {
                    settingX += settingWidthSmall + 8;

                    settingHeightAccum += renderHeight;

                }

            }

        }

        Stencil.dispose();

        FramebufferCaching.removeCurrentlyBinding();

        Framebuffer fbMc = Minecraft.getMinecraft().getFramebuffer();

        fbMc.bindFramebuffer(true);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + width * 0.5, y + height * 0.5, 0);
        GlStateManager.scale(this.panelScaleAnimation.getValue(), this.panelScaleAnimation.getValue(), 1);
        GlStateManager.translate(-(x + width * 0.5), -(y + height * 0.5), 0);

        GlStateManager.color(1, 1, 1,(float) this.panelAlphaAnimation.getValue());
        Image.drawFlipped(panelFb.framebufferTexture, 0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), Image.Type.NoColor);
        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    }

    public void renderBackground() {
        Stencil.write();
        Rect.draw(ClickGui.getInstance().posX + 33, ClickGui.getInstance().posY, ClickGui.getInstance().width - 33, ClickGui.getInstance().height, -1, Rect.RectType.EXPAND);
        Stencil.erase();
        this.roundedRect(ClickGui.getInstance().posX, ClickGui.getInstance().posY, ClickGui.getInstance().width, ClickGui.getInstance().height, 10, new Color(0, 0, 0, (float) this.panelAlphaAnimation.getValue() * 0.5f));
        Stencil.dispose();
//
//        blur.add(() -> {
//            this.roundedRect(ClickGui.getInstance().posX, ClickGui.getInstance().posY, ClickGui.getInstance().width, ClickGui.getInstance().height, 10, new Color(0, 0, 0, (float) this.panelAlphaAnimation.getValue() * 0.5f));
//        });
//
//        Stencil.write();
//        Rect.draw(ClickGui.getInstance().posX + 33, ClickGui.getInstance().posY, ClickGui.getInstance().width - 33, ClickGui.getInstance().height, -1, Rect.RectType.EXPAND);
//        Stencil.erase();
//        Shaders.GAUSSIAN_BLUR_SHADER_SPECIAL.run(ShaderRenderType.OVERLAY, blur);
//        Stencil.dispose();
//
//        blur.clear();
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (ColorRenderer.floatingPane == null) {
            this.renders.forEach(r -> r.mouseClicked(mouseX, mouseY, mouseButton));
        }

    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (ColorRenderer.floatingPane == null) {
            this.renders.forEach(r -> r.mouseReleased(mouseX, mouseY, mouseButton));
        }
    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        if (ColorRenderer.floatingPane == null) {
            this.renders.forEach(r -> r.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick));
        }
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {

            if (this.module == GlobalSettings.dummyModule && ClickGui.getInstance().currentPanel == ClickGui.getInstance().settingsPanel) {
                return;
            }

            if (ColorRenderer.floatingPane != null) {
                ColorRenderer.floatingPane = null;
                return;
            }

            closing = true;
        }

        this.renders.forEach(r -> r.onKeyTyped(typedChar, keyCode));
    }

}
