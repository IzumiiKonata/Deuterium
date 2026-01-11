package tritium.screens.ncm;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Location;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.ncm.music.AudioPlayer;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.rendering.Image;
import tritium.rendering.RGBA;
import tritium.rendering.Rect;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.ScrollText;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.Shaders;
import tritium.rendering.ui.widgets.IconWidget;
import tritium.settings.ClientSettings;
import tritium.utils.cursor.CursorUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.timing.Timer;
import tritium.widget.impl.MusicInfoWidget;
import tritium.widget.impl.MusicLyricsWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * Date: 2025/10/17 21:56
 */
public class MusicLyricsPanel implements SharedRenderingConstants {

    public static final List<LyricLine> lyrics = new CopyOnWriteArrayList<>();
    public static LyricLine currentDisplaying = null;

    static double scrollOffset, scrollTarget;

    double fftScale = 0;
    float musicBgAlpha = 1.0f;
    static ITextureObject prevBg = null, prevCover;
    static Music prevMusic = null;

    float alpha = 0f;
    boolean closing = false;

    Framebuffer baseFb, stencilFb;

    Timer scrollOffsetResetTimer = new Timer();

    double coverSize = (CloudMusic.player == null || CloudMusic.player.isPausing()) ? this.getCoverSizeMin() : this.getCoverSizeMax();
    float coverAlpha = 1f;

    double progressBarHeight = 8, volumeBarHeight = 8;

    boolean prevMouse = false;

    ScrollText stMusicName = new ScrollText(), stArtists = new ScrollText();
    IconWidget playPauseButton = new IconWidget("G", FontManager.music40, 0, 0, 24, 24);
    IconWidget prev = new IconWidget("E", FontManager.music40, 0, 0, 32, 32);
    IconWidget next = new IconWidget("H", FontManager.music40, 0, 0, 32, 32);

    // 提前跳转到下一行
    static final float JUMP_TO_NEXT_LINE_MILLIS = 300.0f;

    private final Music music;
    public MusicLyricsPanel(Music music) {
        this.music = music;
        float currentTimeMillis = CloudMusic.player == null ? 0 : CloudMusic.player.getCurrentTimeMillis();
        updateCurrentDisplayingLyric(currentTimeMillis);
        updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * getLyricWidthFactor());
    }

    public static void initLyric(JsonObject lyric) {
        // reset states

        List<LyricLine> parsed = LyricParser.parse(lyric);

        if (parsed.isEmpty()) {
            parsed.add(new LyricLine(0L, "暂无歌词"));
        }

//        fetchTTMLLyrics(music, parsed);

        addLyrics(parsed);
    }

    private static void addLyrics(List<LyricLine> lyricLines) {
        synchronized (lyrics) {
            lyrics.clear();
            lyrics.addAll(lyricLines);

            currentDisplaying = lyrics.getFirst();
            addLongBreaks();
            updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * getLyricWidthFactor());
//            scrollTarget = scrollOffset = 0;
        }
    }

    private static void addLongBreaks() {
        long longBreaksDuration = 3000L;
        if (lyrics.stream().allMatch(l -> l.words.isEmpty())) {

            long timeStamp = lyrics.getFirst().getTimestamp();
            if (timeStamp >= longBreaksDuration) {
                LyricLine line = new LyricLine(0L, "● ● ●");
//                line.renderEmphasizes = false;
                line.words.add(new LyricLine.Word("● ● ●", timeStamp));

                lyrics.add(line);
                lyrics.sort(Comparator.comparingLong(LyricLine::getTimestamp));
            }

            return;
        }

        long last = 0L;

        List<LyricLine> breaksToAdd = new ArrayList<>();

        for (LyricLine lyric : lyrics) {
            long curDur = getLyricDuration(lyric);
            long l = lyric.getTimestamp() - last;
            if (l >= longBreaksDuration) {
                LyricLine line = new LyricLine(last, "● ● ●");
//                line.renderEmphasizes = false;
                line.words.add(new LyricLine.Word("● ● ●", l));
                breaksToAdd.add(line);
            }
            last = lyric.getTimestamp() + curDur;
        }

        lyrics.addAll(breaksToAdd);
        lyrics.sort(Comparator.comparingLong(LyricLine::getTimestamp));
    }

    private static long getLyricDuration(LyricLine line) {
        return line.words.isEmpty() ? 0 : line.words.getLast().timestamp;
    }

    private static void fetchTTMLLyrics(Music music, List<LyricLine> parsed) {

        MultiThreadingUtil.runAsync(() -> {
            try {
                String lrc = HttpUtils.getString("https://gitee.com/IzumiiKonata/amll-ttml-db/raw/main/ncm-lyrics/" + music.getId() + ".yrc", null);
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
        updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * getLyricWidthFactor());
    }

    private static double getLyricWidthFactor() {
        return .45;
    }

    private static double getLyricLineSpacing() {
        return 20;
    }

    private static double lyricFraction() {
        return .25;
    }

    private static void updateLyricPositionsImmediate(double width) {

        if (currentDisplaying == null) return;

        double offsetY = RenderSystem.getHeight() * lyricFraction() - getLyricLineSpacing();
        int toIndex = lyrics.indexOf(currentDisplaying);

        if (toIndex == -1 || toIndex >= lyrics.size()) return;

        synchronized (lyrics) {
            List<LyricLine> subList = lyrics.subList(0, toIndex);
            for (int i = subList.size() - 1; i >= 0; i--) {
                LyricLine lyric = subList.get(i);

                if (i == subList.size() - 1) {
                    lyric.computeHeight(width);
                    offsetY -= lyric.height;
                }

                lyric.posY = offsetY;
                lyric.spring.setPosition(offsetY);

                lyric.computeHeight(width);
                offsetY -= lyric.height + getLyricLineSpacing();
            }

            offsetY = RenderSystem.getHeight() * lyricFraction();
            for (LyricLine lyric : lyrics.subList(toIndex, lyrics.size())) {
                lyric.posY = offsetY;
                lyric.spring.setPosition(offsetY);

                lyric.computeHeight(width);
                offsetY += lyric.height + getLyricLineSpacing();
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

            if (lyric.getTimestamp() > songProgress + JUMP_TO_NEXT_LINE_MILLIS) {
                if (i > 0) {
                    currentDisplaying = lyrics.get(i - 1);
                }
                break;
            } else if (i == lyrics.size() - 1) {
                currentDisplaying = lyrics.get(i);
            }
        }

        if (cur != currentDisplaying) {
            resetLyricStatus();
        }
    }

    private static long getLyricInterpolationWaitTimeMillis() {
        return 75;
    }

    private static void resetLyricStatus() {
        lyrics.forEach(l -> {
            l.shouldUpdatePosition = false;

            l.delayTimer.reset();

            for (LyricLine.Word word : l.words) {
                Arrays.fill(word.emphasizes, 0);
            }

            l.markDirty();
        });
    }

    public void onInit() {
        resetLyricStatus();
    }

    public void close() {
        closing = true;
    }

    public boolean shouldClose() {
        return closing && alpha <= 0.02f;
    }

    public void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        if (prevMouse && !Mouse.isButtonDown(0)) prevMouse = false;

        alpha = Interpolations.interpBezier(alpha, closing ? 0.0f : 1f, 0.3f);

        GlStateManager.pushMatrix();
        scaleAtPos(posX + width * .5, posY + height * .5, 1.1 - (alpha * 0.1));

        this.renderBackground(posX, posY, width, height, alpha);
        this.renderControlsPart(mouseX, mouseY, posX, posY, width, height, alpha);
        this.renderLyrics(mouseX, mouseY, posX, posY, width, height, dWheel, alpha);
        GlStateManager.popMatrix();
    }

    private void renderLyrics(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel, float alpha) {

        if (lyrics.isEmpty()) return;

        float songProgress = CloudMusic.player == null ? 0 : CloudMusic.player.getCurrentTimeMillis();

        updateCurrentDisplayingLyric(songProgress);

        double lyricsWidth = width * getLyricWidthFactor();
        this.updateLyricPositions(posY, height, lyricsWidth);

        List<Runnable> blurRects = new ArrayList<>();

        boolean hoveringLyrics = isHovered(mouseX, mouseY, posX + width * .5, posY, width * .5, height);

        if (hoveringLyrics && dWheel != 0) {

            double strength = 24;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) strength *= 2;

            if (dWheel > 0) scrollTarget += strength;
            else scrollTarget -= strength;

            scrollOffsetResetTimer.reset();

//            this.scrollTarget = Math.min(this.scrollTarget, 0);
        }

        if (scrollOffsetResetTimer.isDelayed(3000)) {
            scrollTarget = 0;
        }

        scrollOffset = Interpolations.interpBezier(scrollOffset, scrollTarget, 0.25f);

        double lyricRenderOffsetX = RenderSystem.getWidth() * .5;
        for (int k = 0; k < lyrics.size(); k++) {
            LyricLine lyric = lyrics.get(k);

            if (lyric.posY + lyric.height + getLyricLineSpacing() + scrollOffset < posY) {
                continue;
            }

            if (lyric.posY + scrollOffset > posY + height) {
                break;
            }

            int indexOf = lyrics.indexOf(currentDisplaying);

            lyric.alpha = Interpolations.interpBezier(lyric.alpha, lyric == currentDisplaying ? 1f : 0f, 0.1f);
            boolean isHovering = isHovered(mouseX, mouseY - scrollOffset, lyricRenderOffsetX, lyric.posY, lyricsWidth, lyric.height);
            lyric.hoveringAlpha = Interpolations.interpBezier(lyric.hoveringAlpha, isHovering ? 1f : 0f, 0.2f);
            lyric.blurAlpha = Interpolations.interpBezier(lyric.blurAlpha, !hoveringLyrics ? Math.min(1f, Math.abs(k - indexOf) * .85f) : 0f, 0.05f);

            if (isHovering) {
                CursorUtils.setOverride(CursorUtils.HAND);
            }

            if (isHovering && Mouse.isButtonDown(0) && !prevMouse) {
                prevMouse = true;
                if (CloudMusic.player != null) {
                    CloudMusic.player.setPlaybackTime(lyric.timestamp);
                }

                if (scrollTarget != 0) {
                    updateLyricPositionsImmediate(lyricsWidth);
                }
                scrollTarget = 0;
                resetLyricStatus();
            }

            if (lyric.hoveringAlpha >= .02f)
                roundedRect(lyricRenderOffsetX - 4, lyric.posY + scrollOffset + lyric.reboundAnimation, lyricsWidth + lyric.reboundAnimation, lyric.height + 2, 8, 4 + 2 * Easing.EASE_IN_OUT_QUAD.getFunction().apply((double) lyric.hoveringAlpha), 1, 1, 1, alpha * lyric.hoveringAlpha * .15f);

            double renderX = lyricRenderOffsetX + lyric.reboundAnimation;
            double renderY = lyric.posY + lyric.reboundAnimation + scrollOffset;

            lyric.reboundAnimation = Interpolations.interpBezier(lyric.reboundAnimation, lyric == currentDisplaying ? 2f : 0f, 0.1f);

            List<LyricLine.Word> words = lyric.words;
            if (!words.isEmpty()) {
                for (int i = 0; i < words.size(); i++) {
                    LyricLine.Word word = words.get(i);
                    double wordWidth = FontManager.pf65bold.getStringWidthD(word.word);

                    if (renderX + wordWidth >= lyricRenderOffsetX + lyricsWidth + lyric.reboundAnimation) {
                        renderX = lyricRenderOffsetX + lyric.reboundAnimation;
                        renderY += FontManager.pf65bold.getHeight() * .85 + 4;
                    }

                    if (!lyric.renderEmphasizes) Arrays.fill(word.emphasizes, 2);

                    double emphasizeWholeWord = word.emphasizes[0];

                    char[] charArray = word.word.toCharArray();

                    double emphasizeTarget = 1;
                    double emphasizeSpeed = 0.05;

                    if (lyric == currentDisplaying) {
                        if (charArray.length > 1) {
                            double x = renderX;
                            for (int j = 0; j < charArray.length; j++) {
                                char c = charArray[j];

                                FontManager.pf65bold.drawString(String.valueOf(c), x, renderY - word.emphasizes[j], hexColor(1, 1, 1, alpha * .5f));
                                x += FontManager.pf65bold.getCharWidth(c, j + 1 < charArray.length ? charArray[j + 1] : '\0');
                            }
                        } else {
                            FontManager.pf65bold.drawString(word.word, renderX, renderY - word.emphasizes[0], hexColor(1, 1, 1, alpha * .5f));
                        }
                    } else {
                        FontManager.pf65bold.drawString(word.word, renderX, renderY, hexColor(1, 1, 1, alpha * .5f));
                    }

                    if (lyrics.indexOf(currentDisplaying) - k <= 1) {
                        LyricLine.Word prev = i > 0 ? words.get(i - 1) : null;

                        long prevTiming = i == 0 ? 0 : prev.timestamp;
                        long timestamp = word.timestamp;

                        double progress = Math.max(0, Math.min(1, (songProgress - lyric.timestamp - prevTiming) / (double) (timestamp - prevTiming)));
                        double stringWidthD = FontManager.pf65bold.getStringWidthD(word.word);

                        boolean shouldClip = progress > 0 && progress < 1;

                        if (progress == 1) {

                            double x = renderX;
                            for (int j = 0; j < charArray.length; j++) {
                                char c = charArray[j];

                                if (lyric.renderEmphasizes)
                                    word.emphasizes[j] = Interpolations.interpBezier(word.emphasizes[j], emphasizeTarget, emphasizeSpeed);

                                FontManager.pf65bold.drawString(String.valueOf(c), x, renderY - word.emphasizes[j], hexColor(1, 1, 1, alpha * lyric.alpha));
                                x += FontManager.pf65bold.getCharWidth(c, j + 1 < charArray.length ? charArray[j + 1] : '\0');
                            }
                        }

                        if (shouldClip) {

                            int scale = 2;
                            int fbWidth = ((int) stringWidthD) * scale, fbHeight = (FontManager.pf65bold.getHeight() + 6) * scale;

                            GlStateManager.matrixMode(GL11.GL_PROJECTION);
                            GlStateManager.pushMatrix();
                            GlStateManager.loadIdentity();
                            GlStateManager.ortho(0.0D, fbWidth * .5, fbHeight * .5, 0.0D, 1000.0D, 3000.0D);
                            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                            GlStateManager.pushMatrix();
                            GlStateManager.loadIdentity();
                            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

                            double gradientWidth = 16;

                            // stencil texture
                            {
                                stencilFb = RenderSystem.createFrameBuffer(stencilFb, fbWidth, fbHeight);
                                stencilFb.bindFramebuffer(true);
                                stencilFb.setFramebufferColor(1, 1, 1, 0);
                                stencilFb.framebufferClearNoBinding();

                                double w = progress * (stringWidthD + gradientWidth);
                                Rect.draw(0, 0, w - gradientWidth, FontManager.pf65bold.getHeight() + 6, -1);
                                RenderSystem.drawGradientRectLeftToRight(w - gradientWidth, 0, w, FontManager.pf65bold.getHeight() + 6, -1, 0);
                            }

                            // base texture
                            {
                                baseFb = RenderSystem.createFrameBuffer(baseFb, fbWidth, fbHeight);
                                baseFb.bindFramebuffer(true);
                                baseFb.setFramebufferColor(1, 1, 1, 0);
                                baseFb.framebufferClearNoBinding();

                                int prog = (int) (progress * charArray.length);

                                double x = 0;
                                for (int j = 0; j < charArray.length; j++) {
                                    char c = charArray[j];

                                    if (j <= prog) {
                                        if (lyric.renderEmphasizes)
                                            word.emphasizes[j] = Interpolations.interpBezier(word.emphasizes[j], emphasizeTarget, emphasizeSpeed);
                                    }

                                    FontManager.pf65bold.drawString(String.valueOf(c), x, 2 - word.emphasizes[j], hexColor(1, 1, 1, alpha * lyric.alpha));
                                    x += FontManager.pf65bold.getCharWidth(c, j + 1 < charArray.length ? charArray[j + 1] : '\0');
                                }
                            }

                            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

                            GlStateManager.popMatrix();
                            GlStateManager.matrixMode(GL11.GL_PROJECTION);
                            GlStateManager.popMatrix();
                            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

                            Shaders.STENCIL.draw(baseFb.framebufferTexture, stencilFb.framebufferTexture, renderX, renderY - 2, fbWidth * .5, fbHeight * .5);

                            if (ClientSettings.DEBUG_MODE.getValue()) {
//                                FontManager.pf18bold.drawString("Stencil: " + stencilFb.framebufferTextureWidth + "x" + stencilFb.framebufferTextureHeight, 50, 32, -1);
//                                FontManager.pf18bold.drawString("Base: " + baseFb.framebufferTextureWidth + "x" + baseFb.framebufferTextureHeight, 50, 64, -1);

                                double spacing = NCMScreen.getInstance().getSpacing();
                                Rect.draw(spacing, spacing, 400, (fbHeight * .5) * 3 + (20 * 2), 0xff000000);

                                GlStateManager.enableTexture2D();
                                GlStateManager.color(1, 1, 1, 1);

                                GlStateManager.bindTexture(baseFb.framebufferTexture);
                                double xOff = spacing + 120;
                                ShaderProgram.drawQuadFlipped(xOff, spacing, fbWidth * .5, fbHeight * .5);

                                FontManager.pf28bold.drawCenteredStringVertical("Base Texture", spacing + 8, spacing + fbHeight * .25, -1);

                                GlStateManager.bindTexture(stencilFb.framebufferTexture);
                                ShaderProgram.drawQuadFlipped(xOff, spacing + fbHeight * .5 + 20, fbWidth * .5, fbHeight * .5);

                                FontManager.pf28bold.drawCenteredStringVertical("Stencil Texture", spacing + 8, spacing + fbHeight * .5 + 20 + fbHeight * .25, -1);

                                Shaders.STENCIL.draw(baseFb.framebufferTexture, stencilFb.framebufferTexture, xOff, spacing + (fbHeight * .5) * 2 + 40, fbWidth * .5, fbHeight * .5);

                                FontManager.pf28bold.drawCenteredStringVertical("Result", spacing + 8, spacing + (fbHeight * .5) * 2 + 40 + fbHeight * .25, -1);

                            }

//                            Image.draw(stencilFb.framebufferTexture, 50, 72, stencilFb.framebufferTextureWidth * .5, stencilFb.framebufferTextureHeight * .5, Image.Type.Normal);
//                            Image.draw(baseFb.framebufferTexture, 50, 128, baseFb.framebufferTextureWidth * .5, baseFb.framebufferTextureHeight * .5, Image.Type.Normal);
//                            StencilClipManager.beginClip(() -> {
//                                Rect.draw(finalRenderX, finalRenderY - word.emphasize, progress * stringWidthD, FontManager.pf65bold.getHeight(), -1);
//                            });
                        }

                    } else {
                        FontManager.pf65bold.drawString(word.word, renderX, renderY - emphasizeWholeWord, hexColor(1, 1, 1, alpha * lyric.alpha));
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
                double translationX = lyricRenderOffsetX + lyric.reboundAnimation;
                double translationY = renderY + FontManager.pf65bold.getHeight() * .85 + 8;

                String[] strings = FontManager.pf34bold.fitWidth(lyric.translationText, lyricsWidth);
                for (String string : strings) {
                    FontManager.pf34bold.drawString(string, translationX, translationY, hexColor(1, 1, 1, alpha * .75f * ((lyric.alpha * .6f) + .4f)));
                    translationY += FontManager.pf34bold.getHeight() + 4;
                }
//                FontManager.pf34bold.drawString(lyric.translationText, translationX, translationY, hexColor(1, 1, 1, alpha * .75f * ((lyric.alpha * .6f) + .4f)));
            }

            blurRects.add(() -> Rect.draw(lyricRenderOffsetX - 4, lyric.posY + scrollOffset, lyricsWidth, lyric.height + 8, hexColor(1, 1, 1, alpha * lyric.blurAlpha)));
        }

        GlStateManager.pushMatrix();
        this.scaleAtPos(lyricRenderOffsetX, RenderSystem.getHeight() * .5, 1 / (1.1 - (alpha * 0.1)));
        Shaders.BLUR_SHADER.runNoCaching(blurRects);
//        Shaders.UI_BLOOM_SHADER.runNoCaching(bloomRunnables);
        GlStateManager.popMatrix();
    }

    private void updateLyricPositions(double posY, double height, double width) {

        if (currentDisplaying == null) return;

        int idxCurrent = lyrics.indexOf(currentDisplaying);

        if (idxCurrent < 0 || idxCurrent >= lyrics.size()) return;
//
        double offsetY = RenderSystem.getHeight() * lyricFraction()/* - (idxCurrent > 0 ? lyrics.get(idxCurrent - 1).height : 0)*/;

        synchronized (lyrics) {
            List<LyricLine> subList = lyrics.subList(0, idxCurrent);
            double frameDeltaTime = RenderSystem.getFrameDeltaTime() * .0125;
            for (int i = subList.size() - 1; i >= 0; i--) {
                LyricLine lyric = subList.get(i);

                lyric.computeHeight(width);
                offsetY -= lyric.height + getLyricLineSpacing();

                if ((scrollTarget == 0 && (subList.size() - 1 - i) >= 3) && lyric.posY + lyric.height + getLyricLineSpacing() + 2 + scrollOffset < posY)
                    break;

//                lyric.posY = Interpolations.interpBezier(lyric.posY, offsetY, fraction);
                lyric.spring.setTargetPosition(offsetY);
                lyric.spring.update(frameDeltaTime);
                lyric.posY = lyric.spring.getCurrentPosition();
            }

            offsetY = RenderSystem.getHeight() * lyricFraction();
            List<LyricLine> list = lyrics.subList(idxCurrent, lyrics.size());
            int oobCounter = 0;
            for (LyricLine lyric : list) {
                int j = lyrics.indexOf(lyric);

//                Rect.draw(RenderSystem.getWidth() * .5 + lyric.reboundAnimation, lyric.posY, width, lyric.height, 0x80FFFFFF);

                lyric.computeHeight(width);

                LyricLine prev = j > 0 ? lyrics.get(j - 1) : null;

                if (prev != null) {
                    if (prev.delayTimer.isDelayed(getLyricInterpolationWaitTimeMillis()))
                        lyric.shouldUpdatePosition = true;
//                    if (lyric.posY - (prev.posY) >= prev.height * 1.5)
//                        lyric.shouldUpdatePosition = true;
                }

//                if (prev != null && lyric.posY - (prev.posY + prev.height) < 0) {
//                    updateLyricPositionsImmediate(width);
//                    break;
//                }

                if (prev != null && !lyric.shouldUpdatePosition) {
                    lyric.delayTimer.reset();
                    break;
                }

                if (prev == null && !lyric.delayTimer.isDelayed(getLyricInterpolationWaitTimeMillis())) break;

                lyric.spring.setTargetPosition(offsetY);
                lyric.spring.update(frameDeltaTime);
                lyric.posY = lyric.spring.getCurrentPosition();

                if (offsetY + scrollOffset > posY + height) {
                    oobCounter += 1;

                    if (oobCounter >= 4 && scrollTarget == 0) break;
                }

                offsetY += lyric.height + getLyricLineSpacing();
            }
        }

    }

    private double getCoverSizeMax() {
        return RenderSystem.getHeight() * .5;
    }

    private double getCoverSizeMin() {
        return getCoverSizeMax() * .8;
    }

    private void renderControlsPart(double mouseX, double mouseY, double posX, double posY, double width, double height, float alpha) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        AudioPlayer player = CloudMusic.player;

        double center = this.getCoverSizeMin();
        coverSize = Interpolations.interpBezier(coverSize, player == null || player.isPausing() ? this.getCoverSizeMin() : this.getCoverSizeMax(), 0.2f);

        double xOffset = (RenderSystem.getWidth() * .5 - center - this.getCoverSizeMax() * .5) * 0;
        if (prevCover != null && coverAlpha <= .9f) {
            GlStateManager.bindTexture(prevCover.getGlTextureId());
            this.roundedRectTextured(center - coverSize * .5 + xOffset, center - coverSize * .575, coverSize, coverSize, 3, alpha);
        }

        Location musicCover = CloudMusic.currentlyPlaying.getCoverLocation();
        ITextureObject tex = textureManager.getTexture(musicCover);

        if (tex != null) {
            coverAlpha = Interpolations.interpBezier(coverAlpha, 1.0f, 0.2f);
            GlStateManager.bindTexture(tex.getGlTextureId());
            tex.linearFilter();
            this.roundedRectTextured(center - coverSize * .5 + xOffset, center - coverSize * .575, coverSize, coverSize, 3, alpha * coverAlpha);
        }

        double elementsXOffset = center - this.getCoverSizeMax() * .5 + xOffset;
        double elementsYOffset = center + this.getCoverSizeMax() * .45 + 8;

        stMusicName.render(FontManager.pf28bold, CloudMusic.currentlyPlaying.getName(), elementsXOffset, elementsYOffset, this.getCoverSizeMax(), RGBA.color((float) 1, (float) 1, (float) 1, alpha));
        stArtists.render(FontManager.pf20bold, CloudMusic.currentlyPlaying.getArtistsName(), elementsXOffset, elementsYOffset + FontManager.pf20bold.getHeight() + 8, this.getCoverSizeMax(), RGBA.color((float) 1, (float) 1, (float) 1, alpha * .8f));

        // progressbar 背景
        double progressBarYOffset = elementsYOffset + FontManager.pf20bold.getHeight() + 8 + FontManager.pf20bold.getHeight() + 12;
        double progressBarWidth = this.getCoverSizeMax();

        roundedRect(elementsXOffset, progressBarYOffset - progressBarHeight * .5, progressBarWidth, progressBarHeight, (this.progressBarHeight / 8.0f) * 3, hexColor(1, 1, 1, alpha * .5f));

        float currentTimeMillis = player == null ? 0 : player.getCurrentTimeMillis();
        float totalTimeMillis = player == null ? 0.01f : player.getTotalTimeMillis();
        float perc = player == null ? 0 : currentTimeMillis / totalTimeMillis;

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

            if (player != null) {
                float progress = (float) (percent * totalTimeMillis);
                player.setPlaybackTime(progress);
                MusicLyricsWidget.quickResetProgress(progress);
                MusicLyricsPanel.resetProgress(progress);
                scrollTarget = scrollOffset = 0;
            }
        }

        // curTime
        FontManager.pf12bold.drawString(formatDuration(currentTimeMillis), elementsXOffset, progressBarYOffset + 12, hexColor(1, 1, 1, alpha * .5f));
        String remainingTime = "-" + formatDuration(totalTimeMillis - currentTimeMillis);
        FontManager.pf12bold.drawString(remainingTime, elementsXOffset + progressBarWidth - FontManager.pf12bold.getStringWidthD(remainingTime), progressBarYOffset + 12, hexColor(1, 1, 1, alpha * .5f));

        double volumeBarYOffset = posY + height - (center - getCoverSizeMax() * .575 - NCMScreen.getInstance().getSpacing()) - FontManager.music40.getHeight() * .5 + 2;
        double volumeBarWidth = this.getCoverSizeMax() - FontManager.music40.getStringWidthD("I") - FontManager.music40.getStringWidthD("J");
//        double v = (center - getCoverSizeMax() * .575 - NCMScreen.getInstance().getSpacing());
//        Rect.draw(0, posY + height - v, width, v, -1);

        double volumeIconY = volumeBarYOffset - FontManager.music40.getHeight() * .5 - .5;
        FontManager.music40.drawString("I", elementsXOffset - 8, volumeIconY, hexColor(1, 1, 1, alpha * .5f));
        FontManager.music40.drawString("J", elementsXOffset + progressBarWidth - FontManager.music40.getStringWidthD("J") + 4, volumeIconY, hexColor(1, 1, 1, alpha * .5f));

        double volumeBarXOffset = elementsXOffset + FontManager.music40.getStringWidthD("I") - 2;
        roundedRect(volumeBarXOffset, volumeBarYOffset - volumeBarHeight * .5, volumeBarWidth, volumeBarHeight, (this.volumeBarHeight / 8.0f) * 3, hexColor(1, 1, 1, alpha * .5f));
        StencilClipManager.beginClip(() -> {
            Rect.draw(volumeBarXOffset, volumeBarYOffset - volumeBarHeight * .5, volumeBarWidth * (player == null ? 0 : player.getVolume()), volumeBarHeight, -1);
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

        if (hoveringProgressBar || hoveringVolumeBar) {
            CursorUtils.setOverride(CursorUtils.HAND);
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

            if (i == 0) {
                if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) {
                    if (CloudMusic.player.isPausing()) CloudMusic.player.unpause();
                    else CloudMusic.player.pause();

                }
            }

            return true;
        });

        playPauseButton.fontOffsetY = 0;

        prev.setAlpha(alpha);
        prev.setWidth(32);
        prev.setHeight(32);
        prev.setPosition(volumeBarXOffset + volumeBarWidth * .5 - playPauseButton.getWidth() * .5 - 16 - prev.getWidth(), playPauseButton.getY());
        prev.renderWidget(mouseX, mouseY, 0);
        prev.fr = FontManager.music40;
        prev.fontOffsetY = 0;
        prev.setColor(Color.WHITE);

        prev.setOnClickCallback((x, y, i) -> {

            if (i == 0) {
                if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) CloudMusic.prev();
            }

            return true;
        });

        next.setAlpha(alpha);
        next.setWidth(32);
        next.setHeight(32);
        next.setPosition(volumeBarXOffset + volumeBarWidth * .5 + playPauseButton.getWidth() * .5 + 16, playPauseButton.getY());
        next.renderWidget(mouseX, mouseY, 0);
        next.fr = FontManager.music40;
        next.fontOffsetY = 0;
        next.setColor(Color.WHITE);

        next.setOnClickCallback((x, y, i) -> {

            if (i == 0) {
                if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) CloudMusic.next();
            }

            return true;
        });
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        playPauseButton.onMouseClickReceived(mouseX, mouseY, mouseButton);
        prev.onMouseClickReceived(mouseX, mouseY, mouseButton);
        next.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

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

    private void renderBackground(double posX, double posY, double width, double height, float alpha) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location musicCoverBlured = CloudMusic.currentlyPlaying == null ? null : CloudMusic.currentlyPlaying.getBlurredCoverLocation();
        ITextureObject texBg = CloudMusic.currentlyPlaying == null ? null : textureManager.getTexture(musicCoverBlured);

        if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying != prevMusic) {

            if (prevMusic != null) musicBgAlpha = 0.0f;

            prevBg = prevMusic == null ? null : textureManager.getTexture(prevMusic.getBlurredCoverLocation());
            prevCover = prevMusic == null ? null : textureManager.getTexture(prevMusic.getCoverLocation());
            prevMusic = CloudMusic.currentlyPlaying;
            coverAlpha = 0.0f;
        }

        if (texBg != null || prevBg != null) {

            GlStateManager.pushMatrix();

            float max = 0;
            for (int i = 0; i < Math.min(20, AudioPlayer.bandValues.length); i++) {
                max = Math.max(max, AudioPlayer.bandValues[i]);
            }

            if (!Double.isFinite(fftScale)) fftScale = 0;

            if (!Float.isFinite(max) || max <= .1f) max = 0;

            fftScale = Interpolations.interpBezier(fftScale, max * .05, .4f);

            scaleAtPos(RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5, 1 + fftScale);

            double bgSize = Math.max(width, height);

            if (prevBg != null && musicBgAlpha < 0.99f) {
                GlStateManager.bindTexture(prevBg.getGlTextureId());
                prevBg.linearFilter();
                GlStateManager.color(1, 1, 1, alpha);
                Image.draw(posX + width * .5 - bgSize * .5, posY + height * .5 - bgSize * .5, bgSize, bgSize, Image.Type.NoColor);
            }

            if (texBg != null) {
                this.musicBgAlpha = Interpolations.interpBezier(this.musicBgAlpha, 1.0f, prevBg == null ? 0.15f : 0.05f);
                GlStateManager.bindTexture(texBg.getGlTextureId());
                texBg.linearFilter();
                GlStateManager.color(1, 1, 1, alpha * this.musicBgAlpha);
                Image.draw(posX + width * .5 - bgSize * .5, posY + height * .5 - bgSize * .5, bgSize, bgSize, Image.Type.NoColor);
            }

            GlStateManager.popMatrix();
        }

        Rect.draw(posX, posY, width, height, hexColor(0, 0, 0, alpha * .3f));
    }
}
