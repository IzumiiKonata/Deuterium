package tritium.widget.impl;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import tech.konata.ncmplayer.music.CloudMusic;
import tech.konata.ncmplayer.music.dto.Music;
import tech.konata.ncmplayer.music.lyric.LyricLine;
import tech.konata.ncmplayer.music.lyric.LyricParser;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
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
 * 歌词显示Widget
 * @author IzumiiKonata
 * Date: 2025/2/14 20:34
 */
public class MusicLyricsWidget extends Widget {

    // 存储所有歌词行
    public static final List<LyricLine> allLyrics = new CopyOnWriteArrayList<>();
    static double scrollOffset = 0;
    public static LyricLine currentDisplaying = null;

    // 歌词类型flags
    public static boolean hasTransLyrics = false, hasRomanization = false;

    // 逐字歌词timing数据
    public static List<ScrollTiming> timings = new CopyOnWriteArrayList<>();

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

    /**
     * 逐行歌词的滚动时间信息
     */
    public static class ScrollTiming {
        public long start, duration;
        public String text;

        // 分词Timing
        public List<WordTiming> timings = new CopyOnWriteArrayList<>();
//        public List<Long> timingsDuration = new CopyOnWriteArrayList<>();
    }

    /**
     * 单个词的timing信息
     */
    public static class WordTiming {
        public String word;
        public long timing;

        // 渲染效果相关
        public float alpha = 0.0f;
        public double interpPercent = 0.0;
//        public double effectY = -1;

//        public WordTiming(String word, long timing) {
//            this.word = word;
//            this.timing = timing;
//        }

        public WordTiming() {}
    }

    /**
     * 初始化歌词数据
     * Initialize lyrics with parsed data and fetch TTML if available
     */
    public static void initLyric(JsonObject lyric, Music music) {
        // reset states
        hasTransLyrics = false;
        hasRomanization = false;
        timings.clear();

        List<LyricLine> parsed = LyricParser.parse(lyric);

        // 异步获取TTML歌词 (逐字歌词)
        fetchTTMLLyrics(music, parsed);

        synchronized (allLyrics) {
            allLyrics.clear();

            // 如果有逐字歌词，需要merge timing信息
            if (!timings.isEmpty()) {
                mergeLyricsWithTimings(parsed);
            }

            allLyrics.addAll(parsed);
        }

        scrollOffset = 0;
    }

    /**
     * 异步获取TTML歌词数据
     */
    private static void fetchTTMLLyrics(Music music, List<LyricLine> parsed) {
        MultiThreadingUtil.runAsync(() -> {
            try {
                String lrc = HttpUtils.getString(
                        "https://gitee.com/IzumiiKonata/amll-ttml-db/raw/main/ncm-lyrics/" + music.getId() + ".yrc",
                        null
                );
                System.out.println("歌曲 " + music.getName() + " 存在 ttml 歌词, 获取中...");

                timings.clear();
                LyricParser.parseYrc(lrc);

                // 构建新的歌词列表
                List<LyricLine> beans = buildLyricsFromTimings();

//                for (int i = 0; i < allLyrics.size(); i++) {
//                    allLyrics.get(i).timeStamp = timings.get(i).start;
//                }

                for (LyricLine bean : beans) {

                    for (LyricLine lyricLine : allLyrics) {
                        if (lyricLine.getLyric().toLowerCase().replace(" ", "").equals(bean.lyric.toLowerCase().replace(" ", ""))) {
                            bean.romanizationText = lyricLine.romanizationText;
                            bean.translationText = lyricLine.translationText;
                            break;
                        }
                    }

                }

                allLyrics.clear();
                allLyrics.addAll(beans);
            } catch (Exception ignored) {
                // 获取失败，使用普通歌词
            }
        });
    }

    /**
     * 从timing数据构建歌词列表
     */
    private static List<LyricLine> buildLyricsFromTimings() {
        List<LyricLine> beans = new ArrayList<>();
        for (ScrollTiming timing : timings) {
            StringBuilder sb = new StringBuilder();
            for (WordTiming wordTiming : timing.timings) {
                sb.append(wordTiming.word);
            }
            LyricLine lyricLine = new LyricLine(timing.start, "NONE", sb.toString());
            beans.add(lyricLine);
        }
        return beans;
    }

    /**
     * 合并歌词和timing信息
     */
    private static void mergeLyricsWithTimings(List<LyricLine> parsed) {
        for (int i = 0; i < timings.size() && i < parsed.size(); i++) {
            ScrollTiming timing = timings.get(i);
            StringBuilder sb = new StringBuilder();
            for (WordTiming wordTiming : timing.timings) {
                sb.append(wordTiming.word);
            }
            LyricLine lyricLine = parsed.get(i);
            lyricLine.lyric = sb.toString();
            lyricLine.timeStamp = timing.start;
        }
    }

    /**
     * 快速重置进度
     * Reset all lyrics state to specific progress
     */
    public static void quickResetProgress(float progress) {
        if (allLyrics.isEmpty()) return;

        try {
            // reset所有歌词行的渲染状态
            resetAllLyricsState();

            // reset逐字歌词状态
            resetWordTimingsState();

            scrollOffset = 0;

            // 找到当前应该显示的歌词
            findCurrentLyric(progress);

            // 计算滚动偏移
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

    private static void resetWordTimingsState() {
        for (ScrollTiming t : timings) {
            for (WordTiming timing : t.timings) {
                timing.alpha = 0.0f;
                timing.interpPercent = 0.0;
            }
        }
    }

    private static void findCurrentLyric(float progress) {
        currentDisplaying = allLyrics.get(0);

        for (LyricLine lyric : allLyrics) {
            if (lyric.getTimeStamp() > progress) {
                int i = allLyrics.indexOf(lyric);
                if (i > 0) {
                    currentDisplaying = allLyrics.get(i - 1);
                }
                break;
            }
        }
    }

    /**
     * 计算歌词行高度
     */
    public static double getLyricHeight() {
        double baseHeight = getFontRenderer().getHeight();
        double adjustment = hasSecondaryLyrics() ? 0 : -getSmallFontRenderer().getHeight() - 4;
        return baseHeight + adjustment + WidgetsManager.musicLyrics.lyricHeight.getValue();
    }

    /**
     * 是否有副歌词（翻译/罗马音）
     */
    public static boolean hasSecondaryLyrics() {
        return (hasTransLyrics || hasRomanization) && WidgetsManager.musicLyrics.showTranslation.getValue();
    }

    /**
     * 获取副歌词文本
     * Get secondary lyrics based on current settings
     */
    public static String getSecondaryLyrics(LyricLine bean) {
        // 优先显示翻译
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

        // 只有罗马音的情况
        if (hasRomanization) {
            if (WidgetsManager.musicLyrics.showRoman.getValue()) {
                return StringUtils.returnEmptyStringIfNull(bean.getRomanizationText());
            }
        }

        return "";
    }

    @Override
    public void onRender(boolean editing) {

        // 检查是否需要渲染
        if (!shouldRender()) {
            return;
        }

        float songProgress = CloudMusic.player.getCurrentTimeMillis();

        updateCurrentDisplayingLyric(songProgress);

        boolean shouldNotDisplayOtherLyrics = this.singleLine.getValue();

        handleSingleLineMode(shouldNotDisplayOtherLyrics);

        updateScrollOffset(shouldNotDisplayOtherLyrics);

        NORMAL.add(() -> {
            GlStateManager.pushMatrix();

            StencilClipManager.beginClip(() -> {
                Rect.draw(this.getX() - 2, this.getY(), this.getWidth() + 4, this.getHeight(), -1);
            });

            renderAllLyrics(shouldNotDisplayOtherLyrics, songProgress);

            cleanupRender();
            StencilClipManager.endClip();
        });
    }

    /**
     * 检查是否需要渲染
     */
    private boolean shouldRender() {
        return CloudMusic.player != null && !CloudMusic.player.isFinished() && !allLyrics.isEmpty();
    }

    /**
     * 更新当前显示的歌词行
     */
    private void updateCurrentDisplayingLyric(float songProgress) {
        for (int i = 0; i < allLyrics.size(); i++) {
            LyricLine lyric = allLyrics.get(i);

            if (lyric.getTimeStamp() > songProgress) {
                if (i > 0) {
                    currentDisplaying = allLyrics.get(i - 1);
                }
                break;
            } else if (i == allLyrics.size() - 1) {
                currentDisplaying = allLyrics.get(i);
            }
        }
    }

    /**
     * 处理单行显示模式
     */
    private void handleSingleLineMode(boolean shouldNotDisplayOtherLyrics) {
        if (shouldNotDisplayOtherLyrics && currentDisplaying == null) {
            if (!allLyrics.isEmpty()) {
                currentDisplaying = allLyrics.get(0);
            }
        }
    }

    /**
     * 更新滚动偏移量
     */
    private void updateScrollOffset(boolean shouldNotDisplayOtherLyrics) {
        int indexOf = allLyrics.indexOf(currentDisplaying);

        if (!shouldNotDisplayOtherLyrics) {
            if (currentDisplaying == null) {
                scrollOffset = 0;
            } else {
                // smooth scroll animation
                scrollOffset = Interpolations.interpBezier(scrollOffset, (indexOf * getLyricHeight()), 0.2f);
            }
        }
    }

    /**
     * 渲染所有歌词行
     */
    private void renderAllLyrics(boolean shouldNotDisplayOtherLyrics, float songProgress) {
        double offsetY = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 - scrollOffset;
        int indexOf = allLyrics.indexOf(currentDisplaying);

        synchronized (allLyrics) {
            for (int i = 0; i < allLyrics.size(); i++) {
                LyricLine lyric = allLyrics.get(i);

                // 单行模式只渲染当前行
                if (shouldNotDisplayOtherLyrics) {
                    if (i < indexOf) continue;
                    if (i > indexOf) break;
                }

                // 计算歌词位置
                LyricRenderInfo renderInfo = calculateLyricPosition(
                        lyric, i, indexOf, offsetY, shouldNotDisplayOtherLyrics
                );

                if (renderInfo.shouldSkip) {
                    offsetY += getLyricHeight();
                    continue;
                }

                if (renderInfo.shouldBreak) {
                    break;
                }

                // 更新歌词动画状态
                updateLyricAnimation(lyric, i == indexOf);

                // 渲染歌词
                renderLyricText(lyric, renderInfo, i, indexOf);

                // 处理滚动效果
                if (!timings.isEmpty() && lyric == currentDisplaying) {
                    handleScrollEffects(lyric, renderInfo, songProgress);
                }

                offsetY += getLyricHeight();
            }
        }
    }

    /**
     * 计算歌词渲染位置信息
     */
    private LyricRenderInfo calculateLyricPosition(LyricLine lyric, int index, int currentIndex,
                                                   double offsetY, boolean singleLineMode) {
        LyricRenderInfo info = new LyricRenderInfo();

        if (!singleLineMode) {
            // 计算目标位置
            double dest = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 +
                    index * getLyricHeight() - (currentIndex * getLyricHeight());

            // 初始化或大幅度位置变化时直接设置
            if (lyric.offsetY == Double.MIN_VALUE || Math.abs(lyric.offsetY - dest) > 100) {
                lyric.offsetY = dest;
            }

            // 检查是否在可视区域内
            if (lyric.offsetY + getLyricHeight() < this.getY()) {
                info.shouldSkip = true;
                lyric.offsetY = dest;
                return info;
            }

            if (offsetY > this.getY() + this.getHeight()) {
                info.shouldBreak = true;
                return info;
            }

            // 平滑滚动动画
            applyGraceScroll(lyric, index, currentIndex, dest);

            info.yPosition = this.graceScroll.getValue() ? lyric.offsetY : offsetY;
        } else {
            info.yPosition = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0;
            lyric.offsetY = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0;
        }

        return info;
    }

    /**
     * 应用平滑滚动效果
     * Grace scroll animation for lyrics
     */
    private void applyGraceScroll(LyricLine lyric, int index, int currentIndex, double dest) {
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
                lyric.offsetY = Interpolations.interpBezier(lyric.offsetY, dest, speed);
            }
        } else {
            // 第一行直接滚动
            lyric.offsetY = Interpolations.interpBezier(lyric.offsetY, dest, speed);
        }
    }

    /**
     * 更新歌词动画状态
     */
    private void updateLyricAnimation(LyricLine lyric, boolean isCurrent) {
        // 透明度动画
        lyric.alpha = Interpolations.interpBezier(
                lyric.alpha,
                isCurrent ? 1f : 60 / 255.0f,
                0.1f
        );

        // 缩放动画
        lyric.scale = Interpolations.interpBezier(
                lyric.scale,
                isCurrent ? 1.0 : 0.8,
                0.2f
        );
    }

    /**
     * 渲染歌词文本
     */
    private void renderLyricText(LyricLine lyric, LyricRenderInfo renderInfo,
                                 int index, int currentIndex) {
        boolean hasScrollTimings = !timings.isEmpty();
        boolean bSlideIn = this.scrollEffects.getValue() == ScrollEffects.SlideIn;
        boolean shouldRender = !hasScrollTimings || !bSlideIn || index != currentIndex ||
                this.alignMode.getValue() == AlignMode.Left;

        // 计算透明度
        int alpha = calculateAlpha(lyric, index, currentIndex, hasScrollTimings);

        // 获取副歌词
        String secondaryLyric = hasSecondaryLyrics() ? getSecondaryLyrics(lyric) : "";
        boolean secondaryLyricEmpty = secondaryLyric.isEmpty();

        // 创建渲染任务
        Runnable renderTask = createRenderTask(
                lyric, renderInfo, secondaryLyric, secondaryLyricEmpty,
                shouldRender, alpha, index <= currentIndex
        );

        // 执行渲染
        renderTask.run();
    }

    /**
     * 计算歌词透明度
     */
    private int calculateAlpha(LyricLine lyric, int index, int currentIndex, boolean hasScrollTimings) {
        if (hasScrollTimings) {
            return index != currentIndex ? (int) (lyric.alpha * 255) : 80;
        } else {
            return (int) (lyric.alpha * 255);
        }
    }

    /**
     * 创建渲染任务
     */
    private Runnable createRenderTask(LyricLine lyric, LyricRenderInfo renderInfo,
                                      String secondaryLyric, boolean secondaryLyricEmpty,
                                      boolean shouldRender, int alpha,
                                      boolean isActive) {
        return () -> {
            int hexColor = RenderSystem.hexColor(255, 255, 255, alpha);
            int rgb = RenderSystem.hexColor(255, 255, 255, isActive ? (int) (lyric.alpha * 255) : 100);

            // 根据对齐方式渲染
            renderByAlignment(lyric, renderInfo, secondaryLyric, secondaryLyricEmpty,
                    shouldRender, hexColor, rgb);

        };
    }

    /**
     * 根据对齐方式渲染歌词
     */
    private void renderByAlignment(LyricLine lyric, LyricRenderInfo renderInfo,
                                   String secondaryLyric, boolean secondaryLyricEmpty,
                                   boolean shouldRender, int hexColor, int rgb) {
        AlignMode alignMode = this.alignMode.getValue();
        double y = renderInfo.yPosition;

        switch (alignMode) {
            case Left:
                if (shouldRender) {
                    bigFrString(lyric.getLyric(), this.getX(), y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric, this.getX(),
                            y + getFontRenderer().getHeight() + 2, rgb);
                }
                break;
            case Center:
                double centerX = this.getX() + this.getWidth() / 2.0;
                if (shouldRender) {
                    bigFrStringCentered(lyric.getLyric(), centerX, y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrStringCentered(secondaryLyric, centerX,
                            y + getFontRenderer().getHeight() + 2, rgb);
                }
                break;
            case Right:
                if (shouldRender) {
                    bigFrString(lyric.getLyric(),
                            this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(lyric.getLyric()), y, hexColor);
                }
                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric,
                            this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidthD(secondaryLyric),
                            y + getFontRenderer().getHeight() + 2, rgb);
                }
                break;
        }
    }

    /**
     * 处理歌词滚动效果
     * Handle different scroll effects for karaoke-style lyrics
     */
    private void handleScrollEffects(LyricLine lyric, LyricRenderInfo renderInfo, float songProgress) {
        ScrollTiming curTiming = findCurrentTiming(songProgress);

        if (curTiming == null) return;

        // 计算当前词和进度
        WordTimingInfo wordInfo = calculateCurrentWordTiming(curTiming, songProgress);

        // 更新滚动宽度
        updateScrollWidth(lyric, curTiming, wordInfo, songProgress);

        // 渲染滚动效果
        renderScrollEffect(lyric, renderInfo, curTiming, wordInfo, songProgress);
    }

    /**
     * 查找当前timing
     */
    private ScrollTiming findCurrentTiming(float songProgress) {
        for (int j = 0; j < timings.size(); j++) {
            ScrollTiming timing = timings.get(j);

            if (j < timings.size() - 1 && songProgress < timings.get(j + 1).start || j == timings.size() - 1) {
                return timing;
            }
        }
        return null;
    }

    /**
     * 计算当前词的timing信息
     */
    private WordTimingInfo calculateCurrentWordTiming(ScrollTiming timing, float songProgress) {
        WordTimingInfo info = new WordTimingInfo();

        // find current word index
        for (int k = 0; k < timing.timings.size(); k++) {
            WordTiming wordTiming = timing.timings.get(k);

            if (wordTiming.timing > songProgress - timing.start) {
                info.currentIndex = k;
                break;
            } else if (k == timing.timings.size() - 1) {
                info.currentIndex = k;
            }
        }

        // calculate text before current word
        for (int m = 0; m < info.currentIndex; m++) {
            info.textBefore.append(timing.timings.get(m).word);
        }

        // calculate accumulated text
        for (int m = 0; m < info.currentIndex + 1; m++) {
            info.textAccumulated.append(timing.timings.get(m).word);
        }

        return info;
    }

    /**
     * 更新滚动宽度
     */
    private void updateScrollWidth(LyricLine lyric, ScrollTiming timing, WordTimingInfo wordInfo, float songProgress) {
        WordTiming prev = getPrevWordTiming(wordInfo.currentIndex, timings.indexOf(timing), timing);
        WordTiming current = timing.timings.get(wordInfo.currentIndex);

        long prevTiming = wordInfo.currentIndex == 0 ? 0 : prev.timing;
        double progress = (songProgress - timing.start - prevTiming) / (double) (current.timing - prevTiming);

        double offsetX = progress * getFontRenderer().getStringWidthD(current.word);

        lyric.scrollWidth = getFontRenderer().getStringWidthD(wordInfo.textBefore.toString()) + offsetX;
    }

    /**
     * 渲染滚动效果
     */
    private void renderScrollEffect(LyricLine lyric, LyricRenderInfo renderInfo, ScrollTiming timing, WordTimingInfo wordInfo, float songProgress) {
        ScrollEffects effectMode = this.scrollEffects.getValue();

        switch (effectMode) {
            case Scroll:
                renderScrollMode(lyric, renderInfo);
                break;
            case FadeIn:
                renderFadeInMode(lyric, renderInfo, timing, wordInfo, songProgress);
                break;
            case SlideIn:
                renderSlideInMode(lyric, renderInfo, timing, wordInfo, songProgress);
                break;
        }
    }

    /**
     * 渲染Scroll模式
     */
    private void renderScrollMode(LyricLine lyric, LyricRenderInfo renderInfo) {
        AlignMode alignMode = this.alignMode.getValue();
        double x = calculateAlignmentX(lyric.getLyric(), alignMode);

        StencilClipManager.beginClip(() -> {
            Rect.draw(x, renderInfo.yPosition, lyric.scrollWidth + 1, getFontRenderer().getHeight() + 4, -1);
        });

        renderAlignedText(lyric.getLyric(), renderInfo.yPosition, -1, alignMode);

        StencilClipManager.endClip();
    }

    /**
     * 渲染FadeIn模式
     */
    private void renderFadeInMode(LyricLine lyric, LyricRenderInfo renderInfo, ScrollTiming timing, WordTimingInfo wordInfo, float songProgress) {
        AlignMode alignMode = this.alignMode.getValue();

        double offsetX = calculateAlignmentX(lyric.getLyric(), alignMode);
        for (int m = 0; m < wordInfo.currentIndex + 1; m++) {
            WordTiming wordTiming = timing.timings.get(m);
            String word = wordTiming.word;

            // 更新word的动画状态
            if (m == wordInfo.currentIndex) {
                updateCurrentWordAnimation(wordTiming, timing, wordInfo.currentIndex, songProgress);
            } else if (m < wordInfo.currentIndex) {
                wordTiming.alpha = 1;
            }

            double stWidth = getFontRenderer().getStringWidthD(word);
            bigFrString(word, offsetX, renderInfo.yPosition,
                    RenderSystem.hexColor(255, 255, 255, (int) (wordTiming.alpha * 255)));

            offsetX += stWidth;
        }
    }

    /**
     * 渲染SlideIn模式
     */
    private void renderSlideInMode(LyricLine lyric, LyricRenderInfo renderInfo, ScrollTiming timing, WordTimingInfo wordInfo, float songProgress) {
        AlignMode alignMode = this.alignMode.getValue();

        // 计算目标X坐标
        double targetX = calculateSlideInTargetX(lyric, alignMode);

        Runnable renderTask = () -> {
            double offsetX = targetX;
            double targetOffsetX = 0;

            for (int m = 0; m < wordInfo.currentIndex + 1; m++) {
                WordTiming wordTiming = timing.timings.get(m);
                String word = wordTiming.word;
                double stWidth = getFontRenderer().getStringWidthD(word);

                if (m == wordInfo.currentIndex) {
                    // 更新当前词动画
                    updateCurrentWordAnimation(wordTiming, timing, wordInfo.currentIndex, songProgress);

                    Easing easeInOutQuad = Easing.EASE_IN_OUT_CUBIC;
                    targetOffsetX += stWidth * easeInOutQuad.getFunction().apply(wordTiming.interpPercent);
                } else if (m < wordInfo.currentIndex) {
                    wordTiming.alpha = 1;
                    targetOffsetX += stWidth;
                }

                bigFrString(word, offsetX, renderInfo.yPosition,
                        RenderSystem.hexColor(255, 255, 255, (int) (wordTiming.alpha * 255)));

                offsetX += stWidth;
            }

            lyric.targetOffsetX = targetOffsetX;
        };

        renderTask.run();
    }

    /**
     * 更新当前词的动画状态
     */
    private void updateCurrentWordAnimation(WordTiming wordTiming, ScrollTiming timing,
                                            int currentIndex, float songProgress) {
        WordTiming prev = getPrevWordTiming(currentIndex, timings.indexOf(timing), timing);
        long prevTiming = currentIndex == 0 ? 0 : prev.timing;

        double perc = (songProgress - timing.start - prevTiming) /
                (double) (wordTiming.timing - prevTiming);
        double clamped = Math.max(0, Math.min(1, perc));

        wordTiming.interpPercent = Interpolations.interpBezier(wordTiming.interpPercent, clamped, 1);
        wordTiming.alpha = (float) wordTiming.interpPercent;
    }

    /**
     * 计算对齐方式的X坐标
     */
    private double calculateAlignmentX(String text, AlignMode alignMode) {
        if (alignMode == AlignMode.Left) {
            return this.getX();
        } else if (alignMode == AlignMode.Center) {
            return this.getX() + this.getWidth() / 2.0f - getFontRenderer().getStringWidthD(text) / 2.0f;
        } else {
            return this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(text);
        }
    }

    /**
     * 计算SlideIn模式的目标X坐标
     */
    private double calculateSlideInTargetX(LyricLine lyric, AlignMode alignMode) {
        if (alignMode == AlignMode.Left) {
            return this.getX();
        } else if (alignMode == AlignMode.Center) {
            return this.getX() + this.getWidth() / 2.0 - lyric.targetOffsetX / 2.0;
        } else {
            return this.getX() + this.getWidth() - lyric.targetOffsetX;
        }
    }

    /**
     * 根据对齐方式渲染文本
     */
    private void renderAlignedText(String text, double y, int color, AlignMode alignMode) {
        if (alignMode == AlignMode.Left) {
            bigFrString(text, this.getX(), y, color);
        } else if (alignMode == AlignMode.Center) {
            bigFrStringCentered(text, this.getX() + this.getWidth() / 2.0, y, color);
        } else {
            bigFrString(text, this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(text), y, color);
        }
    }

    /**
     * 清理渲染状态
     */
    private void cleanupRender() {
        GlStateManager.popMatrix();
        this.setWidth(this.width.getValue().floatValue());
        this.setHeight(this.height.getValue().floatValue());
    }

    /**
     * 渲染辅助类 - 歌词渲染信息
     */
    private static class LyricRenderInfo {
        double yPosition;
        boolean shouldSkip = false;
        boolean shouldBreak = false;
    }

    /**
     * 渲染辅助类 - 词timing信息
     */
    private static class WordTimingInfo {
        int currentIndex = 0;
        StringBuilder textBefore = new StringBuilder();
        StringBuilder textAccumulated = new StringBuilder();
    }

    // Font相关方法保持不变
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

    /**
     * 获取前一个WordTiming
     * Get previous word timing with edge case handling
     */
    private static WordTiming getPrevWordTiming(int cur, int j, ScrollTiming timing) {
        WordTiming prev;
        if (cur - 1 < 0) {
            if (j - 1 < 0) {
                prev = timing.timings.get(0);
            } else {
                prev = timings.get(j - 1).timings.get(timings.get(j - 1).timings.size() - 1);
            }
        } else {
            prev = timing.timings.get(cur - 1);
        }
        return prev;
    }

}