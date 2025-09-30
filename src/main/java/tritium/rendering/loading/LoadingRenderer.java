package tritium.rendering.loading;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjglx.opengl.Display;
import tritium.Tritium;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.GLContextUtils;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.loading.screens.NormalLoadingScreen;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.other.SplashGenerator;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author IzumiiKonata
 * @since 4/24/2023 9:57 AM
 */
public class LoadingRenderer {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static final int backgroundColor = RenderSystem.hexColor(0, 0, 0, 255);
    private static final Random random = new Random();
    public static final LoadingScreenRenderer loadingScreenRenderer = getLoadingScreen();
    public static int progress = 0;
    public static String progressText = "";
    public static Thread splashThread;
    public static float alphaMask = 1;
    private static final Animation alphaAnimation = new Animation(Easing.LINEAR, Duration.ofSeconds(1));
    public static boolean waiting = false;
    private static boolean firstFrame = false;
    private static Throwable threadError;
    private static int max_texture_size = -1;

    public static boolean crashDetected = false;

    public static long subWindow;

    public static final Object waitLock = new Object();

    /**
     * choose a loading screen randomly.
     *
     * @return loading screen's instance
     */
    @SneakyThrows
    private static LoadingScreenRenderer getLoadingScreen() {
        return new NormalLoadingScreen();
    }

    @SneakyThrows
    public static void init() {

        if (Tritium.POJAVE) {
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, Display.getWidth(), Display.getHeight(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableAlpha();
            GL11.glEnable(GL_TEXTURE_2D);

            Rect.draw(0, 0, Display.getWidth(), Display.getHeight(), RenderSystem.hexColor(23, 23, 23), Rect.RectType.ABSOLUTE_POSITION);

            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);
            GlStateManager.bindTexture(SplashGenerator.t.getGlTextureId());

            Gui.drawScaledCustomSizeModalRect((Display.getWidth() - SplashGenerator.logo.getWidth()) / 2.0, (Display.getHeight() - SplashGenerator.logo.getHeight()) / 2.0, 0, 0, SplashGenerator.logo.getWidth(), SplashGenerator.logo.getHeight(), SplashGenerator.logo.getWidth(), SplashGenerator.logo.getHeight(), SplashGenerator.logo.getWidth(), SplashGenerator.logo.getHeight());
            return;
        }

        // INIT LOL
        subWindow = GLContextUtils.createContext();
        GLFW.glfwMakeContextCurrent(subWindow);
        GL.createCapabilities();

        splashThread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {

                GLFW.glfwMakeContextCurrent(Display.getWindow());
                GL.createCapabilities();

                initGL();

                loadingScreenRenderer.init();


                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, Display.getWidth(), Display.getHeight(), 0.0D, 1000.0D, 3000.0D);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, 0.0F, -2000.0F);
                GlStateManager.disableLighting();
                GlStateManager.disableFog();
                GlStateManager.disableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableAlpha();
                GL11.glEnable(GL_TEXTURE_2D);

                GL11.glColorMask(true, true, true, true);

                while (true) {

                    if (Display.wasResized()) {
                        initGL();
                    }

                    if (Display.isCloseRequested()) {
                        System.exit(0);
                    }

                    synchronized (RenderSystem.ASYNC_LOCK) {
                        glClear(GL_COLOR_BUFFER_BIT);

                        if (!firstFrame) {
                            firstFrame = true;
                            RenderSystem.setFrameDeltaTime(0);
                        }


                        Interpolations.calcFrameDelta();

                        int width = Display.getWidth();
                        int height = Display.getHeight();

                        loadingScreenRenderer.render(Display.getWidth(), Display.getHeight());

                        alphaMask = (float) alphaAnimation.run(0f);

                        Rect.draw(0, 0, width, height, RenderSystem.hexColor(0, 0, 0, (int) (alphaMask * 255)), Rect.RectType.EXPAND);

//                        GlStateManager.enableAlpha();
//                        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                        Display.update();
                        Display.sync(240);
                    }

                    if (waiting && loadingScreenRenderer.isLoadingScreenFinished()) {

                        if (mc == null)
                            mc = Minecraft.getMinecraft();

                        mc.displayWidth = Display.getWidth();
                        mc.displayHeight = Display.getHeight();
                        mc.resize(mc.displayWidth, mc.displayHeight);
                        glClearColor(1, 1, 1, 1);
                        glEnable(GL_DEPTH_TEST);
                        glDepthFunc(GL_LEQUAL);
                        glEnable(GL_ALPHA_TEST);
                        glAlphaFunc(GL_GREATER, .1f);

                        GLFW.glfwMakeContextCurrent(0L);

                        synchronized (notifyLock) {
                            notifyLock.notifyAll();
                        }

                        break;
                    }
                }
            }

            private void initGL() {
                glClearColor((float) ((backgroundColor >> 16) & 0xFF) / 0xFF, (float) ((backgroundColor >> 8) & 0xFF) / 0xFF, (float) (backgroundColor & 0xFF) / 0xFF, 1);
                glDisable(GL_LIGHTING);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }

        }, "Loading Screen Thread");

        splashThread.setUncaughtExceptionHandler((t, e) -> {
            threadError = e;
            e.printStackTrace();
        });
        splashThread.setPriority(Thread.MAX_PRIORITY);
        splashThread.start();
        checkThreadState();
    }

    private static void checkThreadState() {
        if (splashThread.getState() == Thread.State.TERMINATED || threadError != null) {
            throw new IllegalStateException("Loading Screen thread", threadError);
        }
    }

    @SneakyThrows
    public static void hide() {
//        hide = true;

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.cullFace(1029);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        mc.displayWidth = Display.getWidth();
        mc.displayHeight = Display.getHeight();
        mc.resize(mc.displayWidth, mc.displayHeight);
        glClearColor(1, 1, 1, 1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, .1f);

    }

    static final Object notifyLock = new Object();

    @SneakyThrows
    public static void notifyGameLoaded() {

        if (Tritium.POJAVE)
            return;

        if (!crashDetected && threadError == null) {
            loadingScreenRenderer.onGameLoadFinishedNotify();

            waiting = true;

//            while (true) {
//                GLFW.glfwPollEvents();
//            }

            synchronized (notifyLock) {
                notifyLock.wait();
            }

            GLFW.glfwMakeContextCurrent(Display.getWindow());
            hide();
        }
    }

    @SneakyThrows
    public static void show() {
//        hide = false;
        waiting = false;
        alphaMask = 1;

        GLFW.glfwMakeContextCurrent(0L);

        synchronized (waitLock) {
            waitLock.notifyAll();
        }
    }

    private static int getMaxTextureSize() {
        if (max_texture_size != -1) return max_texture_size;
        for (int i = 0x4000; i > 0; i >>= 1) {
            GL11.glTexImage2D(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_RGBA, i, i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            if (GL11.glGetTexLevelParameteri(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH) != 0) {
                max_texture_size = i;
                return i;
            }
        }
        return -1;
    }


    @SneakyThrows
    public static void setProgress(int progress, String detail) {
        LoadingRenderer.progress = progress;
        LoadingRenderer.progressText = detail;

    }

}
