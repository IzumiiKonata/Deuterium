package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import processing.sound.JSynFFT;
import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.settings.*;
import tech.konata.phosphate.widget.Widget;
import tech.konata.phosphate.widget.impl.musicspectrum.ExtendedSpectrumVisualizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 10:02 AM
 */
public class MusicSpectrum extends Widget {

    public ModeSetting<Style> style = new ModeSetting<>("Style", Style.Rect);

    public enum Style {
        Rect,
        Lines,
        Circle
    }

    public final BooleanSetting indicator = new BooleanSetting("Indicator", true, () -> this.style.getValue() == Style.Rect);

    public ColorSetting rectColor = new ColorSetting("Rect Color", new HSBColor(125, 125, 125, 200), () -> this.style.getValue() == Style.Rect);
    public ColorSetting indicatorColor = new ColorSetting("Indicator Color", new HSBColor(125, 125, 125, 200), () -> this.style.getValue() == Style.Rect && indicator.getValue());

    public ColorSetting lineColor = new ColorSetting("Line Color", new HSBColor(255, 255, 255, 200), () -> this.style.getValue() != Style.Rect);

    public BooleanSetting circleOutline = new BooleanSetting("Circle Outline", false, () -> this.style.getValue() == Style.Circle);

    public NumberSetting<Double> multiplier = new NumberSetting<>("Multiplier", 1.0, 0.1, 3.0, 0.1);

    float[] bandValues = new float[1];
    float[] renderSpectrum = new float[1];
    float[] renderSpectrumIndicator = new float[1];

    Map<Integer, Long> indicatorTimeStamp = new HashMap<>();

    ExtendedSpectrumVisualizer visualizer;

    public final JSynFFT.FFTCalcCallback callback = fft -> {

        if (!this.getShouldRender().get())
            return;

        if (visualizer == null || visualizer.getSampleRate() != CloudMusic.player.player.sampleRate() || visualizer.getFftSize() != JSynFFT.FFT_SIZE || true) {
            visualizer = new ExtendedSpectrumVisualizer(CloudMusic.player.player.sampleRate(), JSynFFT.FFT_SIZE, 1024, ExtendedSpectrumVisualizer.FrequencyDistribution.BARK_ENHANCED);
        }

        // 处理FFT数据

        bandValues = visualizer.processFFT(fft);
    };

    public MusicSpectrum() {
        super("Music Spectrum");
    }

    @Override
    public void onRender(boolean editing) {
        double spectrumWidth = RenderSystem.getWidth() / (double) renderSpectrum.length;

        double maximumSpectrum = 1;

        if (CloudMusic.player != null) {

            if (this.style.getValue() == Style.Circle || this.style.getValue() == Style.Rect || this.style.getValue() == Style.Lines) {
                int leng = (int) (bandValues.length * .5);
                if (renderSpectrum.length != leng) {
                    renderSpectrum = Arrays.copyOf(renderSpectrum, leng);
                    renderSpectrumIndicator = new float[leng];
                }

                for (int i = 0; i < leng; i++) {

                    float target = bandValues[i] * 16;

                    if (!Float.isFinite(target)) {
                        target = 0;
                    }

                    if (!CloudMusic.player.player.isPlaying()) {
                        target = 0;
                    }

                    renderSpectrum[i] = Interpolations.interpBezier(renderSpectrum[i], target, 3f);
                    maximumSpectrum = (Math.max(maximumSpectrum, target));
                }
            }

            GlStateManager.pushMatrix();

            double y = RenderSystem.getHeight();

            if (this.style.getValue() == Style.Rect) {

                PRE_SHADER.add(() -> {

                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);

                    int step = 2;
                    this.drawRect(RenderSystem.getWidth() / ((double) renderSpectrum.length / step), y, renderSpectrum.length, worldrenderer, step);

                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();

                    RenderSystem.resetColor();

                });
            }

            if (this.style.getValue() == Style.Lines) {
                NORMAL.add(() -> {

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                    RenderSystem.color(lineColor.getRGB(0));
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);
                    GL11.glLineWidth(2.0f);

                    GL11.glBegin(GL11.GL_LINE_STRIP);

                    GL11.glVertex2d(0, RenderSystem.getHeight());

                    for (int i = 0; i < renderSpectrum.length; i++) {

                        RenderSystem.color(lineColor.getRGB(i));

                        GL11.glVertex2d(spectrumWidth * i + spectrumWidth * 0.5, y + (/*CloudMusic.player.getThreshold()*/ -renderSpectrum[i]) * multiplier.getValue() * 10);

                    }

                    GL11.glVertex2d(RenderSystem.getWidth(), RenderSystem.getHeight());

                    GL11.glEnd();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);

                    RenderSystem.resetColor();
                });
            }

            if (this.style.getValue() == Style.Circle) {

                NORMAL.add(() -> {
                    int mSpectrumCount = renderSpectrum.length;

                    double coverSize = 112;

                    double centerX = this.getX() + this.getWidth() * 0.5;
                    double centerY = this.getY() + this.getHeight() * 0.5;

                    GlStateManager.enableBlend();
//                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                    GlStateManager.disableTexture2D();
                    GlStateManager.disableAlpha();

                    boolean playing = CloudMusic.currentlyPlaying != null/* && CloudMusic.player.player.getStatus() != MediaPlayer.Status.STOPPED*/;

                    alpha = Interpolations.interpBezier(alpha * 255, playing ? 255 : 0, playing ? 0.15f : 0.2f) * RenderSystem.DIVIDE_BY_255;

                    circleFb = RenderSystem.createFrameBuffer(circleFb);
                    circleFb.setFramebufferColor(0, 0, 0, 0);
                    circleFb.bindFramebuffer(true);
                    circleFb.framebufferClearNoBinding();

                    GL11.glEnable(GL11.GL_LINE_SMOOTH);
                    GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                    GL11.glLineWidth(1.5f);

                    double multiplier = this.multiplier.getValue() * 2;

                    int step = 8;

                    this.drawCircleSub(mSpectrumCount, -.5f, 180, true, step, centerX, centerY, coverSize, multiplier, alpha);

                    GlStateManager.pushMatrix();

                    GlStateManager.translate(centerX, centerY, 0);
                    GlStateManager.scale(-1, 1, 1);
                    GlStateManager.translate(-centerX, -centerY, 0);
                    this.drawCircleSub(mSpectrumCount, -.5f, 180, true, step, centerX, centerY, coverSize, multiplier, alpha);

                    GlStateManager.popMatrix();

//                    this.drawCircleSub(mSpectrumCount, 0, 180 - step * 0.5, true, step, centerX, centerY, coverSize, multiplier, alpha);

                    mc.getFramebuffer().bindFramebuffer(true);
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);

                    GlStateManager.enableTexture2D();
                    GlStateManager.color(1, 1, 1, 1);

                    GlStateManager.bindTexture(circleFb.framebufferTexture);
                    ShaderProgram.drawQuad();

                });

//                BLOOM.add(() -> {
//                    GlStateManager.enableTexture2D();
////                    System.out.println(CloudMusic.player.spectrum[0]/*  - CloudMusic.player.getThreshold() */);
//                    float v = (renderSpectrum[0]/*  - CloudMusic.player.getThreshold() */);
//
//                    GlStateManager.color(1, 1, 1, 0.5f + v * 0.5f);
//                    GlStateManager.bindTexture(circleFb.framebufferTexture);
//                    ShaderProgram.drawQuad();
//                });
            }
            if (this.style.getValue() == Style.Rect || this.style.getValue() == Style.Lines) {
                this.setWidth(-1);
//                this.setHeight(offset);
                this.setMovable(false);
            } else {

                if (this.style.getValue() == Style.Circle) {
                    this.setMovable(true);
                    this.setWidth(150);
                    this.setHeight(150);
                }
            }

            double range = 1 + (maximumSpectrum) * 0.0015;

            WidgetsManager.music.fftScale = Interpolations.interpBezier(WidgetsManager.music.fftScale, range, 0.6);
            GlStateManager.popMatrix();
        }

    }

    private Framebuffer circleFb = new Framebuffer(1, 1, false);

    private void drawCircleSub(int mSpectrumCount, float degree, double v, boolean second, int step, double centerX, double centerY, double coverSize, double multiplier, float alpha) {
        double degreeInRadians = Math.toRadians(degree); // 预先计算角度到弧度的转换

        double stepModifier = step;

        GL11.glBegin(GL11.GL_LINES);

        int last = mSpectrumCount - 1 - (mSpectrumCount - 1) % step;
        int i1 = second ? mSpectrumCount : 0;
        double v2 = 1;

        double v1 = (renderSpectrum[last]/*  - CloudMusic.player.getThreshold() */) * multiplier;

        for (int i = 0; i < mSpectrumCount; i += step) {
            double angle = (v / mSpectrumCount) * -(i + 1);
            double rad = degreeInRadians + Math.toRadians(angle);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);

            // 计算正负两个方向的stopX和stopY
            double positiveStopX = centerX + (coverSize * 0.5 + (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * sin;
            double positiveStopY = centerY + (coverSize * 0.5 + (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * cos;
            double negativeStopX = centerX + (coverSize * 0.5 - (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * sin;
            double negativeStopY = centerY + (coverSize * 0.5 - (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * cos;

            // 绘制正向和负向的线段
            int color = lineColor.getRGB((second ? i * v2 : i) + i1, (int) (alpha * 255));
            RenderSystem.color(color);
            GL11.glVertex2d(positiveStopX, positiveStopY);
            GL11.glVertex2d(negativeStopX, negativeStopY);
        }

        GL11.glEnd();

        if (circleOutline.getValue()) {
            // 绘制轮廓线
            GL11.glBegin(GL11.GL_LINE_STRIP);
            {
                // 绘制正向轮廓线
                double outlineAngle = stepModifier;
                double outlineRad = degreeInRadians + Math.toRadians(outlineAngle);
                double outlineSin = Math.sin(outlineRad);
                double outlineCos = Math.cos(outlineRad);

                double outlineX = centerX + (coverSize * 0.5 + v1) * outlineSin;
                double outlineY = centerY + (coverSize * 0.5 + v1) * outlineCos;
                GL11.glVertex2d(outlineX, outlineY);

                for (int i = 0; i < mSpectrumCount; i += step) {
                    double angle = (v / mSpectrumCount) * -(i + 1);
                    double rad = degreeInRadians + Math.toRadians(angle);
                    double sin = Math.sin(rad);
                    double cos = Math.cos(rad);

                    double stopX = centerX + (coverSize * 0.5 + (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * sin;
                    double stopY = centerY + (coverSize * 0.5 + (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * cos;

                    int color = lineColor.getRGB((second ? i * v2 : i) + i1, (int) (alpha * 255));
                    RenderSystem.color(color);
                    GL11.glVertex2d(stopX, stopY);
                }
            }
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_STRIP);
            {
                // 绘制负向轮廓线
                double outlineAngle = stepModifier;
                double outlineRad = degreeInRadians + Math.toRadians(outlineAngle);
                double outlineSin = Math.sin(outlineRad);
                double outlineCos = Math.cos(outlineRad);

                double outlineX = centerX + (coverSize * 0.5 - v1) * outlineSin;
                double outlineY = centerY + (coverSize * 0.5 - v1) * outlineCos;
                GL11.glVertex2d(outlineX, outlineY);

                for (int i = 0; i < mSpectrumCount; i += step) {
                    double angle = (v / mSpectrumCount) * -(i + 1);
                    double rad = degreeInRadians + Math.toRadians(angle);
                    double sin = Math.sin(rad);
                    double cos = Math.cos(rad);

                    double stopX = centerX + (coverSize * 0.5 - (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * sin;
                    double stopY = centerY + (coverSize * 0.5 - (renderSpectrum[i]/*  - CloudMusic.player.getThreshold() */) * multiplier) * cos;

                    int color = lineColor.getRGB((second ? i * v2 : i) + i1, (int) (alpha * 255));
                    RenderSystem.color(color);
                    GL11.glVertex2d(stopX, stopY);
                }
            }
            GL11.glEnd();
        }
    }
    float alpha = 0.0f;

    private void drawRect(double spectrumWidth, double y, int j, WorldRenderer worldRenderer, int step) {

        for (int i = 0; i < j; i += step) {

            if (indicator.getValue()) {
                float now = (-renderSpectrum[i]) * multiplier.getFloatValue() * 10;

                if (now < renderSpectrumIndicator[i]) {
                    renderSpectrumIndicator[i] = now;

                    if (indicatorTimeStamp.containsKey(i)) {
                        indicatorTimeStamp.replace(i, System.currentTimeMillis());
                    } else {
                        indicatorTimeStamp.put(i, System.currentTimeMillis());
                    }

                } else {
                    long timeStamp = indicatorTimeStamp.computeIfAbsent(i, k -> System.currentTimeMillis());

                    if (System.currentTimeMillis() - timeStamp > 150) {
                        renderSpectrumIndicator[i] = Interpolations.interpLinear(renderSpectrumIndicator[i], (float) 0, 10);
                    }

                }
            }

            double posX = spectrumWidth * i / step;
            double height = (/*CloudMusic.player.getThreshold()*/ -renderSpectrum[i]) * multiplier.getValue() * 10;

            double left = posX;
            double top = y;
            double right = posX + spectrumWidth;
            double bottom = y + height;

            if (left < right) {
                double i1 = left;
                left = right;
                right = i1;
            }

            if (top < bottom) {
                double j1 = top;
                top = bottom;
                bottom = j1;
            }

            int rgb = rectColor.getRGB(i);

            int a = (rgb >> 24 & 255);
            int r = (rgb >> 16 & 255);
            int g = (rgb >> 8 & 255);
            int b = (rgb & 255);

            worldRenderer.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
            worldRenderer.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
            worldRenderer.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
            worldRenderer.pos(left, top, 0.0D).color(r, g, b, a).endVertex();

//            Rect.draw(spectrumWidth * i, y, spectrumWidth, -renderSpectrum[i] - offset, new Color().getRGB(), Rect.RectType.EXPAND);


            if (indicator.getValue()) {

                posX = spectrumWidth * i / step;
                height = -1;

                left = posX;
                top = y + renderSpectrumIndicator[i] - 1;
                right = posX + spectrumWidth;
                bottom = y + renderSpectrumIndicator[i] - 1 + height;

                if (left < right) {
                    double i1 = left;
                    left = right;
                    right = i1;
                }

                if (top < bottom) {
                    double j1 = top;
                    top = bottom;
                    bottom = j1;
                }

                rgb = indicatorColor.getRGB(i);

                a = (rgb >> 24 & 255);
                r = (rgb >> 16 & 255);
                g = (rgb >> 8 & 255);
                b = (rgb & 255);

                worldRenderer.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
                worldRenderer.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
                worldRenderer.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
                worldRenderer.pos(left, top, 0.0D).color(r, g, b, a).endVertex();

//                Rect.draw(spectrumWidth * i, y + renderSpectrumIndicator[i] - 1, spectrumWidth, -1, new Color(125, 125, 125, 200).getRGB(), Rect.RectType.EXPAND);
            }

        }
    }

}
