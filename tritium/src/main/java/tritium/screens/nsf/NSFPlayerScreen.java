package tritium.screens.nsf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import tritium.management.FontManager;
import tritium.nsf.NSFPlayer;
import tritium.nsf.Oscilloscope;
import tritium.nsf.ScopeTap;
import tritium.rendering.Rect;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.screens.BaseScreen;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 */
public class NSFPlayerScreen extends BaseScreen {

    public static final NSFPlayer player = new NSFPlayer();

    private static final int COL_BG = 0x0E0F12;
    private static final int COL_PANEL = 0x16171B;
    private static final int COL_HOVER = 0x24262C;
    private static final int COL_PRIMARY = 0xF2F3F5;
    private static final int COL_SECONDARY = 0x8A8D94;
    private static final int COL_WAVE = 0xEAF6FF;
    private static final int COL_ACCENT = 0x2D7FF9;

    private float alpha = 0f;
    private boolean closing = false;

    private int columns = 1;
    private float windowMs = 16f;
    private final double assumedDuration = 300;

    private boolean seeking = false;
    private double seekPreview = 0;

    private Oscilloscope[] oscs = new Oscilloscope[0];
    private ScopeTap[] oscTaps = new ScopeTap[0];
    private float[] snapBuf = new float[1];
    private float lastWindowMs = -1;
    private int lastSampleRate = -1;

    private boolean browserOpen = false;
    private File[] browserFiles = new File[0];
    private double browserScroll = 0;
    private final List<Rect2> browserRows = new ArrayList<>();

    private final Rect2 playBtn = new Rect2();
    private final Rect2 prevBtn = new Rect2();
    private final Rect2 nextBtn = new Rect2();
    private final Rect2 browseBtn = new Rect2();
    private final Rect2 colMinus = new Rect2();
    private final Rect2 colPlus = new Rect2();
    private final Rect2 winMinus = new Rect2();
    private final Rect2 winPlus = new Rect2();
    private final Rect2 seekBar = new Rect2();
    private final Rect2 browserClose = new Rect2();
    private final Rect2 linearMixBtn = new Rect2();
    private final Rect2 triStepsBtn = new Rect2();
    private final Rect2 echoBtn = new Rect2();

    private static boolean dropRegistered = false;
    private static GLFWDropCallback dropCallback;

    @Override
    public void initGui() {
        alpha = 0f;
        closing = false;
        Keyboard.enableRepeatEvents(true);
        ensureDropCallback();
    }

    private static void ensureDropCallback() {
        if (dropRegistered) {
            return;
        }

        long window = Display.getWindow();
        if (window == 0) {
            return;
        }

        dropRegistered = true;
        dropCallback = GLFWDropCallback.create((win, count, names) -> {
            if (!(Minecraft.getMinecraft().currentScreen instanceof NSFPlayerScreen)) {
                return;
            }

            for (int i = 0; i < count; i++) {
                String path = GLFWDropCallback.getName(names, i);
                if (path != null && path.toLowerCase().endsWith(".nsf")) {
                    File f = new File(path);
                    MultiThreadingUtil.runAsync(() -> {
                        if (player.load(f)) {
                            player.play();
                        }
                    });
                    break;
                }
            }
        });
        GLFW.glfwSetDropCallback(window, dropCallback);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private int color(int rgb, float a) {
        int alpha = (int) (Math.max(0, Math.min(1, a)) * 255);
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        if (closing && alpha <= 0.02f) {
            mc.displayGuiScreen(null);
            return;
        }

        alpha = Interpolations.interpolate(alpha, closing ? 0f : 1f, 0.4f);

        double w = RenderSystem.getWidth();
        double h = RenderSystem.getHeight();

        Rect.draw(0, 0, w, h, color(0x000000, alpha * 0.78f));

        GlStateManager.pushMatrix();
        this.scaleAtPos(w * 0.5, h * 0.5, 0.96 + alpha * 0.04);

        double margin = 16;
        double headerH = 38;
        double footerH = 70;

        double contentX = margin;
        double contentY = margin + headerH;
        double contentW = w - margin * 2;
        double contentH = h - margin * 2 - headerH - footerH;

        drawHeader(margin, margin, w - margin * 2, headerH, mouseX, mouseY);

        ScopeTap[] taps = player.getTaps();
        if (taps.length == 0) {
            drawEmptyState(contentX, contentY, contentW, contentH);
        } else {
            ensureOscilloscopes(taps);
            drawGrid(taps, contentX, contentY, contentW, contentH);
        }

        drawFooter(margin, h - margin - footerH, w - margin * 2, footerH, mouseX, mouseY);

        if (browserOpen) {
            drawBrowser(mouseX, mouseY, w, h);
        }

        GlStateManager.popMatrix();
    }

    private void drawHeader(double x, double y, double w, double h, double mouseX, double mouseY) {
        String name = player.isLoaded()
                ? (player.getTitle() == null || player.getTitle().isEmpty() ? player.getFileName() : player.getTitle())
                : "未加载 NSF";

        double bh = 22;
        double cy = y + h * 0.5 - bh * 0.5;
        double rightX = x + w;
        double browseW = 64;

        CFontRenderer tf = FontManager.pf12bold;
        double pad = 14;
        double linW = tf.getStringWidth("线性混音") + pad;
        double triW = tf.getStringWidth("三角波/锯齿波平滑") + pad;
        double echoW = tf.getStringWidth("混响") + pad;

        browseBtn.set(rightX - browseW, cy, browseW, bh);
        echoBtn.set(browseBtn.x - 8 - echoW, cy, echoW, bh);
        triStepsBtn.set(echoBtn.x - 6 - triW, cy, triW, bh);
        linearMixBtn.set(triStepsBtn.x - 6 - linW, cy, linW, bh);

        double titleMax = linearMixBtn.x - x - 10;

        CFontRenderer title = FontManager.pf18bold;
        title.drawString(title.trim(name, titleMax), x, y + h * 0.5 - title.getHeight() * 0.5 - 6, color(COL_PRIMARY, alpha));

        if (player.isLoaded()) {
            String sub = player.getArtist() == null ? "" : player.getArtist();
            CFontRenderer subF = FontManager.pf12;
            subF.drawString(subF.trim(sub, titleMax), x, y + h * 0.5 + 4, color(COL_SECONDARY, alpha));
        }

        drawToggle(linearMixBtn, "线性混音", player.isLinearMixing(), mouseX, mouseY);
        drawToggle(triStepsBtn, "三角波/锯齿波平滑", player.isTriangleMoreSteps(), mouseX, mouseY);
        drawToggle(echoBtn, "混响", player.isEchoEnabled(), mouseX, mouseY);
        drawButton(browseBtn, "浏览...", mouseX, mouseY, true);
    }

    private void drawToggle(Rect2 r, String label, boolean active, double mouseX, double mouseY) {
        boolean hov = r.contains(mouseX, mouseY);
        int bg = active ? COL_ACCENT : (hov ? COL_HOVER : COL_PANEL);
        roundedRect(r.x, r.y, r.w, r.h, 4, color(bg, alpha));
        CFontRenderer f = FontManager.pf12bold;
        f.drawCenteredString(label, r.x + r.w * 0.5, r.y + r.h * 0.5 - f.getHeight() * 0.5, color(active ? 0xFFFFFF : COL_SECONDARY, alpha));
    }

    private void drawEmptyState(double x, double y, double w, double h) {
        roundedRect(x, y, w, h, 8, color(COL_PANEL, alpha * 0.6f));
        CFontRenderer f = FontManager.pf16;
        String s = "将 .nsf 文件拖入窗口，或点击右上角 \"浏览...\" 选择文件";
        f.drawCenteredString(s, x + w * 0.5, y + h * 0.5 - f.getHeight() * 0.5, color(COL_SECONDARY, alpha));
    }

    private void ensureOscilloscopes(ScopeTap[] taps) {
        int sr = player.getSampleRate();
        if (taps == oscTaps && windowMs == lastWindowMs && sr == lastSampleRate) {
            return;
        }

        int display = Math.max(8, Math.round(sr * windowMs / 1000f));
        int capture = display * 2;

        Oscilloscope[] arr = new Oscilloscope[taps.length];
        for (int i = 0; i < taps.length; i++) {
            arr[i] = new Oscilloscope(capture, display);
        }

        this.oscs = arr;
        this.oscTaps = taps;
        this.lastWindowMs = windowMs;
        this.lastSampleRate = sr;
        if (snapBuf.length < capture) {
            snapBuf = new float[capture];
        }
    }

    private void drawGrid(ScopeTap[] taps, double x, double y, double w, double h) {
        int cols = Math.max(1, Math.min(columns, taps.length));
        int rows = (int) Math.ceil(taps.length / (double) cols);

        double gap = 6;
        double cellW = (w - gap * (cols - 1)) / cols;
        double cellH = (h - gap * (rows - 1)) / rows;

        for (int i = 0; i < taps.length; i++) {
            int cx = i % cols;
            int cy = i / cols;
            double px = x + cx * (cellW + gap);
            double py = y + cy * (cellH + gap);

            drawCell(taps[i], oscs[i], px, py, cellW, cellH);
        }
    }

    private void drawCell(ScopeTap tap, Oscilloscope osc, double x, double y, double w, double h) {
        roundedRect(x, y, w, h, 6, color(COL_PANEL, alpha * 0.85f));

        double pad = 6;
        double labelH = 12;

        CFontRenderer f = FontManager.pf12bold;
        f.drawString(tap.getName(), x + pad, y + pad - 1, color(COL_PRIMARY, alpha * 0.9f));

        double waveTop = y + pad + labelH;
        double waveH = h - pad * 2 - labelH;
        double waveW = w - pad * 2;
        if (waveH < 4 || waveW < 4) {
            return;
        }

        Rect.draw(x + pad, waveTop + waveH * 0.5, waveW, 1, color(0xFFFFFF, alpha * 0.06f));

        tap.snapshot(snapBuf, osc.getCaptureSamples());
        int count = osc.compute(snapBuf, waveW, waveH);

        double midY = waveTop + waveH * 0.5;
        drawWave(osc.vertexes, count, x + pad, midY);
    }

    private void drawWave(float[] vertexes, int count, double originX, double originY) {
        if (count < 2) {
            return;
        }

        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.translate(originX, originY, 0);

        float r = ((COL_WAVE >> 16) & 0xFF) * RenderSystem.DIVIDE_BY_255;
        float g = ((COL_WAVE >> 8) & 0xFF) * RenderSystem.DIVIDE_BY_255;
        float b = (COL_WAVE & 0xFF) * RenderSystem.DIVIDE_BY_255;

        GL11.glLineWidth(1.3f);
        GlStateManager.color(r, g, b, alpha * 0.95f);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int j = 0; j < count; j++) {
            GL11.glVertex2d(vertexes[j * 2], vertexes[j * 2 + 1]);
        }
        GL11.glEnd();

        GlStateManager.popMatrix();

        GL11.glLineWidth(1f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderSystem.resetColor();
    }

    private void drawFooter(double x, double y, double w, double h, double mouseX, double mouseY) {
        roundedRect(x, y, w, h, 8, color(COL_PANEL, alpha * 0.92f));

        double inset = 12;
        double rowY = y + h - 28;

        double btn = 26;
        double cx = x + w * 0.5;

        prevBtn.set(cx - btn * 1.5 - 8, rowY, btn, btn);
        playBtn.set(cx - btn * 0.5, rowY, btn, btn);
        nextBtn.set(cx + btn * 0.5 + 8, rowY, btn, btn);

        drawIconButton(prevBtn, "H", mouseX, mouseY);
        drawIconButton(playBtn, player.isPaused() ? "B" : "A", mouseX, mouseY);
        drawIconButton(nextBtn, "E", mouseX, mouseY);

        double pos = player.getPositionSeconds();
        double frac;
        if (seeking) {
            frac = seekPreview;
        } else {
            frac = Math.max(0, Math.min(1, pos / assumedDuration));
        }

        CFontRenderer timeF = FontManager.pf12;
        String cur = formatTime(seeking ? seekPreview * assumedDuration : pos);
        String tot = formatTime(assumedDuration);

        double barY = y + 14;
        double timeW = 34;
        double barX = x + inset + timeW;
        double barW = w - inset * 2 - timeW * 2;
        seekBar.set(barX, barY - 4, barW, 12);

        timeF.drawString(cur, x + inset, barY - timeF.getHeight() * 0.5 + 1, color(COL_SECONDARY, alpha));
        timeF.drawString(tot, x + w - inset - timeW + 4, barY - timeF.getHeight() * 0.5 + 1, color(COL_SECONDARY, alpha));

        double trackH = 3;
        roundedRect(barX, barY - trackH * 0.5, barW, trackH, trackH * 0.5, color(0xFFFFFF, alpha * 0.18f));
        if (frac > 0) {
            roundedRect(barX, barY - trackH * 0.5, barW * frac, trackH, trackH * 0.5, color(COL_WAVE, alpha));
        }
        double knobX = barX + barW * frac;
        roundedRect(knobX - 3, barY - 3, 6, 6, 3, color(0xFFFFFF, alpha));

        CFontRenderer trackInfoF = FontManager.pf12bold;
        String trackInfo = player.isLoaded()
                ? ("曲目 " + (player.getCurrentTrack() + 1) + " / " + player.getTrackCount())
                : "曲目 - / -";
        trackInfoF.drawString(trackInfo, x + inset, rowY + btn * 0.5 - trackInfoF.getHeight() * 0.5, color(COL_PRIMARY, alpha));

        double smallBtn = 16;
        double rightX = x + w - inset;

        double winValW = 40;
        winPlus.set(rightX - smallBtn, rowY + btn * 0.5 - smallBtn * 0.5, smallBtn, smallBtn);
        winMinus.set(rightX - smallBtn - winValW - smallBtn, rowY + btn * 0.5 - smallBtn * 0.5, smallBtn, smallBtn);
        drawButton(winMinus, "-", mouseX, mouseY, true);
        drawButton(winPlus, "+", mouseX, mouseY, true);
        FontManager.pf12.drawCenteredString(String.format("%.0fms", windowMs), winMinus.x + smallBtn + winValW * 0.5, rowY + btn * 0.5 - FontManager.pf12.getHeight() * 0.5, color(COL_PRIMARY, alpha));
        FontManager.pf12.drawCenteredString("窗口", winMinus.x + smallBtn + winValW * 0.5, rowY - 8, color(COL_SECONDARY, alpha));

        double colValW = 22;
        double colBlockRight = winMinus.x - 14;
        colPlus.set(colBlockRight - smallBtn, rowY + btn * 0.5 - smallBtn * 0.5, smallBtn, smallBtn);
        colMinus.set(colBlockRight - smallBtn - colValW - smallBtn, rowY + btn * 0.5 - smallBtn * 0.5, smallBtn, smallBtn);
        drawButton(colMinus, "-", mouseX, mouseY, true);
        drawButton(colPlus, "+", mouseX, mouseY, true);
        FontManager.pf12.drawCenteredString(String.valueOf(columns), colMinus.x + smallBtn + colValW * 0.5, rowY + btn * 0.5 - FontManager.pf12.getHeight() * 0.5, color(COL_PRIMARY, alpha));
        FontManager.pf12.drawCenteredString("列数", colMinus.x + smallBtn + colValW * 0.5, rowY - 8, color(COL_SECONDARY, alpha));
    }

    private void drawBrowser(double mouseX, double mouseY, double w, double h) {
        Rect.draw(0, 0, w, h, color(0x000000, alpha * 0.45f));

        double pw = Math.min(420, w - 80);
        double ph = Math.min(360, h - 80);
        double px = w * 0.5 - pw * 0.5;
        double py = h * 0.5 - ph * 0.5;

        roundedRect(px, py, pw, ph, 10, color(COL_BG, alpha));

        CFontRenderer titleF = FontManager.pf16bold;
        titleF.drawString("选择 NSF 文件", px + 14, py + 12, color(COL_PRIMARY, alpha));

        double clw = 18;
        browserClose.set(px + pw - clw - 12, py + 10, clw, clw);
        drawButton(browserClose, "x", mouseX, mouseY, true);

        double listX = px + 12;
        double listY = py + 36;
        double listW = pw - 24;
        double listH = ph - 48;

        StencilClipManager.beginClip(() -> Rect.draw(listX, listY, listW, listH, -1));

        browserRows.clear();
        double rowH = 22;
        double oy = listY - browserScroll;

        if (browserFiles.length == 0) {
            FontManager.pf12.drawString("未找到 .nsf 文件 (放入游戏目录或 nsf 文件夹)", listX + 6, listY + 6, color(COL_SECONDARY, alpha));
        }

        for (int i = 0; i < browserFiles.length; i++) {
            double ry = oy + i * rowH;
            Rect2 r = new Rect2();
            r.set(listX, ry, listW, rowH - 2);
            browserRows.add(r);

            if (ry + rowH < listY || ry > listY + listH) {
                continue;
            }

            boolean hov = r.contains(mouseX, mouseY);
            if (hov) {
                roundedRect(r.x, r.y, r.w, r.h, 4, color(COL_HOVER, alpha));
            }
            FontManager.pf12.drawString(FontManager.pf12.trim(browserFiles[i].getName(), listW - 12), listX + 6, ry + rowH * 0.5 - FontManager.pf12.getHeight() * 0.5 - 1, color(COL_PRIMARY, alpha));
        }

        StencilClipManager.endClip();
    }

    private void drawButton(Rect2 r, String text, double mouseX, double mouseY, boolean enabled) {
        boolean hov = enabled && r.contains(mouseX, mouseY);
        roundedRect(r.x, r.y, r.w, r.h, 4, color(hov ? COL_HOVER : COL_PANEL, alpha));
        CFontRenderer f = FontManager.pf14;
        f.drawCenteredString(text, r.x + r.w * 0.5, r.y + r.h * 0.5 - f.getHeight() * 0.5, color(enabled ? COL_PRIMARY : COL_SECONDARY, alpha));
    }

    private void drawIconButton(Rect2 r, String icon, double mouseX, double mouseY) {
        boolean hov = r.contains(mouseX, mouseY);
        roundedRect(r.x, r.y, r.w, r.h, r.h * 0.5, color(hov ? COL_HOVER : COL_PANEL, alpha));
        CFontRenderer f = FontManager.icon25;
        f.drawCenteredString(icon, r.x + r.w * 0.5, r.y + r.h * 0.5 - f.getHeight() * 0.5, color(COL_PRIMARY, alpha));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        if (browserOpen) {
            if (browserClose.contains(mouseX, mouseY)) {
                browserOpen = false;
                return;
            }
            for (int i = 0; i < browserRows.size(); i++) {
                if (browserRows.get(i).contains(mouseX, mouseY)) {
                    File f = browserFiles[i];
                    browserOpen = false;
                    MultiThreadingUtil.runAsync(() -> {
                        if (player.load(f)) {
                            player.play();
                        }
                    });
                    return;
                }
            }
            return;
        }

        if (browseBtn.contains(mouseX, mouseY)) {
            openBrowser();
            return;
        }

        if (linearMixBtn.contains(mouseX, mouseY)) {
            player.setLinearMixing(!player.isLinearMixing());
            return;
        }
        if (triStepsBtn.contains(mouseX, mouseY)) {
            player.setTriangleMoreSteps(!player.isTriangleMoreSteps());
            return;
        }
        if (echoBtn.contains(mouseX, mouseY)) {
            player.setEchoEnabled(!player.isEchoEnabled());
            return;
        }

        if (playBtn.contains(mouseX, mouseY)) {
            player.togglePause();
            return;
        }
        if (prevBtn.contains(mouseX, mouseY)) {
            player.prevTrack();
            return;
        }
        if (nextBtn.contains(mouseX, mouseY)) {
            player.nextTrack();
            return;
        }

        if (colMinus.contains(mouseX, mouseY)) {
            columns = Math.max(1, columns - 1);
            return;
        }
        if (colPlus.contains(mouseX, mouseY)) {
            columns = Math.min(8, columns + 1);
            return;
        }
        if (winMinus.contains(mouseX, mouseY)) {
            windowMs = Math.max(4f, windowMs - 2f);
            return;
        }
        if (winPlus.contains(mouseX, mouseY)) {
            windowMs = Math.min(64f, windowMs + 2f);
            return;
        }

        if (seekBar.contains(mouseX, mouseY) && player.isLoaded()) {
            seeking = true;
            seekPreview = clamp01((mouseX - seekBar.x) / seekBar.w);
        }
    }

    @Override
    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        if (seeking) {
            seekPreview = clamp01((mouseX - seekBar.x) / seekBar.w);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (seeking) {
            seeking = false;
            if (player.isLoaded()) {
                player.seek(seekPreview * assumedDuration);
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (browserOpen) {
                browserOpen = false;
            } else {
                closing = true;
            }
            return;
        }

        if (keyCode == Keyboard.KEY_SPACE && player.isLoaded()) {
            player.togglePause();
        }
    }

    @Override
    public void renderLast(double mouseX, double mouseY) {
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0 && browserOpen) {
            double rowH = 22;
            double maxScroll = Math.max(0, browserFiles.length * rowH - (Math.min(360, RenderSystem.getHeight() - 80) - 48));
            browserScroll = Math.max(0, Math.min(maxScroll, browserScroll - dWheel * 0.25));
        }
    }

    private void openBrowser() {
        browserScroll = 0;
        browserOpen = true;
        MultiThreadingUtil.runAsync(() -> browserFiles = scanNsfFiles());
    }

    private File[] scanNsfFiles() {
        List<File> found = new ArrayList<>();
        File gameDir = mc.mcDataDir;

        File nsfDir = new File(gameDir, "nsf");
        if (!nsfDir.exists()) {
            nsfDir.mkdirs();
        }

        collectNsf(nsfDir, found, 0);
        collectNsf(gameDir, found, -1);

        found.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return found.toArray(new File[0]);
    }

    private void collectNsf(File dir, List<File> out, int depth) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".nsf")) {
                if (!out.contains(f)) {
                    out.add(f);
                }
            } else if (f.isDirectory() && depth >= 0 && depth < 3) {
                collectNsf(f, out, depth + 1);
            }
        }
    }

    private static double clamp01(double v) {
        if (v < 0) {
            return 0;
        }
        return Math.min(v, 1);
    }

    private static String formatTime(double seconds) {
        int s = (int) seconds;
        int m = s / 60;
        s = s % 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    private static final class Rect2 {
        double x, y, w, h;

        void set(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}
