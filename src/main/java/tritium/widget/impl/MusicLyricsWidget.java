package tritium.widget.impl;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.rendering.RGBA;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.screens.ncm.LyricLine;
import tritium.screens.ncm.LyricParser;
import tritium.settings.BooleanSetting;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.StringUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * Date: 2025/2/14 20:34
 */
public class MusicLyricsWidget extends Widget {

    public static final List<LyricLine> allLyrics = new CopyOnWriteArrayList<>();
    static double scrollOffset = 0;
    public static LyricLine currentDisplaying = null;

    public static boolean hasTransLyrics = false, hasRomanization = false;

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

    public static void initLyric(JsonObject lyric, Music music) {
        // reset states
        hasTransLyrics = false;
        hasRomanization = false;

        if (lyric.has("tlyric") || lyric.has("ytlrc")) hasTransLyrics = true;
        if (lyric.has("romalrc") || lyric.has("yromalrc")) hasRomanization = true;
        List<LyricLine> parsed = LyricParser.parse(lyric);

//        fetchTTMLLyrics(music, parsed);

        synchronized (allLyrics) {
            allLyrics.clear();
            allLyrics.addAll(parsed);
        }

        scrollOffset = 0;
    }

    private static void fetchTTMLLyrics(Music music, List<LyricLine> parsed) {
        MultiThreadingUtil.runAsync(() -> {
            try {
                String lrc = HttpUtils.getString(
                        "https://gitee.com/IzumiiKonata/amll-ttml-db/raw/main/ncm-lyrics/" + music.getId() + ".yrc",
                        null
                );
                System.out.println("歌曲 " + music.getName() + " 存在 ttml 歌词, 获取中...");

                ArrayList<LyricLine> lines = new ArrayList<>();
                LyricParser.parseYrc(lrc, lines);

                for (LyricLine bean : lines) {

//                    System.out.println(bean.words.size());

                    for (LyricLine line : parsed) {
                        if (line.getLyric().toLowerCase().replace(" ", "").equals(bean.lyric.toLowerCase().replace(" ", ""))) {
                            bean.romanizationText = line.romanizationText;
                            bean.translationText = line.translationText;
                            break;
                        }
                    }

                }

                synchronized (allLyrics) {
                    allLyrics.addAll(lines);
                }
            } catch (Exception ignored) {
            }
        });
    }

    public static void quickResetProgress(float progress) {
        if (allLyrics.isEmpty()) return;

        try {
            resetAllLyricsState();

            resetWordStates();

            scrollOffset = 0;

            findCurrentLyric(progress);

            scrollOffset = (allLyrics.indexOf(currentDisplaying)) * getLyricHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void resetAllLyricsState() {
        for (LyricLine l : allLyrics) {
            l.scrollWidth = 0;
            l.offsetX = 0;
            l.offsetY = Double.MIN_VALUE;
            l.targetOffsetX = 0;
        }
    }

    private static void resetWordStates() {
        for (LyricLine allLyric : allLyrics) {
            for (LyricLine.Word word : allLyric.words) {
                word.alpha = 0.0f;
                word.progress = 0.0;
            }
        }
    }

    private static void findCurrentLyric(float progress) {
        currentDisplaying = allLyrics.get(0);

        for (LyricLine line : allLyrics) {
            if (line.getTimestamp() > progress) {
                int i = allLyrics.indexOf(line);
                if (i > 0) {
                    currentDisplaying = allLyrics.get(i - 1);
                }
                break;
            }
        }
    }

    public static double getLyricHeight() {
        double baseHeight = getFontRenderer().getHeight();
        double adjustment = hasSecondaryLyrics() ? 0 : -getSmallFontRenderer().getHeight() - 4;
        return baseHeight + adjustment + WidgetsManager.musicLyrics.lyricHeight.getValue();
    }

    public static boolean hasSecondaryLyrics() {
        return (hasTransLyrics || hasRomanization) && WidgetsManager.musicLyrics.showTranslation.getValue();
    }

    public static String getSecondaryLyrics(LyricLine bean) {
        if (hasTransLyrics) {
            if (!WidgetsManager.musicLyrics.showRoman.getValue()) {
                return StringUtils.returnEmptyStringIfNull(bean.getTranslationText());
            } else {
                // 如果有罗马音且开启了罗马音显示
                if (hasRomanization) {
                    return StringUtils.returnEmptyStringIfNull(bean.getRomanizationText());
                } else {
                    return StringUtils.returnEmptyStringIfNull(bean.getTranslationText());
                }
            }
        }

        // 只有罗马音
        if (hasRomanization) {
            if (WidgetsManager.musicLyrics.showRoman.getValue()) {
                return StringUtils.returnEmptyStringIfNull(bean.getRomanizationText());
            }
        }

        return "";
    }

    @Override
    public void onRender(boolean editing) {

        if (!shouldRender()) {
            return;
        }

        float songProgress = CloudMusic.player.getCurrentTimeMillis();

        updateCurrentDisplayingLyric(songProgress);

        boolean shouldNotDisplayOtherLyrics = this.singleLine.getValue();

        handleSingleLineMode(shouldNotDisplayOtherLyrics);

        updateScrollOffset(shouldNotDisplayOtherLyrics);

        GlStateManager.pushMatrix();

        StencilClipManager.beginClip(() -> {
            Rect.draw(this.getX() - 2, this.getY(), this.getWidth() + 4, this.getHeight(), -1);
        });

        renderAllLyrics(shouldNotDisplayOtherLyrics, songProgress);

        cleanupRender();
        StencilClipManager.endClip();
    }

    private boolean shouldRender() {
        return CloudMusic.player != null && !CloudMusic.player.isFinished() && !allLyrics.isEmpty();
    }

    private void updateCurrentDisplayingLyric(float songProgress) {
        for (int i = 0; i < allLyrics.size(); i++) {
            LyricLine line = allLyrics.get(i);

            if (line.getTimestamp() > songProgress) {
                if (i > 0) {
                    currentDisplaying = allLyrics.get(i - 1);
                }
                break;
            } else if (i == allLyrics.size() - 1) {
                currentDisplaying = allLyrics.get(i);
            }
        }
    }

    private void handleSingleLineMode(boolean shouldNotDisplayOtherLyrics) {
        if (shouldNotDisplayOtherLyrics && currentDisplaying == null) {
            if (!allLyrics.isEmpty()) {
                currentDisplaying = allLyrics.get(0);
            }
        }
    }

    private void updateScrollOffset(boolean shouldNotDisplayOtherLyrics) {
        int indexOf = allLyrics.indexOf(currentDisplaying);

        if (!shouldNotDisplayOtherLyrics) {
            if (currentDisplaying == null) {
                scrollOffset = 0;
            } else {
                scrollOffset = Interpolations.interpBezier(scrollOffset, (indexOf * getLyricHeight()), 0.2f);
            }
        }
    }

    private void renderAllLyrics(boolean shouldNotDisplayOtherLyrics, float songProgress) {
        double offsetY = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 - scrollOffset;
        int indexOf = allLyrics.indexOf(currentDisplaying);

        synchronized (allLyrics) {
            for (int i = 0; i < allLyrics.size(); i++) {
                LyricLine line = allLyrics.get(i);

                if (shouldNotDisplayOtherLyrics) {
                    if (i < indexOf) continue;
                    if (i > indexOf) break;
                }

                LyricRenderInfo renderInfo = calculateLyricPosition(
                        line, i, indexOf, offsetY, shouldNotDisplayOtherLyrics
                );

                if (renderInfo.shouldSkip) {
                    offsetY += getLyricHeight();
                    continue;
                }

                if (renderInfo.shouldBreak) {
                    break;
                }

                updateLyricAnimation(line, i == indexOf);

                renderLyricText(line, renderInfo, i, indexOf);

                if (line == currentDisplaying && !line.words.isEmpty()) {
                    handleScrollEffects(line, renderInfo, songProgress);
                }

                offsetY += getLyricHeight();
            }
        }
    }

    private LyricRenderInfo calculateLyricPosition(LyricLine line, int index, int currentIndex,
                                                   double offsetY, boolean singleLineMode) {
        LyricRenderInfo info = new LyricRenderInfo();

        if (!singleLineMode) {
            double dest = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 +
                    index * getLyricHeight() - (currentIndex * getLyricHeight());

            if (line.offsetY == Double.MIN_VALUE || Math.abs(line.offsetY - dest) > 100) {
                line.offsetY = dest;
            }

            if (line.offsetY + getLyricHeight() < this.getY()) {
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
            info.yPosition = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0;
            line.offsetY = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0;
        }

        return info;
    }

    private void applyGraceScroll(LyricLine line, int index, int currentIndex, double dest) {
        float speed = 0.15f;
        LyricLine prevLrc = null;

        try {
            if (index > 0) {
                prevLrc = allLyrics.get(index - 1);
            }
        } catch (Exception ignored) {}

        if (prevLrc != null) {
            double prevDest = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 +
                    (index - 1) * getLyricHeight() - (currentIndex * getLyricHeight());
            double v = prevLrc.offsetY - prevDest;

            // 前一行接近目标位置时才开始滚动
            if (v < MusicLyricsWidget.getLyricHeight() * 0.55f) {
                line.offsetY = Interpolations.interpBezier(line.offsetY, dest, speed);
            }
        } else {
            // 第一行直接滚动
            line.offsetY = Interpolations.interpBezier(line.offsetY, dest, speed);
        }
    }

    private void updateLyricAnimation(LyricLine line, boolean isCurrent) {
        line.alpha = Interpolations.interpBezier(
                line.alpha,
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

        int alpha = calculateAlpha(line, index, currentIndex, hasWords);

        String secondaryLyric = hasSecondaryLyrics() ? getSecondaryLyrics(line) : "";
        boolean secondaryLyricEmpty = secondaryLyric.isEmpty();

        Runnable renderTask = createRenderTask(
                line, renderInfo, secondaryLyric, secondaryLyricEmpty,
                shouldRender, alpha, index <= currentIndex
        );

        renderTask.run();
    }

    private int calculateAlpha(LyricLine line, int index, int currentIndex, boolean hasWords) {
        if (hasWords) {
            return index != currentIndex ? (int) (line.alpha * 255) : 80;
        } else {
            return (int) (line.alpha * 255);
        }
    }

    private Runnable createRenderTask(LyricLine line, LyricRenderInfo renderInfo,
                                      String secondaryLyric, boolean secondaryLyricEmpty,
                                      boolean shouldRender, int alpha,
                                      boolean isActive) {
        return () -> {
            int hexColor = RGBA.color(255, 255, 255, alpha);
            int rgb = RGBA.color(255, 255, 255, isActive ? (int) (line.alpha * 255) : 100);

            renderByAlignment(line, renderInfo, secondaryLyric, secondaryLyricEmpty,
                    shouldRender, hexColor, rgb);

        };
    }

    private void renderByAlignment(LyricLine line, LyricRenderInfo renderInfo,
                                   String secondaryLyric, boolean secondaryLyricEmpty,
                                   boolean shouldRender, int hexColor, int rgb) {
        AlignMode alignMode = this.alignMode.getValue();
        double y = renderInfo.yPosition;

        switch (alignMode) {
            case Left:
                if (shouldRender) {
                    bigFrString(line.getLyric(), this.getX(), y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric, this.getX(),
                            y + getFontRenderer().getHeight() + 2, rgb);
                }
                break;
            case Center:
                double centerX = this.getX() + this.getWidth() / 2.0;
                if (shouldRender) {
                    bigFrStringCentered(line.getLyric(), centerX, y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrStringCentered(secondaryLyric, centerX,
                            y + getFontRenderer().getHeight() + 2, rgb);
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
                            y + getFontRenderer().getHeight() + 2, rgb);
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

            if (word.timestamp > songProgress - line.timestamp) {
                info.currentIndex = k;
                break;
            } else if (k == line.words.size() - 1) {
                info.currentIndex = k;
            }
        }

        // calculate text before current word
        for (int m = 0; m < info.currentIndex; m++) {
            info.textBefore.append(line.words.get(m).word);
        }

        // calculate accumulated text
        for (int m = 0; m < info.currentIndex + 1; m++) {
            info.textAccumulated.append(line.words.get(m).word);
        }

        return info;
    }

    private void updateScrollWidth(LyricLine line, WordInfo wordInfo, float songProgress) {
        LyricLine.Word prev = getPrevWord(wordInfo.currentIndex, allLyrics.indexOf(line), line);
        LyricLine.Word current = line.words.get(wordInfo.currentIndex);

        long prevWordTimestamp = wordInfo.currentIndex == 0 ? 0 : prev.timestamp;
        double progress = (songProgress - line.timestamp - prevWordTimestamp) / (double) (current.timestamp - prevWordTimestamp);

        double offsetX = progress * getFontRenderer().getStringWidthD(current.word);

        line.scrollWidth = getFontRenderer().getStringWidthD(wordInfo.textBefore.toString()) + offsetX;
    }

    private void renderScrollEffect(LyricLine line, LyricRenderInfo renderInfo, WordInfo wordInfo, float songProgress) {
        ScrollEffects effectMode = this.scrollEffects.getValue();

        switch (effectMode) {
            case Scroll:
                renderScrollMode(line, renderInfo);
                break;
            case FadeIn:
                renderFadeInMode(line, renderInfo, wordInfo, songProgress);
                break;
            case SlideIn:
                renderSlideInMode(line, renderInfo, wordInfo, songProgress);
                break;
        }
    }

    private void renderScrollMode(LyricLine line, LyricRenderInfo renderInfo) {
        AlignMode alignMode = this.alignMode.getValue();
        double x = calculateAlignmentX(line.getLyric(), alignMode);

        StencilClipManager.beginClip(() -> {
            Rect.draw(x, renderInfo.yPosition, line.scrollWidth + 1, getFontRenderer().getHeight() + 4, -1);
        });

        renderAlignedText(line.getLyric(), renderInfo.yPosition, -1, alignMode);

        StencilClipManager.endClip();
    }

    private void renderFadeInMode(LyricLine line, LyricRenderInfo renderInfo, WordInfo wordInfo, float songProgress) {
        AlignMode alignMode = this.alignMode.getValue();

        double offsetX = calculateAlignmentX(line.getLyric(), alignMode);
        for (int m = 0; m < wordInfo.currentIndex + 1; m++) {
            LyricLine.Word word = line.words.get(m);
            String wordText = word.word;

            if (m == wordInfo.currentIndex) {
                updateCurrentWordAnimation(word, line, wordInfo.currentIndex, songProgress);
            } else if (m < wordInfo.currentIndex) {
                word.alpha = 1;
            }

            double stWidth = getFontRenderer().getStringWidthD(wordText);
            bigFrString(wordText, offsetX, renderInfo.yPosition,
                    RGBA.color(255, 255, 255, (int) (word.alpha * 255)));

            offsetX += stWidth;
        }
    }

    private void renderSlideInMode(LyricLine line, LyricRenderInfo renderInfo, WordInfo wordInfo, float songProgress) {
        AlignMode alignMode = this.alignMode.getValue();

        double targetX = calculateSlideInTargetX(line, alignMode);

        Runnable renderTask = () -> {
            double offsetX = targetX;
            double targetOffsetX = 0;

            for (int m = 0; m < wordInfo.currentIndex + 1; m++) {
                LyricLine.Word word = line.words.get(m);
                String wordText = word.word;
                double stWidth = getFontRenderer().getStringWidthD(wordText);

                if (m == wordInfo.currentIndex) {
                    updateCurrentWordAnimation(word, line, wordInfo.currentIndex, songProgress);

                    Easing easeInOutQuad = Easing.EASE_OUT_CUBIC;
                    targetOffsetX += stWidth * easeInOutQuad.getFunction().apply(word.progress);
                } else if (m < wordInfo.currentIndex) {
                    word.alpha = 1;
                    targetOffsetX += stWidth;
                }

                bigFrString(wordText, offsetX, renderInfo.yPosition,
                        RGBA.color(255, 255, 255, (int) (word.alpha * 255)));

                offsetX += stWidth;
            }

            line.targetOffsetX = targetOffsetX;
        };

        renderTask.run();
    }

    private void updateCurrentWordAnimation(LyricLine.Word word, LyricLine line,
                                            int currentIndex, float songProgress) {
        LyricLine.Word prev = getPrevWord(currentIndex, allLyrics.indexOf(line), line);
        long prevWordTimestamp = currentIndex == 0 ? 0 : prev.timestamp;

        double perc = (songProgress - line.timestamp - prevWordTimestamp) /
                (double) (word.timestamp - prevWordTimestamp);
        double clamped = Math.max(0, Math.min(1, perc));

        word.progress = Interpolations.interpBezier(word.progress, clamped, 1);
        word.alpha = (float) Math.min(1, clamped * 1.25f);
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

    private void cleanupRender() {
        GlStateManager.popMatrix();
        this.setWidth(this.width.getValue().floatValue());
        this.setHeight(this.height.getValue().floatValue());
    }

    private static class LyricRenderInfo {
        double yPosition;
        boolean shouldSkip = false;
        boolean shouldBreak = false;
    }

    private static class WordInfo {
        int currentIndex = 0;
        StringBuilder textBefore = new StringBuilder();
        StringBuilder textAccumulated = new StringBuilder();
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

    private static LyricLine.Word getPrevWord(int cur, int j, LyricLine line) {
        LyricLine.Word prev;
        if (cur - 1 < 0) {
            if (j - 1 < 0) {
                prev = line.words.getFirst();
            } else {
                prev = allLyrics.get(j - 1).words.getLast();
            }
        } else {
            prev = line.words.get(cur - 1);
        }
        return prev;
    }

}