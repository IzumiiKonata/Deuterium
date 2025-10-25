package tritium.rendering.loading.screens;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.Display;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.loading.LoadingRenderer;
import tritium.rendering.loading.LoadingScreenRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;
import tritium.utils.timing.Timer;

/**
 * @author IzumiiKonata
 * @since 4/24/2023 6:25 PM
 */
public class NormalLoadingScreen extends LoadingScreenRenderer implements SharedRenderingConstants {

    double progressWidth = 0;

    double pbWidth;

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
    public void render(double width, double height) {
        GlStateManager.disableAlpha();

        pbWidth = width * .45;
        progressWidth = Interpolations.interpBezier(progressWidth, pbWidth * MathHelper.clamp_double(LoadingRenderer.progress / 100F, 0, 1), 0.2f);

        ThemeManager.Theme theme = ClientSettings.THEME.getValue();

        int bgColor = theme == ThemeManager.Theme.Dark ? RenderSystem.hexColor(32, 32, 43, (int) (alpha * 255)) : RenderSystem.hexColor(235, 235, 235, (int) (alpha * 255));
        int progressBarColor = theme == ThemeManager.Theme.Light ? RenderSystem.hexColor(32, 32, 43, (int) (alpha * 255)) : RenderSystem.hexColor(235, 235, 235, (int) (alpha * 255));

        Rect.draw(0, 0, width, height, bgColor);

        GlStateManager.color(1, 1, 1, alpha);
        Image.draw(Location.of("tritium/textures/logo" + (theme == ThemeManager.Theme.Light ? "" : "_white") + ".png"), width / 2.0d - 64, height / 2.0d - 64, 128, 128, Image.Type.NoColor);

        Rect.draw(width / 2.0d - pbWidth / 2.0, height * 5.0 / 6.0, pbWidth, 4, RenderSystem.hexColor(128, 128, 128, (int) (alpha * 255)));
        Rect.draw(width / 2.0d - pbWidth / 2.0, height * 5.0 / 6.0, progressWidth, 4, progressBarColor);

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
