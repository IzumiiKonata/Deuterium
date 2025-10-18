package tritium.screens.ncm;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
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
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.widgets.IconWidget;
import tritium.widget.impl.MusicInfoWidget;
import tritium.widget.impl.MusicLyricsWidget;

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

    public static void initLyric(JsonObject lyric) {
        // reset states

        List<LyricLine> parsed = LyricParser.parse(lyric);

        synchronized (lyrics) {
            lyrics.clear();
            lyrics.addAll(parsed);

            currentDisplaying = lyrics.get(0);
            updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * .5);
        }
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

    private void renderLyrics(double mouseX, double mouseY, double posX, double posY, double width, double height, float alpha) {

        if (lyrics.isEmpty())
            return;

        double spacingToRight = 16;

        float songProgress = CloudMusic.player.getCurrentTimeMillis();

        updateCurrentDisplayingLyric(songProgress);

        double lyricsWidth = width * .4;
        this.updateLyricPositions(lyricsWidth);

        for (LyricLine lyric : lyrics) {

            if (lyric.posY + lyric.height + 16 < posY) {
                continue;
            }

            if (lyric.posY > posX + height) {
                break;
            }

            lyric.alpha = Interpolations.interpBezier(lyric.alpha, lyric == currentDisplaying ? 1f : 0.4f, 0.2f);
            boolean isHovering = isHovered(mouseX, mouseY, RenderSystem.getWidth() * .5, lyric.posY, lyricsWidth, lyric.height);
            lyric.hoveringAlpha = Interpolations.interpBezier(lyric.hoveringAlpha, isHovering ? .2f : 0f, 0.2f);
            roundedRect(RenderSystem.getWidth() * .5 - 4, lyric.posY, lyricsWidth, lyric.height + 8, 8, 8, 1, 1, 1, alpha * lyric.hoveringAlpha);

            double renderX = RenderSystem.getWidth() * .5;
            double renderY = lyric.posY;

            List<LyricLine.Word> words = lyric.words;
            if (!words.isEmpty()) {
                for (int i = 0; i < words.size(); i++) {
                    LyricLine.Word word = words.get(i);
                    double wordWidth = FontManager.pf65bold.getStringWidthD(word.word);

                    if (renderX + wordWidth > RenderSystem.getWidth() * .5 + lyricsWidth) {
                        renderX = RenderSystem.getWidth() * .5;
                        renderY += FontManager.pf65bold.getHeight() * .85;
                    }

                    FontManager.pf65bold.drawString(word.word, renderX, renderY - word.emphasize, hexColor(1, 1, 1, alpha * .5f));

                    if (lyric == currentDisplaying) {
                        LyricLine.Word prev = i > 0 ? words.get(i - 1) : null;

                        long prevTiming = i == 0 ? 0 : prev.timing;
                        double progress = Math.max(0, Math.min(1, (songProgress - lyric.timeStamp - prevTiming) / (double) (word.timing - prevTiming)));
                        double stringWidthD = FontManager.pf65bold.getStringWidthD(word.word);
                        word.emphasize = 0;

                        double finalRenderX = renderX;
                        double finalRenderY = renderY;
                        StencilClipManager.beginClip(() -> {
                            Rect.draw(finalRenderX, finalRenderY - word.emphasize, progress * stringWidthD, FontManager.pf65bold.getHeight(), -1);
                        });

                        FontManager.pf65bold.drawString(word.word, renderX, renderY - word.emphasize, hexColor(1, 1, 1, alpha));
                        StencilClipManager.endClip();
                    }

                    renderX += wordWidth;
                }
            } else {
                String[] strings = FontManager.pf65bold.fitWidth(lyric.lyric, lyricsWidth);

                for (String string : strings) {
                    FontManager.pf65bold.drawString(string, renderX, renderY, hexColor(1, 1, 1, alpha * lyric.alpha));
                    renderY += FontManager.pf65bold.getHeight() * .85 + 4;
                }

                renderY -= FontManager.pf65bold.getHeight() * .85 + 4;
            }

            if (lyric.translationText != null) {
                FontManager.pf34bold.drawString(lyric.translationText, RenderSystem.getWidth() * .5, renderY + FontManager.pf65bold.getHeight() * .85 + 8, hexColor(1, 1, 1, alpha * .75f * lyric.alpha));
            }
        }
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
        List<LyricLine> subList = lyrics.subList(0, idxCurrent);
        float fraction = 0.15f;
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
            double prevOffsetY = prev == null ? offsetY : offsetY - 16 - prev.height;

            if (prev == null || Math.abs(prev.posY - prevOffsetY) / prev.height <= .75) {
                lyric.posY = Interpolations.interpBezier(lyric.posY, offsetY, fraction);
            }

            offsetY += lyric.height + 16;
        }

    }

    private static void updateLyricPositionsImmediate(double width) {

        if (currentDisplaying == null)
            return;

        double offsetY = RenderSystem.getHeight() * lyricFraction() - 16;
        int toIndex = lyrics.indexOf(currentDisplaying);

        if (toIndex == -1 || toIndex >= lyrics.size())
            return;

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

    /**
     * 更新当前显示的歌词行
     */
    private static void updateCurrentDisplayingLyric(float songProgress) {
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
            this.roundedRectTextured(center - coverSize * .5, center - coverSize * .5, coverSize, coverSize, 3, alpha * coverAlpha);
        }

        double elementsXOffset = center - this.getCoverSizeMax() * .5;
        double elementsYOffset = center + this.getCoverSizeMax() * .5 + 16;

        FontManager.pf28bold.drawString(CloudMusic.currentlyPlaying.getName(), elementsXOffset, elementsYOffset, RenderSystem.hexColor(1, 1, 1, alpha));
        FontManager.pf20bold.drawString(CloudMusic.currentlyPlaying.getArtistsName(), elementsXOffset, elementsYOffset + FontManager.pf20bold.getHeight() + 8, RenderSystem.hexColor(1, 1, 1, alpha * .8f));

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

    private void renderBackground(double posX, double posY, double width, double height, float alpha) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location musicCoverBlured = CloudMusic.currentlyPlaying == null ? null : MusicInfoWidget.getMusicCoverBlurred(CloudMusic.currentlyPlaying);
        ITextureObject texBg = CloudMusic.currentlyPlaying == null ? null : textureManager.getTexture(musicCoverBlured);

        if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying != prevMusic) {
            System.out.println("SWITCH, " + (prevMusic == null ? "NULL" : prevMusic.getName()) + " -> " + CloudMusic.currentlyPlaying.getName());
            prevBg = prevMusic == null ? null : textureManager.getTexture(MusicInfoWidget.getMusicCoverBlurred(prevMusic));
            prevCover = prevMusic == null ? null : textureManager.getTexture(MusicInfoWidget.getMusicCover(prevMusic));
            prevMusic = CloudMusic.currentlyPlaying;
            musicBgAlpha = coverAlpha = 0.0f;
        }

        if (texBg != null || prevBg != null) {


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
        }

        Rect.draw(posX, posY, width, height, hexColor(0, 0, 0, alpha * .3f));
    }
}
