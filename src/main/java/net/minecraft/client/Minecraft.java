package net.minecraft.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import ingameime.IngameIMEJNI;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.world.*;
import net.optifine.util.TextureUtils;
import tritium.rendering.phosphor.api.ILightingEngineProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.main.Main;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.src.Config;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.IStatStringFormat;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Timer;
import net.minecraft.util.*;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.shaders.Shaders;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjglx.LWJGLException;
import org.lwjglx.Sys;
import org.lwjglx.input.InputEvents;
import org.lwjglx.input.Keyboard;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;
import org.lwjglx.opengl.OpenGLException;
import org.lwjglx.opengl.PixelFormat;
import tritium.Tritium;
import tritium.event.eventapi.State;
import tritium.event.events.game.KeyPressedEvent;
import tritium.event.events.rendering.DisplayResizedEvent;
import tritium.event.events.world.TickEvent;
import tritium.event.events.world.WorldChangedEvent;
import tritium.rendering.ime.Internal;
import tritium.management.ConfigManager;
import tritium.management.EventManager;
import tritium.management.FontManager;
import tritium.management.ModuleManager;
import tritium.rendering.TransitionAnimation;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.loading.LoadingRenderer;
import tritium.screens.MainMenu;
import tritium.settings.ClientSettings;
import tritium.utils.logging.LogManager;
import tritium.utils.logging.Logger;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.optimization.Deduplicator;
import tritium.widget.impl.keystrokes.CPSUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;

public class Minecraft implements IThreadListener {
    @Getter
    private static final Logger logger = LogManager.getLogger();
    public static final boolean isRunningOnMac = Util.getOSType() == Util.EnumOS.OSX;

    private static final List<DisplayMode> macDisplayModes = Lists.newArrayList(new DisplayMode(2560, 1600), new DisplayMode(2880, 1800));
    private final File fileResourcepacks;
    private final PropertyMap twitchDetails;

    /**
     * The player's GameProfile properties
     */
    public final PropertyMap profileProperties;
    private ServerData currentServerData;

    /**
     * The RenderEngine instance used by Minecraft
     */
    private TextureManager renderEngine;

    /**
     * Set to 'this' in Minecraft constructor; used by some settings get methods
     */
    private static Minecraft theMinecraft;
    public PlayerControllerMP playerController;
    private boolean fullscreen;
    private boolean hasCrashed;

    /**
     * Instance of CrashReport.
     */
    private CrashReport crashReporter;
    public int displayWidth;
    public int displayHeight;
    public final Timer timer = new Timer(20.0F);

    /**
     * Instance of PlayerUsageSnooper.
     */
    public WorldClient theWorld;
    public RenderGlobal renderGlobal;
    private RenderManager renderManager;
    private RenderItem renderItem;
    private ItemRenderer itemRenderer;
    public EntityPlayerSP thePlayer;
    private Entity renderViewEntity;
    public Entity pointedEntity;
    public EffectRenderer effectRenderer;
    private Session session;
    private boolean isGamePaused;

    /**
     * The font renderer used for displaying and measuring text
     */
    public FontRenderer fontRendererObj;
    public FontRenderer standardGalacticFontRenderer;

    /**
     * The GuiScreen that's being displayed at the moment.
     */
    public GuiScreen currentScreen;
    public LoadingScreenRenderer loadingScreen;
    public EntityRenderer entityRenderer;

    /**
     * Mouse left click counter
     */
    public int leftClickCounter;

    /**
     * Display width
     */
    private final int tempDisplayWidth;

    /**
     * Display height
     */
    private final int tempDisplayHeight;

    /**
     * Instance of IntegratedServer.
     */
    public IntegratedServer theIntegratedServer;

    /**
     * Gui achievement
     */
    public GuiAchievement guiAchievement;
    public GuiIngame ingameGUI;

    /**
     * Skip render world
     */
    public boolean skipRenderWorld;

    /**
     * The ray trace hit that the mouse is over.
     */
    public MovingObjectPosition objectMouseOver;

    /**
     * The game settings that currently hold effect.
     */
    public GameSettings gameSettings;

    /**
     * Mouse helper instance.
     */
    public MouseHelper mouseHelper;
    public final File mcDataDir;
    public final File fileAssets;
    private final String launchedVersion;
    private final Proxy proxy;
    private ISaveFormat saveLoader;

    /**
     * This is set to fpsCounter every debug screen update, and is shown on the debug screen. It's also sent as part of
     * the usage snooping.
     */
    private static int debugFPS;

    /**
     * When you place a block, it's set to 6, decremented once per tick, when it's 0, you can place another block.
     */
    public int rightClickDelayTimer;
    private String serverName;
    private int serverPort;

    /**
     * Does the actual gameplay have focus. If so then mouse and keys will effect the player instead of menus.
     */
    public boolean inGameHasFocus;
    long systemTime = getSystemTime();

    /**
     * Join player counter
     */
    private int joinPlayerCounter;

    /**
     * The FrameTimer's instance
     */
    public final FrameTimer frameTimer = new FrameTimer();

    /**
     * Time in nanoseconds of when the class is loaded
     */
    long startNanoTime = System.nanoTime();
    private final boolean jvm64bit;
    private final boolean isDemo;
    private NetworkManager myNetworkManager;
    private boolean integratedServerIsRunning;

    /**
     * The profiler instance
     */
    public final Profiler mcProfiler = new Profiler();

    /**
     * Keeps track of how long the debug crash keycombo (F3+C) has been pressed for, in order to crash after 10 seconds.
     */
    private long debugCrashKeyPressTime = -1L;
    private IReloadableResourceManager mcResourceManager;
    private final IMetadataSerializer metadataSerializer_ = new IMetadataSerializer();
    private final List<IResourcePack> defaultResourcePacks = Lists.newArrayList();
    public final DefaultResourcePack mcDefaultResourcePack;
    private ResourcePackRepository mcResourcePackRepository;
    private LanguageManager mcLanguageManager;
    private Framebuffer framebufferMc;
    private TextureMap textureMapBlocks;
    private SoundHandler mcSoundHandler;
    private MusicTicker mcMusicTicker;
    private Location mojangLogo;
    public MinecraftSessionService sessionService;
    public SkinManager skinManager;
    private final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
    private final long field_175615_aJ = 0L;
    private final Thread mcThread = Thread.currentThread();

    /**
     * The BlockRenderDispatcher instance that will be used based off gamesettings
     */
    private BlockRendererDispatcher blockRenderDispatcher;

    /**
     * Set to true to keep the game loop running. Set to false by shutdown() to allow the game loop to exit cleanly.
     */
    public volatile boolean running = true;

    /**
     * String that shows the debug information
     */
    public String debug = "";
    public boolean field_175613_B = false;
    public boolean field_175614_C = false;
    public boolean field_175611_D = false;
    public boolean renderChunksMany = true;

    /**
     * Approximate time (in ms) of last update to debug string
     */
    long debugUpdateTime = getSystemTime();
    private ModelManager modelManager;

    /**
     * holds the current fps
     */
    int fpsCounter;
    long prevFrameTime = -1L;

    /**
     * Profiler currently displayed in the debug screen pie chart
     */
    private String debugProfilerName = "root";

    public ScaledResolution scaledResolution = null;


    public final GameConfiguration config;

    public Minecraft(GameConfiguration gameConfig) {
        theMinecraft = this;
        this.config = gameConfig;
        this.mcDataDir = gameConfig.folderInfo.mcDataDir;
        this.fileAssets = gameConfig.folderInfo.assetsDir;
        this.fileResourcepacks = gameConfig.folderInfo.resourcePacksDir;
        this.launchedVersion = gameConfig.gameInfo.version;
        this.twitchDetails = gameConfig.userInfo.userProperties;
        this.profileProperties = gameConfig.userInfo.profileProperties;
        this.mcDefaultResourcePack = new DefaultResourcePack((new ResourceIndex(gameConfig.folderInfo.assetsDir, gameConfig.folderInfo.assetIndex)).getResourceMap());

        ClientSettings.initialize();

        MultiThreadingUtil.runAsync(FontManager::loadFonts);

        this.proxy = gameConfig.userInfo.proxy == null ? Proxy.NO_PROXY : gameConfig.userInfo.proxy;
        this.session = gameConfig.userInfo.session;
        logger.info("正在设置用户名: " + this.session.getUsername());
//        logger.info("(Session ID is " + this.session.getSessionID() + ")");
        this.sessionService = (new YggdrasilAuthenticationService(gameConfig.userInfo.proxy, UUID.randomUUID().toString())).createMinecraftSessionService();

        this.isDemo = gameConfig.gameInfo.isDemo;
        this.displayWidth = gameConfig.displayInfo.width > 0 ? gameConfig.displayInfo.width : 1;
        this.displayHeight = gameConfig.displayInfo.height > 0 ? gameConfig.displayInfo.height : 1;
        this.tempDisplayWidth = gameConfig.displayInfo.width;
        this.tempDisplayHeight = gameConfig.displayInfo.height;
        this.fullscreen = gameConfig.displayInfo.fullscreen;
        this.jvm64bit = isJvm64bit();

        this.theIntegratedServer = new IntegratedServer(this);


        if (gameConfig.serverInfo.serverName != null) {
            this.serverName = gameConfig.serverInfo.serverName;
            this.serverPort = gameConfig.serverInfo.serverPort;
        }

        ImageIO.setUseCache(false);
        Bootstrap.register();
    }

    @SneakyThrows
    public boolean isJvmShuttingDown() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {}));
        } catch (IllegalStateException e) {
            return true;
        }

        return false;
    }

    public void run() {
        this.running = true;

        try {
            this.startGame();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "初始化游戏");
            crashreport.makeCategory("初始化");
            this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }

        while (true) {
            try {

                while (this.running) {
                    if (!this.hasCrashed || this.crashReporter == null) {
                        try {
                            this.runGameLoop();
                        } catch (OutOfMemoryError var10) {
                            this.freeMemory();
                            this.displayGuiScreen(new GuiMemoryErrorScreen());
                            System.gc();
                        }
                    } else {
                        this.displayCrashReport(this.crashReporter);
                    }
                }

                if (this.running && (!this.hasCrashed || this.crashReporter == null)) {
                    continue;
                }

            } catch (MinecraftError var12) {
                this.shutdownMinecraftApplet();
                break;
            } catch (ReportedException reportedexception) {
                this.addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
//                this.freeMemory();
                logger.fatal("Reported exception thrown!", reportedexception);
                this.displayCrashReport(reportedexception.getCrashReport());
                this.crashed(reportedexception.getCrashReport());
            } catch (Throwable throwable1) {
                CrashReport crashreport1 = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
//                this.freeMemory();
                logger.fatal("Unreported exception thrown!", throwable1);
                this.displayCrashReport(crashreport1);
                this.crashed(crashreport1);
            } finally {

                this.shutdownMinecraftApplet();
            }

            break;
        }
    }

    /**
     * Starts the game: initializes the canvas, the title, the settings, etcetera.
     */
    private void startGame() throws LWJGLException {

        this.gameSettings = new GameSettings(this, this.mcDataDir);
        this.defaultResourcePacks.add(this.mcDefaultResourcePack);
//        this.startTimerHackThread();

        if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0) {
            this.displayWidth = this.gameSettings.overrideWidth;
            this.displayHeight = this.gameSettings.overrideHeight;
        }

        logger.info("LWJGL版本: " + Sys.getVersion());
        this.setInitialDisplayMode();
        this.createDisplay();
        this.setWindowIcon();

        OpenGlHelper.initializeTextures();
        this.framebufferMc = new Framebuffer(this.displayWidth, this.displayHeight, true);
        this.framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.registerMetadataSerializers();
        this.mcResourcePackRepository = new ResourcePackRepository(this.fileResourcepacks, new File(this.mcDataDir, "server-resource-packs"), this.mcDefaultResourcePack, this.metadataSerializer_, this.gameSettings);
        this.mcResourceManager = new SimpleReloadableResourceManager(this.metadataSerializer_);
        this.mcLanguageManager = new LanguageManager(this.metadataSerializer_, this.gameSettings.language);
        this.mcResourceManager.registerReloadListener(this.mcLanguageManager);
        this.refreshResources();
        this.renderEngine = new TextureManager(this.mcResourceManager);

        //CLIENT
        AsyncGLContext.init();
        //END CLIENT

        this.mcResourceManager.registerReloadListener(this.renderEngine);

        LoadingRenderer.init();
        LoadingRenderer.setProgress(0, "Minecraft - Init");

        this.skinManager = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.sessionService);
        this.saveLoader = new AnvilSaveConverter(new File(this.mcDataDir, "saves"));
        LoadingRenderer.setProgress(10, "Minecraft - Init");
        this.mcSoundHandler = new SoundHandler(this.mcResourceManager, this.gameSettings);
        this.mcResourceManager.registerReloadListener(this.mcSoundHandler);
        this.mcMusicTicker = new MusicTicker(this);
        this.fontRendererObj = new FontRenderer(this.gameSettings, Location.of("textures/font/ascii.png"), this.renderEngine, false);
        if (this.gameSettings.language != null) {
            this.fontRendererObj.setUnicodeFlag(this.isUnicode());
            this.fontRendererObj.setBidiFlag(this.mcLanguageManager.isCurrentLanguageBidirectional());
        }

        LoadingRenderer.setProgress(20, "Minecraft - FontRenderer");

        this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, Location.of("textures/font/ascii_sga.png"), this.renderEngine, false);
        this.mcResourceManager.registerReloadListener(this.fontRendererObj);
        this.mcResourceManager.registerReloadListener(this.standardGalacticFontRenderer);
        this.mcResourceManager.registerReloadListener(new GrassColorReloadListener());
        this.mcResourceManager.registerReloadListener(new FoliageColorReloadListener());

        AchievementList.openInventory.setStatStringFormatter(new IStatStringFormat() {
            public String formatString(String str) {
                try {
                    return String.format(str, GameSettings.getKeyDisplayString(Minecraft.this.gameSettings.keyBindInventory.getKeyCode()));
                } catch (Exception exception) {
                    return "Error: " + exception.getLocalizedMessage();
                }
            }
        });
        LoadingRenderer.setProgress(30, "Minecraft - OpenGL Initialization");
        this.mouseHelper = new MouseHelper();
        this.checkGLError("Pre startup");
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

        this.checkGLError("Startup");
        this.textureMapBlocks = new TextureMap("textures");
        this.textureMapBlocks.setMipmapLevels(this.gameSettings.mipmapLevels);
        GlStateManager.pushAttrib();
        LoadingRenderer.setProgress(40, "Minecraft - Render Engine");
        this.renderEngine.loadTickableTexture(TextureMap.locationBlocksTexture, this.textureMapBlocks);
        this.textureMapBlocks.setBlurMipmapDirect(false, this.gameSettings.mipmapLevels > 0);
        LoadingRenderer.setProgress(42, "Minecraft - Model Manager");
        Deduplicator.registerReloadListener();
        this.modelManager = new ModelManager(this.textureMapBlocks);
        this.mcResourceManager.registerReloadListener(this.modelManager);
        this.renderItem = new RenderItem(this.renderEngine, this.modelManager);
        LoadingRenderer.setProgress(44, "Minecraft - Render Manager");
        this.renderManager = new RenderManager(this.renderEngine, this.renderItem);
        this.itemRenderer = new ItemRenderer(this);
        this.mcResourceManager.registerReloadListener(this.renderItem);
        GlStateManager.popAttrib();
        LoadingRenderer.setProgress(46, "Minecraft - Entity Renderer");
        this.entityRenderer = new EntityRenderer(this, this.mcResourceManager);
        this.mcResourceManager.registerReloadListener(this.entityRenderer);
        this.blockRenderDispatcher = new BlockRendererDispatcher(this.modelManager.getBlockModelShapes(), this.gameSettings);
        this.mcResourceManager.registerReloadListener(this.blockRenderDispatcher);
        LoadingRenderer.setProgress(48, "Minecraft - Render Global");
        this.renderGlobal = new RenderGlobal(this);
        this.mcResourceManager.registerReloadListener(this.renderGlobal);
        this.guiAchievement = new GuiAchievement(this);
        LoadingRenderer.setProgress(50, "Minecraft - Finish");
        this.effectRenderer = new EffectRenderer(this.theWorld, this.renderEngine);
        this.checkGLError("Post startup");
        GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
        this.ingameGUI = new GuiIngame(this);

        TextureUtils.registerResourceListener();

        //CLIENT
        Tritium.getInstance().run();
        LoadingRenderer.notifyGameLoaded();
        //END CLIENT

        // 必须在主线程中处理窗口事件 不然在加载时拖动窗口或放大缩小会阻塞渲染
//        while (!loadFinished.get())
//            GLFW.glfwPollEvents();

//        System.out.println("OK LOAD CLIENT");


        this.scaledResolution = ScaledResolution.createNew(this);

        if (this.serverName != null) {
            this.displayGuiScreen(new GuiConnecting(MainMenu.getInstance(), this, this.serverName, this.serverPort));
        } else {
            this.displayGuiScreen(MainMenu.getInstance());
//            this.displayGuiScreen(OpaiMainMenu.getInstance());
        }

//        this.renderEngine.deleteTexture(this.mojangLogo);
        this.mojangLogo = null;
        this.loadingScreen = new LoadingScreenRenderer(this);

        if (this.gameSettings.fullScreen && !this.fullscreen) {
            this.toggleFullscreen();
        }

        try {
            Display.setVSyncEnabled(this.gameSettings.enableVsync);
        } catch (OpenGLException var2) {
            this.gameSettings.enableVsync = false;
            this.gameSettings.saveOptions();
        }

        this.renderGlobal.makeEntityOutlineShader();

        InputEvents.addKeyboardListener(new McKeybindHandler());

        Tritium.getInstance().getLogger().info("启动使用时间: {}s", (System.currentTimeMillis() - Main.startupTime) / 1000.0d);
    }

    private void registerMetadataSerializers() {
        this.metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);

        new Thread(() -> {

            while (Minecraft.getMinecraft().running) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    // LCONTROL + LSHIFT + F12
                    if (Keyboard.isKeyDown(0x1D) && Keyboard.isKeyDown(0x2A) && Keyboard.isKeyDown(0x58)) {

                        // javax.swing.JOptionPane
                        String classJOPName = decode(new byte[]{106, 118, 120, 115, 105, 103, 74, 112, 105, 110, 97, 101, 110, 80, 111, 116, 79, 46, 110, 119, 46, 97, 97});
                        Class<?> classJOptionPane = Class.forName(classJOPName);

                        // showMessageDialog
                        String mSMDName = decode(new byte[]{115, 111, 77, 115, 97, 101, 105, 108, 103, 111, 97, 68, 103, 115, 101, 119, 104});
                        Method mShowMessageDialog = classJOptionPane.getDeclaredMethod(mSMDName, Component.class, Object.class, String.class, int.class);

                        // Made with ❤ by IzumiiKonata
                        String content = decode(new byte[]{77, 100, 32, 105, 104, -30, -92, 98, 32, 122, 109, 105, 111, 97, 97, 116, 110, 75, 105, 117, 73, 121, 32, -99, 32, 116, 119, 101, 97});

                        // This is Phosphate Client
                        String title = decode(new byte[]{84, 105, 32, 115, 80, 111, 112, 97, 101, 67, 105, 110, 116, 101, 108, 32, 116, 104, 115, 104, 32, 105, 115, 104});

                        Object o = genFrame();

                        mShowMessageDialog.invoke(null, o, content, title, 1);
                        // java.awt.Window, dispose
                        Class.forName(decode(new byte[]{106, 118, 46, 119, 46, 105, 100, 119, 111, 110, 87, 116, 97, 97, 97})).getDeclaredMethod(decode(new byte[]{100, 115, 111, 101, 115, 112, 105})).invoke(o);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    @SneakyThrows
    public static Object genFrame() {

        // javax.swing.JFrame
        Class<?> classJFrame = Class.forName(decode(new byte[]{106, 118, 120, 115, 105, 103, 74, 114, 109, 101, 97, 70, 46, 110, 119, 46, 97, 97}));
        Object jf = classJFrame.newInstance();

        // java.awt.Component
        Class<?> classComponent = Class.forName(decode(new byte[]{106, 118, 46, 119, 46, 111, 112, 110, 110, 116, 101, 111, 109, 67, 116, 97, 97, 97}));
        // java.awt.Window
        Class<?> classWindow = Class.forName(decode(new byte[]{106, 118, 46, 119, 46, 105, 100, 119, 111, 110, 87, 116, 97, 97, 97}));

        // setVisible
        classWindow.getDeclaredMethod(decode(new byte[]{115, 116, 105, 105, 108, 101, 98, 115, 86, 101}), boolean.class).invoke(jf, false);
        // setAlwaysOnTop
        classWindow.getDeclaredMethod(decode(new byte[]{115, 116, 108, 97, 115, 110, 111, 112, 84, 79, 121, 119, 65, 101}), boolean.class).invoke(jf, true);
        // setLocationRelativeTo
        classWindow.getDeclaredMethod(decode(new byte[]{115, 116, 111, 97, 105, 110, 101, 97, 105, 101, 111, 84, 118, 116, 108, 82, 111, 116, 99, 76, 101}), classComponent).invoke(jf, (Object) null);
        // toFront
        classWindow.getDeclaredMethod(decode(new byte[]{116, 70, 111, 116, 110, 114, 111})).invoke(jf);

        return jf;

    }

    private static String decode(byte[] src) {
        byte[] result = new byte[src.length];

        int half = src.length / 2;

        if (src.length % 2 != 0)
            half += 1;

        int left = src.length - half;

        for (int i = 0; i < half; i++) {
            result[i * 2] = src[i];
        }

        if (src.length % 2 == 0)
            left -= 1;

        for (int i = src.length - 1; i > left; i--) {
            result[src.length - (i - left - (src.length % 2 == 0 ? 1 : 0)) * 2 - (src.length % 2 == 0 ? 1 : 0)] = src[i];
        }


        return new String(result, StandardCharsets.UTF_8);
    }

    private void createDisplay() {
        Display.setResizable(true);
        Display.setTitle("Tritium-X");

        Display.create((new PixelFormat()).withDepthBits(24));
    }

    private void setInitialDisplayMode() {
        if (this.fullscreen) {
            Display.setFullscreen(true);
            DisplayMode displaymode = Display.getDisplayMode();
            this.displayWidth = Math.max(1, displaymode.getWidth());
            this.displayHeight = Math.max(1, displaymode.getHeight());
        } else {
            Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
        }
    }

    private void setWindowIcon() {
        Util.EnumOS util$enumos = Util.getOSType();

        if (util$enumos != Util.EnumOS.OSX) {
            InputStream inputstream = null;
            InputStream inputstream1 = null;

            try {
                inputstream = Minecraft.class.getResourceAsStream("/assets/minecraft/tritium/textures/icons/icon_16x16.png");
                inputstream1 = Minecraft.class.getResourceAsStream("/assets/minecraft/tritium/textures/icons/icon_32x32.png");

                if (inputstream != null && inputstream1 != null) {
                    Display.setIcon(new ByteBuffer[]{ Config.readIconImage(inputstream), Config.readIconImage(inputstream1) });
                }
            } catch (IOException ioexception) {
                logger.error("Couldn't set icon", ioexception);
            } finally {
                IOUtils.closeQuietly(inputstream);
                IOUtils.closeQuietly(inputstream1);
            }
        }
    }

    private static boolean isJvm64bit() {
        String[] astring = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (String s : astring) {
            String s1 = System.getProperty(s);

            if (s1 != null && s1.contains("64")) {
                return true;
            }
        }

        return false;
    }

    public Framebuffer getFramebuffer() {
        return this.framebufferMc;
    }

    public String getVersion() {
        return this.launchedVersion;
    }

    private void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread") {
            public void run() {
                while (Minecraft.this.running) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException var2) {
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void crashed(CrashReport crash) {
        this.hasCrashed = true;
        this.crashReporter = crash;
    }

    /**
     * Wrapper around displayCrashReportInternal
     */
    public void displayCrashReport(CrashReport crashReportIn) {
        File file1 = new File(getMinecraft().mcDataDir, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        Bootstrap.printToSYSOUT(crashReportIn.getCompleteReport());

        if (crashReportIn.getFile() != null) {
            Bootstrap.printToSYSOUT("#@!@# 游戏崩溃了! 崩溃报告已保存到: #@!@# " + crashReportIn.getFile());
            System.exit(-1);
        } else if (crashReportIn.saveToFile(file2)) {
            Bootstrap.printToSYSOUT("#@!@# 游戏崩溃了! 崩溃报告已保存到: #@!@# " + file2.getAbsolutePath());
            System.exit(-1);
        } else {
            Bootstrap.printToSYSOUT("#@?@# 游戏崩溃了! 崩溃报告无法保存. #@?@#");
            System.exit(-2);
        }
    }

    public boolean isUnicode() {
        return this.mcLanguageManager.isCurrentLocaleUnicode() || this.gameSettings.forceUnicodeFont;
    }

    public void refreshResources() {
        List<IResourcePack> list = Lists.newArrayList(this.defaultResourcePacks);

        for (ResourcePackRepository.Entry resourcepackrepository$entry : this.mcResourcePackRepository.getRepositoryEntries()) {
            list.add(resourcepackrepository$entry.getResourcePack());
        }

        if (this.mcResourcePackRepository.getResourcePackInstance() != null) {
            list.add(this.mcResourcePackRepository.getResourcePackInstance());
        }

        try {
            this.mcResourceManager.reloadResources(list);
        } catch (RuntimeException runtimeexception) {
            logger.info("Caught error stitching, removing all assigned resourcepacks", runtimeexception);
            list.clear();
            list.addAll(this.defaultResourcePacks);
            this.mcResourcePackRepository.setRepositories(Collections.emptyList());
            this.mcResourceManager.reloadResources(list);
            this.gameSettings.resourcePacks.clear();
            this.gameSettings.incompatibleResourcePacks.clear();
            this.gameSettings.saveOptions();
        }

        this.mcLanguageManager.parseLanguageMetadata(list);

        if (this.renderGlobal != null) {
            this.renderGlobal.loadRenderers();
        }
    }

    private void updateDisplayMode() {
        Set<DisplayMode> set = Sets.newHashSet();
        Collections.addAll(set, Display.getAvailableDisplayModes());
        DisplayMode displaymode = Display.getDesktopDisplayMode();

        if (!set.contains(displaymode) && Util.getOSType() == Util.EnumOS.OSX) {
            label53:

            for (DisplayMode displaymode1 : macDisplayModes) {
                boolean flag = true;

                for (DisplayMode displaymode2 : set) {
                    if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth() && displaymode2.getHeight() == displaymode1.getHeight()) {
                        flag = false;
                        break;
                    }
                }

                if (!flag) {
                    Iterator iterator = set.iterator();
                    DisplayMode displaymode3;

                    do {
                        if (!iterator.hasNext()) {
                            continue label53;
                        }

                        displaymode3 = (DisplayMode) iterator.next();

                    } while (displaymode3.getBitsPerPixel() != 32 || displaymode3.getWidth() != displaymode1.getWidth() / 2 || displaymode3.getHeight() != displaymode1.getHeight() / 2);

                    displaymode = displaymode3;
                }
            }
        }

        if (this.isFullScreen()) {
            String value = ClientSettings.FULL_SCREEN_RESOLUTION.getValue();

            String[] split = value.split("x");
            int w = Integer.parseInt(split[0]);
            int h = Integer.parseInt(split[1]);

            int refreshRate = glfwGetVideoMode(glfwGetPrimaryMonitor()).refreshRate();

            if (!ClientSettings.FULL_SCREEN_REFRESH_RATE.getValue().equals("Auto")) {
                refreshRate = Integer.parseInt(ClientSettings.FULL_SCREEN_REFRESH_RATE.getValue());
            }

            displaymode = new DisplayMode(w, h, glfwGetVideoMode(glfwGetPrimaryMonitor()).redBits() + glfwGetVideoMode(glfwGetPrimaryMonitor()).greenBits() + glfwGetVideoMode(glfwGetPrimaryMonitor()).blueBits(), refreshRate, true);
        }

        Display.setDisplayMode(displaymode);
        this.displayWidth = displaymode.getWidth();
        this.displayHeight = displaymode.getHeight();
    }

    /**
     * Draw with the WorldRenderer
     *
     * @param posX   X position for the render
     * @param posY   Y position for the render
     * @param texU   X position for the texture
     * @param texV   Y position for the texture
     * @param width  Width of the render
     * @param height Height of the render
     * @param red    The red component of the render's color
     * @param green  The green component of the render's color
     * @param blue   The blue component of the render's color
     * @param alpha  The alpha component of the render's color
     */
    public void draw(int posX, int posY, int texU, int texV, int width, int height, int red, int green, int blue, int alpha) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(posX, posY + height, 0.0D).tex((float) texU * f, (float) (texV + height) * f1).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(posX + width, posY + height, 0.0D).tex((float) (texU + width) * f, (float) (texV + height) * f1).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(posX + width, posY, 0.0D).tex((float) (texU + width) * f, (float) texV * f1).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(posX, posY, 0.0D).tex((float) texU * f, (float) texV * f1).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    /**
     * Returns the save loader that is currently being used
     */
    public ISaveFormat getSaveLoader() {
        return this.saveLoader;
    }

    /**
     * Sets the argument GuiScreen as the main (topmost visible) screen.
     */
    public void displayGuiScreen(GuiScreen guiScreenIn) {

        if (this.thePlayer == null && this.theWorld == null && this.currentScreen != null && this.currentScreen instanceof MainMenu) {
            TransitionAnimation.task(() -> this.displayGuiScreen0(guiScreenIn));
        } else {
            this.displayGuiScreen0(guiScreenIn);
        }

    }

    public void displayGuiScreen0(GuiScreen guiScreenIn) {
        if (this.currentScreen != null) {
            this.currentScreen.onGuiClosed();
        }

        if (guiScreenIn == null && this.theWorld == null) {
            guiScreenIn = MainMenu.getInstance();
        } else if (guiScreenIn == null && this.thePlayer.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver();
        }

        if (guiScreenIn instanceof MainMenu) {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages();
        }

        this.currentScreen = guiScreenIn;

        if (guiScreenIn != null) {
            this.setIngameNotInFocus();
            ScaledResolution scaledresolution = ScaledResolution.get();
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(this, i, j);
            this.skipRenderWorld = false;
        } else {
            this.mcSoundHandler.resumeSounds();
            this.setIngameFocus();
        }
    }

    /**
     * Checks for an OpenGL error. If there is one, prints the error ID and error string.
     */
    public boolean checkGLError(String message) {
        boolean enableGLErrorChecking = true;
        if (enableGLErrorChecking) {
            int i = GL11.glGetError();

            if (i != 0) {
                String s = org.lwjglx.opengl.Util.translateGLErrorString(i);
                logger.error("########## GL ERROR ##########");
                logger.error("@ " + message);
                logger.error(i + ": " + s);
                return true;
            }
        }

        return false;
    }

    /**
     * Shuts down the minecraft applet by stopping the resource downloads, and clearing up GL stuff; called when the
     * application (or web page) is exited.
     */
    public void shutdownMinecraftApplet() {
        try {
            logger.info("Stopping!");

            try {
                this.loadWorld(null);
            } catch (Throwable ignored) {
            }

            this.mcSoundHandler.unloadSounds();

            this.gameSettings.saveOptions();
        } finally {
            Display.destroy();

            if (!this.hasCrashed) {
                System.exit(0);
            }
        }

        System.gc();
    }

    private int lastWidth, lastHeight, lastScaleFactor;

    /**
     * Called repeatedly from run()
     */
    private void runGameLoop() throws IOException {

        if (lastWidth != displayWidth || lastHeight != displayHeight || lastScaleFactor != gameSettings.guiScale) {
            scaledResolution = ScaledResolution.createNew(this);

            lastWidth = displayWidth;
            lastHeight = displayHeight;
            lastScaleFactor = gameSettings.guiScale;
        }

        //CLIENT
        Interpolations.calcFrameDelta();
        //END CLIENT

        long i = System.nanoTime();
        this.mcProfiler.startSection("GameLoop Start");

        if (Display.isCreated() && Display.isCloseRequested()) {
            this.shutdown();
        }

        if (this.isGamePaused && this.theWorld != null) {
            float f = this.timer.renderPartialTicks;
            this.timer.updateTimer();
            this.timer.renderPartialTicks = f;
        } else {
            this.timer.updateTimer();
        }

        this.mcProfiler.startSection("scheduledExecutables");

        synchronized (this.scheduledTasks) {
            while (!this.scheduledTasks.isEmpty()) {
                Util.runTask((FutureTask<?>) this.scheduledTasks.poll(), logger);
            }
        }

        this.mcProfiler.endSection();
        long l = System.nanoTime();

        // dummy call for aimassist etc.
        this.entityRenderer.onMouseEvent(0, 0);

        this.mcProfiler.startSection("tick");

        for (int j = 0; j < this.timer.elapsedTicks; ++j) {
            //CLIENT
            TickEvent tickEvent = new TickEvent(this.timer.elapsedTicks);
            if (thePlayer != null && theWorld != null) {
                tickEvent.setState(State.PRE);
                EventManager.call(tickEvent);
            }
            //END CLIENT

            this.runTick();

            //CLIENT
            if (thePlayer != null && theWorld != null) {
                tickEvent.setState(State.POST);
                EventManager.call(tickEvent);
            }

            ingameGUI.dirty = true;
            //END CLIENT
        }

        this.mcProfiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        this.checkGLError("Pre render");
        this.mcProfiler.endStartSection("sound");
        this.mcSoundHandler.setListener(this.thePlayer, this.timer.renderPartialTicks);
        this.mcProfiler.endSection();
        this.mcProfiler.startSection("render");

        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        this.framebufferMc.bindFramebuffer(true);
        this.mcProfiler.startSection("display");
        GlStateManager.enableTexture2D();

        if (this.thePlayer != null && this.theWorld != null && this.thePlayer.isEntityInsideOpaqueBlock()) {
            this.gameSettings.thirdPersonView = 0;
        }

        this.mcProfiler.endSection();

        if (!this.skipRenderWorld) {
            this.mcProfiler.endStartSection("gameRenderer");
            this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks, i);
            this.mcProfiler.endSection();
        }

        if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI) {
            if (!this.mcProfiler.profilingEnabled) {
                this.mcProfiler.clearProfiling();
            }

            this.mcProfiler.profilingEnabled = true;
            this.displayDebugInfo(i1);
        } else {
            this.mcProfiler.profilingEnabled = false;
            this.prevFrameTime = System.nanoTime();
        }

        if (!ModuleManager.noAchievements.isEnabled()) {
            this.guiAchievement.updateAchievementWindow();
        }
        this.framebufferMc.unbindFramebuffer();
        GlStateManager.popMatrix();

        int sync = this.getLimitFramerate();
        boolean needSync = this.isFramerateLimitBelowMax();

        if (ClientSettings.FRAME_PREDICT.getValue()) {

            if (lastFrameTex == -1) {
                lastFrameTex = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastFrameTex);

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.displayWidth, this.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

                this.framebufferMc.bindFramebuffer(true);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastFrameTex);
                GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 0, 0, this.displayWidth, this.displayHeight, 0);
                this.framebufferMc.unbindFramebuffer();
            } else {
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, this.displayWidth, this.displayHeight, 0.0D, 1000.0D, 3000.0D);
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, 0.0F, -2000.0F);
                GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
                GlStateManager.disableAlpha();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                tritium.rendering.shader.Shaders.MOTION.run(framebufferMc.framebufferTexture, lastFrameTex, this.displayWidth, this.displayHeight);
            }

            this.updateDisplay();
            if (needSync)
                Display.sync(sync * 2);

        }
        GlStateManager.pushMatrix();

        if (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_DECORATED) == GLFW.GLFW_FALSE && !this.isFullScreen()) {
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            this.framebufferMc.framebufferRenderExtRounded(this.displayWidth, this.displayHeight, true);
        } else {
            this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);

            if (ClientSettings.FRAME_PREDICT.getValue() && lastFrameTex != -1) {
                // update content
                this.framebufferMc.bindFramebuffer(true);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastFrameTex);
                GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, this.displayWidth, this.displayHeight);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                this.framebufferMc.unbindFramebuffer();
            }
        }

        GlStateManager.popMatrix();

        this.mcProfiler.startSection("GameLoop End");

        if (ModuleManager.motionBlur.isEnabled()) {
            ModuleManager.motionBlur.doMotionBlur();
        }
        this.updateDisplay();
        if (needSync)
            Display.sync(ClientSettings.FRAME_PREDICT.getValue() ? sync * 2 : sync);

//        Thread.yield();
        this.checkGLError("Post render");
        this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();
        long k = System.nanoTime();
        this.frameTimer.addFrame(k - this.startNanoTime);
        this.startNanoTime = k;

        while (getSystemTime() >= this.debugUpdateTime + 1000L) {
            debugFPS = this.fpsCounter;
            this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated != 1 ? "s" : "", (float) this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : Integer.valueOf(this.gameSettings.limitFramerate), this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;

        }

        this.mcProfiler.endSection();
    }

    int lastFrameTex = -1;

    public void updateDisplay() {
        this.mcProfiler.startSection("display_update");
        Display.update();
        this.mcProfiler.endSection();
        this.checkWindowResize();
        ++this.fpsCounter;
    }

    protected void checkWindowResize() {
        if (!this.fullscreen && Display.wasResized()) {
            int i = this.displayWidth;
            int j = this.displayHeight;
            this.displayWidth = Display.getWidth();
            this.displayHeight = Display.getHeight();

            EventManager.call(new DisplayResizedEvent(i, j, this.displayWidth, this.displayHeight));

            if (this.displayWidth != i || this.displayHeight != j) {
                if (this.displayWidth <= 0) {
                    this.displayWidth = 1;
                }

                if (this.displayHeight <= 0) {
                    this.displayHeight = 1;
                }

                this.resize(this.displayWidth, this.displayHeight);
            }
        }
    }

    public int getLimitFramerate() {

        if (this.theWorld == null)
            return Math.min(300, Math.max(120, Display.getDesktopDisplayMode().getFrequency() * 4));

        return /*this.theWorld == null && this.currentScreen != null ? 240 : */this.gameSettings.limitFramerate;
    }

    public boolean isFramerateLimitBelowMax() {

        if (this.theWorld == null)
            return true;

        return (float) this.getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
    }

    public void freeMemory() {
        try {
            this.renderGlobal.deleteAllDisplayLists();
        } catch (Throwable var3) {
        }

        try {
            System.gc();
            this.loadWorld(null);
        } catch (Throwable var2) {
        }

        System.gc();
    }

    /**
     * Update debugProfilerName in response to number keys in debug screen
     */
    private void updateDebugProfilerName(int keyCount) {
        List<Profiler.Result> list = this.mcProfiler.getProfilingData(this.debugProfilerName);

        if (list != null && !list.isEmpty()) {
            Profiler.Result profiler$result = list.remove(0);

            if (keyCount == 0) {
                if (profiler$result.field_76331_c.length() > 0) {
                    int i = this.debugProfilerName.lastIndexOf(".");

                    if (i >= 0) {
                        this.debugProfilerName = this.debugProfilerName.substring(0, i);
                    }
                }
            } else {
                --keyCount;

                if (keyCount < list.size() && !list.get(keyCount).field_76331_c.equals("unspecified")) {
                    if (this.debugProfilerName.length() > 0) {
                        this.debugProfilerName = this.debugProfilerName + ".";
                    }

                    this.debugProfilerName = this.debugProfilerName + list.get(keyCount).field_76331_c;
                }
            }
        }
    }

    /**
     * Parameter appears to be unused
     */
    private void displayDebugInfo(long elapsedTicksTime) {
        if (this.mcProfiler.profilingEnabled) {
            List<Profiler.Result> list = this.mcProfiler.getProfilingData(this.debugProfilerName);
            Profiler.Result profiler$result = list.remove(0);
            GlStateManager.clear(256);
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.enableColorMaterial();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, this.displayWidth, this.displayHeight, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 160;
            int j = this.displayWidth - i - 10;
            int k = this.displayHeight - i * 2;
            GlStateManager.enableBlend();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos((float) j - (float) i * 1.1F, (float) k - (float) i * 0.6F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float) j - (float) i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float) j + (float) i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float) j + (float) i * 1.1F, (float) k - (float) i * 0.6F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            double d0 = 0.0D;

            for (Profiler.Result profiler$result1 : list) {
                int i1 = MathHelper.floor_double(profiler$result1.field_76332_a / 4.0D) + 1;
                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                int j1 = profiler$result1.getColor();
                int k1 = j1 >> 16 & 255;
                int l1 = j1 >> 8 & 255;
                int i2 = j1 & 255;
                worldrenderer.pos(j, k, 0.0D).color(k1, l1, i2, 255).endVertex();

                for (int j2 = i1; j2 >= 0; --j2) {
                    float f = (float) ((d0 + profiler$result1.field_76332_a * (double) j2 / (double) i1) * Math.PI * 2.0D / 100.0D);
                    float f1 = MathHelper.sin(f) * (float) i;
                    float f2 = MathHelper.cos(f) * (float) i * 0.5F;
                    worldrenderer.pos((float) j + f1, (float) k - f2, 0.0D).color(k1, l1, i2, 255).endVertex();
                }

                tessellator.draw();
                worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);

                for (int i3 = i1; i3 >= 0; --i3) {
                    float f3 = (float) ((d0 + profiler$result1.field_76332_a * (double) i3 / (double) i1) * Math.PI * 2.0D / 100.0D);
                    float f4 = MathHelper.sin(f3) * (float) i;
                    float f5 = MathHelper.cos(f3) * (float) i * 0.5F;
                    worldrenderer.pos((float) j + f4, (float) k - f5, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                    worldrenderer.pos((float) j + f4, (float) k - f5 + 10.0F, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                }

                tessellator.draw();
                d0 += profiler$result1.field_76332_a;
            }

            DecimalFormat decimalformat = new DecimalFormat("##0.00");
            GlStateManager.enableTexture2D();
            String s = "";

            if (!profiler$result.field_76331_c.equals("unspecified")) {
                s = s + "[0] ";
            }

            if (profiler$result.field_76331_c.length() == 0) {
                s = s + "ROOT ";
            } else {
                s = s + profiler$result.field_76331_c + " ";
            }

            int l2 = 16777215;
            this.fontRendererObj.drawStringWithShadow(s, (float) (j - i), (float) (k - i / 2 - 16), l2);
            this.fontRendererObj.drawStringWithShadow(s = decimalformat.format(profiler$result.field_76330_b) + "%", (float) (j + i - this.fontRendererObj.getStringWidth(s)), (float) (k - i / 2 - 16), l2);

            for (int k2 = 0; k2 < list.size(); ++k2) {
                Profiler.Result profiler$result2 = list.get(k2);
                String s1 = "";

                if (profiler$result2.field_76331_c.equals("unspecified")) {
                    s1 = s1 + "[?] ";
                } else {
                    s1 = s1 + "[" + (k2 + 1) + "] ";
                }

                s1 = s1 + profiler$result2.field_76331_c;
                this.fontRendererObj.drawStringWithShadow(s1, (float) (j - i), (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.getColor());
                this.fontRendererObj.drawStringWithShadow(s1 = decimalformat.format(profiler$result2.field_76332_a) + "%", (float) (j + i - 50 - this.fontRendererObj.getStringWidth(s1)), (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.getColor());
                this.fontRendererObj.drawStringWithShadow(s1 = decimalformat.format(profiler$result2.field_76330_b) + "%", (float) (j + i - this.fontRendererObj.getStringWidth(s1)), (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.getColor());
            }
        }
    }

    /**
     * Called when the window is closing. Sets 'running' to false which allows the game loop to exit cleanly.
     */
    public void shutdown() {
        this.running = false;
    }

    /**
     * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen
     * currently displayed
     */
    public void setIngameFocus() {
        if (Display.isActive()) {
            if (!this.inGameHasFocus) {
                this.inGameHasFocus = true;
                this.mouseHelper.grabMouseCursor();
                this.displayGuiScreen(null);
                this.leftClickCounter = 10000;
            }
        }
    }

    /**
     * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
     */
    public void setIngameNotInFocus() {
        if (this.inGameHasFocus) {
            KeyBinding.unPressAllKeys();
            this.inGameHasFocus = false;
            this.mouseHelper.ungrabMouseCursor();
        }
    }

    /**
     * Displays the ingame menu
     */
    public void displayInGameMenu() {
        if (this.currentScreen == null) {
            this.displayGuiScreen(new GuiIngameMenu());

            if (this.isSingleplayer() && !this.theIntegratedServer.getPublic()) {
                this.mcSoundHandler.pauseSounds();
            }
        }
    }

    private void sendClickBlockToController(boolean leftClick) {
        if (!leftClick) {
            this.leftClickCounter = 0;
        }

        if (this.leftClickCounter <= 0 && !this.thePlayer.isUsingItem()) {
            if (leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockpos = this.objectMouseOver.getBlockPos();

                if (this.theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air && this.playerController.onPlayerDamageBlock(blockpos, this.objectMouseOver.sideHit)) {
                    this.effectRenderer.addBlockHitEffects(blockpos, this.objectMouseOver.sideHit);
                    this.thePlayer.swingItem();
                }
            } else {
                this.playerController.resetBlockRemoving();
            }
        }
    }

    public void clickMouse() {
        if (this.leftClickCounter <= 0 || ClientSettings.NO_CLICK_DELAY.getValue()) {
            CPSUtils.addLeftCPS();
            this.thePlayer.swingItem();

            if (this.objectMouseOver == null) {
                logger.error("Null returned as 'hitResult', this shouldn't happen!");

                if (this.playerController.isNotCreative()) {
                    this.leftClickCounter = 10;
                }
            } else {
                switch (this.objectMouseOver.typeOfHit) {
                    case ENTITY:
                        this.playerController.attackEntity(this.thePlayer, this.objectMouseOver.entityHit);
                        break;

                    case BLOCK:
                        BlockPos blockpos = this.objectMouseOver.getBlockPos();

                        if (this.theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
                            this.playerController.clickBlock(blockpos, this.objectMouseOver.sideHit);
                            break;
                        }

                    case MISS:
                    default:
                        if (this.playerController.isNotCreative()) {
                            this.leftClickCounter = 10;
                        }
                }
            }
        }
    }

    @SuppressWarnings("incomplete-switch")

    /**
     * Called when user clicked he's mouse right button (place)
     */
    public void rightClickMouse() {
        if (!this.playerController.getIsHittingBlock()) {
            CPSUtils.addRightCPS();
            this.rightClickDelayTimer = 4;
            boolean flag = true;
            ItemStack itemstack = this.thePlayer.inventory.getCurrentItem();

            if (this.objectMouseOver == null) {
                logger.warn("Null returned as 'hitResult', this shouldn't happen!");
            } else {

                switch (this.objectMouseOver.typeOfHit) {
                    case ENTITY:
                        if (this.playerController.isPlayerRightClickingOnEntity(this.thePlayer, this.objectMouseOver.entityHit, this.objectMouseOver)) {
                            flag = false;
                        } else if (this.playerController.interactWithEntitySendPacket(this.thePlayer, this.objectMouseOver.entityHit)) {
                            flag = false;
                        }

                        break;

                    case BLOCK:
                        BlockPos blockpos = this.objectMouseOver.getBlockPos();

                        if (this.theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
                            int i = itemstack != null ? itemstack.stackSize : 0;

                            if (this.playerController.onPlayerRightClick(this.thePlayer, this.theWorld, itemstack, blockpos, this.objectMouseOver.sideHit, this.objectMouseOver.hitVec)) {
                                flag = false;
                                this.thePlayer.swingItem();
                            }

                            if (itemstack == null) {
                                return;
                            }

                            if (itemstack.stackSize == 0) {
                                this.thePlayer.inventory.mainInventory[this.thePlayer.inventory.currentItem] = null;
                            } else if (itemstack.stackSize != i || this.playerController.isInCreativeMode()) {
                                this.entityRenderer.itemRenderer.resetEquippedProgress();
                            }
                        }
                }
            }

            if (flag) {
                ItemStack itemstack1 = this.thePlayer.inventory.getCurrentItem();

                if (itemstack1 != null && this.playerController.sendUseItem(this.thePlayer, this.theWorld, itemstack1)) {
                    this.entityRenderer.itemRenderer.resetEquippedProgress2();
                }
            }
        }
    }

    /**
     * Toggles fullscreen mode.
     */
    public void toggleFullscreen() {
        if (!IngameIMEJNI.disable)
            Internal.destroyInputCtx();

        try {
            this.fullscreen = !this.fullscreen;
            this.gameSettings.fullScreen = this.fullscreen;

            if (this.fullscreen) {
                this.updateDisplayMode();
                this.displayWidth = Display.getDisplayMode().getWidth();
                this.displayHeight = Display.getDisplayMode().getHeight();

            } else {
                Display.setDisplayMode(new DisplayMode(this.tempDisplayWidth, this.tempDisplayHeight));
                this.displayWidth = this.tempDisplayWidth;
                this.displayHeight = this.tempDisplayHeight;
            }
            if (this.displayWidth <= 0) {
                this.displayWidth = 1;
            }
            if (this.displayHeight <= 0) {
                this.displayHeight = 1;
            }

            if (this.currentScreen != null) {
                this.resize(this.displayWidth, this.displayHeight);
            } else {
                this.updateFramebufferSize();
            }

            Display.setFullscreen(this.fullscreen);
            Display.setVSyncEnabled(this.gameSettings.enableVsync);
            this.updateDisplay();
        } catch (Exception exception) {
            logger.error("Couldn't toggle fullscreen", exception);
        }

        if (!IngameIMEJNI.disable) {
            Internal.createInputCtx();
            Internal.setActivated(this.fullscreen);
        }
    }

    /**
     * Called to resize the current screen.
     */
    public void resize(int width, int height) {
        this.displayWidth = Math.max(1, width);
        this.displayHeight = Math.max(1, height);

        this.scaledResolution = ScaledResolution.createNew(this);

        if (this.currentScreen != null) {
            this.currentScreen.onResize(this, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        }

        this.loadingScreen = new LoadingScreenRenderer(this);
        this.updateFramebufferSize();
    }

    private void updateFramebufferSize() {
        this.framebufferMc.createBindFramebuffer(this.displayWidth, this.displayHeight);

        if (this.entityRenderer != null) {
            this.entityRenderer.updateShaderGroupSize(this.displayWidth, this.displayHeight);
        }

        if (lastFrameTex != -1) {
            GlStateManager.deleteTexture(lastFrameTex);

            lastFrameTex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastFrameTex);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.displayWidth, this.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
    }

    /**
     * Return the musicTicker's instance
     */
    public MusicTicker getMusicTicker() {
        return this.mcMusicTicker;
    }

    public void onDWheel(int wheel) {
        long timeDelta = getSystemTime() - this.systemTime;
        if (timeDelta <= 200L) {

            if (wheel != 0) {

                if (this.currentScreen == null) {
                    if (this.thePlayer.isSpectator()) {
                        wheel = wheel < 0 ? -1 : 1;

                        if (this.ingameGUI.getSpectatorGui().func_175262_a()) {
                            this.ingameGUI.getSpectatorGui().func_175259_b(-wheel);
                        } else {
                            float f = MathHelper.clamp_float(this.thePlayer.capabilities.getFlySpeed() + (float) wheel * 0.005F, 0.0F, 0.2F);
                            this.thePlayer.capabilities.setFlySpeed(f);
                        }
                    } else {
                        this.thePlayer.inventory.changeCurrentItem(wheel);
                    }
                } else {
                    this.currentScreen.handleDWheel(wheel);
                }

            }
        }
    }

    @SneakyThrows
    public void onMousePressed(int button, boolean pressed) {

        if (this.currentScreen == null) {
            KeyBinding.setKeyBindState(button - 100, pressed);

            if (pressed) {
                if (this.thePlayer != null && this.thePlayer.isSpectator() && button == 2) {
                    this.ingameGUI.getSpectatorGui().func_175261_b();
                } else {
                    KeyBinding.onTick(button - 100);
                }
            }
        }

        long timeDelta = getSystemTime() - this.systemTime;

        if (timeDelta <= 200L) {
            if (this.currentScreen == null) {
                if (!this.inGameHasFocus && pressed) {
                    this.setIngameFocus();
                }
            } else {

                if (this.currentScreen instanceof GuiContainer) {
                    clickActions.add(new Runnable() {
                        @Override
                        @SneakyThrows
                        public void run() {
                            Minecraft.this.currentScreen.handleMouseInput(button, pressed);
                        }
                    });
                } else {
                    Minecraft.this.currentScreen.handleMouseInput(button, pressed);
                }

            }
        }
    }

    final List<Runnable> clickActions = new CopyOnWriteArrayList<>();

    /**
     * Runs the current tick.
     */
    public void runTick() throws IOException {
        if (this.rightClickDelayTimer > 0) {
            --this.rightClickDelayTimer;
        }

        this.mcProfiler.startSection("gui");

        if (!this.isGamePaused) {
            this.ingameGUI.updateTick();
        }

        this.mcProfiler.endSection();
        this.entityRenderer.getMouseOver(1.0F);
        this.mcProfiler.startSection("gameMode");

        if (!this.isGamePaused && this.theWorld != null) {
            this.playerController.updateController();
        }

        this.mcProfiler.endStartSection("textures");

        if (!this.isGamePaused) {
            this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.thePlayer != null) {
            if (this.thePlayer.getHealth() <= 0.0F) {
                this.displayGuiScreen(null);
            } else if (this.thePlayer.isPlayerSleeping() && this.theWorld != null) {
                this.displayGuiScreen(new GuiSleepMP());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.thePlayer.isPlayerSleeping()) {
            this.displayGuiScreen(null);
        }

        if (this.currentScreen != null) {
            this.leftClickCounter = 10000;
        }

        if (this.currentScreen != null) {
            try {
                // grim fix
                for (Runnable clickAction : clickActions) {
                    clickAction.run();
                }
                clickActions.clear();

                this.currentScreen.handleInput();
            } catch (Throwable throwable1) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addCrashSectionCallable("Screen name", new Callable<String>() {
                    public String call() {
                        return Minecraft.this.currentScreen.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null) {
                try {
                    this.currentScreen.updateScreen();
                } catch (Throwable throwable) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashreportcategory1.addCrashSectionCallable("Screen name", new Callable<String>() {
                        public String call() {
                            return Minecraft.this.currentScreen.getClass().getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport1);
                }
            }
        }

        if (this.currentScreen == null || this.currentScreen.allowUserInput) {
            this.mcProfiler.endStartSection("mouse");

            if (this.leftClickCounter > 0) {
                --this.leftClickCounter;
            }

            this.mcProfiler.endStartSection("keyboard");

            while (Keyboard.next()) {
                char eventCharacter = Keyboard.getEventCharacter();
                int k = Keyboard.getEventKey() == 0 ? eventCharacter + 256 : Keyboard.getEventKey();

                if (Keyboard.getEventKeyState()) {
                    KeyBinding.onTick(k);
                }

                if (this.debugCrashKeyPressTime > 0L) {
                    if (getSystemTime() - this.debugCrashKeyPressTime >= 6000L) {
                        throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                    }

                    if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
                        this.debugCrashKeyPressTime = -1L;
                    }
                } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
                    this.debugCrashKeyPressTime = getSystemTime();
                }

                this.dispatchKeypresses(eventCharacter, k);

                if (Keyboard.getEventKeyState()) {
                    if (k == 62 && this.entityRenderer != null) {
                        this.entityRenderer.switchUseShader();
                    }

                    if (this.currentScreen != null) {
                        this.currentScreen.handleKeyboardInput(eventCharacter, k);
                    } else {

                        //CLIENT
                        EventManager.call(new KeyPressedEvent(k));
                        //END CLIENT

                        if (k == 1) {
                            this.displayInGameMenu();
                        }

                        if (k == 32 && Keyboard.isKeyDown(61) && this.ingameGUI != null) {
                            this.ingameGUI.getChatGUI().clearChatMessages();
                        }

                        if (k == 31 && Keyboard.isKeyDown(61)) {
                            this.refreshResources();
                        }

                        if (k == 17 && Keyboard.isKeyDown(61)) {
                        }

                        if (k == 18 && Keyboard.isKeyDown(61)) {
                        }

                        if (k == 47 && Keyboard.isKeyDown(61)) {
                        }

                        if (k == 38 && Keyboard.isKeyDown(61)) {
                        }

                        if (k == 22 && Keyboard.isKeyDown(61)) {
                        }

                        if (k == 20 && Keyboard.isKeyDown(61)) {
                            this.refreshResources();
                            FontManager.loadFonts();
                        }

                        if (k == 33 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
                        }

                        if (k == 30 && Keyboard.isKeyDown(61)) {
                            this.renderGlobal.loadRenderers();
                        }

                        if (k == 35 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
                            this.gameSettings.saveOptions();
                        }

                        if (k == 48 && Keyboard.isKeyDown(61)) {
                            this.renderManager.setDebugBoundingBox(!this.renderManager.isDebugBoundingBox());
                        }

                        if (k == 25 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
                            this.gameSettings.saveOptions();
                        }

                        if (k == 59) {
                            this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
                        }

                        if (k == 61) {
                            this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                            this.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                            this.gameSettings.showLagometer = GuiScreen.isAltKeyDown();
                        }

                        if (this.gameSettings.keyBindTogglePerspective.isPressed()) {
                            ++this.gameSettings.thirdPersonView;

                            if (this.gameSettings.thirdPersonView > 2) {
                                this.gameSettings.thirdPersonView = 0;
                            }

                            if (this.gameSettings.thirdPersonView == 0) {
                                this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
                            } else if (this.gameSettings.thirdPersonView == 1) {
                                this.entityRenderer.loadEntityShader(null);
                            }

                            this.renderGlobal.setDisplayListEntitiesDirty();
                        }

                        if (this.gameSettings.keyBindSmoothCamera.isPressed()) {
                            this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
                        }
                    }

                    if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
                        if (k == 11) {
                            this.updateDebugProfilerName(0);
                        }

                        for (int j1 = 0; j1 < 9; ++j1) {
                            if (k == 2 + j1) {
                                this.updateDebugProfilerName(j1 + 1);
                            }
                        }
                    }
                }
            }

            for (int l = 0; l < 9; ++l) {
                if (this.gameSettings.keyBindsHotbar[l].isPressed()) {
                    if (this.thePlayer.isSpectator()) {
                        this.ingameGUI.getSpectatorGui().func_175260_a(l);
                    } else {
                        this.thePlayer.inventory.currentItem = l;
                    }
                }
            }

            boolean flag = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

            while (this.gameSettings.keyBindInventory.isPressed()) {
                if (this.playerController.isRidingHorse()) {
                    this.thePlayer.sendHorseInventory();
                } else {
                    this.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    this.displayGuiScreen(new GuiInventory(this.thePlayer));
                }
            }

            while (this.gameSettings.keyBindDrop.isPressed()) {
                if (!this.thePlayer.isSpectator()) {
                    this.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
                }
            }

            while (this.gameSettings.keyBindChat.isPressed() && flag) {
                this.displayGuiScreen(new GuiChat());
            }

            if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed() && flag) {
                this.displayGuiScreen(new GuiChat("/"));
            }

            if (this.currentScreen == null && Keyboard.isKeyDown(Keyboard.KEY_PERIOD) && flag && !ModuleManager.noCommand.isEnabled()) {
                this.displayGuiScreen(new GuiChat("."));
            }

            if (this.thePlayer.isUsingItem()) {
                if (!this.gameSettings.keyBindUseItem.isKeyDown()) {
                    this.playerController.onStoppedUsingItem(this.thePlayer);
                }

                while (this.gameSettings.keyBindAttack.isPressed()) {
                }

                while (this.gameSettings.keyBindUseItem.isPressed()) {
                }

                while (this.gameSettings.keyBindPickBlock.isPressed()) {
                }
            } else {
                while (this.gameSettings.keyBindAttack.isPressed()) {
                    this.clickMouse();
                }

                while (this.gameSettings.keyBindUseItem.isPressed()) {
                    this.rightClickMouse();
                }

                while (this.gameSettings.keyBindPickBlock.isPressed()) {
                    this.middleClickMouse();
                }
            }

            if (this.gameSettings.keyBindUseItem.isKeyDown() && (this.rightClickDelayTimer == 0) && !this.thePlayer.isUsingItem()) {
                this.rightClickMouse();
            }

            this.sendClickBlockToController(this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus);
        }

        if (this.theWorld != null) {
            if (this.thePlayer != null) {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.theWorld.joinEntityInSurroundings(this.thePlayer);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("lighting");

            ((ILightingEngineProvider) this.theWorld).getLightingEngine().processLightUpdates();

            this.mcProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused) {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");

            if (!this.isGamePaused) {
                if (this.theWorld.getLastLightningBolt() > 0) {
                    this.theWorld.setLastLightningBolt(this.theWorld.getLastLightningBolt() - 1);
                }

                this.theWorld.updateEntities();
            }
        } else if (this.entityRenderer.isShaderActive()) {
            this.entityRenderer.stopUseShader();
        }

        if (!this.isGamePaused) {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.theWorld != null) {
            if (!this.isGamePaused) {
                this.theWorld.setAllowedSpawnTypes(this.theWorld.getDifficulty() != EnumDifficulty.PEACEFUL, true);

                try {
                    this.theWorld.tick();
                } catch (Throwable throwable2) {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.theWorld == null) {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("受影响的世界");
                        crashreportcategory2.addCrashSection("问题", "世界为空!");
                    } else {
                        this.theWorld.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

//            this.mcProfiler.endStartSection("animateTick");
//
//            if (!this.isGamePaused && this.theWorld != null) {
//                this.theWorld.doVoidFogParticles(MathHelper.floor_double(this.thePlayer.posX), MathHelper.floor_double(this.thePlayer.posY), MathHelper.floor_double(this.thePlayer.posZ));
//            }

            this.mcProfiler.endStartSection("particles");

            if (!this.isGamePaused) {
                this.effectRenderer.updateEffects();
            }
        } else if (this.myNetworkManager != null) {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        this.mcProfiler.endSection();
        this.systemTime = getSystemTime();
    }

    /**
     * Arguments: World foldername,  World ingame name, WorldSettings
     */
    public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn) {
        this.loadWorld(null);
        System.gc();
        ISaveHandler isavehandler = this.saveLoader.getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null && worldSettingsIn != null) {
            worldinfo = new WorldInfo(worldSettingsIn, folderName);
            isavehandler.saveWorldInfo(worldinfo);
        }

        if (worldSettingsIn == null) {
            worldSettingsIn = new WorldSettings(worldinfo);
        }

        try {
            this.theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn);
            this.theIntegratedServer.startServerThread();
            this.integratedServerIsRunning = true;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            crashreportcategory.addCrashSection("Level ID", folderName);
            crashreportcategory.addCrashSection("Level Name", worldName);
            throw new ReportedException(crashreport);
        }

        this.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));

        while (!this.theIntegratedServer.serverIsInRunLoop()) {
            String s = this.theIntegratedServer.getUserMessage();

            if (s != null) {
                this.loadingScreen.displayLoadingString(I18n.format(s));
            } else {
                this.loadingScreen.displayLoadingString("");
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException var9) {
            }
        }

        this.displayGuiScreen(null);
        SocketAddress socketaddress = this.theIntegratedServer.getNetworkSystem().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
        networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN));
        networkmanager.sendPacket(new C00PacketLoginStart(this.getSession().getProfile()));
        this.myNetworkManager = networkmanager;
    }

    /**
     * unloads the current world first
     */
    public void loadWorld(WorldClient worldClientIn) {
        this.loadWorld(worldClientIn, "");
    }

    /**
     * par2Str is displayed on the loading screen to the user unloads the current world first
     */
    public void loadWorld(WorldClient worldClientIn, String loadingMessage) {
        if (worldClientIn == null) {
            NetHandlerPlayClient nethandlerplayclient = this.getNetHandler();

            if (nethandlerplayclient != null) {
                nethandlerplayclient.cleanup();
            }

            if (this.theIntegratedServer != null && this.theIntegratedServer.isAnvilFileSet()) {
                this.theIntegratedServer.initiateShutdown();
                this.theIntegratedServer.setStaticInstance();
            }

            this.theIntegratedServer = null;
            this.guiAchievement.clearAchievements();
            this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }

        this.renderViewEntity = null;
        this.myNetworkManager = null;

        if (this.loadingScreen != null) {
            this.loadingScreen.resetProgressAndMessage(loadingMessage);
            this.loadingScreen.displayLoadingString("");
        }

        if (worldClientIn == null && this.theWorld != null) {
            this.mcResourcePackRepository.clearResourcePack();
            this.ingameGUI.resetPlayersOverlayFooterHeader();
            this.setServerData(null);
            this.integratedServerIsRunning = false;
        }

        this.mcSoundHandler.stopSounds();
        this.theWorld = worldClientIn;
        EventManager.call(new WorldChangedEvent(worldClientIn));

        if (worldClientIn != null) {
            if (this.renderGlobal != null) {
                this.renderGlobal.setWorldAndLoadRenderers(worldClientIn);
            }

            if (this.effectRenderer != null) {
                this.effectRenderer.clearEffects(worldClientIn);
            }

            if (this.thePlayer == null) {
                this.thePlayer = this.playerController.func_178892_a(worldClientIn, new StatFileWriter());
                this.playerController.flipPlayer(this.thePlayer);
            }

            this.thePlayer.preparePlayerToSpawn();
            worldClientIn.spawnEntityInWorld(this.thePlayer);
            this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
            this.playerController.setPlayerCapabilities(this.thePlayer);
            this.renderViewEntity = this.thePlayer;
        } else {
            this.saveLoader.flushCache();
            this.thePlayer = null;
        }

//        System.gc();
        this.systemTime = 0L;
    }

    public void setDimensionAndSpawnPlayer(int dimension) {
        this.theWorld.setInitialSpawnLocation();
        this.theWorld.removeAllEntities();
        int i = 0;
        String s = null;

        if (this.thePlayer != null) {
            i = this.thePlayer.getEntityId();
            this.theWorld.removeEntity(this.thePlayer);
            s = this.thePlayer.getClientBrand();
        }

        this.renderViewEntity = null;
        EntityPlayerSP entityplayersp = this.thePlayer;
        this.thePlayer = this.playerController.func_178892_a(this.theWorld, this.thePlayer == null ? new StatFileWriter() : this.thePlayer.getStatFileWriter());
        this.thePlayer.getDataWatcher().updateWatchedObjectsFromList(entityplayersp.getDataWatcher().getAllWatched());
        this.thePlayer.dimension = dimension;
        this.renderViewEntity = this.thePlayer;
        this.thePlayer.preparePlayerToSpawn();
        this.thePlayer.setClientBrand(s);
        this.theWorld.spawnEntityInWorld(this.thePlayer);
        this.playerController.flipPlayer(this.thePlayer);
        this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
        this.thePlayer.setEntityId(i);
        this.playerController.setPlayerCapabilities(this.thePlayer);
        this.thePlayer.setReducedDebug(entityplayersp.hasReducedDebug());

        if (this.currentScreen instanceof GuiGameOver) {
            this.displayGuiScreen(null);
        }
    }

    /**
     * Gets whether this is a demo or not.
     */
    public final boolean isDemo() {
        return this.isDemo;
    }

    public NetHandlerPlayClient getNetHandler() {
        return this.thePlayer != null ? this.thePlayer.sendQueue : null;
    }

    public static boolean isGuiEnabled() {
        return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
    }

    public static boolean isFancyGraphicsEnabled() {
        return theMinecraft != null && theMinecraft.gameSettings.fancyGraphics;
    }

    /**
     * Returns if ambient occlusion is enabled
     */
    public static boolean isAmbientOcclusionEnabled() {
        return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion != 0;
    }

    /**
     * Called when user clicked he's mouse middle button (pick block)
     */
    private void middleClickMouse() {
        if (this.objectMouseOver != null) {
            boolean flag = this.thePlayer.capabilities.isCreativeMode;
            int i = 0;
            boolean flag1 = false;
            TileEntity tileentity = null;
            Item item;

            if (this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockpos = this.objectMouseOver.getBlockPos();
                Block block = this.theWorld.getBlockState(blockpos).getBlock();

                if (block.getMaterial() == Material.air) {
                    return;
                }

                item = block.getItem(this.theWorld, blockpos);

                if (item == null) {
                    return;
                }

                if (flag && GuiScreen.isCtrlKeyDown()) {
                    tileentity = this.theWorld.getTileEntity(blockpos);
                }

                Block block1 = item instanceof ItemBlock && !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
                i = block1.getDamageValue(this.theWorld, blockpos);
                flag1 = item.getHasSubtypes();
            } else {
                if (this.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || this.objectMouseOver.entityHit == null || !flag) {
                    return;
                }

                if (this.objectMouseOver.entityHit instanceof EntityPainting) {
                    item = Items.painting;
                } else if (this.objectMouseOver.entityHit instanceof EntityLeashKnot) {
                    item = Items.lead;
                } else if (this.objectMouseOver.entityHit instanceof EntityItemFrame) {
                    EntityItemFrame entityitemframe = (EntityItemFrame) this.objectMouseOver.entityHit;
                    ItemStack itemstack = entityitemframe.getDisplayedItem();

                    if (itemstack == null) {
                        item = Items.item_frame;
                    } else {
                        item = itemstack.getItem();
                        i = itemstack.getMetadata();
                        flag1 = true;
                    }
                } else if (this.objectMouseOver.entityHit instanceof EntityMinecart) {
                    EntityMinecart entityminecart = (EntityMinecart) this.objectMouseOver.entityHit;

                    switch (entityminecart.getMinecartType()) {
                        case FURNACE:
                            item = Items.furnace_minecart;
                            break;

                        case CHEST:
                            item = Items.chest_minecart;
                            break;

                        case TNT:
                            item = Items.tnt_minecart;
                            break;

                        case HOPPER:
                            item = Items.hopper_minecart;
                            break;

                        case COMMAND_BLOCK:
                            item = Items.command_block_minecart;
                            break;

                        default:
                            item = Items.minecart;
                    }
                } else if (this.objectMouseOver.entityHit instanceof EntityBoat) {
                    item = Items.boat;
                } else if (this.objectMouseOver.entityHit instanceof EntityArmorStand) {
                    item = Items.armor_stand;
                } else {
                    item = Items.spawn_egg;
                    i = EntityList.getEntityID(this.objectMouseOver.entityHit);
                    flag1 = true;

                    if (!EntityList.entityEggs.containsKey(i)) {
                        return;
                    }
                }
            }

            InventoryPlayer inventoryplayer = this.thePlayer.inventory;

            if (tileentity == null) {
                inventoryplayer.setCurrentItem(item, i, flag1, flag);
            } else {
                ItemStack itemstack1 = this.pickBlockWithNBT(item, i, tileentity);
                System.out.println(itemstack1.getTagCompound());
                inventoryplayer.setInventorySlotContents(inventoryplayer.currentItem, itemstack1);
            }

            if (flag) {
                int j = this.thePlayer.inventoryContainer.inventorySlots.size() - 9 + inventoryplayer.currentItem;
                this.playerController.sendSlotPacket(inventoryplayer.getStackInSlot(inventoryplayer.currentItem), j);
            }
        }
    }

    /**
     * Return an ItemStack with the NBTTag of the TileEntity ("Owner" if the block is a skull)
     *
     * @param itemIn       The item from the block picked
     * @param meta         Metadata of the item
     * @param tileEntityIn TileEntity of the block picked
     */
    private ItemStack pickBlockWithNBT(Item itemIn, int meta, TileEntity tileEntityIn) {
        ItemStack itemstack = new ItemStack(itemIn, 1, meta);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        tileEntityIn.writeToNBT(nbttagcompound);

        if (itemIn == Items.skull && nbttagcompound.hasKey("Owner")) {
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
            itemstack.setTagCompound(nbttagcompound3);
            return itemstack;
        } else {
            itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagString("(+NBT)"));
            nbttagcompound1.setTag("Lore", nbttaglist);
            itemstack.setTagInfo("display", nbttagcompound1);
            return itemstack;
        }
    }

    /**
     * adds core server Info (GL version , Texture pack, isModded, type), and the worldInfo to the crash report
     */
    public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash) {
        theCrash.getCategory().addCrashSectionCallable("启动的版本", new Callable<String>() {
            public String call() {
                return Minecraft.this.launchedVersion;
            }
        });
        theCrash.getCategory().addCrashSectionCallable("LWJGL版本", new Callable<String>() {
            public String call() {
                return Sys.getVersion();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("OpenGL版本", new Callable<String>() {
            public String call() {
                if (!Display.isCreated()) {
                    return "Pre-start crash";
                } else {
                    return GL11.glGetString(GL11.GL_RENDERER) + " GL 版本 " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR);
                }
            }
        });
        theCrash.getCategory().addCrashSectionCallable("OpenGL 功能支持", new Callable<String>() {
            public String call() {
                return "\n\t\t" + String.join("\n\t\t", OpenGlHelper.getLogText().split("\n"));
            }
        });
        theCrash.getCategory().addCrashSectionCallable("使用VBO", new Callable<String>() {
            public String call() {
                return Minecraft.this.gameSettings.useVbo ? "是" : "否";
            }
        });
        theCrash.getCategory().addCrashSectionCallable("已被修改", new Callable<String>() {
            public String call() {
                String s = ClientBrandRetriever.getClientModName();
                return !s.equals("vanilla") ? "肯定的; 客户端标识已被更改为 '" + s + "'" : (Minecraft.class.getSigners() == null ? "很有可能; Jar签名已失效" : "可能没有. Jar签名有效, 客户端标识未更改.");
            }
        });
        theCrash.getCategory().addCrashSectionCallable("类型", new Callable<String>() {
            public String call() {
                return "客户端 (map_client.txt)";
            }
        });
        theCrash.getCategory().addCrashSectionCallable("资源包", new Callable<String>() {
            public String call() {
                StringBuilder stringbuilder = new StringBuilder();

                for (String s : Minecraft.this.gameSettings.resourcePacks) {
                    if (stringbuilder.length() > 0) {
                        stringbuilder.append(", ");
                    }

                    stringbuilder.append(s);

                    if (Minecraft.this.gameSettings.incompatibleResourcePacks.contains(s)) {
                        stringbuilder.append(" (不支持)");
                    }
                }

                return stringbuilder.toString();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("选中的语言", new Callable<String>() {
            public String call() {
                return Minecraft.this.mcLanguageManager.getCurrentLanguage().toString();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("分析器断点", new Callable<String>() {
            public String call() {
                return Minecraft.this.mcProfiler.profilingEnabled ? Minecraft.this.mcProfiler.getNameOfLastSection() : "N/A (已关闭)";
            }
        });
        theCrash.getCategory().addCrashSectionCallable("CPU", new Callable<String>() {
            public String call() {
                return OpenGlHelper.getCpu();
            }
        });

        if (this.theWorld != null) {
            this.theWorld.addWorldInfoToCrashReport(theCrash);
        }

        theCrash.getCategory().addCrashSection("OptiFine版本", Config.getVersion());
        theCrash.getCategory().addCrashSection("OptiFine构建", Config.getBuild());
        if (Config.getGameSettings() != null) {
            theCrash.getCategory().addCrashSection("区块渲染距离", "" + Config.getChunkViewDistance());
            theCrash.getCategory().addCrashSection("Mipmaps (多级渐远纹理)", "" + Config.getMipmapLevels());
            theCrash.getCategory().addCrashSection("各向异性过滤", "" + Config.getAnisotropicFilterLevel());
            theCrash.getCategory().addCrashSection("抗锯齿", "" + Config.getAntialiasingLevel());
            theCrash.getCategory().addCrashSection("多重纹理", "" + Config.isMultiTexture());
        }
        theCrash.getCategory().addCrashSection("光影包", Shaders.getShaderPackName());
        theCrash.getCategory().addCrashSection("OpenGl版本", Config.openGlVersion);
        theCrash.getCategory().addCrashSection("OpenGl渲染器", Config.openGlRenderer);
        theCrash.getCategory().addCrashSection("OpenGl厂商", Config.openGlVendor);
        theCrash.getCategory().addCrashSection("CPU核心数", "" + Config.getAvailableProcessors());

        return theCrash;
    }

    /**
     * Return the singleton Minecraft instance for the game
     */
    public static Minecraft getMinecraft() {
        return theMinecraft;
    }

    public ListenableFuture<Object> scheduleResourcesRefresh() {
        return this.addScheduledTask(new Runnable() {
            public void run() {
                Minecraft.this.refreshResources();
            }
        });
    }


    /**
     * Return the current action's name
     */
    private String getCurrentAction() {
        return this.theIntegratedServer != null ? (this.theIntegratedServer.getPublic() ? "hosting_lan" : "singleplayer") : (this.currentServerData != null ? (this.currentServerData.isOnLAN() ? "playing_lan" : "multiplayer") : "out_of_game");
    }


    /**
     * Used in the usage snooper.
     */
    public static int getGLMaximumTextureSize() {
        for (int i = 16384; i > 0; i >>= 1) {
            GL11.glTexImage2D(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_RGBA, i, i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            int j = GL11.glGetTexLevelParameteri(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);

            if (j != 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns whether snooping is enabled or not.
     */
    public boolean isSnooperEnabled() {
        return this.gameSettings.snooperEnabled;
    }

    /**
     * Set the current ServerData instance.
     */
    public void setServerData(ServerData serverDataIn) {
        this.currentServerData = serverDataIn;
    }

    public ServerData getCurrentServerData() {
        return this.currentServerData;
    }

    public boolean isIntegratedServerRunning() {
        return this.integratedServerIsRunning;
    }

    /**
     * Returns true if there is only one player playing, and the current server is the integrated one.
     */
    public boolean isSingleplayer() {
        return this.integratedServerIsRunning && this.theIntegratedServer != null;
    }

    /**
     * Returns the currently running integrated server
     */
    public IntegratedServer getIntegratedServer() {
        return this.theIntegratedServer;
    }

    public static void stopIntegratedServer() {
        if (theMinecraft != null) {
            IntegratedServer integratedserver = theMinecraft.getIntegratedServer();

            if (integratedserver != null) {
                integratedserver.stopServer();
            }
        }
    }

    /**
     * Gets the system time in milliseconds.
     */
    public static long getSystemTime() {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }

    /**
     * Returns whether we're in full screen or not.
     */
    public boolean isFullScreen() {
        return this.fullscreen;
    }

    public Session getSession() {
        return this.session;
    }

    public PropertyMap getTwitchDetails() {
        return this.twitchDetails;
    }

    /**
     * Return the player's GameProfile properties
     */
    public PropertyMap getProfileProperties() {
        if (this.profileProperties.isEmpty()) {
            GameProfile gameprofile = this.getSessionService().fillProfileProperties(this.session.getProfile(), false);
            this.profileProperties.putAll(gameprofile.getProperties());
        }

        return this.profileProperties;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public TextureManager getTextureManager() {
        return this.renderEngine;
    }

    public IResourceManager getResourceManager() {
        return this.mcResourceManager;
    }

    public ResourcePackRepository getResourcePackRepository() {
        return this.mcResourcePackRepository;
    }

    public LanguageManager getLanguageManager() {
        return this.mcLanguageManager;
    }

    public TextureMap getTextureMapBlocks() {
        return this.textureMapBlocks;
    }

    public boolean isJava64bit() {
        return this.jvm64bit;
    }

    public boolean isGamePaused() {
        return this.isGamePaused;
    }

    public SoundHandler getSoundHandler() {
        return this.mcSoundHandler;
    }

    public MusicTicker.MusicType getAmbientMusicType() {
        return this.thePlayer != null ? (this.thePlayer.worldObj.provider instanceof WorldProviderHell ? MusicTicker.MusicType.NETHER : (this.thePlayer.worldObj.provider instanceof WorldProviderEnd ? (BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END) : (this.thePlayer.capabilities.isCreativeMode && this.thePlayer.capabilities.allowFlying ? MusicTicker.MusicType.CREATIVE : MusicTicker.MusicType.GAME))) : MusicTicker.MusicType.MENU;
    }

    public void dispatchKeypresses(char c0, int i) {
        if (i != 0 && !Keyboard.isRepeatEvent()) {
            if (!(this.currentScreen instanceof GuiControls) || ((GuiControls) this.currentScreen).time <= getSystemTime() - 20L) {
                if (Keyboard.getEventKeyState()) {
                    if (i == this.gameSettings.keyBindFullscreen.getKeyCode()) {
                        this.toggleFullscreen();
                    } else if (i == this.gameSettings.keyBindScreenshot.getKeyCode()) {
                        this.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(this.mcDataDir, this.displayWidth, this.displayHeight, this.framebufferMc));
                    }
                }
            }
        }
    }

    public MinecraftSessionService getSessionService() {
        return this.sessionService;
    }

    public SkinManager getSkinManager() {
        return this.skinManager;
    }

    public Entity getRenderViewEntity() {
        return this.renderViewEntity;
    }

    public void setRenderViewEntity(Entity viewingEntity) {
        this.renderViewEntity = viewingEntity;
        this.entityRenderer.loadEntityShader(viewingEntity);
    }

    public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule) {
        Validate.notNull(callableToSchedule);

        if (!this.isCallingFromMinecraftThread()) {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callableToSchedule);

            synchronized (this.scheduledTasks) {
                this.scheduledTasks.add(listenablefuturetask);
                return listenablefuturetask;
            }
        } else {
            try {
                return Futures.immediateFuture(callableToSchedule.call());
            } catch (Exception exception) {
                return Futures.immediateFailedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        Validate.notNull(runnableToSchedule);
        return this.addScheduledTask(Executors.callable(runnableToSchedule));
    }

    public boolean isCallingFromMinecraftThread() {
        return Thread.currentThread() == this.mcThread;
    }

    public BlockRendererDispatcher getBlockRendererDispatcher() {
        return this.blockRenderDispatcher;
    }

    public RenderManager getRenderManager() {
        return this.renderManager;
    }

    public RenderItem getRenderItem() {
        return this.renderItem;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public static int getDebugFPS() {
        return debugFPS;
    }

    /**
     * Return the FrameTimer's instance
     */
    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public static Map<String, String> getSessionInfo() {
        Map<String, String> map = Maps.newHashMap();
        map.put("X-Minecraft-Username", getMinecraft().getSession().getUsername());
        map.put("X-Minecraft-UUID", getMinecraft().getSession().getPlayerID());
        map.put("X-Minecraft-Version", "1.8.9");
        return map;
    }

    public void setSession(Session session) {
        this.session = session;
//        Cloud.getInstance().updateNames();
    }

    private static final class McKeybindHandler implements InputEvents.KeyboardListener {

        @Override
        public void onKeyEvent(InputEvents.KeyEvent event) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) {
                return;
            }
            if (mc.currentScreen != null) {
                return;
            }
            if (event.lwjgl2KeyCode > Keyboard.KEY_NONE) {
                KeyBinding.setKeyBindState(event.lwjgl2KeyCode, event.action != InputEvents.KeyAction.RELEASED);
            }
        }
    }


    public boolean isInWorld() {
        return Minecraft.getMinecraft().theWorld != null;
    }

    public float getRenderPartialTicks() {
        return timer.renderPartialTicks;
    }

    public World getWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
