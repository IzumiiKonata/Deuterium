package tritium.screens.nsf;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import org.jtransforms.fft.DoubleFFT_1D;
import org.kc7bfi.jflac.apps.ExtensionFileFilter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import processing.sound.Engine;
import processing.sound.SoundFile;
import processing.sound.Waveform;
import tritium.management.ConfigManager;
import tritium.management.FontManager;
import tritium.nsf.NSFRenderer;
import tritium.rendering.StencilClipManager;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.screens.BaseScreen;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * NSF Player with Phase-Aligned Oscilloscope
 * @author IzumiiKonata (Modified with phase alignment)
 */
public class NSFScreen extends BaseScreen {

    @Getter
    private static final NSFScreen instance = new NSFScreen();

    List<SoundFile> soundFiles = new ArrayList<>();
    List<Waveform> waveforms = new ArrayList<>();
    List<PhaseAlignedOsc> phaseOscs = new ArrayList<>();

    boolean loaded = false, renderFinished = false;
    boolean usePhaseAlignment = true; // Toggle for phase-aligned view

    public NSFScreen() {
    }

    @Override
    public void initGui() {
    }

    @Override
    public void onGuiClosed() {
        for (Waveform waveform : waveforms) {
            waveform.removeInput();
        }
        waveforms.clear();

        soundFiles.forEach(sf -> {
            sf.stop();
            sf.cleanUp();
        });
        soundFiles.clear();

        phaseOscs.clear();

        loaded = renderFinished = false;
        nsfFile = null;
        infos = null;

        System.gc();
    }

    float nSamples = 30.0f;
    float totTime = 0;
    SoundFile player = null;
    NSFRenderer.NSFRenderInfo[] infos;

    private void initStuffs() {
        loaded = true;
        totTime = 0f;

        MultiThreadingUtil.runAsync(() -> {
            renderFinished = false;

            List<Tuple<File, NSFRenderer.NSFRenderInfo>> result = null;
            try {
                result = NSFRenderer.export(nsfFile.getAbsolutePath(), new File(ConfigManager.configDir, "NSFPlayer"));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (result.isEmpty()) {
                System.err.println("无法导出NSF文件");
                return;
            }

            infos = result.stream().map(Tuple::getB).toArray(NSFRenderer.NSFRenderInfo[]::new);

            for (Tuple<File, NSFRenderer.NSFRenderInfo> rendered : result) {
                soundFiles.add(new SoundFile(rendered.getFirst().getAbsolutePath()));
            }

            for (SoundFile sf : soundFiles) {
                sf.play();
                sf.amp(this.getVolume());

                if (sf.getTotalTimeMillis() > totTime) {
                    totTime = sf.getTotalTimeMillis();
                    player = sf;
                }

                Waveform waveform = new Waveform(nSamples * 0.001f);
                waveform.input(sf);
                waveforms.add(waveform);

                // Initialize phase-aligned oscilloscope for each channel
                phaseOscs.add(new PhaseAlignedOsc());
            }

            pausing = false;
            renderFinished = true;
        });
    }

    double oscilloscopeScaleFactor = 1.0;
    boolean lmbPressed = false, rmbPressed = false;

    public void drawScreen(double mouseX, double mouseY) {
        if (lmbPressed && !Mouse.isButtonDown(0))
            lmbPressed = false;

        if (rmbPressed && !Mouse.isButtonDown(1))
            rmbPressed = false;

        if (loaded) {
            Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0, 80));

            CFontRenderer fr = FontManager.pf40bold;

            if (!renderFinished) {
                fr.drawString("NSF Player", 8, 8, 0xFFFFFFFF);
                fr.drawCenteredString("Rendering...", RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5, 0xFFFFFFFF);
            } else {
                double startX = 8, startY = 8;
                double width = 230 * oscilloscopeScaleFactor;
                double height = ((480 - startY) / 4 - startY) * oscilloscopeScaleFactor;

                for (int i = 0; i < waveforms.size(); i++) {
                    Waveform analyzer = waveforms.get(i);

                    if (startX + width > RenderSystem.getWidth()) {
                        startX = 8;
                        startY += height + 8;
                    }

                    float[] raw = analyzer.analyze();

                    for (int i1 = 0; i1 < raw.length; i1++) {
                        raw[i1] /= this.getVolume() * .05f;
                    }

                    float[] wave = usePhaseAlignment ?
                            phaseOscs.get(i).getAlignedWaveform(raw) :
                            raw;

                    this.render(i, wave, startX, startY, width, height, mouseX, mouseY);

                    startX += width + 8;
                }

                String text = "Samples: " + nSamples + (usePhaseAlignment ? " [Phase Aligned]" : " [Raw]");
                fr.drawString(text, RenderSystem.getWidth() - 8 - fr.getStringWidth(text), RenderSystem.getHeight() - 8 - fr.getHeight(), -1);
                fr.drawString(nsfFile.getName(), RenderSystem.getWidth() - 8 - fr.getStringWidth(nsfFile.getName()), RenderSystem.getHeight() - 8 - fr.getHeight() * 2, -1);

                // Progress bar
                double spacing = 8;
                double progressBarHeight = 8;
                this.roundedRect(spacing, RenderSystem.getHeight() - spacing - progressBarHeight, RenderSystem.getWidth() - spacing * 2, progressBarHeight, 3, 1, 1, 1, .25f);

                StencilClipManager.beginClip(() -> {
                    Rect.draw(spacing, RenderSystem.getHeight() - spacing - progressBarHeight, (RenderSystem.getWidth() - spacing * 2) * (player.getCurrentTimeMillis() / totTime), progressBarHeight, -1);
                });

                this.roundedRect(spacing, RenderSystem.getHeight() - spacing - progressBarHeight, RenderSystem.getWidth() - spacing * 2, progressBarHeight, 3, Color.WHITE);

                StencilClipManager.endClip();

                // Time display
                String currentTime = formatTime(player.getCurrentTimeSeconds());
                String totalTime = formatTime(player.getTotalTimeSeconds());

                CFontRenderer fr2 = FontManager.pf18;
                fr2.drawString(currentTime, spacing, RenderSystem.getHeight() - spacing - progressBarHeight - fr2.getHeight() - spacing * .5, -1);
                fr2.drawString(totalTime, RenderSystem.getWidth() - spacing - fr2.getWidth(totalTime), RenderSystem.getHeight() - spacing - progressBarHeight - fr2.getHeight() - spacing * .5, -1);

                // Progress bar hover
                if (RenderSystem.isHovered(mouseX, mouseY, spacing, RenderSystem.getHeight() - spacing - progressBarHeight, RenderSystem.getWidth() - spacing * 2, progressBarHeight)) {
                    double perc = Math.max(0, Math.min(1, (mouseX - spacing) / (RenderSystem.getWidth() - spacing * 2)));
                    int currentTimeSeconds = (int) (player.getTotalTimeSeconds() * perc);
                    String time = formatTime(currentTimeSeconds);

                    double hoverPanelWidth = 8 + fr2.getWidth(time);
                    double hoverPanelHeight = 8 + fr2.getHeight();
                    double hoverPanelX = Math.max(spacing, Math.min(RenderSystem.getWidth() - spacing - hoverPanelWidth, mouseX - hoverPanelWidth * 0.5));
                    double hoverPanelY = RenderSystem.getHeight() - spacing - progressBarHeight - hoverPanelHeight - spacing;

                    this.roundedRect(hoverPanelX, hoverPanelY, hoverPanelWidth, hoverPanelHeight, 4, 0, 0, 0, .4f);
                    fr2.drawString(time, hoverPanelX + 4, hoverPanelY + 4, -1);

                    if (Mouse.isButtonDown(0)) {
                        soundFiles.forEach(sf -> {
                            sf.jump((float) (perc * totTime / 1000.0f));
                            if (sf.isMuted()) {
                                sf.amp(0.001f);
                            } else {
                                sf.amp(this.getVolume());
                            }
                        });
                    }
                }

                // Mouse wheel for scale
                int dWheel = Mouse.getDWheel();
                if (dWheel != 0) {
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        oscilloscopeScaleFactor += (dWheel > 0 ? 0.025 : -0.025) * (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 4 : 1);
                        oscilloscopeScaleFactor = Math.max(0.5, Math.min(1.5, oscilloscopeScaleFactor));
                    }
                }
            }
        } else {
            this.renderPrepFrame(mouseX, mouseY);
        }
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
    }

    private void render(int index, float[] wave, double posX, double posY, double width, double height, double mouseX, double mouseY) {
        if (wave == null) return;

        Rect.draw(posX, posY, width, height, RenderSystem.hexColor(0, 0, 0, 120));

        SoundFile sf = soundFiles.get(index);
        String chipName = "Channel #" + index + " " + infos[index].getSoundRenderer().getName();

        FontManager.pf20bold.drawString(chipName, posX + 4, posY + 4, -1);
        FontManager.pf20bold.drawString(sf.isMuted() ? "Muted" : "", posX + 4, posY + height - 4 - FontManager.pf20bold.getHeight(), -1);

        double startX = posX;
        double startY = posY + height * 0.5;
        double spacing = width / wave.length;

        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1f);

        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (int i = 0; i < wave.length; i++) {
            float v = wave[i];
            double targetY = startY + height * v * 0.4;

            if (targetY > posY + height || targetY < posY) {
                targetY = startY + height * v * 0.4 / this.getVolume();
            }

            GL11.glVertex2d(startX + spacing * i, targetY);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.resetColor();

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, posX, posY, width, height);

        if (hovered) {
            Rect.draw(posX, posY, width, height, RenderSystem.hexColor(255, 255, 255, 20));
        }

        if (hovered && Mouse.isButtonDown(0) && !lmbPressed) {
            lmbPressed = true;
            sf.setMuted(!sf.isMuted());
        }

        if (hovered && Mouse.isButtonDown(1) && !rmbPressed) {
            rmbPressed = true;
            boolean hasLeast1 = soundFiles.stream().filter(soundFile -> sf != soundFile).anyMatch(soundFile -> !soundFile.isMuted());
            soundFiles.forEach(s -> { if (s != sf) s.setMuted(hasLeast1); });
            sf.setMuted(false);
        }
    }

    File nsfFile;

    private void renderPrepFrame(double mouseX, double mouseY) {
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0, 160));
        CFontRenderer fr = FontManager.pf40bold;
        fr.drawString("NSF Player", 8, 8, 0xFFFFFFFF);

        String s = nsfFile == null ? "Select NSF..." : nsfFile.getName();
        fr.drawCenteredString(s, RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5 - 50, 0xFFFFFFFF);
        RenderSystem.drawOutLine(RenderSystem.getWidth() * 0.5 - fr.getStringWidthD(s) * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5 - 50, fr.getStringWidthD(s), fr.getHeight(), 2, 8, -1);

        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() * 0.5 - fr.getStringWidthD(s) * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5 - 50, fr.getStringWidthD(s), fr.getHeight()) && Mouse.isButtonDown(0)) {
            nsfFile = chooseFile("nsf", "NSF File");
        }

        fr.drawCenteredString("Play!", RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5 + 130, 0xFFFFFFFF);
        RenderSystem.drawOutLine(RenderSystem.getWidth() * 0.5 - fr.getStringWidthD("Play!") * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5 + 130, fr.getStringWidthD("Play!"), fr.getHeight(), 2, 8, -1);

        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() * 0.5 - fr.getStringWidthD("Play!") * 0.5, RenderSystem.getHeight() * 0.5 - fr.getHeight() * 0.5 + 130, fr.getStringWidthD("Play!"), fr.getHeight()) && Mouse.isButtonDown(0)) {
            if (nsfFile != null) {
                initStuffs();
            }
        }
    }

    JFileChooser jFileChooser = new JFileChooser(new File("."), FileSystemView.getFileSystemView());

    private File chooseFile(String extension, String desc) {
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setDragEnabled(true);
        ExtensionFileFilter filter = new ExtensionFileFilter();
        filter.addExtension(extension);
        filter.setDescription(desc);
        jFileChooser.resetChoosableFileFilters();
        jFileChooser.setFileFilter(filter);

        JFrame jFrame = genFrame();
        int flag = jFileChooser.showOpenDialog(jFrame);

        if (flag == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        }

        jFrame.dispose();
        return null;
    }

    public JFrame genFrame() {
        JFrame frame = new JFrame();
        frame.setVisible(false);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.toFront();
        return frame;
    }

    boolean pausing = false;

    @Override
    public void onKeyTyped(char chr, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }

        if (keyCode == Keyboard.KEY_SPACE) {
            pausing = !pausing;
            if (pausing) {
                soundFiles.forEach(SoundFile::pause);
            } else {
                soundFiles.forEach(sf -> {
                    sf.play();
                    sf.amp(sf.isMuted() ? 0 : this.getVolume());
                });
            }
        }

        // Toggle phase alignment with 'P' key
        if (keyCode == Keyboard.KEY_P) {
            usePhaseAlignment = !usePhaseAlignment;
        }
    }

    private float getVolume() {
        return .75f;
    }

    /**
     * Phase-aligned oscilloscope wrapper for Processing Waveform
     */
    private static class PhaseAlignedOsc {
        private static final int FFT_SIZE = 8192;
        private static final int BUFFER_SIZE = 65536;

        private DoubleFFT_1D fft;
        private DoubleFFT_1D ifft;

        private double[] inBuf;
        private double[] fftBuf;
        private double[] corrBuf;

        private short[] audioBuffer;
        private int bufferWritePos = 0;
        private boolean ready = false;

        private double waveLen = 0;
        private double pitch = 0;

        public PhaseAlignedOsc() {
            try {
                inBuf = new double[FFT_SIZE];
                fftBuf = new double[FFT_SIZE * 2];
                corrBuf = new double[FFT_SIZE];
                audioBuffer = new short[BUFFER_SIZE];

                // Initialize with silence
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    audioBuffer[i] = 0;
                }

                fft = new DoubleFFT_1D(FFT_SIZE);
                ifft = new DoubleFFT_1D(FFT_SIZE);

                ready = true;
            } catch (Exception e) {
                System.err.println("Failed to initialize phase-aligned osc: " + e.getMessage());
                ready = false;
            }
        }

        public float[] getAlignedWaveform(float[] rawWave) {
            if (!ready) {
                return rawWave; // Fallback to raw waveform
            }

            // Get raw waveform data
            if (rawWave == null) {
                return null;
            }

            // Convert float samples to short and store in circular buffer
            for (int i = 0; i < rawWave.length; i++) {
                audioBuffer[bufferWritePos] = (short)(rawWave[i] * 32767.0f);
                bufferWritePos = (bufferWritePos + 1) & 0xFFFF;
            }

            // Process with phase alignment
            return processBuffer(30.0f, true, 0.0);
        }

        private float[] processBuffer(float windowSize, boolean waveCorr, double phaseOffset) {
            int needle = bufferWritePos;
            boolean loudEnough = false;

            int displaySize = (int)(65536.0 * (windowSize / 1000.0));
            int displaySize2 = (int)(65536.0 * (windowSize / 500.0));

            // Fill input buffer with windowed signal
            loudEnough = fillInputBuffer(needle, displaySize2);

            if (!loudEnough) {
                return extractWaveform(needle - displaySize, displaySize);
            }

            // Perform FFT
            System.arraycopy(inBuf, 0, fftBuf, 0, FFT_SIZE);
            fft.realForwardFull(fftBuf);

            // Auto-correlation preparation
            prepareAutoCorrelation();

            // Inverse FFT
            ifft.complexInverse(fftBuf, true);

            // Extract correlation buffer
            for (int i = 0; i < FFT_SIZE; i++) {
                corrBuf[i] = fftBuf[i * 2];
            }

            // Apply window to correlation
            for (int j = 0; j < (FFT_SIZE >> 1); j++) {
                corrBuf[j] *= 1.0 - ((double)j / (double)(FFT_SIZE << 1));
            }

            // Find period size
            findPeriodSize();

            double phase = 0.0;

            // Calculate phase if valid period found
            if (waveLen < (FFT_SIZE - 32)) {
                pitch = Math.pow(1.0 - (waveLen / (double)(FFT_SIZE >> 1)), 4.0);
                waveLen *= (double)displaySize * 2.0 / (double)FFT_SIZE;

                phase = calculatePhase(needle, displaySize);

                if (waveCorr) {
                    needle -= (int)((phase + (phaseOffset * 2)) * waveLen);
                }
            }

            needle -= displaySize;



            return extractWaveform(needle, displaySize);
        }

        private boolean fillInputBuffer(int needle, int displaySize2) {
            int k = 0;
            short lastSample = 0;
            boolean loud = false;

            for (int i = 0; i < FFT_SIZE; i++) {
                inBuf[i] = 0.0;
            }

            if (displaySize2 < FFT_SIZE) {
                for (int j = -FFT_SIZE; j < FFT_SIZE; j++) {
                    int bufIndex = (needle - displaySize2 + ((j * displaySize2) / FFT_SIZE)) & 0xFFFF;
                    short newData = audioBuffer[bufIndex];

                    if (newData != -1) {
                        lastSample = newData;
                    }

                    if (j < 0) continue;

                    inBuf[j] = (double)lastSample / 32768.0;

                    if (Math.abs(inBuf[j]) > 0.001) {
                        loud = true;
                    }

                    // Hamming window
                    inBuf[j] *= 0.55 - 0.45 * Math.cos(Math.PI * (double)j / (double)(FFT_SIZE >> 1));
                }
            } else {
                for (int j = needle - displaySize2; k < displaySize2; j++, k++) {
                    int kIn = (k * FFT_SIZE) / displaySize2;
                    if (kIn >= FFT_SIZE) break;

                    int bufIndex = j & 0xFFFF;
                    if (audioBuffer[bufIndex] != -1) {
                        lastSample = audioBuffer[bufIndex];
                    }

                    inBuf[kIn] = (double)lastSample / 32768.0;

                    if (Math.abs(inBuf[kIn]) > 0.001) {
                        loud = true;
                    }

                    // Hamming window
                    inBuf[kIn] *= 0.55 - 0.45 * Math.cos(Math.PI * (double)kIn / (double)(FFT_SIZE >> 1));
                }
            }

            return loud;
        }

        private void prepareAutoCorrelation() {
            for (int j = 0; j < FFT_SIZE; j++) {
                double real = fftBuf[j * 2] / FFT_SIZE;
                double imag = fftBuf[j * 2 + 1] / FFT_SIZE;

                fftBuf[j * 2] = real * real + imag * imag;
                fftBuf[j * 2 + 1] = 0;
            }

            fftBuf[0] = 0;
            fftBuf[1] = 0;
            fftBuf[2] = 0;
            fftBuf[3] = 0;
        }

        private void findPeriodSize() {
            double waveLenCandL = Double.MAX_VALUE;
            double waveLenCandH = Double.MIN_VALUE;
            waveLen = FFT_SIZE - 1;
            int waveLenBottom = 0;

            // Find lowest point
            for (int j = (FFT_SIZE >> 2); j > 2; j--) {
                if (corrBuf[j] < waveLenCandL) {
                    waveLenCandL = corrBuf[j];
                    waveLenBottom = j;
                }
            }

            // Find highest point
            for (int j = (FFT_SIZE >> 1) - 1; j > waveLenBottom; j--) {
                if (corrBuf[j] > waveLenCandH) {
                    waveLenCandH = corrBuf[j];
                    waveLen = j;
                }
            }
        }

        private double calculatePhase(int needle, int displaySize) {
            double dftReal = 0.0;
            double dftImag = 0.0;
            short lastSample = 0;

            int startJ = needle - 1 - displaySize - (int)waveLen;
            int k = -(displaySize >> 1);

            for (int j = startJ; k < waveLen; j++, k++) {
                int bufIndex = j & 0xFFFF;

                if (audioBuffer[bufIndex] != -1) {
                    lastSample = audioBuffer[bufIndex];
                }

                if (k < 0) continue;

                double sampleValue = (double)lastSample / 32768.0;
                double angle = (double)k * (-2.0 * Math.PI) / waveLen;

                dftReal += sampleValue * Math.cos(angle);
                dftImag += sampleValue * Math.sin(angle);
            }

            return 0.5 + (Math.atan2(dftImag, dftReal) / (2.0 * Math.PI));
        }

        private float[] extractWaveform(int needle, int displaySize) {
            int numSamples = (int) (Engine.getEngine().getSampleRate() * 30.0f * 0.001f);
            float[] waveform = new float[numSamples];

            short lastSample = 0;

            for (int i = 0; i < numSamples; i++) {
                int bufIndex = (needle + (i * displaySize) / numSamples) & 0xFFFF;

                if (audioBuffer[bufIndex] != -1) {
                    lastSample = audioBuffer[bufIndex];
                }

                waveform[i] = (float)lastSample / 32768.0f;
            }

            return waveform;
        }
    }
}