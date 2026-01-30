package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import tritium.ncm.music.AudioPlayer;
import tritium.ncm.music.CloudMusic;
import tritium.management.WidgetsManager;
import tritium.rendering.HSBColor;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.settings.ColorSetting;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;
import tritium.widget.Widget;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static tritium.widget.impl.MusicSpectrumWidget.Style.*;

/**
 * @author IzumiiKonata
 * Date: 2025/3/8 16:17
 */
public class MusicSpectrumWidget extends Widget {

    float[] renderSpectrum = new float[1];
    float[] renderSpectrumIndicator = new float[1];

    Map<Integer, Long> indicatorTimeStamp = new HashMap<>();

    public final ModeSetting<Style> style = new ModeSetting<>("Style", Rect);

    public enum Style {
        Rect,
        Waveform,
        Oscilloscope,
        Line
    }

    public final BooleanSetting compatMode = new BooleanSetting("Compact Mode", false);
    public final BooleanSetting indicator = new BooleanSetting("Indicator", true);
    public final ColorSetting rectColor = new ColorSetting("Rect Color", new HSBColor(125, 125, 125, 200));

    public final NumberSetting<Double> multiplier = new NumberSetting<>("Multiplier", 1.0, 0.1, 3.0, 0.1);
    public final BooleanSetting absVol = new BooleanSetting("Absolute Volume", true);
    public final BooleanSetting stereo = new BooleanSetting("Waveform Stereo", false);

    public final NumberSetting<Float> windowTime = new NumberSetting<Float>("Window Time (ms)", 16.0f, 4.0f, 256.0f, 0.1f) {
        @Override
        public void onValueChanged(Float last, Float now) {
            if (CloudMusic.player != null) {

                if (lastNSamples != now) {
                    CloudMusic.player.setListeners();
                    CloudMusic.player.spectrumDataLFilled = CloudMusic.player.spectrumDataRFilled = false;
                }

                lastNSamples = now;
            }
        }
    };
    private float lastNSamples = windowTime.getValue();

    public MusicSpectrumWidget() {
        super("Music Spectrum");

        indicator.setShouldRender(() -> style.getValue() == Rect);
        rectColor.setShouldRender(() -> style.getValue() == Rect);

        stereo.setShouldRender(() -> style.getValue() == Waveform || style.getValue() == Oscilloscope);
        windowTime.setShouldRender(() -> style.getValue() == Waveform || style.getValue() == Oscilloscope);

        multiplier.setShouldRender(() -> style.getValue() == Rect || style.getValue() == Line);
    }

    @Override
    public void onRender(boolean editing) {
        float offset = 170;

        AtomicReference<Double> spectrumWidth = new AtomicReference<>(RenderSystem.getWidth() / (double) renderSpectrum.length);

        double maximumSpectrum = 1;

        Style style = this.style.getValue();

        boolean compatMode = this.compatMode.getValue();



        if (CloudMusic.player != null) {

            boolean rect = style == Rect;
            boolean line = style == Line;
            boolean waveform = style == Waveform;
            boolean oscilloscope = style == Oscilloscope;

            if (compatMode || waveform || oscilloscope) {
                this.roundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 6, 0, 0, 0, 0.4f);
            }

//            if (this.visualizer != null) {
//                List<ExtendedSpectrumVisualizer.FrequencyBand> bands = visualizer.getBands();
//
//                double offsetY = 4;
//
//                for (ExtendedSpectrumVisualizer.FrequencyBand band : bands) {
//
//                    if (band.lowBin < 2040)
//                        continue;
//
//                    FontManager.pf18bold.drawString("Center: " + band.centerFreq + ", lowFreq: " + band.lowFreq + ", highFreq: " + band.highFreq + ", lowBin: " + band.lowBin + ", highBin: " + band.highBin, 4, offsetY, -1);
//                    offsetY += FontManager.pf18bold.getHeight();
//                }
//            }

            if (rect || line) {

                int leng = (int) (AudioPlayer.bandValues.length * .5);
                if (renderSpectrum.length != leng) {
                    renderSpectrum = Arrays.copyOf(renderSpectrum, leng);
                    renderSpectrumIndicator = new float[leng];
                }

                for (int i = 0; i < leng; i++) {

                    float target = AudioPlayer.bandValues[i] * (compatMode ? 8 : 16);

                    if (!Float.isFinite(target)) {
                        target = 0;
                    }

                    if (!CloudMusic.player.player.isPlaying()) {
                        target = 0;
                    }

                    float factor = 1f;
                    renderSpectrum[i] = Interpolations.interpBezier(renderSpectrum[i], target, (float) (2f - (this.absVol.getValue() ? 0 : .5f * (1 - WidgetsManager.musicInfo.volume.getValue()))) * factor);
                    maximumSpectrum = (Math.max(maximumSpectrum, target));
                }

            }

            GlStateManager.pushMatrix();

            if (rect) {

                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

                int zLayerFixer = 100;
                GlStateManager.translate(0, 0, -zLayerFixer);

                boolean gradientRect = this.rectColor.chroma.getValue();

                if (gradientRect) {
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                }

                GL11.glBegin(gradientRect ? GL11.GL_QUADS : GL11.GL_TRIANGLES);
                int step = compatMode ? 8 : 3;
                spectrumWidth.set((compatMode ? this.getWidth() : RenderSystem.getWidth()) / ((double) renderSpectrum.length / step));
                this.drawRect(spectrumWidth.get(), renderSpectrum.length, step, gradientRect);
                GL11.glEnd();

                if (gradientRect) {
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }

                GlStateManager.translate(0, 0, zLayerFixer);
            }

            if (waveform) {
                boolean stereo = this.stereo.getValue();

                double pWidgetHeight = stereo ? this.getHeight() * 0.5 : this.getHeight();

                // ⚠⚠⚠ race conditions 警告 ⚠⚠⚠
                if (CloudMusic.player.spectrumDataLFilled) {
                    GlStateManager.color(1, 1, 1, 1);
                    this.drawWaveSub(pWidgetHeight, false, CloudMusic.player.waveVertexesBufferBackend, CloudMusic.player.waveVertexes.length / 2);
                }

                if (stereo && CloudMusic.player.spectrumDataRFilled) {
                    double lineHeight = 0.5;
                    tritium.rendering.Rect.draw(this.getX() + 4, (float) (this.getY() + pWidgetHeight - lineHeight * 0.5), this.getWidth() - 8, (float) lineHeight, hexColor(255, 255, 255, 160));

                    GlStateManager.color(1, 1, 1, 1);
                    this.drawWaveSub(pWidgetHeight, true, CloudMusic.player.waveRightVertexesBufferBackend, CloudMusic.player.waveRightVertexes.length / 2);
                }

            }

            // 渲染oscilloscope模式
            if (oscilloscope) {
                boolean stereo = this.stereo.getValue();

                double pWidgetHeight = stereo ? this.getHeight() * 0.5 : this.getHeight();

                if (CloudMusic.player.oscilloscopeDataLFilled) {
                    GlStateManager.color(1, 1, 1, 1);
                    this.drawWaveSub(pWidgetHeight, false, CloudMusic.player.oscilloscopeVertexesBufferBackendL, CloudMusic.player.oscilloscopeVertexesL.length / 2);
                }

                if (stereo && CloudMusic.player.oscilloscopeDataRFilled) {
                    double lineHeight = 0.5;
                    tritium.rendering.Rect.draw(this.getX() + 4, (float) (this.getY() + pWidgetHeight - lineHeight * 0.5), this.getWidth() - 8, (float) lineHeight, hexColor(255, 255, 255, 160));

                    GlStateManager.color(1, 1, 1, 1);
                    this.drawWaveSub(pWidgetHeight, true, CloudMusic.player.oscilloscopeVertexesBufferBackendR, CloudMusic.player.oscilloscopeVertexesR.length / 2);
                }
            }

            if (line) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.disableTexture2D();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glLineWidth(compatMode ? .75f : 1.0f);

                GL11.glBegin(GL11.GL_LINE_STRIP);

                double shrink = 4;
                int step = compatMode ? 2 : 1;

                if (compatMode) {
                    GL11.glVertex2d(this.getX() + 4, this.getY() + this.getHeight() - shrink);
                    spectrumWidth.set((this.getWidth() - shrink * 2) / ((double) renderSpectrum.length / step));
                } else {
                    GL11.glVertex2d(0, RenderSystem.getHeight());
                }

                for (int i = 0; i < renderSpectrum.length; i += step) {
                    GlStateManager.color(1, 1, 1, 1);
                    double height = -renderSpectrum[i] * this.multiplier.getValue() * 10;

                    if (compatMode)
                        height = Math.max(height, -this.getHeight() + shrink * 2);

                    GL11.glVertex2d((compatMode ? (this.getX() + 4) : 0) + spectrumWidth.get() * (i + 1) / step + spectrumWidth.get() * 0.5, (compatMode ? (this.getY() + this.getHeight() - shrink) : RenderSystem.getHeight()) + height);
                }

                if (compatMode) {
                    GL11.glVertex2d(this.getX() + this.getWidth() - shrink, this.getY() + this.getHeight() - shrink);
                } else {
                    GL11.glVertex2d(RenderSystem.getWidth(), RenderSystem.getHeight());
                }

                GL11.glEnd();
            }

            if (rect) {
                this.setWidth(-1);
//                this.setHeight(offset);
            }

            if (waveform || oscilloscope || compatMode){
                this.setWidth(200);
                this.setHeight(80);
            }

            GlStateManager.popMatrix();
        }

    }

    public void drawWaveSub(double pWidgetHeight, boolean secondHalf, ByteBuffer bb, int vertCount) {
        double startX = this.getX() + 4;
        double startY = this.getY() + pWidgetHeight * 0.5 + (secondHalf ? pWidgetHeight : 0);

        if (bb != null) {
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            GL11.glDisable(GL11.GL_TEXTURE_2D);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1f);

            GlStateManager.pushMatrix();

            GlStateManager.translate(startX, startY, 0);

            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

            GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, bb);

            // 我操 不会真有人直接把几千几万个顶点全用glVertex喂给显卡吧
            GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, vertCount);

            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            GlStateManager.popMatrix();

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }


    private void drawRect(double spectrumWidth, int j, int step, boolean gradientRect) {

        boolean compatMode = this.compatMode.getValue();

        double shrink = 4;
        spectrumWidth -= (compatMode ? shrink * 2 : 0) / ((double) j / step);

        for (int i = 0; i < j; i += step) {

            double height = -renderSpectrum[i] * this.multiplier.getValue() * 10;

            if (compatMode)
                height = Math.max(height, -this.getHeight() + shrink * 2);

            if (this.indicator.getValue()) {

                if ((float) height < renderSpectrumIndicator[i]) {
                    renderSpectrumIndicator[i] = (float) height;

                    if (indicatorTimeStamp.containsKey(i)) {
                        indicatorTimeStamp.replace(i, System.currentTimeMillis());
                    } else {
                        indicatorTimeStamp.put(i, System.currentTimeMillis());
                    }

                } else {
                    long timeStamp = indicatorTimeStamp.computeIfAbsent(i, k -> System.currentTimeMillis());

                    if (System.currentTimeMillis() - timeStamp > 200) {
                        renderSpectrumIndicator[i] = Interpolations.interpLinear(renderSpectrumIndicator[i], (float) 6, 8);
                    }

                }
            }

            double posX = (compatMode ? this.getX() + shrink : 0) + spectrumWidth * i / step;
            double y = compatMode ? (this.getY() + this.getHeight() - shrink) : RenderSystem.getHeight();

            double left = posX;
            double top = y;
            double right = posX + spectrumWidth;
            double bottom = y + height;

            if (left > right) {
                double i1 = left;
                left = right;
                right = i1;
            }

            if (top > bottom) {
                double j1 = top;
                top = bottom;
                bottom = j1;
            }

            int rgb = this.rectColor.getRGB(i);
            int nextRgb = this.rectColor.getRGB(i + step);

            float a = (rgb >> 24 & 255) * RenderSystem.DIVIDE_BY_255;
            float r = (rgb >> 16 & 255) * RenderSystem.DIVIDE_BY_255;
            float g = (rgb >> 8 & 255) * RenderSystem.DIVIDE_BY_255;
            float b = (rgb & 255) * RenderSystem.DIVIDE_BY_255;
            float nextA = (nextRgb >> 24 & 255) * RenderSystem.DIVIDE_BY_255;
            float nextR = (nextRgb >> 16 & 255) * RenderSystem.DIVIDE_BY_255;
            float nextG = (nextRgb >> 8 & 255) * RenderSystem.DIVIDE_BY_255;
            float nextB = (nextRgb & 255) * RenderSystem.DIVIDE_BY_255;

            if (gradientRect) {
                GlStateManager.color(r, g, b, a);

                GL11.glVertex2d(left, top);
                GL11.glVertex2d(left, bottom);

                GlStateManager.color(nextR, nextG, nextB, nextA);

                GL11.glVertex2d(right, bottom);
                GL11.glVertex2d(right, top);
            } else {
                GlStateManager.color(r, g, b, a);

                GL11.glVertex2d(right, bottom);
                GL11.glVertex2d(left, top);
                GL11.glVertex2d(left, bottom);

                GL11.glVertex2d(right, top);
                GL11.glVertex2d(left, top);
                GL11.glVertex2d(right, bottom);
            }

            if (this.indicator.getValue()) {

//                posX = spectrumWidth * i / step;
                height = -1;

                left = posX;
                top = y + renderSpectrumIndicator[i] - 1;
                right = posX + spectrumWidth;
                bottom = y + renderSpectrumIndicator[i] - 1 + height;

                if (left > right) {
                    double i1 = left;
                    left = right;
                    right = i1;
                }

                if (top > bottom) {
                    double j1 = top;
                    top = bottom;
                    bottom = j1;
                }

                if (gradientRect) {
                    GlStateManager.color(r, g, b, a);

                    GL11.glVertex2d(left, top);
                    GL11.glVertex2d(left, bottom);

                    GlStateManager.color(nextR, nextG, nextB, nextA);

                    GL11.glVertex2d(right, bottom);
                    GL11.glVertex2d(right, top);
                } else {
                    GL11.glVertex2d(right, bottom);
                    GL11.glVertex2d(left, top);
                    GL11.glVertex2d(left, bottom);

                    GL11.glVertex2d(right, top);
                    GL11.glVertex2d(left, top);
                    GL11.glVertex2d(right, bottom);
                }
            }

        }

    }

}
