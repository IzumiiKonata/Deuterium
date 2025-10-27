package tritium.screens.nsf;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import org.kc7bfi.jflac.apps.ExtensionFileFilter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2024/12/22 13:37
 */
public class NSFScreen extends BaseScreen {

    @Getter
    private static final NSFScreen instance = new NSFScreen();

    List<SoundFile> soundFiles = new ArrayList<>();
    List<Waveform> waveforms = new ArrayList<>();

    boolean loaded = false, renderFinished = false;

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

        loaded = renderFinished = false;

        nsfFile = null;
        infos = null;

        System.gc();
    }

    float nSamples = 25.0f;
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
                sf.amp(0.25f);

                if (sf.getTotalTimeMillis() > totTime) {
                    totTime = sf.getTotalTimeMillis();
                    player = sf;
                }

                Waveform waveform = new Waveform(nSamples * 0.001f);
                waveform.input(sf);

                waveforms.add(waveform);
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

            Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0,  80));

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

                    this.render(i, analyzer.analyze(), startX, startY, width, height, mouseX, mouseY);

                    startX += width + 8;
                }

                String text = "Samples: " + nSamples;
                fr.drawString(text, RenderSystem.getWidth() - 8 - fr.getStringWidth(text), RenderSystem.getHeight() - 8 - fr.getHeight(), -1);
                fr.drawString(nsfFile.getName(), RenderSystem.getWidth() - 8 - fr.getStringWidth(nsfFile.getName()), RenderSystem.getHeight() - 8 - fr.getHeight() * 2, -1);


                double spacing = 8;
                double progressBarHeight = 8;
                this.roundedRect(spacing, RenderSystem.getHeight() - spacing - progressBarHeight, RenderSystem.getWidth() - spacing * 2, progressBarHeight, 3, 1, 1, 1, .25f);

                StencilClipManager.beginClip(() -> {
                    Rect.draw(spacing, RenderSystem.getHeight() - spacing - progressBarHeight, (RenderSystem.getWidth() - spacing * 2) * (player.getCurrentTimeMillis() / totTime), progressBarHeight, -1);
                });

                this.roundedRect(spacing, RenderSystem.getHeight() - spacing - progressBarHeight, RenderSystem.getWidth() - spacing * 2, progressBarHeight, 3, Color.WHITE);

                StencilClipManager.endClip();

                String currentTime = "00:00";
                String totalTime = "00:00";

                int cMin = player.getCurrentTimeSeconds() / 60;
                int cSec = (player.getCurrentTimeSeconds() - (player.getCurrentTimeSeconds() / 60) * 60);
                currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                int tMin = player.getTotalTimeSeconds() / 60;
                int tSec = (player.getTotalTimeSeconds() - (player.getTotalTimeSeconds() / 60) * 60);
                totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                CFontRenderer fr2 = FontManager.pf18;
                fr2.drawString(currentTime, spacing, RenderSystem.getHeight() - spacing - progressBarHeight - fr2.getHeight() - spacing * .5, -1);
                fr2.drawString(totalTime, RenderSystem.getWidth() - spacing - fr2.getWidth(totalTime), RenderSystem.getHeight() - spacing - progressBarHeight - fr2.getHeight() - spacing * .5, -1);

                if (RenderSystem.isHovered(mouseX, mouseY, spacing, RenderSystem.getHeight() - spacing - progressBarHeight, RenderSystem.getWidth() - spacing * 2, progressBarHeight)) {

                    double perc = Math.max(0, Math.min(1, (mouseX - spacing) / (RenderSystem.getWidth() - spacing * 2)));
                    int currentTimeSeconds = (int) (player.getTotalTimeSeconds() * perc);
                    int min = currentTimeSeconds / 60;
                    int sec = (currentTimeSeconds - (currentTimeSeconds / 60) * 60);
                    String time = (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);

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
                                float volume = this.getVolume();
                                sf.amp(0.001f);
                                sf.volume = volume;
                            } else {
                                sf.amp(this.getVolume());
                            }
                        });
                    }

                }

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

            double targetY = startY + height * v / (sf.isMuted() ? 0.001f : this.getVolume() * .05) * 0.4;

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

            soundFiles.forEach(s -> { if (s != sf) s.setMuted(hasLeast1); } );
            sf.setMuted(false);
        }
    }

    File nsfFile;

    private void renderPrepFrame(double mouseX, double mouseY) {

        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0,  160));

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
    }
    
    private float getVolume() {
        return .25f;
    }
}
