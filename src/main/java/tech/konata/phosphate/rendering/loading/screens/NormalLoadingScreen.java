package tech.konata.phosphate.rendering.loading.screens;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjglx.opengl.Display;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.loading.LoadingRenderer;
import tech.konata.phosphate.rendering.loading.LoadingScreenRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.utils.other.SplashGenerator;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 4/24/2023 6:25 PM
 */
public class NormalLoadingScreen extends LoadingScreenRenderer implements SharedRenderingConstants {


    double progressWidth = 0;

    double pbWidth;
    double pbHeight;
    double pbOffsetY;

    float screenMaskAlpha = 1.0f;

    Timer timer = new Timer();

//    CFontRenderer pf40;

    @Override
    public void init() {
        super.init();
//        pf40 = new CFontRenderer(FontManager.fontFromTTF("pf_normal.ttf", 40, Font.PLAIN), true, true);
        timer.reset();
    }

    @Override
    public void render(int width, int height) {

        pbWidth = width - 80;
        pbHeight = 20;
        pbOffsetY = height * 0.8576923076923077; // curious why? cuz it's 6.0 / 7.0
        progressWidth = Interpolations.interpBezier(progressWidth, pbWidth * MathHelper.clamp_double(LoadingRenderer.progress / 100F, 0, 1), 0.2f);

        Rect.draw(0, 0, width, height, RenderSystem.hexColor(23, 23, 23), Rect.RectType.ABSOLUTE_POSITION);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);
        GlStateManager.bindTexture(SplashGenerator.t.getGlTextureId());

        drawModalRectWithCustomSizedTexture((Display.getWidth() - SplashGenerator.logo.getWidth()) / 2.0, (Display.getHeight() - SplashGenerator.logo.getHeight()) / 2.0, 0, 0, SplashGenerator.logo.getWidth(), SplashGenerator.logo.getHeight(), SplashGenerator.logo.getWidth(), SplashGenerator.logo.getHeight());

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        this.roundedRect(width / 2.0d - pbWidth / 2.0, pbOffsetY, pbWidth, pbHeight, 5, new Color(128, 128, 128));

        this.roundedRectAccentColor(width / 2.0d - pbWidth / 2.0, pbOffsetY, progressWidth, pbHeight, 5);

        if (timer.isDelayed(500)) {
            screenMaskAlpha = Interpolations.interpBezier(screenMaskAlpha, 0, 0.1f);
        }

        Rect.draw(0, 0, Display.getWidth(), Display.getHeight(), hexColor(0, 0, 0, (int) (screenMaskAlpha * 255)), Rect.RectType.EXPAND);
    }


    public void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }

    @Override
    public boolean isLoadingScreenFinished() {
        return LoadingRenderer.progress == 100
                && progressWidth >= pbWidth * LoadingRenderer.progress / 100.0 - 0.1;
    }
}
