package tritium;

import ingameime.IngameIMEJNI;
import lombok.Getter;
import org.lwjgl.opengl.Display;
import tritium.rendering.ime.IngameIMERenderer;
import tritium.management.*;
import tritium.rendering.loading.LoadingRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.screens.ConsoleScreen;
import tritium.settings.ClientSettings;
import tritium.utils.other.info.BuildInfo;
import tritium.utils.other.info.UpdateChecker;
import tritium.utils.other.info.Version;
import tritium.utils.logging.LogLevel;
import tritium.utils.logging.LogManager;
import tritium.utils.logging.Logger;
import tritium.utils.other.DevUtils;
import tritium.utils.other.info.VersionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Tritium {

    @Getter
    private static Version version;

    public static final String NAME = "Tritium";

    @Getter
    private static final Tritium instance = new Tritium();

    @Getter
    private static final Logger logger = LogManager.getLogger("Tritium");

    @Getter
    private final List<AbstractManager> managers = new ArrayList<>();

    @Getter
    private FontManager fontManager;

    @Getter
    private EventManager eventManager;

    @Getter
    private Localizer localizer;

    @Getter
    private ModuleManager moduleManager;

    @Getter
    private WidgetsManager widgetsManager;

    @Getter
    private CommandManager commandManager;

    @Getter
    private ExtensionManager extensionManager;

    @Getter
    private ConfigManager configManager;

    @Getter
    private ThemeManager themeManager;

    @Getter
    private NCMManager ncmManager;

    @Getter
    private boolean clientLoaded = false;

    @Getter
    private final boolean obfuscated = DevUtils.isObfuscated();

    public Tritium() {

    }

    public void run() {

        logger.debug("run() 方法 已被调用");

        Version ver = Tritium.getVersion();

        Display.setTitle(
            String.format(
                NAME + "-X %s",
                ver.toString()
            )
        );

//        if (Platform.get() == Platform.WINDOWS) {
//            DropTarget.getInstance().registerDropTarget(WindowUtil.getWindowHandle(Display.getWindow()));
//        }

        LoadingRenderer.setProgress(90, NAME + " - Managers");

        this.fontManager = new FontManager();
        this.eventManager = new EventManager();
        this.localizer = new Localizer();
        this.moduleManager = new ModuleManager();
        this.widgetsManager = new WidgetsManager();
        this.commandManager = new CommandManager();
        this.extensionManager = new ExtensionManager();
        this.configManager = new ConfigManager();
        this.themeManager = new ThemeManager();
        this.ncmManager = new NCMManager();

        managers.addAll(Arrays.asList(this.fontManager, this.eventManager, this.localizer, this.moduleManager, widgetsManager, extensionManager, commandManager, configManager, themeManager, ncmManager));

        for (AbstractManager manager : this.managers) {

            logger.debug("正在调用 {} 的 init() 方法...", manager.getName());
            manager.init();
            EventManager.register(manager);

        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> Tritium.getInstance().stop()));

        RenderSystem.refreshSkinCache();

        // init ingame ime
        IngameIMEJNI.loadNative();
        if (IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue())
            IngameIMERenderer.createInputCtx();

        LoadingRenderer.setProgress(100, NAME + " - Finish");

        logger.debug("加载完成!");

        System.gc();
        clientLoaded = true;

        ConsoleScreen.log("[Tritium] Tritium {}", ver.toString());
        UpdateChecker.check();
    }

    public void stop() {
        logger.debug("stop() 方法 已被调用");

        for (AbstractManager manager : this.managers) {

            logger.debug("正在调用 {} 的 stop() 方法...", manager.getName());
            manager.stop();
            EventManager.unregister(manager);

        }
    }

    static {
        // 获取 version.json
        Optional<BuildInfo> buildInfo = VersionUtils.getBuildInfo();

        buildInfo
            .ifPresentOrElse(
                info -> {
                    String[] splitVer = info.getVersion().split("\\.");
                    int major = Integer.parseInt(splitVer[0]);
                    int minor = Integer.parseInt(splitVer[1]);
                    int patch = Integer.parseInt(splitVer[2]);

                    // 如果存在那么是发行版
                    version = new Version(Version.ReleaseType.Release, major, minor, patch);
                },
                () -> {
                    // 否则就是开发环境
                    String branch = VersionUtils.getCurrentBranch();
                    String currentCommit = VersionUtils.getCurrentCommitShort();
                    version = new Version(currentCommit, branch);
                    logger.setOverrideLevel(LogLevel.DEBUG);

                    DevUtils.registerDevCommand();
                }
            );
    }

}
