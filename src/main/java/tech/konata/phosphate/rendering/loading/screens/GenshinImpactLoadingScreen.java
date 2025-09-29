package tech.konata.phosphate.rendering.loading.screens;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.loading.LoadingScreenRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import java.util.Collections;

public class GenshinImpactLoadingScreen extends LoadingScreenRenderer {


    FadeInOutImage gs1, gs2;

    Timer startTimer = new Timer();
    boolean firstFrame = false;

    @Override
    public void init() {
        super.init();

        gs1 = new FadeInOutImage(Location.of(Phosphate.NAME + "/textures/loadingscreen/genshin/gs1.png"));
        gs2 = new FadeInOutImage(Location.of(Phosphate.NAME + "/textures/loadingscreen/genshin/gs2.png"));

        CloudMusic.initialize("");
        Music music = Music.getMusicById(1455706958);
        CloudMusic.play(Collections.singletonList(music), 0);

        CloudMusic.loadMusicCover(music);
    }

    @Override
    public void render(int width, int height) {
        Rect.draw(0, 0, width, height, RenderSystem.hexColor(255, 255, 255), Rect.RectType.ABSOLUTE_POSITION);

        if (!firstFrame) {
            firstFrame = true;
            startTimer.reset();
        }

        if (!startTimer.isDelayed(1000))
            return;

        if (!gs1.isFinished())
            gs1.render(width, height);
        else
            gs2.render(width, height);
    }

    @Override
    public boolean isLoadingScreenFinished() {
        return gs1.isFinished() && gs2.isFinished();
    }

    private static class FadeInOutImage {

        @Getter
        private final Location img;

        float screeMaskAlpha = 0;
        boolean increasing = true;

        @Getter
        boolean finished = false;

        boolean firstFrame = false;

        Timer timer = new Timer();

        public FadeInOutImage(Location loc) {
            img = loc;
        }

        public void render(int width, int height) {

            if (!firstFrame) {
                firstFrame = true;
                timer.reset();
            }

            if (increasing || timer.isDelayed(2000)) {
                screeMaskAlpha += increasing ? 1 * RenderSystem.DIVIDE_BY_255 : -1 * RenderSystem.DIVIDE_BY_255;
            }

            if ((!increasing && screeMaskAlpha < 0.01))
                finished = true;

            if (increasing && screeMaskAlpha > 0.99) {
                increasing = false;
                timer.reset();
            }

            GlStateManager.color(1, 1, 1, screeMaskAlpha);
//            Image.draw(img, 0, 0, width, height, Image.Type.NoColor);

            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);
            Minecraft.getMinecraft().getTextureManager().bindTexture(img);

            RenderSystem.linearFilter();

            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

        }

    }
}
