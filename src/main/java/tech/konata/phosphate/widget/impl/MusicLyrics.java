package tech.konata.phosphate.widget.impl;

import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.StencilShader;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.utils.logging.Logger;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.utils.music.lyric.LyricLine;
import tech.konata.phosphate.utils.music.lyric.LyricParser;
import tech.konata.phosphate.utils.network.HttpUtils;
import tech.konata.phosphate.utils.other.StringUtils;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.widget.Widget;

import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 10:04 AM
 */
public class MusicLyrics extends Widget {

    // 存储所有歌词行
    public static final List<LyricLine> allLyrics = new CopyOnWriteArrayList<>();
    static double scrollOffset = 0;
    public static LyricLine currentDisplaying = null;

    public NumberSetting<Double> lyricHeight = new NumberSetting<>("Lyric Height", 20.0, 14.0, 50.0, 0.5);

    // 歌词类型flags
    public static boolean hasTransLyrics = false, hasRomanization = false;

    // 逐字歌词timing数据
    public static List<ScrollTiming> timings = new CopyOnWriteArrayList<>();

    public ModeSetting<ScrollEffects> scrollEffects = new ModeSetting<>("Scroll Effects", ScrollEffects.Scroll);

    public enum ScrollEffects {
        Scroll,
        FadeInOut,
        SlideIn,
        SlideUp,
        SlideDown,
        SlideMix
    }

    public ModeSetting<AlignMode> alignMode = new ModeSetting<>("Align Mode", AlignMode.Center);

    public enum AlignMode {
        Left,
        Center,
        Right
    }

    public BooleanSetting shadow = new BooleanSetting("Text Shadow", false);
    public BooleanSetting singleLine = new BooleanSetting("Single Line", false);
    public BooleanSetting showTranslation = new BooleanSetting("Show Translation", true);
    public BooleanSetting graceScroll = new BooleanSetting("Elegant Scrolling", true, () -> !singleLine.getValue());
    public BooleanSetting rectBlur = new BooleanSetting("Lyrics Blur Rect", false);
    public BooleanSetting rectShadow = new BooleanSetting("Lyrics Rect Shadow", false, () -> rectBlur.getValue());
    public BooleanSetting lyricsShadow = new BooleanSetting("Lyrics Shadow", false, () -> !rectBlur.getValue());
    public BooleanSetting showRoman = new BooleanSetting("Show Romanization in Japanese songs", false);

    /**
     * 逐行歌词的滚动时间信息
     */
    public static class ScrollTiming {
        public long start, duration;
        public String text;

        // 分词Timing
        public List<WordTiming> timings = new CopyOnWriteArrayList<>();
        public List<Long> timingsDuration = new CopyOnWriteArrayList<>();
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
        public double effectY = -1;

        public WordTiming(String word, long timing) {
            this.word = word;
            this.timing = timing;
        }

        public WordTiming() {
        }
    }

    public MusicLyrics() {
        super("Music Lyrics");
        super.setResizable(true, 450, 120);
    }

    final static Logger logger = new Logger("MusicLyrics");

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
    public void quickResetProgress(long progress) {
        if (allLyrics.isEmpty())
            return;

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

    private static void findCurrentLyric(long progress) {
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

    private static CFontRenderer getFontRenderer() {
        return FontManager.pf28bold;
    }

    private CFontRenderer getSmallFontRenderer() {
        return FontManager.pf18bold;
    }

    Framebuffer backgroundBuffer;
    Framebuffer blendBuffer;
    Framebuffer blendBuffer2;
    Framebuffer coverBuffer;

    public static void drawQuads2(double x, double y, double width, double height, int color) {
        GlStateManager.disableTexture2D();

        RenderSystem.color(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
    }

    public static void drawHorizontalGradientRect(double x, double y, double width, double height, int yColor, int y1Color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(7);
        RenderSystem.color(yColor);
        GL11.glVertex2d(x, y + height);
        RenderSystem.color(y1Color);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);
        RenderSystem.color(yColor);
        GL11.glVertex2d(x, y);
        GL11.glEnd();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    double yStart = 20;

    private void debugMsg(String text, String desc) {
        double posX = 160;

        FontManager.pf25.drawString(text, posX, yStart, -1);
        FontManager.pf25.drawString(desc, posX - FontManager.pf25.getStringWidth(desc), yStart, -1);

        yStart += 20;
    }

    @Override
    public void onRender(boolean editing) {
        if (CloudMusic.player == null || CloudMusic.player.isFinished() || allLyrics.isEmpty()) {
            return;
        }

        float songProgress = CloudMusic.player.getCurrentTimeMillis();

        // 更新当前显示的歌词
        updateCurrentDisplayingLyric(songProgress);

        boolean shouldNotDisplayOtherLyrics = this.singleLine.getValue();

        // 处理单行显示模式
        handleSingleLineMode(shouldNotDisplayOtherLyrics);

        // 更新滚动偏移
        updateScrollOffset(shouldNotDisplayOtherLyrics);

        // 开始渲染
        setupRenderEnvironment();

        double offsetY = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 - scrollOffset;
        int indexOf = allLyrics.indexOf(currentDisplaying);

        // 渲染所有歌词行
        renderAllLyrics(offsetY, indexOf, songProgress, shouldNotDisplayOtherLyrics);

        // 清理渲染状态
        cleanupRender();
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
            } else {
                return;
            }
        }
    }

    /**
     * 更新滚动偏移量
     */
    private void updateScrollOffset(boolean shouldNotDisplayOtherLyrics) {
        int indexOf = allLyrics.indexOf(currentDisplaying);

        if (!shouldNotDisplayOtherLyrics) {
            if (currentDisplaying == null)
                scrollOffset = 0;
            else
                scrollOffset = Interpolations.interpBezier(scrollOffset, (indexOf * getLyricHeight()), 0.2f);
        }
    }

    /**
     * 设置渲染环境
     */
    private void setupRenderEnvironment() {
        NORMAL.add(() -> {
            GlStateManager.pushMatrix();

            GlStateManager.translate(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, 1);
            GlStateManager.scale(this.scaleFactor, this.scaleFactor, 1);
            GlStateManager.translate(-(this.getX() + this.getWidth() * 0.5), -(this.getY() + this.getHeight() * 0.5), 1);

            blendBuffer = RenderSystem.createFrameBuffer(blendBuffer);
            blendBuffer2 = RenderSystem.createFrameBuffer(blendBuffer2);
            backgroundBuffer = RenderSystem.createFrameBuffer(backgroundBuffer);
            if (coverBuffer == null) {
                coverBuffer = new Framebuffer(200, getFontRenderer().getHeight() + 2, false);
            }

            mc.getFramebuffer().bindFramebuffer(true);

            RenderSystem.doScissor((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
        });

        NORMAL.add(() -> mc.getFramebuffer().bindFramebuffer(true));
    }

    /**
     * 渲染所有歌词行
     */
    private void renderAllLyrics(double offsetY, int indexOf, float songProgress, boolean shouldNotDisplayOtherLyrics) {
        // 这写的真是纯纯的一坨 我写完我自己都看不懂
        synchronized (allLyrics) {
            for (int i = 0; i < allLyrics.size(); i++) {
                LyricLine lyric = allLyrics.get(i);

                // 单行模式只渲染当前行
                if (shouldNotDisplayOtherLyrics) {
                    if (i < indexOf) {
                        continue;
                    }

                    if (i > indexOf) {
                        break;
                    }
                } else {
                    // perform scroll effects
                    double dest = this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 + i * getLyricHeight() - (indexOf * getLyricHeight());

                    if (lyric.offsetY == Double.MIN_VALUE || Math.abs(lyric.offsetY - dest) > 100) {
                        lyric.offsetY = dest;
                    }

                    float speed = 0.15f;

                    LyricLine prevLrc = null;

                    try {
                        if (i > 0) {
                            prevLrc = allLyrics.get(i - 1);
                        }
                    } catch (Exception ignored) {
                    }

                    if (lyric.offsetY + getLyricHeight() < this.getY()) {
                        offsetY += getLyricHeight();
                        lyric.offsetY = dest;
                        continue;
                    }

                    if (offsetY > this.getY() + this.getHeight())
                        break;

                    if (prevLrc != null) {
                        double v = prevLrc.offsetY - (this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0 + (i - 1) * getLyricHeight() - (indexOf * getLyricHeight()));

                        if (v < this.getLyricHeight() * 0.45f) {
                            lyric.offsetY = Interpolations.interpBezier(lyric.offsetY, dest, speed);
                        }
                    }

                    if (prevLrc == null) {
                        lyric.offsetY = Interpolations.interpBezier(lyric.offsetY, dest, speed);
                    }
                }

                // 获取副歌词
                String secondaryLyric = hasSecondaryLyrics() ? getSecondaryLyrics(lyric) : "";
                boolean secondaryLyricEmpty = secondaryLyric.isEmpty();

                double f = shouldNotDisplayOtherLyrics ? (this.getY() + this.getHeight() / 2.0 - getFontRenderer().getHeight() / 2.0) : (graceScroll.getValue() ? lyric.offsetY : offsetY);

                boolean hasScrollTimings = !timings.isEmpty();

                boolean bSlideIn = this.scrollEffects.getValue() == ScrollEffects.SlideIn;
                boolean bSlide = this.scrollEffects.getValue() == ScrollEffects.SlideUp || this.scrollEffects.getValue() == ScrollEffects.SlideMix || this.scrollEffects.getValue() == ScrollEffects.SlideDown;

                boolean shouldRender = !hasScrollTimings || ((!bSlideIn && !bSlide) || i != indexOf || (bSlideIn && this.alignMode.getValue() == AlignMode.Left));

                // 处理模糊效果
                handleBlurEffects(lyric, secondaryLyric, secondaryLyricEmpty, f, i, indexOf, shouldRender);

                // 处理阴影效果
                handleShadowEffects(lyric, secondaryLyric, secondaryLyricEmpty, f, i, indexOf, shouldRender);

                // 更新歌词动画状态
                updateLyricAnimation(lyric, i == indexOf);

                // 渲染歌词文本
                renderLyricText(lyric, secondaryLyric, secondaryLyricEmpty, f, i, indexOf, shouldRender);

                // 处理滚动效果
                if (lyric == currentDisplaying) {
                    handleScrollEffects(lyric, songProgress, f, hasScrollTimings);
                }

                offsetY += getLyricHeight();
            }
        }
    }

    /**
     * 处理模糊效果
     */
    private void handleBlurEffects(LyricLine lyric, String secondaryLyric, boolean secondaryLyricEmpty, double f, int i, int indexOf, boolean shouldRender) {
        if (rectBlur.getValue()) {
            BLUR.add(() -> {
                GlStateManager.pushMatrix();

                GlStateManager.translate(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, 1);
                GlStateManager.scale(this.scaleFactor, this.scaleFactor, 1);
                GlStateManager.translate(-(this.getX() + this.getWidth() * 0.5), -(this.getY() + this.getHeight() * 0.5), 1);

                RenderSystem.doScissor((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

                if (this.alignMode.getValue() == AlignMode.Left) {
                    drawQuads2(this.getX() - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, Color.WHITE.getRGB());

                    if (!secondaryLyricEmpty) {
                        drawQuads2(this.getX() - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, Color.WHITE.getRGB());
                    }
                } else if (this.alignMode.getValue() == AlignMode.Center) {
                    drawQuads2(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2f - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, Color.WHITE.getRGB());

                    if (!secondaryLyricEmpty) {
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - getSmallFontRenderer().getStringWidth(secondaryLyric) / 2f - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, Color.WHITE.getRGB());
                    }
                } else if (this.alignMode.getValue() == AlignMode.Right) {
                    drawQuads2(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()) - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, Color.WHITE.getRGB());

                    if (!secondaryLyricEmpty) {
                        drawQuads2(this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidth(secondaryLyric) - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, Color.WHITE.getRGB());
                    }
                }

                RenderSystem.endScissor();
                GlStateManager.popMatrix();
            });

            NORMAL.add(() -> {
                Stencil.write();
                Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), -1, Rect.RectType.EXPAND);
                Stencil.erase();

                int bloomColor = hexColor(0, 0, 0, 20);

                if (this.alignMode.getValue() == AlignMode.Left) {
                    drawQuads2(this.getX() - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, bloomColor);

                    if (!secondaryLyricEmpty) {
                        drawQuads2(this.getX() - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, bloomColor);
                    }
                } else if (this.alignMode.getValue() == AlignMode.Center) {
                    drawQuads2(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2f - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, bloomColor);

                    if (!secondaryLyricEmpty) {
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - getSmallFontRenderer().getStringWidth(secondaryLyric) / 2f - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, bloomColor);
                    }
                } else if (this.alignMode.getValue() == AlignMode.Right) {
                    drawQuads2(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()) - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, bloomColor);

                    if (!secondaryLyricEmpty) {
                        drawQuads2(this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidth(secondaryLyric) - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, bloomColor);
                    }
                }

                Stencil.dispose();
            });

            if (rectShadow.getValue()) {
                BLOOM.add(() -> {
                    GlStateManager.pushMatrix();

                    GlStateManager.translate(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, 1);
                    GlStateManager.scale(this.scaleFactor, this.scaleFactor, 1);
                    GlStateManager.translate(-(this.getX() + this.getWidth() * 0.5), -(this.getY() + this.getHeight() * 0.5), 1);

                    RenderSystem.doScissor((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

                    int bloomColor = hexColor(0, 0, 0, 80);

                    if (this.alignMode.getValue() == AlignMode.Left) {
                        drawQuads2(this.getX() - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, bloomColor);

                        if (!secondaryLyricEmpty) {
                            drawQuads2(this.getX() - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, bloomColor);
                        }
                    } else if (this.alignMode.getValue() == AlignMode.Center) {
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2f - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, bloomColor);

                        if (!secondaryLyricEmpty) {
                            drawQuads2(this.getX() + this.getWidth() / 2.0 - getSmallFontRenderer().getStringWidth(secondaryLyric) / 2f - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, bloomColor);
                        }
                    } else if (this.alignMode.getValue() == AlignMode.Right) {
                        drawQuads2(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()) - 2, f - 2, getFontRenderer().getStringWidth(lyric.getLyric()) + 4, getFontRenderer().getHeight() + 4, bloomColor);

                        if (!secondaryLyricEmpty) {
                            drawQuads2(this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidth(secondaryLyric) - 2, f + getFontRenderer().getHeight() + 2, getSmallFontRenderer().getStringWidth(secondaryLyric) + 4, getSmallFontRenderer().getHeight() + 6, bloomColor);
                        }
                    }

                    RenderSystem.endScissor();
                    GlStateManager.popMatrix();
                });
            }
        }
    }

    /**
     * 处理阴影效果
     */
    private void handleShadowEffects(LyricLine lyric, String secondaryLyric, boolean secondaryLyricEmpty, double f, int i, int indexOf, boolean shouldRender) {
        if (!rectBlur.getValue() && lyricsShadow.getValue()) {
            int finalI = i;
            BLOOM.add(() -> {
                GlStateManager.pushMatrix();

                GlStateManager.translate(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, 1);
                GlStateManager.scale(this.scaleFactor, this.scaleFactor, 1);
                GlStateManager.translate(-(this.getX() + this.getWidth() * 0.5), -(this.getY() + this.getHeight() * 0.5), 1);

                RenderSystem.doScissor((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
                int hexColor = hexColor(0, 0, 0, (int) (lyric.alpha * 160));

                if (this.alignMode.getValue() == AlignMode.Left) {
                    if (shouldRender)
                        bigFrString(lyric.getLyric(), this.getX(), f, hexColor);
                    if (!secondaryLyricEmpty) {
                        smallFrString(secondaryLyric, this.getX(), f + getFontRenderer().getHeight() + getSmallFontRenderer().getHeight() * 0.5, new Color(0, 0, 0, finalI <= indexOf ? (int) (lyric.alpha * 0.6 * 255) : 100).getRGB());
                    }
                } else if (this.alignMode.getValue() == AlignMode.Center) {
                    if (shouldRender)
                        bigFrStringCentered(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, f, hexColor);
                    if (!secondaryLyricEmpty) {
                        smallFrStringCentered(secondaryLyric, this.getX() + this.getWidth() / 2.0, f + getFontRenderer().getHeight() + getSmallFontRenderer().getHeight() * 0.5, new Color(0, 0, 0, finalI <= indexOf ? (int) (lyric.alpha * 0.6 * 255) : 100).getRGB());
                    }
                } else if (this.alignMode.getValue() == AlignMode.Right) {
                    if (shouldRender)
                        bigFrString(lyric.getLyric(), this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()), f, hexColor);
                    if (!secondaryLyricEmpty) {
                        smallFrString(secondaryLyric, this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidth(secondaryLyric), f + getFontRenderer().getHeight() + getSmallFontRenderer().getHeight() * 0.5, new Color(0, 0, 0, finalI <= indexOf ? (int) (lyric.alpha * 0.6 * 255) : 100).getRGB());
                    }
                }

                RenderSystem.endScissor();
                GlStateManager.popMatrix();
            });
        }
    }

    /**
     * 更新歌词动画状态
     */
    private void updateLyricAnimation(LyricLine lyric, boolean isCurrent) {
        boolean flag = GlobalSettings.RENDER2D_FRAMERATE.getValue() == 0;

        // 透明度动画
        lyric.alpha = Interpolations.interpBezier(
                lyric.alpha,
                isCurrent ? 1f : (flag ? 70 : 60) * RenderSystem.DIVIDE_BY_255,
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
    private void renderLyricText(LyricLine lyric, String secondaryLyric, boolean secondaryLyricEmpty, double f, int i, int indexOf, boolean shouldRender) {
        int finalI = i;
        NORMAL.add(() -> {
            int alpha;

            if (!timings.isEmpty()) {
                if (finalI != indexOf) {
                    alpha = (int) (lyric.alpha * 255);
                } else {
                    boolean flag = GlobalSettings.RENDER2D_FRAMERATE.getValue() == 0;
                    alpha = flag ? 70 + (int) (40 * lyric.alpha) : 80;
                }
            } else {
                alpha = (int) (lyric.alpha * 255);
            }

            int hexColor = hexColor(255, 255, 255, alpha);

            if (this.alignMode.getValue() == AlignMode.Left) {
                if (shouldRender)
                    bigFrString(lyric.getLyric(), this.getX(), f, hexColor);

                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric, this.getX(), f + getFontRenderer().getHeight() + getSmallFontRenderer().getHeight() * 0.5, new Color(255, 255, 255, finalI <= indexOf ? ((int) (lyric.alpha * 255)) : 100).getRGB());
                }
            } else if (this.alignMode.getValue() == AlignMode.Center) {
                if (shouldRender)
                    bigFrStringCentered(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, f, hexColor);

                if (!secondaryLyricEmpty) {
                    smallFrStringCentered(secondaryLyric, this.getX() + this.getWidth() / 2.0, f + getFontRenderer().getHeight() + getSmallFontRenderer().getHeight() * 0.5, new Color(255, 255, 255, finalI <= indexOf ? ((int) (lyric.alpha * 255)) : 100).getRGB());
                }
            } else if (this.alignMode.getValue() == AlignMode.Right) {
                if (shouldRender)
                    bigFrString(lyric.getLyric(), this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()), f, hexColor);

                if (!secondaryLyricEmpty) {
                    smallFrString(secondaryLyric, this.getX() + this.getWidth() - getSmallFontRenderer().getStringWidth(secondaryLyric), f + getFontRenderer().getHeight() + getSmallFontRenderer().getHeight() * 0.5, new Color(255, 255, 255, finalI <= indexOf ? ((int) (lyric.alpha * 255)) : 100).getRGB());
                }
            }
        });
    }

    /**
     * 处理歌词滚动效果
     */
    private void handleScrollEffects(LyricLine lyric, float songProgress, double f, boolean hasScrollTimings) {
        ScrollTiming curTiming = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder sbAccu = new StringBuilder();

        // lyric scroll timing scheme
        // bro this thing is totally fucked up
        if (hasScrollTimings && lyric == currentDisplaying) {
            yStart = 20;
            for (int j = 0; j < timings.size(); j++) {
                ScrollTiming timing = timings.get(j);

                if (j < timings.size() - 1 && songProgress < timings.get(j + 1).start || j == timings.size() - 1) {
                    curTiming = timing;

                    int cur = 0;

                    for (int k = 0; k < timing.timings.size(); k++) {
                        WordTiming wordTiming = timing.timings.get(k);

                        if (wordTiming.timing > songProgress - timing.start) {
                            cur = k;
                            break;
                        } else if (k == timing.timings.size() - 1) {
                            cur = k;
                        }
                    }

                    WordTiming prev = getPrevWordTiming(cur, j, timing);

                    for (int m = 0; m < cur; m++) {
                        sb.append(timing.timings.get(m).word);
                    }

                    for (int m = 0; m < cur + 1; m++) {
                        sbAccu.append(timing.timings.get(m).word);
                    }

                    double offsetX = (songProgress - timing.start - (cur == 0 ? 0 : prev.timing)) / (double) (timing.timings.get(cur).timing - (cur == 0 ? 0 : prev.timing)) *
                            getFontRenderer().getStringWidth(timing.timings.get(cur).word);

                    double length = (sb.toString().length() + 0.0001) / (double) lyric.getLyric().length();

                    // Apply the scroll width based on the mode
                    lyric.scrollWidth = getFontRenderer().getStringWidth(sb.toString()) + offsetX;

                    if (GlobalSettings.DEBUG_MODE.getValue()) {
                        debugMsg(lyric.getLyric(), "lyric.getLyric(): ");
                        debugMsg(String.valueOf(sb.toString().length()), "sb.toString().length(): ");
                        debugMsg(sb.toString(), "Scroll Lrc: ");
                        debugMsg(String.valueOf((sb.toString().length() + 0.0001) / (double) lyric.getLyric().length()), "Percent: ");
                        debugMsg(timing.timings.get(cur).word, "Current Word: ");
                        debugMsg(String.valueOf(songProgress), "Song Progress: ");
                        if (cur + 1 < timing.timings.size()) {
                            WordTiming curr = timing.timings.get(cur + 1);
                            debugMsg(curr.word, "Curr: ");
                            debugMsg(prev.word, "Prev: ");
                            debugMsg(String.valueOf(songProgress - timing.start - prev.timing), "CurPosition: ");
                            debugMsg(String.valueOf(curr.timing - prev.timing), "CurWordLength: ");
                            debugMsg(String.valueOf(curr.timing), "CurWordTiming: ");
                            debugMsg(String.valueOf(prev.timing), "PrevWordTiming: ");
                        }
                    }

                    break;
                }
            }
        }

        ScrollTiming finalCurTiming = curTiming;
        NORMAL.add(() -> {
            if (hasScrollTimings) {
                if (this.scrollEffects.getValue() == ScrollEffects.Scroll) {
                    blendBuffer.bindFramebuffer(true);
                    blendBuffer.framebufferClearNoBinding();

                    double fadeWidth = 5;

                    if (this.alignMode.getValue() == AlignMode.Left) {
                        drawQuads2(this.getX(), f, lyric.scrollWidth, getFontRenderer().getHeight() + 4, RenderSystem.hexColor(255, 255, 255, 255));
                        drawHorizontalGradientRect(this.getX() + lyric.scrollWidth, f, fadeWidth, getFontRenderer().getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255), 0);
                    } else if (this.alignMode.getValue() == AlignMode.Center) {
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2.0, f, lyric.scrollWidth, getFontRenderer().getHeight() + 4, RenderSystem.hexColor(255, 255, 255, 255));
                        drawHorizontalGradientRect(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2.0 + lyric.scrollWidth, f, fadeWidth, getFontRenderer().getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255), 0);
                    } else if (this.alignMode.getValue() == AlignMode.Right) {
                        drawQuads2(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()), f, lyric.scrollWidth, getFontRenderer().getHeight() + 4, RenderSystem.hexColor(255, 255, 255, 255));
                        drawHorizontalGradientRect(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()) + lyric.scrollWidth, f, fadeWidth, getFontRenderer().getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255), 0);
                    }

//                    Stencil.write(blendBuffer);
//                    if (this.alignMode.getValue() == AlignMode.Left) {
//                        drawQuads2(this.getX(), f, lyric.scrollWidth, getFontRenderer().getHeight() + 4, RenderSystem.hexColor(255, 255, 255, 255));
//                        drawHorizontalGradientRect(this.getX() + lyric.scrollWidth, f, fadeWidth, getFontRenderer().getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255), 0);
//                    } else if (this.alignMode.getValue() == AlignMode.Center) {
//                        drawQuads2(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2.0, f, lyric.scrollWidth, getFontRenderer().getHeight() + 4, RenderSystem.hexColor(255, 255, 255, 255));
//                        drawHorizontalGradientRect(this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidth(lyric.getLyric()) / 2.0 + lyric.scrollWidth, f, fadeWidth, getFontRenderer().getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255), 0);
//                    } else if (this.alignMode.getValue() == AlignMode.Right) {
//                        drawQuads2(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()), f, lyric.scrollWidth, getFontRenderer().getHeight() + 4, RenderSystem.hexColor(255, 255, 255, 255));
//                        drawHorizontalGradientRect(this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()) + lyric.scrollWidth, f, fadeWidth, getFontRenderer().getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255), 0);
//                    }
//                    Stencil.erase();
//
//                    if (this.alignMode.getValue() == AlignMode.Left) {
//                        bigFrString(lyric.getLyric(), this.getX(), f, new Color(255, 255, 255, 255).getRGB());
//                    } else if (this.alignMode.getValue() == AlignMode.Center) {
//                        bigFrStringCentered(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, f, new Color(255, 255, 255, 255).getRGB());
//                    } else if (this.alignMode.getValue() == AlignMode.Right) {
//                        bigFrString(lyric.getLyric(), this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()), f, new Color(255, 255, 255, 255).getRGB());
//                    }
//                    Stencil.dispose();

                    backgroundBuffer.bindFramebuffer(true);
                    backgroundBuffer.framebufferClearNoBinding();

                    dontRenderShadow = true;

                    if (this.alignMode.getValue() == AlignMode.Left) {
                        bigFrString(lyric.getLyric(), this.getX(), f, new Color(255, 255, 255, 255).getRGB());
                    } else if (this.alignMode.getValue() == AlignMode.Center) {
                        bigFrStringCentered(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, f, new Color(255, 255, 255, 255).getRGB());
                    } else if (this.alignMode.getValue() == AlignMode.Right) {
                        bigFrString(lyric.getLyric(), this.getX() + this.getWidth() - getFontRenderer().getStringWidth(lyric.getLyric()), f, new Color(255, 255, 255, 255).getRGB());
                    }

                    dontRenderShadow = false;

                    mc.getFramebuffer().bindFramebuffer(true);

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, 1);
                    GlStateManager.scale(1 / this.scaleFactor, 1 / this.scaleFactor, 1);
                    GlStateManager.translate(-(this.getX() + this.getWidth() * 0.5), -(this.getY() + this.getHeight() * 0.5), 1);
                    StencilShader.render(blendBuffer.framebufferTexture, backgroundBuffer.framebufferTexture);
                    GlStateManager.popMatrix();
                } else if (this.scrollEffects.getValue() == ScrollEffects.FadeInOut) {
                    double x = 0;

                    if (this.alignMode.getValue() == AlignMode.Left) {
                        x = this.getX();
                    } else if (this.alignMode.getValue() == AlignMode.Center) {
                        x = this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidthD(lyric.getLyric()) / 2.0;
                    } else if (this.alignMode.getValue() == AlignMode.Right) {
                        x = this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(lyric.getLyric());
                    }

                    if (finalCurTiming != null) {
                        int cur = 0;
                        for (int k = 0; k < finalCurTiming.timings.size(); k++) {
                            WordTiming wordTiming = finalCurTiming.timings.get(k);

                            if (wordTiming.timing > songProgress - finalCurTiming.start) {
                                cur = k;
                                break;
                            } else if (k == finalCurTiming.timings.size() - 1) {
                                cur = k;
                            }
                        }

                        double offsetX = x;
                        for (int m = 0; m < cur + 1; m++) {
                            WordTiming wordTiming = finalCurTiming.timings.get(m);
                            String word = wordTiming.word;

                            if (m == cur) {
                                WordTiming prev = getPrevWordTiming(cur, m, finalCurTiming);

                                double perc = (songProgress - finalCurTiming.start - (cur == 0 ? 0 : prev.timing)) / (double) (finalCurTiming.timings.get(cur).timing - (cur == 0 ? 0 : prev.timing));
                                double clamped = Math.max(0, Math.min(1, perc));

                                wordTiming.interpPercent = Interpolations.interpBezier(wordTiming.interpPercent, clamped, 1);
                                wordTiming.alpha = (float) wordTiming.interpPercent;
                            } else if (m < cur) {
                                wordTiming.alpha = 1;
                            }

                            double stWidth = getFontRenderer().getStringWidthD(word);
                            double height = getFontRenderer().getHeight();

                            bigFrString(word, offsetX, f, new Color(255, 255, 255, (int) (wordTiming.alpha * 255)).getRGB());

                            offsetX += stWidth;
                        }
                    }
                } else if (this.scrollEffects.getValue() == ScrollEffects.SlideIn) {
                    double targetX = 0;

                    if (this.alignMode.getValue() == AlignMode.Left) {
                        targetX = this.getX();
                    } else if (this.alignMode.getValue() == AlignMode.Center) {
                        targetX = this.getX() + this.getWidth() / 2.0 - lyric.targetOffsetX / 2.0;
                    } else if (this.alignMode.getValue() == AlignMode.Right) {
                        targetX = this.getX() + this.getWidth() - lyric.targetOffsetX;
                    }

                    double x = targetX;

                    if (finalCurTiming != null) {
                        int cur = 0;
                        for (int k = 0; k < finalCurTiming.timings.size(); k++) {
                            WordTiming wordTiming = finalCurTiming.timings.get(k);

                            if (wordTiming.timing > songProgress - finalCurTiming.start) {
                                cur = k;
                                break;
                            } else if (k == finalCurTiming.timings.size() - 1) {
                                cur = k;
                            }
                        }

                        double offsetX = x;
                        double targetOffsetX = 0;
                        for (int m = 0; m < cur + 1; m++) {
                            WordTiming wordTiming = finalCurTiming.timings.get(m);
                            String word = wordTiming.word;

                            double stWidth = getFontRenderer().getStringWidthD(word);
                            double height = getFontRenderer().getHeight();

                            if (m == cur) {
                                WordTiming prev = getPrevWordTiming(cur, m, finalCurTiming);

                                long prevTiming = cur == 0 ? 0 : prev.timing;

                                double perc = (songProgress - finalCurTiming.start - prevTiming) / (finalCurTiming.timings.get(cur).timing - prevTiming);

                                double clamped = Math.max(0, Math.min(1, perc));

                                wordTiming.interpPercent = Interpolations.interpBezier(wordTiming.interpPercent, clamped, 1);
                                wordTiming.alpha = (float) wordTiming.interpPercent;

                                Easing easeInOutQuad = Easing.EASE_IN_OUT_CUBIC;

                                targetOffsetX += stWidth * easeInOutQuad.getFunction().apply(wordTiming.interpPercent);
                            } else if (m < cur) {
                                wordTiming.alpha = 1;
                                targetOffsetX += stWidth;
                            }

                            bigFrString(word, offsetX, f, new Color(255, 255, 255, (int) (wordTiming.alpha * 255)).getRGB());

                            offsetX += stWidth;
                        }

                        lyric.targetOffsetX = targetOffsetX;
                    }
                } else if (this.scrollEffects.getValue() == ScrollEffects.SlideUp || this.scrollEffects.getValue() == ScrollEffects.SlideDown || this.scrollEffects.getValue() == ScrollEffects.SlideMix) {
                    double x = 0;

                    if (this.alignMode.getValue() == AlignMode.Left) {
                        x = this.getX();
                    } else if (this.alignMode.getValue() == AlignMode.Center) {
                        x = this.getX() + this.getWidth() / 2.0 - getFontRenderer().getStringWidthD(lyric.getLyric()) / 2.0;
                    } else if (this.alignMode.getValue() == AlignMode.Right) {
                        x = this.getX() + this.getWidth() - getFontRenderer().getStringWidthD(lyric.getLyric());
                    }

                    if (finalCurTiming != null) {
                        int cur = 0;
                        for (int k = 0; k < finalCurTiming.timings.size(); k++) {
                            WordTiming wordTiming = finalCurTiming.timings.get(k);

                            if (wordTiming.timing > songProgress - finalCurTiming.start) {
                                cur = k;
                                break;
                            } else if (k == finalCurTiming.timings.size() - 1) {
                                cur = k;
                            }
                        }

                        Stencil.write();
                        Rect.draw(x, f, getFontRenderer().getStringWidthD(lyric.getLyric()), getFontRenderer().getHeight(), -1, Rect.RectType.EXPAND);
                        Stencil.erase();

                        int modifier = 1;

                        if (this.scrollEffects.getValue() == ScrollEffects.SlideDown)
                            modifier = -1;

                        double offsetX = x;
                        for (int m = 0; m < cur + 1; m++) {
                            WordTiming wordTiming = finalCurTiming.timings.get(m);
                            String word = wordTiming.word;

                            int i1 = getFontRenderer().getHeight() * (this.scrollEffects.getValue() == ScrollEffects.SlideMix ? (m % 2 == 0 ? 1 : -1) : modifier);

                            if (wordTiming.effectY == -1)
                                wordTiming.effectY = i1;

                            if (m == cur) {
                                WordTiming prev = getPrevWordTiming(cur, m, finalCurTiming);

                                double perc = (songProgress - finalCurTiming.start - (cur == 0 ? 0 : prev.timing)) / (double) (finalCurTiming.timings.get(cur).timing - (cur == 0 ? 0 : prev.timing));
                                double clamped = Math.max(0, Math.min(1, perc));
                                wordTiming.interpPercent = Interpolations.interpBezier(wordTiming.interpPercent, clamped, 1);
                                wordTiming.alpha = (float) wordTiming.interpPercent;

                                wordTiming.effectY = i1 - wordTiming.interpPercent * (i1);
                            } else if (m < cur) {
                                wordTiming.alpha = 1;
                                wordTiming.effectY = 0;
                            }

                            double stWidth = getFontRenderer().getStringWidthD(word);
                            double height = getFontRenderer().getHeight();

                            bigFrString(word, offsetX, f + wordTiming.effectY, new Color(255, 255, 255, (int) (wordTiming.alpha * 255)).getRGB());

                            offsetX += stWidth;
                        }

                        Stencil.dispose();
                    }
                }
            }
        });
    }

    public void horizontalGradient(final double x, final double y, final double width, final double height, final int leftColor, final int rightColor) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        RenderSystem.color(leftColor);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y + height);

        RenderSystem.color(rightColor);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    boolean dontRenderShadow = false;

    private void bigFrString(String text, double x, double y, int color) {
        if (shadow.getValue() && !dontRenderShadow) {
            getFontRenderer().drawStringWithShadow(text, x, y, color);
        } else {
            getFontRenderer().drawString(text, x, y, color);
        }
    }

    private void bigFrStringCentered(String text, double x, double y, int color) {
        if (shadow.getValue() && !dontRenderShadow) {
            getFontRenderer().drawCenteredStringWithShadow(text, x, y, color);
        } else {
            getFontRenderer().drawCenteredString(text, x, y, color);
        }
    }

    private void smallFrString(String text, double x, double y, int color) {
        if (shadow.getValue() && !dontRenderShadow) {
            getSmallFontRenderer().drawStringWithShadow(text, x, y, color);
        } else {
            getSmallFontRenderer().drawString(text, x, y, color);
        }
    }

    private void smallFrStringCentered(String text, double x, double y, int color) {
        if (shadow.getValue() && !dontRenderShadow) {
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
                if (hasRomanization)
                    return StringUtils.returnEmptyStringIfNull(bean.getRomanizationText());
                else
                    return StringUtils.returnEmptyStringIfNull(bean.getTranslationText());
            }
        }

        // 只有罗马音的情况
        if (hasRomanization) {
            if (WidgetsManager.musicLyrics.showRoman.getValue()) {
                return StringUtils.returnEmptyStringIfNull(bean.getRomanizationText());
            } else {
                return "";
            }
        }

        return "";
    }

    public void drawQuads(double x, double y, double width, double height) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
    }

    /**
     * 计算歌词行高度
     */
    public double getLyricHeight() {
        return getFontRenderer().getHeight() + (hasSecondaryLyrics() ? 0 : -getSmallFontRenderer().getHeight() - 4) + lyricHeight.getValue();
    }

    /**
     * 清理渲染状态
     */
    private void cleanupRender() {
        NORMAL.add(() -> {
            GlStateManager.popMatrix();
            RenderSystem.endScissor();
        });
    }
}
