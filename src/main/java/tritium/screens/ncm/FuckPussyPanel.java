package tritium.screens.ncm;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Location;
import org.lwjglx.input.Mouse;
import tech.konata.ncmplayer.music.AudioPlayer;
import tech.konata.ncmplayer.music.CloudMusic;
import tech.konata.ncmplayer.music.dto.Music;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.entities.impl.ScrollText;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shader;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.Shaders;
import tritium.rendering.ui.widgets.IconWidget;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.widget.impl.MusicInfoWidget;
import tritium.widget.impl.MusicLyricsWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * Date: 2025/10/17 21:56
 */
public class FuckPussyPanel implements SharedRenderingConstants {

    private Music music;
    float alpha = 0f;
    boolean closing = false;

    public static final List<LyricLine> lyrics = new CopyOnWriteArrayList<>();

    public static void initLyric(JsonObject lyric, Music music) {
        // reset states

        List<LyricLine> parsed = LyricParser.parse(lyric);

        fetchTTMLLyrics(music, parsed);

        addLyrics(parsed);
    }

    private static void addLyrics(List<LyricLine> lyricLines) {
        synchronized (lyrics) {
            lyrics.clear();
            lyrics.addAll(lyricLines);

            currentDisplaying = lyrics.get(0);
            updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * .5);
        }
    }

    private static void fetchTTMLLyrics(Music music, List<LyricLine> parsed) {

        MultiThreadingUtil.runAsync(() -> {
            try {
                String lrc = HttpUtils.getString(
                        "https://gitee.com/IzumiiKonata/amll-ttml-db/raw/main/ncm-lyrics/" + music.getId() + ".yrc",
                        null
                );
//                System.out.println("歌曲 " + music.getName() + " 存在 ttml 歌词, 获取中...");

                ArrayList<LyricLine> lyricLines = new ArrayList<>();
                LyricParser.parseYrc(lrc, lyricLines);

                for (LyricLine bean : lyricLines) {

//                    System.out.println(bean.words.size());

                    for (LyricLine lyricLine : parsed) {
                        if (lyricLine.getLyric().toLowerCase().replace(" ", "").equals(bean.lyric.toLowerCase().replace(" ", ""))) {
                            bean.romanizationText = lyricLine.romanizationText;
                            bean.translationText = lyricLine.translationText;
                            break;
                        }
                    }

                }

                addLyrics(lyricLines);
            } catch (Exception ignored) {
            }
        });
    }

    public static void resetProgress(float progress) {
        updateCurrentDisplayingLyric(progress);
        updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * .5);
    }

    public FuckPussyPanel(Music music) {
        this.music = music;
        updateCurrentDisplayingLyric(CloudMusic.player.getCurrentTimeMillis());
        updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * .5);
    }

    public void onInit() {

    }

    public void close() {
        closing = true;
    }

    public boolean shouldClose() {
        return closing && alpha <= 0.02f;
    }

    public void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        if (prevMouse && !Mouse.isButtonDown(0))
            prevMouse = false;

        alpha = Interpolations.interpBezier(alpha, closing ? 0.0f : 1f, 0.3f);

        GlStateManager.pushMatrix();
        scaleAtPos(posX + width * .5, posY + height * .5, 1.1 - (alpha * 0.1));

        this.renderBackground(posX, posY, width, height, alpha);
        this.renderControlsPart(mouseX, mouseY, posX, posY, width, height, alpha);
        this.renderLyrics(mouseX, mouseY, posX, posY, width, height, alpha);
        GlStateManager.popMatrix();
    }

    public static LyricLine currentDisplaying = null;

    Framebuffer baseFb, stencilFb;

    private void renderLyrics(double mouseX, double mouseY, double posX, double posY, double width, double height, float alpha) {

        if (lyrics.isEmpty())
            return;

        double spacingToRight = 16;

        float songProgress = CloudMusic.player.getCurrentTimeMillis();

        updateCurrentDisplayingLyric(songProgress);

        double lyricsWidth = width * .4;
        this.updateLyricPositions(lyricsWidth);

        List<Runnable> blurRects = new ArrayList<>();
//        List<Runnable> bloomRunnables = new ArrayList<>();

        boolean hoveringLyrics = isHovered(mouseX, mouseY, posX + width * .5, posY, width * .5, height);

        for (LyricLine lyric : lyrics) {

            if (lyric.posY + lyric.height + 16 < posY) {
                continue;
            }

            if (lyric.posY > posX + height) {
                break;
            }

            lyric.alpha = Interpolations.interpBezier(lyric.alpha, lyric == currentDisplaying ? 1f : 0f, 0.1f);
            boolean isHovering = isHovered(mouseX, mouseY, RenderSystem.getWidth() * .5, lyric.posY, lyricsWidth, lyric.height);
            lyric.hoveringAlpha = Interpolations.interpBezier(lyric.hoveringAlpha, isHovering ? .2f : 0f, 0.2f);
            lyric.blurAlpha = Interpolations.interpBezier(lyric.blurAlpha, !hoveringLyrics && lyric != currentDisplaying ? 1f : 0f, 0.1f);

            if (isHovering && Mouse.isButtonDown(0) && !prevMouse) {
                prevMouse = true;
                CloudMusic.player.setPlaybackTime(lyric.timeStamp);
//                MusicLyricsWidget.quickResetProgress(lyric.timeStamp);
//                FuckPussyPanel.resetProgress(lyric.timeStamp);
            }

            if (lyric.hoveringAlpha >= .02f)
                roundedRect(RenderSystem.getWidth() * .5 - 4, lyric.posY, lyricsWidth, lyric.height + 8, 8, 8, 1, 1, 1, alpha * lyric.hoveringAlpha);

            double renderX = RenderSystem.getWidth() * .5 + lyric.reboundAnimation;
            double renderY = lyric.posY + lyric.reboundAnimation;

            lyric.reboundAnimation = Interpolations.interpBezier(lyric.reboundAnimation, lyric == currentDisplaying ? 2f : 0f, 0.1f);

            List<LyricLine.Word> words = lyric.words;
            if (!words.isEmpty()) {
                for (int i = 0; i < words.size(); i++) {
                    LyricLine.Word word = words.get(i);
                    double wordWidth = FontManager.pf65bold.getStringWidthD(word.word);

                    if (renderX + wordWidth > RenderSystem.getWidth() * .5 + lyricsWidth + lyric.reboundAnimation) {
                        renderX = RenderSystem.getWidth() * .5 + lyric.reboundAnimation;
                        renderY += FontManager.pf65bold.getHeight() * .85 + 4;
                    }

                    FontManager.pf65bold.drawString(word.word, renderX, renderY - word.emphasize, hexColor(1, 1, 1, alpha * .5f));

                    if (lyric == currentDisplaying) {
                        LyricLine.Word prev = i > 0 ? words.get(i - 1) : null;

                        long prevTiming = i == 0 ? 0 : prev.timing;
                        double progress = Math.max(0, Math.min(1, (songProgress - lyric.timeStamp - prevTiming) / (double) (word.timing - prevTiming)));
                        double stringWidthD = FontManager.pf65bold.getStringWidthD(word.word);
                        word.emphasize = Interpolations.interpBezier(word.emphasize, progress > 0 ? 1 : 0, 0.05f);

                        boolean shouldClip = progress > 0 && progress < 1;

                        if (progress == 1) {
                            FontManager.pf65bold.drawString(word.word, renderX, renderY - word.emphasize, hexColor(1, 1, 1, alpha));
                        }

                        if (shouldClip) {

                            stencilFb = RenderSystem.createFrameBuffer(stencilFb);
                            stencilFb.bindFramebuffer(true);
                            stencilFb.setFramebufferColor(1, 1, 1, 0);
                            stencilFb.framebufferClearNoBinding();

                            GlStateManager.pushMatrix();
                            this.scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 1 / (1.1 - (alpha * 0.1)));
                            Rect.draw(0, 0, progress * stringWidthD, FontManager.pf65bold.getHeight(), -1);
                            RenderSystem.drawGradientRectLeftToRight(progress * stringWidthD, 0, progress * stringWidthD + 8, FontManager.pf65bold.getHeight(), -1, 0);
                            GlStateManager.popMatrix();

                            baseFb = RenderSystem.createFrameBuffer(baseFb);
                            baseFb.bindFramebuffer(true);
                            baseFb.setFramebufferColor(1, 1, 1, 0);
                            baseFb.framebufferClearNoBinding();

                            GlStateManager.pushMatrix();
                            this.scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 1 / (1.1 - (alpha * 0.1)));
                            FontManager.pf65bold.drawString(word.word, 0, 0, hexColor(1, 1, 1, alpha));
                            GlStateManager.popMatrix();

                            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

                            Shaders.STENCIL.draw(baseFb.framebufferTexture, stencilFb.framebufferTexture,  renderX, renderY - word.emphasize);

//                            FontManager.pf18bold.drawString("Stencil: " + stencilFb.framebufferTextureWidth + "x" + stencilFb.framebufferTextureHeight, 50, 32, -1);
//                            FontManager.pf18bold.drawString("Base: " + baseFb.framebufferTextureWidth + "x" + baseFb.framebufferTextureHeight, 50, 64, -1);
//
//                            GlStateManager.bindTexture(stencilFb.framebufferTexture);
//                            ShaderProgram.drawQuad(100, 100, RenderSystem.getWidth(), RenderSystem.getHeight());
//                            GlStateManager.bindTexture(baseFb.framebufferTexture);
//                            ShaderProgram.drawQuad(100, 100, RenderSystem.getWidth(), RenderSystem.getHeight());

//                            Image.draw(stencilFb.framebufferTexture, 50, 72, stencilFb.framebufferTextureWidth, stencilFb.framebufferTextureHeight, Image.Type.Normal);
//                            Image.draw(baseFb.framebufferTexture, 50, 128, baseFb.framebufferTextureWidth, baseFb.framebufferTextureHeight, Image.Type.Normal);
//                            StencilClipManager.beginClip(() -> {
//                                Rect.draw(finalRenderX, finalRenderY - word.emphasize, progress * stringWidthD, FontManager.pf65bold.getHeight(), -1);
//                            });
                        }

                    } else {
                        FontManager.pf65bold.drawString(word.word, renderX, renderY - word.emphasize, hexColor(1, 1, 1, alpha * lyric.alpha));
                    }

                    renderX += wordWidth;
                }
            } else {
                String[] strings = FontManager.pf65bold.fitWidth(lyric.lyric, lyricsWidth);

                for (String string : strings) {
                    FontManager.pf65bold.drawString(string, renderX, renderY, hexColor(1, 1, 1, alpha * ((lyric.alpha * .6f) + .4f)));
                    renderY += FontManager.pf65bold.getHeight() * .85 + 4;
                }

                renderY -= FontManager.pf65bold.getHeight() * .85 + 4;
            }

            if (lyric.translationText != null) {
                double translationX = RenderSystem.getWidth() * .5 + lyric.reboundAnimation;
                double translationY = renderY + FontManager.pf65bold.getHeight() * .85 + 8;

                String[] strings = FontManager.pf34bold.fitWidth(lyric.translationText, lyricsWidth);
                for (String string : strings) {
                    FontManager.pf34bold.drawString(string, translationX, translationY, hexColor(1, 1, 1, alpha * .75f * ((lyric.alpha * .6f) + .4f)));
                    translationY += FontManager.pf34bold.getHeight() + 4;
                }
//                FontManager.pf34bold.drawString(lyric.translationText, translationX, translationY, hexColor(1, 1, 1, alpha * .75f * ((lyric.alpha * .6f) + .4f)));
            }

            blurRects.add(() -> Rect.draw(RenderSystem.getWidth() * .5 - 4, lyric.posY, lyricsWidth, lyric.height + 8, hexColor(1, 1, 1, alpha * lyric.blurAlpha)));
        }

        GlStateManager.pushMatrix();
        this.scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 1 / (1.1 - (alpha * 0.1)));
        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(blurRects);
//        Shaders.UI_BLOOM_SHADER.runNoCaching(bloomRunnables);
        GlStateManager.popMatrix();
    }

    private static double lyricFraction() {
        return .25;
    }

    private void updateLyricPositions(double width) {

        if (currentDisplaying == null)
            return;

        int idxCurrent = lyrics.indexOf(currentDisplaying);

        if (idxCurrent < 0 || idxCurrent >= lyrics.size())
            return;
//
        double offsetY = RenderSystem.getHeight() * lyricFraction()/* - (idxCurrent > 0 ? lyrics.get(idxCurrent - 1).height : 0)*/;

        synchronized (lyrics) {
            List<LyricLine> subList = lyrics.subList(0, idxCurrent);
            float fraction = 0.1f;
            for (int i = subList.size() - 1; i >= 0; i--) {
                LyricLine lyric = subList.get(i);

                lyric.computeHeight(width);
                offsetY -= lyric.height + 16;

                lyric.posY = Interpolations.interpBezier(lyric.posY, offsetY, fraction);
            }

            offsetY = RenderSystem.getHeight() * lyricFraction();
            List<LyricLine> list = lyrics.subList(idxCurrent, lyrics.size());
            for (int i = 0; i < list.size(); i++) {
                LyricLine lyric = list.get(i);
                int j = lyrics.indexOf(lyric);

                lyric.computeHeight(width);

                LyricLine prev = j > 0 ? lyrics.get(j - 1) : null;

                if (prev != null) {
                    if (lyric.posY - (prev.posY + prev.height) >= 50)
                        lyric.shouldUpdatePosition = true;
                }

                if (prev != null && lyric.posY - (prev.posY + prev.height) < 0) {
                    updateLyricPositionsImmediate(width);
                    break;
                }

                if (prev == null || lyric.shouldUpdatePosition) {
                    lyric.posY = Interpolations.interpBezier(lyric.posY, offsetY, fraction);

//                    if (Math.abs(lyric.posY - offsetY) <= 10f) {
//                        boolean forward = lyric.reboundAnimationForward;
//                        lyric.reboundAnimation = Interpolations.interpBezier(lyric.reboundAnimation, forward ? 2f : 0f, forward ? .1f : .2f);
//
//                        if (forward && lyric.reboundAnimation >= 1.5f)
//                            lyric.reboundAnimationForward = false;
//                    }
                }

                offsetY += lyric.height + 16;
            }
        }

    }

    private static void updateLyricPositionsImmediate(double width) {

        if (currentDisplaying == null)
            return;

        double offsetY = RenderSystem.getHeight() * lyricFraction() - 16;
        int toIndex = lyrics.indexOf(currentDisplaying);

        if (toIndex == -1 || toIndex >= lyrics.size())
            return;

        synchronized (lyrics) {
            List<LyricLine> subList = lyrics.subList(0, toIndex);
            for (int i = subList.size() - 1; i >= 0; i--) {
                LyricLine lyric = subList.get(i);

                if (i == subList.size() - 1) {
                    lyric.computeHeight(width);
                    offsetY -= lyric.height;
                }

                lyric.posY = offsetY;

                lyric.computeHeight(width);
                offsetY -= lyric.height + 16;
            }

            offsetY = RenderSystem.getHeight() * lyricFraction();
            for (LyricLine lyric : lyrics.subList(toIndex, lyrics.size())) {
                lyric.posY = offsetY;

                lyric.computeHeight(width);
                offsetY += lyric.height + 16;
            }
        }

    }

    /**
     * 更新当前显示的歌词行
     */
    private static void updateCurrentDisplayingLyric(float songProgress) {

        LyricLine cur = currentDisplaying;

        for (int i = 0; i < lyrics.size(); i++) {
            LyricLine lyric = lyrics.get(i);

            if (lyric.getTimeStamp() > songProgress) {
                if (i > 0) {
                    currentDisplaying = lyrics.get(i - 1);
                }
                break;
            } else if (i == lyrics.size() - 1) {
                currentDisplaying = lyrics.get(i);
            }
        }

        if (cur != currentDisplaying) {
            lyrics.forEach(l -> {
                l.shouldUpdatePosition = false;
                l.reboundAnimationForward = true;
            });
        }
    }

    private double getCoverSizeMax() {
        return RenderSystem.getWidth() * .25;
    }

    private double getCoverSizeMin() {
        return getCoverSizeMax() * .8;
    }

    double coverSize = CloudMusic.player.isPausing() ? this.getCoverSizeMin() : this.getCoverSizeMax();
    double progressBarHeight = 8, volumeBarHeight = 8;

    float coverAlpha = .0f;
    boolean prevMouse = false;

    ScrollText stMusicName = new ScrollText(), stArtists = new ScrollText();

    private void renderControlsPart(double mouseX, double mouseY, double posX, double posY, double width, double height, float alpha) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        double center = this.getCoverSizeMin();
        coverSize = Interpolations.interpBezier(coverSize, CloudMusic.player.isPausing() ? this.getCoverSizeMin() : this.getCoverSizeMax(), 0.2f);

        if (prevCover != null && coverAlpha <= .99f) {
            GlStateManager.bindTexture(prevCover.getGlTextureId());
            this.roundedRectTextured(center - coverSize * .5, center - coverSize * .5, coverSize, coverSize, 3, alpha);
        }

        Location musicCover = MusicInfoWidget.getMusicCover(CloudMusic.currentlyPlaying);
        ITextureObject tex = textureManager.getTexture(musicCover);

        if (tex != null) {
            coverAlpha = Interpolations.interpBezier(coverAlpha, 1.0f, 0.2f);
            GlStateManager.bindTexture(tex.getGlTextureId());
            tex.linearFilter();
            this.roundedRectTextured(center - coverSize * .5, center - coverSize * .5, coverSize, coverSize, 3, alpha * coverAlpha);
        }

        double elementsXOffset = center - this.getCoverSizeMax() * .5;
        double elementsYOffset = center + this.getCoverSizeMax() * .5 + 16;

        stMusicName.render(FontManager.pf28bold, CloudMusic.currentlyPlaying.getName(), elementsXOffset, elementsYOffset, this.getCoverSizeMax(), RenderSystem.hexColor(1, 1, 1, alpha));
        stArtists.render(FontManager.pf20bold, CloudMusic.currentlyPlaying.getArtistsName(), elementsXOffset, elementsYOffset + FontManager.pf20bold.getHeight() + 8, this.getCoverSizeMax(), RenderSystem.hexColor(1, 1, 1, alpha * .8f));

        // progressbar 背景
        double progressBarYOffset = elementsYOffset + FontManager.pf20bold.getHeight() + 8 + FontManager.pf20bold.getHeight() + 12;
        double progressBarWidth = this.getCoverSizeMax();

        roundedRect(elementsXOffset, progressBarYOffset - progressBarHeight * .5, progressBarWidth, progressBarHeight, (this.progressBarHeight / 8.0f) * 3, hexColor(1, 1, 1, alpha * .5f));

        AudioPlayer player = CloudMusic.player;
        float perc = player.getCurrentTimeMillis() / player.getTotalTimeMillis();

        StencilClipManager.beginClip(() -> {
            Rect.draw(elementsXOffset, progressBarYOffset - progressBarHeight * .5, progressBarWidth * perc, progressBarHeight, -1);
        });

        roundedRect(elementsXOffset, progressBarYOffset - progressBarHeight * .5, progressBarWidth, progressBarHeight, (this.progressBarHeight / 8.0f) * 3, hexColor(1, 1, 1, alpha));
        StencilClipManager.endClip();

        boolean hoveringProgressBar = this.isHovered(mouseX, mouseY, elementsXOffset, progressBarYOffset - progressBarHeight * .5, progressBarWidth, 8);
        this.progressBarHeight = Interpolations.interpBezier(this.progressBarHeight, hoveringProgressBar ? 8 : 5, 0.3f);

        if (hoveringProgressBar && Mouse.isButtonDown(0) && !prevMouse) {
            prevMouse = true;
            double xDelta = Math.max(0, Math.min(progressBarWidth, (mouseX - elementsXOffset)));
            double percent = xDelta / progressBarWidth;

            if (CloudMusic.player != null) {
                float progress = (float) (percent * CloudMusic.player.getTotalTimeMillis());
                CloudMusic.player.setPlaybackTime(progress);
                MusicLyricsWidget.quickResetProgress(progress);
                FuckPussyPanel.resetProgress(progress);
            }
        }

        // curTime
        FontManager.pf12.drawString(formatDuration(CloudMusic.player.getCurrentTimeMillis()), elementsXOffset, progressBarYOffset + 12, hexColor(1, 1, 1, alpha * .5f));
        String remainingTime = "-" + formatDuration(CloudMusic.player.getTotalTimeMillis() - CloudMusic.player.getCurrentTimeMillis());
        FontManager.pf12.drawString(remainingTime, elementsXOffset + progressBarWidth - FontManager.pf12.getStringWidth(remainingTime), progressBarYOffset + 12, hexColor(1, 1, 1, alpha * .5f));

        FontManager.music40.drawString("I", elementsXOffset - 8, height - 32, hexColor(1, 1, 1, alpha * .5f));
        FontManager.music40.drawString("J", elementsXOffset + progressBarWidth - FontManager.music40.getStringWidth("J") + 4, height - 32, hexColor(1, 1, 1, alpha * .5f));

        double volumeBarYOffset = height - 24;
        double volumeBarWidth = this.getCoverSizeMax() - FontManager.music40.getStringWidth("I") - FontManager.music40.getStringWidth("J");

        double volumeBarXOffset = elementsXOffset + FontManager.music40.getStringWidth("I") - 2;
        roundedRect(volumeBarXOffset, volumeBarYOffset - volumeBarHeight * .5, volumeBarWidth, volumeBarHeight, (this.volumeBarHeight / 8.0f) * 3, hexColor(1, 1, 1, alpha * .5f));
        StencilClipManager.beginClip(() -> {
            Rect.draw(volumeBarXOffset, volumeBarYOffset - volumeBarHeight * .5, volumeBarWidth * CloudMusic.player.getVolume(), volumeBarHeight, -1);
        });
        roundedRect(volumeBarXOffset, volumeBarYOffset - volumeBarHeight * .5, volumeBarWidth, volumeBarHeight, (this.volumeBarHeight / 8.0f) * 3, hexColor(1, 1, 1, alpha));
        StencilClipManager.endClip();

        boolean hoveringVolumeBar = this.isHovered(mouseX, mouseY, volumeBarXOffset, volumeBarYOffset - volumeBarHeight * .5, volumeBarWidth, 8);
        this.volumeBarHeight = Interpolations.interpBezier(this.volumeBarHeight, hoveringVolumeBar ? 8 : 5, 0.3f);
        if (hoveringVolumeBar && Mouse.isButtonDown(0)) {
            double xDelta = Math.max(0, Math.min(volumeBarWidth, (mouseX - (volumeBarXOffset))));
            double percent = xDelta / volumeBarWidth;

            WidgetsManager.musicInfo.volume.setValue(percent);
        }

        playPauseButton.setAlpha(alpha);
        playPauseButton.setWidth(32);
        playPauseButton.setHeight(32);
        playPauseButton.setPosition(volumeBarXOffset + volumeBarWidth * .5 - playPauseButton.getWidth() * .5, progressBarYOffset + (volumeBarYOffset - progressBarYOffset) * .5 - playPauseButton.getHeight() * .5);
        playPauseButton.renderWidget(mouseX, mouseY, 0);
        playPauseButton.setColor(Color.WHITE);

        playPauseButton.setBeforeRenderCallback(() -> {
            if (CloudMusic.player == null || CloudMusic.player.isPausing()) {
                playPauseButton.setIcon("G");
            } else {
                playPauseButton.setIcon("F");
            }
        });

        playPauseButton.setOnClickCallback((x, y, i) -> {

            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) {
                if (CloudMusic.player.isPausing())
                    CloudMusic.player.unpause();
                else
                    CloudMusic.player.pause();

            }

            return true;
        });

        playPauseButton.fontOffsetY = 1;

        prev.setAlpha(alpha);
        prev.setWidth(32);
        prev.setHeight(32);
        prev.setPosition(volumeBarXOffset + volumeBarWidth * .5 - playPauseButton.getWidth() * .5 - 16 - prev.getWidth(), playPauseButton.getY());
        prev.renderWidget(mouseX, mouseY, 0);
        prev.fr = FontManager.music40;
        prev.fontOffsetY = 1;
        prev.setColor(Color.WHITE);

        prev.setOnClickCallback((x, y, i) -> {
            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                CloudMusic.prev();

            return true;
        });

        next.setAlpha(alpha);
        next.setWidth(32);
        next.setHeight(32);
        next.setPosition(volumeBarXOffset + volumeBarWidth * .5 + playPauseButton.getWidth() * .5 + 16, playPauseButton.getY());
        next.renderWidget(mouseX, mouseY, 0);
        next.fr = FontManager.music40;
        next.fontOffsetY = 1;
        next.setColor(Color.WHITE);

        next.setOnClickCallback((x, y, i) -> {
            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                CloudMusic.next();

            return true;
        });
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        playPauseButton.onMouseClickReceived(mouseX, mouseY, mouseButton);
        prev.onMouseClickReceived(mouseX, mouseY, mouseButton);
        next.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

    IconWidget playPauseButton = new IconWidget("G", FontManager.music40, 0, 0, 24, 24);
    IconWidget prev = new IconWidget("E", FontManager.music40, 0, 0, 32, 32);
    IconWidget next = new IconWidget("H", FontManager.music40, 0, 0, 32, 32);

    private String formatDuration(float totalMillis) {
        float totalSeconds = totalMillis / 1000;

        float hours = totalSeconds / 3600;
        float minutes = (totalSeconds % 3600) / 60;
        float seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if ((int) hours > 0) {
            sb.append(String.format("%02d:", (int) hours));
        }

        sb.append(String.format("%02d:", (int) minutes));
        sb.append(String.format("%02d", (int) seconds));

        return sb.toString();
    }

    float musicBgAlpha = 0.0f;
    ITextureObject prevBg = null, prevCover;
    static Music prevMusic = null;
    double fftScale = 0;

    private void renderBackground(double posX, double posY, double width, double height, float alpha) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location musicCoverBlured = CloudMusic.currentlyPlaying == null ? null : MusicInfoWidget.getMusicCoverBlurred(CloudMusic.currentlyPlaying);
        ITextureObject texBg = CloudMusic.currentlyPlaying == null ? null : textureManager.getTexture(musicCoverBlured);

        if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying != prevMusic) {
//            System.out.println("SWITCH, " + (prevMusic == null ? "NULL" : prevMusic.getName()) + " -> " + CloudMusic.currentlyPlaying.getName());
            prevBg = prevMusic == null ? null : textureManager.getTexture(MusicInfoWidget.getMusicCoverBlurred(prevMusic));
            prevCover = prevMusic == null ? null : textureManager.getTexture(MusicInfoWidget.getMusicCover(prevMusic));
            prevMusic = CloudMusic.currentlyPlaying;
            musicBgAlpha = coverAlpha = 0.0f;
        }

        if (texBg != null || prevBg != null) {

            GlStateManager.pushMatrix();

            float max = 0;
            for (int i = 0; i < Math.min(20, AudioPlayer.bandValues.length); i++) {
                max = Math.max(max, AudioPlayer.bandValues[i]);
            }

            if (!Double.isFinite(fftScale))
                fftScale = 0;

            if (!Float.isFinite(max) || max <= .2f)
                max = 0;

            fftScale = Interpolations.interpBezier(fftScale, max * .1, .4f);

            scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 1 + fftScale);

            if (prevBg != null && musicBgAlpha < 0.99f) {
                GlStateManager.bindTexture(prevBg.getGlTextureId());
                prevBg.linearFilter();
                GlStateManager.color(1, 1, 1, alpha);
                Image.draw(posX, posY + height * .5 - width * .5, width, width, Image.Type.NoColor);
            }

            if (texBg != null) {
                this.musicBgAlpha = Interpolations.interpBezier(this.musicBgAlpha, 1.0f, prevBg == null ? 0.15f : 0.05f);
                GlStateManager.bindTexture(texBg.getGlTextureId());
                texBg.linearFilter();
                GlStateManager.color(1, 1, 1, alpha * this.musicBgAlpha);
                Image.draw(posX, posY + height * .5 - width * .5, width, width, Image.Type.NoColor);
            }

            GlStateManager.popMatrix();
        }

        Rect.draw(posX, posY, width, height, hexColor(0, 0, 0, alpha * .3f));
    }
}
