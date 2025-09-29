package tech.konata.phosphate;

import ingameime.IngameIMEJNI;
import lombok.Getter;
import org.lwjgl.system.Platform;
import org.lwjglx.opengl.Display;
import tech.konata.phosphate.event.eventapi.Event;
import tech.konata.phosphate.rendering.async.GLContextUtils;
import tech.konata.phosphate.rendering.ime.Internal;
import tech.konata.phosphate.management.*;
import tech.konata.phosphate.rendering.loading.LoadingRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.utils.dnd.DropTarget;
import tech.konata.phosphate.utils.other.info.Version;
import tech.konata.phosphate.utils.logging.LogLevel;
import tech.konata.phosphate.utils.logging.LogManager;
import tech.konata.phosphate.utils.logging.Logger;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.other.DevUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 11/19/2023
 * <p>
 * 欢迎你，旅行者！
 * 接下来你将看到的是: 脑残开发把好几个方法写在同一个方法里的超级冗杂的大方法、重复的代码、不规范的耦合、多线程不安全代码，以及更多。
 * <p>
 * 优化:
 * 这个客户端的优化确实是很多的 我改了很多渲染底层的东西
 * 1. 移植Phosphor Legacy (光照引擎优化)
 * 2. 移植valkyrie
 * 3. 移除所有Optifine的反射
 * 4. 优化原版Framebuffer的framebufferClear(), 删除不必要的帧缓冲绑定和解绑 #[Framebuffer.framebufferClearNoBind()]
 * 5. 优化GuiIngame的renderGameOverlay(), 将其设置为每秒更新二十次
 * 6. 优化Render2D, 将其设置为每秒更新显示器刷新率次
 * 7. 在java8中使用lambda factory来加快EventBus调用
 * 8. 优化WorldVertexBufferUploader, 优化顶点从cpu传到显卡的逻辑
 * 9. 快速数学运算
 * 10. glDrawArrays() 优化
 * 11. 限制着色器帧数 (高斯模糊, 外发光) 至60fps
 * 12. 不在主线程中轮询鼠标/键盘数据
 *
 * <p>
 * 还有别的什么很多的东西我想不起来了；；；
 * <p>
 * 最后还会过一遍ProGuard的optimization, 优化代码结构和调用
 */
public class Phosphate {

    @Getter
    private static final Phosphate instance = new Phosphate();

    @Getter
    private static final Version version = new Version(2, 1, 0, " Release");

    public static final String NAME = "Phosphate";

    public static String BUILD_DATE = "Dev Environment";

    @Getter
    private final Logger logger = LogManager.getLogger("Phosphate");

    @Getter
    private final List<AbstractManager> managers = new ArrayList<>();

    @Getter
    private FontManager fontManager;

    @Getter
    private EventManager<Event> eventManager;

    @Getter
    private Localizer localizer;

    @Getter
    private ModuleManager moduleManager;

    @Getter
    private WidgetsManager widgetsManager;

    @Getter
    private CommandManager commandManager;

    @Getter
    private ConfigManager configManager;

    @Getter
    private ThemeManager themeManager;

    @Getter
    private boolean clientLoaded = false;

    @Getter
    private final boolean obfuscated = DevUtils.isObfuscated();

    public static boolean POJAVE = false;

    public Phosphate() {
        this.logger.setOverrideLevel(LogLevel.DEBUG);
    }

    /**
     * Called when the client is being initialized.
     */
    public void run() {

        logger.debug("run() 方法 已被调用");

        Display.setTitle(
                String.format(
                        NAME + " %s",
                        Phosphate.getVersion().toString().equals(" Release") ? "" : Phosphate.getVersion().toString()
                )
        );

//        if (Platform.get() == Platform.WINDOWS) {
//            DropTarget.getInstance().registerDropTarget(WindowUtil.getWindowHandle(Display.getWindow()));
//        }

        LoadingRenderer.setProgress(70, NAME + " - Start");

        LoadingRenderer.setProgress(90, NAME + " - Managers");

        this.fontManager = new FontManager();
        this.eventManager = new EventManager<>();
        this.localizer = new Localizer();
        this.moduleManager = new ModuleManager();
        this.widgetsManager = new WidgetsManager();
        this.commandManager = new CommandManager();
        this.configManager = new ConfigManager();
        this.themeManager = new ThemeManager();

        managers.addAll(Arrays.asList(this.fontManager, this.eventManager, this.localizer, this.moduleManager, widgetsManager, commandManager, configManager, themeManager));

        for (AbstractManager manager : this.managers) {

            this.logger.debug("正在调用 {} 的 init() 方法...", manager.getName());
            manager.init();
            EventManager.register(manager);

        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> Phosphate.getInstance().stop()));

        RenderSystem.refreshSkinCache();

        // init ingame ime
        IngameIMEJNI.loadNative();
        if (!IngameIMEJNI.disable)
            Internal.createInputCtx();

        LoadingRenderer.setProgress(100, NAME + " - Finish");

        logger.debug("加载完成!");

        System.gc();
        clientLoaded = true;
    }

    /**
     * Called when the jvm is shutting down.
     */
    public void stop() {
        logger.debug("stop() 方法 已被调用");

        for (AbstractManager manager : this.managers) {

            this.logger.debug("正在调用 {} 的 stop() 方法...", manager.getName());
            manager.stop();
            EventManager.unregister(manager);

        }


    }

}
