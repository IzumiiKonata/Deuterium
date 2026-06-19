package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import tritium.ncm.music.AudioPlayer;
import tritium.ncm.music.CloudMusic;
import tritium.rendering.HSBColor;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.settings.ColorSetting;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;
import tritium.widget.Widget;

import java.nio.ByteBuffer;

import static tritium.widget.impl.MusicSpectrumWidget.Style.*;

/**
 * @author IzumiiKonata
 * Date: 2025/3/8 16:17
 */
public class MusicSpectrumWidget extends Widget {

    float[] renderSpectrum = new float[1];
    float[] renderSpectrumIndicator = new float[1];

    long[] indicatorTimeStamp = new long[1];

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
    public final NumberSetting<Double> spectrumTilt = new NumberSetting<>("Spectrum Tilt", 3.0, 0.0, 6.0, 0.5);
    public final NumberSetting<Double> smoothing = new NumberSetting<>("Smoothing", 0.55, 0.0, 0.95, 0.05);
    public final BooleanSetting absVol = new BooleanSetting("Absolute Volume", true);
    public final BooleanSetting stereo = new BooleanSetting("Waveform Stereo", false);

    public final NumberSetting<Float> windowTime = new NumberSetting<>("Window Time (ms)", 16.0f, 4.0f, 256.0f, 0.1f) {
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
        spectrumTilt.setShouldRender(() -> style.getValue() == Rect || style.getValue() == Line);
        smoothing.setShouldRender(() -> style.getValue() == Rect || style.getValue() == Line);
    }

    @Override
    public void onRender(boolean editing) {
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
                this.updateSpectrum();
            }

            GlStateManager.pushMatrix();

            if (rect) {
                this.drawBars(compatMode);
            }

            if (waveform || oscilloscope) {
                boolean stereo = this.stereo.getValue();

                double pWidgetHeight = stereo ? this.getHeight() * 0.5 : this.getHeight();

                // ⚠⚠⚠ race conditions 警告 ⚠⚠⚠
                if (CloudMusic.player.spectrumDataLFilled && CloudMusic.player.lockL.tryLock()) {
                    GlStateManager.color(1, 1, 1, 1);
                    this.drawWaveSub(pWidgetHeight, false, CloudMusic.player.waveVertexesBufferBackend, CloudMusic.player.waveVertexes.length / 2);
                    CloudMusic.player.lockL.unlock();
                }

                if (stereo && CloudMusic.player.spectrumDataRFilled && CloudMusic.player.lockR.tryLock()) {
                    double lineHeight = 0.5;
                    tritium.rendering.Rect.draw(this.getX() + 4, (float) (this.getY() + pWidgetHeight - lineHeight * 0.5), this.getWidth() - 8, (float) lineHeight, hexColor(255, 255, 255, 160));

                    GlStateManager.color(1, 1, 1, 1);
                    this.drawWaveSub(pWidgetHeight, true, CloudMusic.player.waveRightVertexesBufferBackend, CloudMusic.player.waveRightVertexes.length / 2);
                    CloudMusic.player.lockR.unlock();
                }

            }

            if (line) {
                this.drawLine(compatMode);
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
        if (bb == null) {
            return;
        }

        double startX = this.getX() + 4;
        double startY = this.getY() + pWidgetHeight * 0.5 + (secondHalf ? pWidgetHeight : 0);

        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GlStateManager.pushMatrix();
        GlStateManager.translate(startX, startY, 0);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, bb);

//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
//
//        GL11.glLineWidth(7f);
//        GlStateManager.color(0.30f, 0.62f, 1.0f, 0.05f);
//        GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, vertCount);
//
//        GL11.glLineWidth(4f);
//        GlStateManager.color(0.38f, 0.72f, 1.0f, 0.10f);
//        GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, vertCount);
//
//        GL11.glLineWidth(2f);
//        GlStateManager.color(0.55f, 0.85f, 1.0f, 0.22f);
//        GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, vertCount);

        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GL11.glLineWidth(1.4f);
        GlStateManager.color(0.92f, 0.98f, 1.0f, 0.95f);
        GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, vertCount);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.popMatrix();

        GL11.glLineWidth(1f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
    }


    private void updateSpectrum() {
        int n = AudioPlayer.bandValues.length;

        if (renderSpectrum.length != n) {
            renderSpectrum = new float[n];
            renderSpectrumIndicator = new float[n];
            indicatorTimeStamp = new long[n];
        }

        boolean playing = CloudMusic.player.player.isPlaying();
        float smooth = this.smoothing.getValue().floatValue();
        float attackFraction = 1.0f + (1.0f - smooth) * 1.4f;
        float decayFraction = 0.07f + (1.0f - smooth) * 1.6f;

        long now = System.currentTimeMillis();
        boolean indicator = this.indicator.getValue();

        for (int i = 0; i < n; i++) {
            float target = AudioPlayer.bandValues[i];

            if (!Float.isFinite(target) || !playing) {
                target = 0.0f;
            }

            float previous = renderSpectrum[i];
            float current = Interpolations.interpolate(previous, target, target > previous ? attackFraction : decayFraction);
            renderSpectrum[i] = current;

            if (indicator) {
                if (current >= renderSpectrumIndicator[i]) {
                    renderSpectrumIndicator[i] = current;
                    indicatorTimeStamp[i] = now;
                } else if (now - indicatorTimeStamp[i] > 450) {
                    float fallen = Interpolations.interpolate(renderSpectrumIndicator[i], 0.0f, 0.12f);
                    renderSpectrumIndicator[i] = Math.max(fallen, current);
                }
            }
        }
    }

    private void drawBars(boolean compact) {
        int n = renderSpectrum.length;
        if (n == 0) {
            return;
        }

        double pad = 4;
        double regionX, regionW, baseY, maxH;

        if (compact) {
            regionX = this.getX() + pad;
            regionW = this.getWidth() - pad * 2;
            baseY = this.getY() + this.getHeight() - pad;
            maxH = this.getHeight() - pad * 2;
        } else {
            regionX = 0;
            regionW = RenderSystem.getWidth();
            baseY = RenderSystem.getHeight();
            maxH = RenderSystem.getHeight() * 0.33;
        }

        double mult = this.multiplier.getValue();
        double pitch = regionW / n;
        double barW = compact ? Math.max(1.0, pitch * 0.82) : pitch;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < n; i++) {
            double h = Math.min(maxH, renderSpectrum[i] * maxH * mult);
            if (h <= 0) {
                continue;
            }

            double x0 = regionX + i * pitch + (pitch - barW) * 0.5;
            double x1 = x0 + barW;
            double top = baseY - h;

            int rgb = this.rectColor.getRGB(i);
            float a = (rgb >> 24 & 255) * RenderSystem.DIVIDE_BY_255;
            float r = (rgb >> 16 & 255) * RenderSystem.DIVIDE_BY_255;
            float g = (rgb >> 8 & 255) * RenderSystem.DIVIDE_BY_255;
            float b = (rgb & 255) * RenderSystem.DIVIDE_BY_255;

            float topAlpha = a * (1.0f - 0.8f * (float) (h / maxH));

            GlStateManager.color(r, g, b, a);
            GL11.glVertex2d(x0, baseY);
            GL11.glVertex2d(x1, baseY);

            GlStateManager.color(r, g, b, topAlpha);
            GL11.glVertex2d(x1, top);
            GL11.glVertex2d(x0, top);
        }
        GL11.glEnd();

        if (this.indicator.getValue()) {
            double capH = compact ? 1.0 : 1.5;

            GL11.glBegin(GL11.GL_QUADS);
            for (int i = 0; i < n; i++) {
                double ph = Math.min(maxH, renderSpectrumIndicator[i] * maxH * mult);
                if (ph <= capH) {
                    continue;
                }

                double x0 = regionX + i * pitch + (pitch - barW) * 0.5;
                double x1 = x0 + barW;
                double capY = baseY - ph;

                int rgb = this.rectColor.getRGB(i);
                float a = (rgb >> 24 & 255) * RenderSystem.DIVIDE_BY_255;
                float r = (rgb >> 16 & 255) * RenderSystem.DIVIDE_BY_255;
                float g = (rgb >> 8 & 255) * RenderSystem.DIVIDE_BY_255;
                float b = (rgb & 255) * RenderSystem.DIVIDE_BY_255;

                GlStateManager.color(r, g, b, Math.min(1.0f, a + 0.25f));
                GL11.glVertex2d(x0, capY - capH);
                GL11.glVertex2d(x1, capY - capH);
                GL11.glVertex2d(x1, capY);
                GL11.glVertex2d(x0, capY);
            }
            GL11.glEnd();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
    }

    private void drawLine(boolean compact) {
        int n = renderSpectrum.length;
        if (n < 2) {
            return;
        }

        double pad = 4;
        double regionX, regionW, baseY, maxH;

        if (compact) {
            regionX = this.getX() + pad;
            regionW = this.getWidth() - pad * 2;
            baseY = this.getY() + this.getHeight() - pad;
            maxH = this.getHeight() - pad * 2;
        } else {
            regionX = 0;
            regionW = RenderSystem.getWidth();
            baseY = RenderSystem.getHeight();
            maxH = RenderSystem.getHeight() * 0.33;
        }

        double mult = this.multiplier.getValue();
        double pitch = regionW / (n - 1);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i < n; i++) {
            double h = Math.min(maxH, renderSpectrum[i] * maxH * mult);
            double x = regionX + i * pitch;

            GlStateManager.color(1f, 1f, 1f, 0.16f);
            GL11.glVertex2d(x, baseY);
            GlStateManager.color(1f, 1f, 1f, 0.34f);
            GL11.glVertex2d(x, baseY - h);
        }
        GL11.glEnd();

        GlStateManager.color(1f, 1f, 1f, 0.9f);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(compact ? 1.0f : 1.5f);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < n; i++) {
            double h = Math.min(maxH, renderSpectrum[i] * maxH * mult);
            double x = regionX + i * pitch;
            GL11.glVertex2d(x, baseY - h);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
    }

}
