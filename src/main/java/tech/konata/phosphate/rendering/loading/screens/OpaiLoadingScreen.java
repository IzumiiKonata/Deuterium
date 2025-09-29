package tech.konata.phosphate.rendering.loading.screens;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.Location;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjglx.opengl.Display;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.loading.LoadingRenderer;
import tech.konata.phosphate.rendering.loading.LoadingScreenRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.utils.other.SplashGenerator;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;
import java.time.Duration;

/**
 * @author IzumiiKonata
 * Date: 2025/3/2 20:29
 */
public class OpaiLoadingScreen extends LoadingScreenRenderer implements SharedRenderingConstants {

    float screenMaskAlpha = 1.0f;

    Timer timer = new Timer();

//    CFontRenderer pf40;

    @Override
    public void init() {
        super.init();
//        pf40 = new CFontRenderer(FontManager.fontFromTTF("pf_normal.ttf", 40, Font.PLAIN), true, true);
        timer.reset();
    }

    Animation circleAnim = new Animation(Easing.EASE_IN_QUAD, Duration.ofMillis(1500));
    Animation maskAnim = new Animation(Easing.EASE_IN_QUAD, Duration.ofMillis(1500));
    Animation rotAnim = new Animation(Easing.EASE_IN_QUAD, Duration.ofMillis(-1));
    Animation scaleAnim = new Animation(Easing.EASE_IN_QUAD, Duration.ofMillis(750));
    boolean animOpposite = false;

    Animation scale1 = new Animation(Easing.EASE_OUT_QUART, Duration.ofMillis(500));
    Animation scale2 = new Animation(Easing.EASE_OUT_QUART, Duration.ofMillis(500));
    Animation scale3 = new Animation(Easing.EASE_OUT_QUART, Duration.ofMillis(500));

    float rot = 0.0F;

    boolean finished = false;

    Timer t = new Timer();

    @Override
    public void render(int width, int height) {
        if (timer.isDelayed(500)) {
            screenMaskAlpha = Interpolations.interpBezier(screenMaskAlpha, 0, 0.1f);
        }

        Image.draw(Location.of("Phosphate/textures/opai/full.png"), 10, 10, 10, 10, Image.Type.Normal);

        Rect.draw(0, 0, Display.getWidth(), Display.getHeight(), hexColor(38, 30, 32), Rect.RectType.EXPAND);

        if (t.isDelayed(7000)) {
            t.reset();
//            finished = !finished;
            finished = true;
        }

        double size = 120;

        if (finished) {

            if (rotAnim.getDuration() == 0L) {
                rotAnim.reset2();
                rotAnim.setEasing(Easing.EASE_IN_OUT_CUBIC);
                rotAnim.setDuration(Duration.ofMillis(750));
                rotAnim.setStartValue(rot + 360.0F);
                rotAnim.setValue(rot + 360.0F);
                rotAnim.setDestinationValue(90.0f);
            } else {
                rot = (float) rotAnim.run(rotAnim.getDestinationValue());
            }

        } else {
            rotAnim.setDuration(Duration.ofNanos(0));

            rot -= (float) (2f * RenderSystem.getFrameDeltaTime());
            rot = rot % 360.0f;
        }

//        if (finished) {
//            circleAnim.setDuration(Duration.ofMillis(250));
//        }

        if (finished) {

            if (Duration.ofNanos(circleAnim.getDuration()).toMillis() == 1500L) {
                double value = circleAnim.getValue();
                circleAnim.setEasing(Easing.EASE_IN_OUT_CUBIC);
                circleAnim.setDuration(Duration.ofMillis(750));
                circleAnim.reset2();
                circleAnim.setStartValue(value);
                circleAnim.setValue(value);
                circleAnim.setDestinationValue(330.0F);
            }

            circleAnim.run(330.0F);
        } else {
            circleAnim.setEasing(Easing.EASE_IN_QUAD);
            circleAnim.setDuration(Duration.ofMillis(1500));
            circleAnim.run(animOpposite ? 0 : 300F);
        }

        if (circleAnim.isFinished()) {

            if (!finished) {
                animOpposite = !animOpposite;
                if (!animOpposite) {
                    rot += 60f;
                }
            }

        }

        if (finished && circleAnim.getProgress() >= 0.5) {
            scale1.run(1);

            if (scale1.getProgress() >= 0.25) {
                scale2.run(1);

                if (scale2.getProgress() >= 0.25) {
                    scale3.run(1);
                } else {
                    scale3.reset2();
                }

            } else {
                scale2.reset2();
            }
        } else {
            scale1.reset2();
            scale1.setValue(0.4);
            scale1.setStartValue(0.4);
            scale1.setDestinationValue(1);
            scale1.setDuration(Duration.ofMillis(500));

            scale2.reset2();
            scale2.setValue(0.4);
            scale2.setStartValue(0.4);
            scale2.setDestinationValue(1);
            scale2.setDuration(Duration.ofMillis(500));

            scale3.reset2();
            scale3.setValue(0.4);
            scale3.setStartValue(0.4);
            scale3.setDestinationValue(1);
            scale3.setDuration(Duration.ofMillis(500));
        }

        double sz = 580;
        float rot2 = 5f;

        if (finished && circleAnim.getProgress() >= 0.5) {

            if (scale3.getProgress() <= 0.7) {
                GlStateManager.pushMatrix();
                scaleAtPos(width * 0.5f, height * 0.5f, 0.9);
                scaleAtPos(width * 0.5f, height * 0.5f, 1 - scaleAnim.getValue());

                scaleAtPos(width * 0.5f, height * 0.5f, scale3.getValue());
                rotateAtPos(width * 0.5f, height * 0.5f, (float) (-rot2 + Math.min(1, scale3.getProgress() * 1.5) * rot2));
                Image.draw(Location.of("Phosphate/textures/opai/3.png"), width * 0.5 - sz * 0.5, height * 0.5 - sz * 0.5, sz, sz, Image.Type.Normal);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                scaleAtPos(width * 0.5f, height * 0.5f, 0.9);
                scaleAtPos(width * 0.5f, height * 0.5f, 1 - scaleAnim.getValue());

                scaleAtPos(width * 0.5f, height * 0.5f, scale2.getValue());
                rotateAtPos(width * 0.5f, height * 0.5f, (float) (-rot2 + Math.min(1, scale2.getProgress() * 1.5) * rot2));
                Image.draw(Location.of("Phosphate/textures/opai/2.png"), width * 0.5 - sz * 0.5, height * 0.5 - sz * 0.5, sz, sz, Image.Type.Normal);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                scaleAtPos(width * 0.5f, height * 0.5f, 0.9);
                scaleAtPos(width * 0.5f, height * 0.5f, 1 - scaleAnim.getValue());

                scaleAtPos(width * 0.5f, height * 0.5f, scale1.getValue());
                rotateAtPos(width * 0.5f, height * 0.5f, (float) (-rot2 + Math.min(1, scale1.getProgress() * 1.5) * rot2));
                Image.draw(Location.of("Phosphate/textures/opai/1.png"), width * 0.5 - sz * 0.5, height * 0.5 - sz * 0.5, sz, sz, Image.Type.Normal);
                GlStateManager.popMatrix();
            } else {
                sz = 316;
                Image.draw(Location.of("Phosphate/textures/opai/full.png"), width * 0.5 - sz * 0.5, height * 0.5 - sz * 0.5, sz, sz, Image.Type.Normal);
            }

        }

        GlStateManager.pushMatrix();

        rotateAtPos(width * 0.5f, height * 0.5f, rot);

        scaleAnim.setEasing(Easing.EASE_IN_OUT_CUBIC);
        scaleAnim.setDestinationValue(finished ? 0.4 : 0);
        scaleAnim.setProgress(circleAnim.getProgress());
        scaleAtPos(width * 0.5f, height * 0.5f, 1 - scaleAnim.getValue());
//        rotateAtPos(width * 0.5f, height * 0.5f, base);

        if (animOpposite) {
            rotateAtPos(width * 0.5f, height * 0.5f, (float) circleAnim.getValue() + 60f);
        }

        Shaders.ROUND_SHADER.draw((float) (width * 0.5f - size), (float) (height * 0.5f - size), (float) size, (float) circleAnim.getValue() + 30F, new Color(232, 168, 139));

        GlStateManager.popMatrix();

        size = size * 0.85 - (finished ? size * 0.85 : 0) * circleAnim.getProgress();
        Shaders.ROUND_SHADER.draw((float) (width * 0.5f - size), (float) (height * 0.5f - size), (float) size, 360F, new Color(38, 30, 32));

//        roundedOutline(width * 0.5 - size * 0.5, height * 0.5 - size * 0.5, size, size, (size) * 0.45, 20, new Color(232, 168, 139));

        Rect.draw(0, 0, Display.getWidth(), Display.getHeight(), hexColor(0, 0, 0, (int) (screenMaskAlpha * 255)), Rect.RectType.EXPAND);
    }


    public void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }

    @Override
    public void onGameLoadFinishedNotify() {
//        finished = true;
    }

    @Override
    public boolean isLoadingScreenFinished() {
//        return false;
        return LoadingRenderer.progress == 100 && scale3.isFinished();
    }

}
