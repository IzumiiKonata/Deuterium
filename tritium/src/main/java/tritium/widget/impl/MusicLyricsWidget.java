package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import tritium.ncm.music.CloudMusic;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.rendering.RGBA;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.screens.ncm.LyricLine;
import tritium.settings.BooleanSetting;
import tritium.settings.ClientSettings;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;
import tritium.utils.math.Mth;
import tritium.widget.Widget;

/**
 * @author IzumiiKonata
 * Date: 2025/2/14 20:34
 */
public class MusicLyricsWidget extends Widget {

    static double scrollOffset = 0;

    private double fontH, lyricH;

    public ModeSetting<ScrollEffects> scrollEffects = new ModeSetting<>("Scroll Effects", ScrollEffects.Scroll);
    public ModeSetting<AlignMode> alignMode = new ModeSetting<>("Align Mode", AlignMode.Center);

    public enum ScrollEffects {
        Scroll,
        FadeIn,
        SlideIn
    }

    public enum AlignMode {
        Left,
        Center,
        Right
    }

    public NumberSetting<Integer> width = new NumberSetting<>("Width", 450, 225, 900, 5);
    public NumberSetting<Integer> height = new NumberSetting<>("Height", 120, 60, 480, 5);
    public NumberSetting<Double> lyricHeight = new NumberSetting<>("Lyric Height", 20.0, 14.0, 50.0, 0.5);

    public BooleanSetting shadow = new BooleanSetting("Shadow", false);
    public BooleanSetting singleLine = new BooleanSetting("Single Line Mode", false);
    public BooleanSetting showTranslation = new BooleanSetting("Show Translation", true);
    public BooleanSetting graceScroll = new BooleanSetting("Elegant Scrolling", true);
    public BooleanSetting showRoman = new BooleanSetting("Show Romanization in Japanese songs", false);

    public MusicLyricsWidget() {
        super("MusicLyrics");

        graceScroll.setShouldRender(() -> !singleLine.getValue());
        showRoman.setShouldRender(() -> showTranslation.getValue());
    }

    public static void resetProgress(float progress) {
        if (CloudMusic.lyrics.isEmpty()) return;

        try {
            CloudMusic.setLyricsProgress(progress);
            scrollOffset = (CloudMusic.lyrics.indexOf(CloudMusic.currentLyric)) * getLyricHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getLyricHeight() {
        double baseHeight = getFontRenderer().getHeight();
        double adjustment = CloudMusic.hasSecondaryLyrics() ? 0 : -getSmallFontRenderer().getHeight() - 4;
        return baseHeight + adjustment + WidgetsManager.musicLyrics.lyricHeight.getValue();
    }

    public static boolean hasSecondaryLyrics() {
        return CloudMusic.hasSecondaryLyrics();
    }

    public static String getSecondaryLyrics(LyricLine bean) {
        return CloudMusic.getSecondaryLyrics(bean);
    }

    @Override
    public void onRender(boolean editing) {

        if (!shouldRender()) {
            if (editing) {
                renderEditingPlaceholder();
            }
            return;
        }

        this.setWidth(this.width.getValue().floatValue());
        this.setHeight(this.height.getValue().floatValue());

        this.fontH = getFontRenderer().getHeight();
        this.lyricH = getLyricHeight();

        float songProgress = CloudMusic.player.getCurrentTimeMillis();

        boolean shouldNotDisplayOtherLyrics = this.singleLine.getValue();

        handleSingleLineMode(shouldNotDisplayOtherLyrics);

        updateScrollOffset(shouldNotDisplayOtherLyrics);

        GlStateManager.pushMatrix();

        StencilClipManager.beginClip(() -> Rect.draw(this.getX() - 2, this.getY(), this.getWidth() + 4, this.getHeight(), -1));

        renderAllLyrics(shouldNotDisplayOtherLyrics, songProgress);

        GlStateManager.popMatrix();
        StencilClipManager.endClip();

        if (ClientSettings.DEBUG_MODE.getValue()) {
            LyricLine currentLine = CloudMusic.currentLyric;
            if (currentLine != null && !CloudMusic.haveNoWords) {
                WordInfo wordInfo = calculateCurrentWordInfo(currentLine, songProgress);

                LyricLine.Word current = currentLine.words.get(wordInfo.currentIndex);
                FontManager.pf28bold.drawStringWithShadow("Current word: " + current.word, 100, 100, -1);
                double value = (songProgress - current.timestamp) / (double) (current.duration);
                FontManager.pf28bold.drawStringWithShadow("Perc: " + value, 100, 120, -1);
                FontManager.pf28bold.drawStringWithShadow("Dur: " + current.duration, 100, 140, -1);
                FontManager.pf28bold.drawStringWithShadow("Pos: " + (songProgress - current.timestamp), 100, 160, -1);
            }
        }
    }

    private boolean shouldRender() {
        return CloudMusic.player != null && !CloudMusic.player.isFinished() && !CloudMusic.lyrics.isEmpty();
    }

    private void handleSingleLineMode(boolean shouldNotDisplayOtherLyrics) {
        if (shouldNotDisplayOtherLyrics && CloudMusic.currentLyric == null) {
            if (!CloudMusic.lyrics.isEmpty()) {
                CloudMusic.currentLyric = CloudMusic.lyrics.getFirst();
            }
        }
    }

    private void updateScrollOffset(boolean shouldNotDisplayOtherLyrics) {
        int indexOf = CloudMusic.lyrics.indexOf(CloudMusic.currentLyric);

        if (!shouldNotDisplayOtherLyrics) {
            if (CloudMusic.currentLyric == null) {
                scrollOffset = 0;
            } else {
                scrollOffset = Interpolations.interpolate(scrollOffset, indexOf * lyricH, 0.2f);
            }
        }
    }

    private void renderAllLyrics(boolean shouldNotDisplayOtherLyrics, float songProgress) {
        double offsetY = this.getY() + this.getHeight() / 2.0 - fontH / 2.0 - scrollOffset;
        int indexOf = CloudMusic.lyrics.indexOf(CloudMusic.currentLyric);

        AlignMode alignMode = this.alignMode.getValue();
        double pivotX = alignPivotX(alignMode);

        synchronized (CloudMusic.lyrics) {
            for (int i = 0; i < CloudMusic.lyrics.size(); i++) {
                LyricLine line = CloudMusic.lyrics.get(i);

                if (shouldNotDisplayOtherLyrics) {
                    if (i < indexOf) continue;
                    if (i > indexOf) break;
                }

                LyricRenderInfo renderInfo = calculateLyricPosition(
                        line, i, indexOf, offsetY, shouldNotDisplayOtherLyrics
                );

                if (renderInfo.shouldSkip) {
                    offsetY += lyricH;
                    continue;
                }

                if (renderInfo.shouldBreak) {
                    break;
                }

                updateLyricAnimation(line, i == indexOf);

                double focus = Math.max(0f, line.lineAlpha - 0.25f) / 0.75;
                double scale = 1.0 + focus * 0.05;

                GlStateManager.pushMatrix();
                scaleAtPos(pivotX, renderInfo.yPosition + fontH * 0.5, scale);

                renderLyricText(line, renderInfo, i, indexOf);

                if (line == CloudMusic.currentLyric && !line.words.isEmpty()) {
                    handleScrollEffects(line, renderInfo, songProgress);
                }

                GlStateManager.popMatrix();

                offsetY += lyricH;
            }
        }
    }

    private double alignPivotX(AlignMode alignMode) {
        return switch (alignMode) {
            case Left -> this.getX();
            case Center -> this.getX() + this.getWidth() / 2.0;
            case Right -> this.getX() + this.getWidth();
        };
    }

    private double computeEdgeFade(double yPosition) {
        double height = this.getHeight();
        if (height <= 0) return 1.0;

        double band = Math.min(height * 0.5, lyricH * 1.4);
        if (band <= 0) return 1.0;

        double cy = yPosition + fontH * 0.5;
        double distance = Math.min(cy - this.getY(), this.getY() + height - cy);

        return Easing.EASE_OUT_CUBIC.getFunction().apply(Mth.limit(distance / band, 0, 1));
    }

    private static int withFade(int color, double fade) {
        if (fade >= 1.0) return color;
        int alpha = (int) (((color >>> 24) & 0xFF) * Mth.limit(fade, 0, 1));
        return RGBA.color(color & 0xFFFFFF, alpha);
    }

    private void renderEditingPlaceholder() {
        this.setWidth(this.width.getValue().floatValue());
        this.setHeight(this.height.getValue().floatValue());
        this.fontH = getFontRenderer().getHeight();
        this.lyricH = getLyricHeight();

        AlignMode alignMode = this.alignMode.getValue();
        double pivotX = alignPivotX(alignMode);
        double centerY = this.getY() + this.getHeight() / 2.0 - fontH / 2.0;
        String[] samples = {"Tritium Music", "正在播放歌词预览", "Now Playing Preview"};

        GlStateManager.pushMatrix();
        StencilClipManager.beginClip(() -> Rect.draw(this.getX() - 2, this.getY(), this.getWidth() + 4, this.getHeight(), -1));

        for (int i = -1; i <= 1; i++) {
            double y = centerY + i * lyricH;
            double fade = computeEdgeFade(y);
            int alpha = (int) ((i == 0 ? 1.0 : 0.25) * 255 * fade);
            double scale = i == 0 ? 1.05 : 1.0;

            GlStateManager.pushMatrix();
            scaleAtPos(pivotX, y + fontH * 0.5, scale);
            renderAlignedText(samples[i + 1], y, RGBA.color(255, 255, 255, alpha), alignMode);
            GlStateManager.popMatrix();
        }

        StencilClipManager.endClip();
        GlStateManager.popMatrix();
    }

    private LyricRenderInfo calculateLyricPosition(LyricLine line, int index, int currentIndex,
                                                   double offsetY, boolean singleLineMode) {
        LyricRenderInfo info = new LyricRenderInfo();

        if (!singleLineMode) {
            double dest = this.getY() + this.getHeight() / 2.0 - fontH / 2.0 +
                    index * lyricH - (currentIndex * lyricH);

            if (line.offsetY == Double.MIN_VALUE || Math.abs(line.offsetY - dest) > 100) {
                line.offsetY = dest;
            }

            if (line.offsetY + lyricH < this.getY()) {
                info.shouldSkip = true;
                line.offsetY = dest;
                return info;
            }

            if (offsetY > this.getY() + this.getHeight()) {
                info.shouldBreak = true;
                return info;
            }

            applyGraceScroll(line, index, currentIndex, dest);

            info.yPosition = this.graceScroll.getValue() ? line.offsetY : offsetY;
        } else {
            info.yPosition = this.getY() + this.getHeight() / 2.0 - fontH / 2.0;
            line.offsetY = info.yPosition;
        }

        info.fade = computeEdgeFade(info.yPosition);
        return info;
    }

    private void applyGraceScroll(LyricLine line, int index, int currentIndex, double dest) {
        float speed = 0.15f;
        LyricLine prevLrc = null;

        try {
            if (index > 0) {
                prevLrc = CloudMusic.lyrics.get(index - 1);
            }
        } catch (Exception ignored) {}

        if (prevLrc != null) {
            double prevDest = this.getY() + this.getHeight() / 2.0 - fontH / 2.0 +
                    (index - 1) * lyricH - (currentIndex * lyricH);
            double v = prevLrc.offsetY - prevDest;

            if (v < lyricH * 0.55f) {
                line.offsetY = Interpolations.interpolate(line.offsetY, dest, speed);
            }
        } else {
            line.offsetY = Interpolations.interpolate(line.offsetY, dest, speed);
        }
    }

    private void updateLyricAnimation(LyricLine line, boolean isCurrent) {
        line.lineAlpha = Interpolations.interpolate(
                line.lineAlpha,
                isCurrent ? 1f : .25f,
                0.1f
        );
    }

    private void renderLyricText(LyricLine line, LyricRenderInfo renderInfo,
                                 int index, int currentIndex) {
        boolean hasWords = !line.words.isEmpty();
        boolean bSlideIn = this.scrollEffects.getValue() == ScrollEffects.SlideIn;
        boolean shouldRender = !hasWords || !bSlideIn || index != currentIndex ||
                this.alignMode.getValue() == AlignMode.Left;

        boolean isActive = index <= currentIndex;
        int primaryColor = withFade(RGBA.color(255, 255, 255, calculateAlpha(line, index, currentIndex, hasWords)), renderInfo.fade);
        int secondaryColor = withFade(RGBA.color(255, 255, 255, isActive ? (int) (line.lineAlpha * 255) : 100), renderInfo.fade);

        String secondaryLyric = hasSecondaryLyrics() ? getSecondaryLyrics(line) : "";
        boolean secondaryLyricEmpty = secondaryLyric.isEmpty();

        renderByAlignment(line, renderInfo, secondaryLyric, secondaryLyricEmpty,
                shouldRender, primaryColor, secondaryColor);
    }

    private int calculateAlpha(LyricLine line, int index, int currentIndex, boolean hasWords) {
        if (hasWords) {
            return index != currentIndex ? (int) (line.lineAlpha * 255) : 80;
        } else {
            return (int) (line.lineAlpha * 255);
        }
    }

    private void renderByAlignment(LyricLine line, LyricRenderInfo renderInfo,
                                   String secondaryLyric, boolean secondaryLyricEmpty,
                                   boolean shouldRender, int hexColor, int rgb) {
        AlignMode alignMode = this.alignMode.getValue();
        double y = renderInfo.yPosition;
        double secondaryY = y + fontH + 2;

        switch (alignMode) {
            case Left:
                if (shouldRender) {
                    bigFrString(line.getLyric(), this.getX(), y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric, this.getX(), secondaryY, rgb);
                }
                break;
            case Center:
                double centerX = this.getX() + this.getWidth() / 2.0;
                if (shouldRender) {
                    bigFrStringCentered(line.getLyric(), centerX, y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrStringCentered(secondaryLyric, centerX, secondaryY, rgb);
                }
                break;
            case Right:
                if (shouldRender) {
                    bigFrString(line.getLyric(),
                            this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(line.getLyric()), y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric,
                            this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidthD(secondaryLyric),
                            secondaryY, rgb);
                }
                break;
        }
    }

    private void handleScrollEffects(LyricLine line, LyricRenderInfo renderInfo, float songProgress) {
        WordInfo wordInfo = calculateCurrentWordInfo(line, songProgress);

        updateScrollWidth(line, wordInfo, songProgress);

        renderScrollEffect(line, renderInfo, wordInfo, songProgress);
    }

    private WordInfo calculateCurrentWordInfo(LyricLine line, float songProgress) {
        WordInfo info = new WordInfo();

        // find current word index
        for (int k = 0; k < line.words.size(); k++) {
            LyricLine.Word word = line.words.get(k);

            if (word.timestamp > songProgress) {
                info.currentIndex = Math.max(0, k - 1);
                break;
            } else if (k == line.words.size() - 1) {
                info.currentIndex = k;
            }
        }

        // calculate text before current word
        for (int m = 0; m < info.currentIndex; m++) {
            info.textBefore.append(line.words.get(m).word);
        }

        return info;
    }

    private void updateScrollWidth(LyricLine line, WordInfo wordInfo, float songProgress) {
        LyricLine.Word current = line.words.get(wordInfo.currentIndex);

        double value = (songProgress - current.timestamp) / (double) (current.duration);

        double progress = Mth.limit(value, 0, 1);

        double offsetX = progress * getFontRenderer().getStringWidthD(current.word);

        line.scrollWidth = getFontRenderer().getStringWidthD(wordInfo.textBefore.toString()) + offsetX;
    }

    private void renderScrollEffect(LyricLine line, LyricRenderInfo renderInfo, WordInfo wordInfo, float songProgress) {
        switch (this.scrollEffects.getValue()) {
            case Scroll -> renderScrollMode(line, renderInfo);
            case FadeIn -> renderFadeInMode(line, renderInfo, wordInfo, songProgress);
            case SlideIn -> renderSlideInMode(line, renderInfo, wordInfo, songProgress);
        }
    }

    private void renderScrollMode(LyricLine line, LyricRenderInfo renderInfo) {
        AlignMode alignMode = this.alignMode.getValue();
        double x = calculateAlignmentX(line.getLyric(), alignMode);

        StencilClipManager.beginClip(() -> Rect.draw(x, renderInfo.yPosition, line.scrollWidth + 1, fontH + 4, -1));

        renderAlignedText(line.getLyric(), renderInfo.yPosition, withFade(-1, renderInfo.fade), alignMode);

        StencilClipManager.endClip();
    }

    private void renderFadeInMode(LyricLine line, LyricRenderInfo renderInfo, WordInfo wordInfo, float songProgress) {
        double offsetX = calculateAlignmentX(line.getLyric(), this.alignMode.getValue());

        for (int m = 0; m <= wordInfo.currentIndex; m++) {
            LyricLine.Word word = line.words.get(m);

            if (m == wordInfo.currentIndex) {
                updateCurrentWordAnimation(word, songProgress);
            } else {
                word.alpha = 1;
            }

            bigFrString(word.word, offsetX, renderInfo.yPosition,
                    RGBA.color(255, 255, 255, (int) (word.alpha * 255 * renderInfo.fade)));

            offsetX += getFontRenderer().getStringWidthD(word.word);
        }
    }

    private void renderSlideInMode(LyricLine line, LyricRenderInfo renderInfo, WordInfo wordInfo, float songProgress) {
        double offsetX = calculateSlideInTargetX(line, this.alignMode.getValue());
        double targetOffsetX = 0;

        for (int m = 0; m <= wordInfo.currentIndex; m++) {
            LyricLine.Word word = line.words.get(m);
            double stWidth = getFontRenderer().getStringWidthD(word.word);

            if (m == wordInfo.currentIndex) {
                updateCurrentWordAnimation(word, songProgress);
                targetOffsetX += stWidth * Easing.EASE_OUT_CUBIC.getFunction().apply(word.progress);
            } else {
                word.alpha = 1;
                targetOffsetX += stWidth;
            }

            bigFrString(word.word, offsetX, renderInfo.yPosition,
                    RGBA.color(255, 255, 255, (int) (word.alpha * 255 * renderInfo.fade)));

            offsetX += stWidth;
        }

        line.targetOffsetX = targetOffsetX;
    }

    private void updateCurrentWordAnimation(LyricLine.Word word, float songProgress) {
        double progress = Mth.limit((songProgress - word.timestamp) / (double) word.duration, 0, 1);
        word.progress = progress;
        word.alpha = (float) Math.min(1, progress * 1.25f);
    }

    private double calculateAlignmentX(String text, AlignMode alignMode) {
        if (alignMode == AlignMode.Left) {
            return this.getX();
        } else if (alignMode == AlignMode.Center) {
            return this.getX() + this.getWidth() / 2.0f - getFontRenderer().getStringWidthD(text) / 2.0f;
        } else {
            return this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(text);
        }
    }

    private double calculateSlideInTargetX(LyricLine line, AlignMode alignMode) {
        if (alignMode == AlignMode.Left) {
            return this.getX();
        } else if (alignMode == AlignMode.Center) {
            return this.getX() + this.getWidth() / 2.0 - line.targetOffsetX / 2.0;
        } else {
            return this.getX() + this.getWidth() - line.targetOffsetX;
        }
    }

    private void renderAlignedText(String text, double y, int color, AlignMode alignMode) {
        if (alignMode == AlignMode.Left) {
            bigFrString(text, this.getX(), y, color);
        } else if (alignMode == AlignMode.Center) {
            bigFrStringCentered(text, this.getX() + this.getWidth() / 2.0, y, color);
        } else {
            bigFrString(text, this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(text), y, color);
        }
    }

    private static class LyricRenderInfo {
        double yPosition;
        double fade = 1.0;
        boolean shouldSkip = false;
        boolean shouldBreak = false;
    }

    private static class WordInfo {
        int currentIndex = 0;
        StringBuilder textBefore = new StringBuilder();
    }

    private static CFontRenderer getFontRenderer() {
        return FontManager.pf28bold;
    }

    private static CFontRenderer getSmallFontRenderer() {
        return FontManager.pf18bold;
    }

    private void bigFrString(String text, double x, double y, int color) {
        if (this.shadow.getValue()) {
            getFontRenderer().drawStringWithShadow(text, x, y, color);
        } else {
            getFontRenderer().drawString(text, x, y, color);
        }
    }

    private void bigFrStringCentered(String text, double x, double y, int color) {
        if (this.shadow.getValue()) {
            getFontRenderer().drawCenteredStringWithShadow(text, x, y, color);
        } else {
            getFontRenderer().drawCenteredString(text, x, y, color);
        }
    }

    private void smallFrString(String text, double x, double y, int color) {
        if (this.shadow.getValue()) {
            getSmallFontRenderer().drawStringWithShadow(text, x, y, color);
        } else {
            getSmallFontRenderer().drawString(text, x, y, color);
        }
    }

    private void smallFrStringCentered(String text, double x, double y, int color) {
        if (this.shadow.getValue()) {
            getSmallFontRenderer().drawCenteredStringWithShadow(text, x, y, color);
        } else {
            getSmallFontRenderer().drawCenteredString(text, x, y, color);
        }
    }

}