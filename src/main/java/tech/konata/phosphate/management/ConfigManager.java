package tech.konata.phosphate.management;

import com.google.gson.*;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.ncm.OptionsUtil;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.alt.Alt;
import tech.konata.phosphate.utils.alt.AltManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.dto.PlayList;
import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.rendering.texture.Textures;
import tech.konata.phosphate.screens.clickgui.panels.MusicPanel;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.network.HttpClient;
import tech.konata.phosphate.widget.Widget;
import tech.konata.phosphate.widget.impl.GifTextureWidget;
import tech.konata.phosphate.widget.impl.StaticTextureWidget;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author IzumiiKonata
 * @since 2023/12/23
 */
public class ConfigManager extends AbstractManager {

    public ConfigManager() {
        super("ConfigManager");
    }

    public static final File configDir = new File(Minecraft.getMinecraft().mcDataDir, Phosphate.NAME);
    private final File ALT = new File(configDir, "Alts.json");
    public String currentConfig = "Default";

    static final Timer configSavingScheduler = new Timer();

    static {
        configSavingScheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Phosphate.getInstance().getConfigManager().stop();
            }
        }, 1000 * 60 * 5, 1000 * 60 * 5);
    }

    @Getter
    private boolean firstTime = false;

    @Override
    @SneakyThrows
    public void init() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
            firstTime = true;
        }

        File curConfigFile = new File(configDir, "Config.json");

        if (!curConfigFile.exists()) {
            curConfigFile.createNewFile();

            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(curConfigFile), StandardCharsets.UTF_8));

            writer.write("{\"Config\": \"Default\",\"CMCookie\": \"\"}");

            writer.flush();
            writer.close();
            firstTime = true;
        }

        File configsFile = new File(configDir, "Profiles");

        if (!configsFile.exists()) {
            configsFile.mkdir();
            firstTime = true;
        }

        File musicCacheDir = new File(configDir, "MusicCache");

        if (!musicCacheDir.exists()) {
            musicCacheDir.mkdir();
        }

        File dynamicCoverDir = new File(configDir, "DynamicCover");

        if (!dynamicCoverDir.exists()) {
            dynamicCoverDir.mkdir();
        }

        Gson gson = new Gson();

        @Cleanup
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(curConfigFile), StandardCharsets.UTF_8));
        JsonObject element = gson.fromJson(reader, JsonObject.class);

        JsonElement config = element.get("Config");
        if (config != null && !config.isJsonNull()) {
            currentConfig = config.getAsString();
        }

        JsonElement cookie = element.get("CMCookie");
        if (cookie != null && !cookie.isJsonNull()) {
            String cmCookie = cookie.getAsString();
            CloudMusic.initialize(cmCookie);
//            MultiThreadingUtil.runAsync(this::refreshNCM);
        }

        this.loadConfig();
        this.loadAlts();
    }

    public void refreshNCM() {
        try {
            CloudMusic.initialize(OptionsUtil.getCookie());
            MusicPanel.profile = CloudMusic.getUserProfile();

            if (MusicPanel.profile == null)
                return;

            List<PlayList> playLists = new ArrayList<>();

            int page = 0;

            while (true) {

                List<PlayList> pl = MusicPanel.profile.playLists(page, 30);

                if (pl == null || pl.isEmpty())
                    break;

                playLists.addAll(pl);

                page += 1;
            }

            MusicPanel.playLists = playLists;
            MusicPanel.likeList = CloudMusic.likeList();

            if (!MusicPanel.playLists.isEmpty()) {
                MusicPanel.selectedList = MusicPanel.playLists.get(0);
//
//                MultiThreadingUtil.runAsync(new Runnable() {
//                    @Override
//                    @SneakyThrows
//                    public void run() {
//
//                        for (PlayList playList : MusicPanel.playLists) {
//                            Location location = MusicPanel.getPlaylistCoverLocation(playList);
//                            Location locationSmall = MusicPanel.getPlaylistCoverLocationSmall(playList);
//
//                            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
//                            AsyncGLContext.submit(() -> {
//                                if (textureManager.getTexture(location) != null)
//                                    textureManager.deleteTexture(location);
//
//                                if (textureManager.getTexture(locationSmall) != null)
//                                    textureManager.deleteTexture(locationSmall);
//                            });
//
//                            BufferedImage img = NativeBackedImage.make(HttpClient.downloadStream(playList.coverUrl + "?param=128y128"));
//
//                            Textures.loadTextureAsyncly(location, img);
//
//                            BufferedImage imgSmall = NativeBackedImage.make(HttpClient.downloadStream(playList.coverUrl + "?param=40y40"));
//                            Textures.loadTextureAsyncly(locationSmall, imgSmall);
//
//                        }
//
//                    }
//                });
            }

        } catch (Throwable t) {
//            CloudMusic.api.setCookie("");
            t.printStackTrace();
        }
    }

    @SneakyThrows
    public static JsonObject preloadGlobalSettings() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
        }

        File curConfigFile = new File(configDir, "Config.json");

        if (!curConfigFile.exists()) {
            return new JsonObject();
        }

        File configsFile = new File(configDir, "Profiles");

        if (!configsFile.exists()) {
            return new JsonObject();
        }

        Gson gson = new Gson();

        try {
            @Cleanup
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(curConfigFile), StandardCharsets.UTF_8));
            JsonObject element = gson.fromJson(reader, JsonObject.class);

            String currentConfig = element.get("Config").getAsString();

            //加载模块设置
            //模块配置目录

            File configFile = new File(configsFile, currentConfig + ".json");

            if (!configFile.exists()) {
                return new JsonObject();
            }

            @Cleanup
            Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            JsonObject config = gson.fromJson(fileReader, JsonObject.class);

            return config.get("Settings").getAsJsonObject();
        } catch (Throwable t) {
            return new JsonObject();
        }
    }

    @SneakyThrows
    public void loadConfig() {

        WidgetsManager.getWidgets().removeIf(w -> w instanceof StaticTextureWidget || w instanceof GifTextureWidget);

        //加载模块设置
        //模块配置目录

        File configsFile = new File(configDir, "Profiles");
        File configFile = new File(configsFile, currentConfig + ".json");

        if (!configFile.exists()) {
            configFile.createNewFile();
            return;
        }

        try {
            Gson gson = new Gson();

            @Cleanup
            Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            JsonObject config = gson.fromJson(fileReader, JsonObject.class);

            JsonObject modules = config.get("Modules").getAsJsonObject();

            modules.entrySet().forEach(m -> {
                Optional<Module> module = client.getModuleManager().getModuleByName(m.getKey());

                module.ifPresent(mod -> {
                    mod.loadConfig(m.getValue().getAsJsonObject());
                });

                if (!module.isPresent()) {
                    this.logger.error("Module {} is missing!", m.getKey());
                }
            });

            JsonObject widgets = config.get("Widgets").getAsJsonObject();

            widgets.entrySet().forEach(w -> {
                if (w.getKey().equals("Texture")) {
                    JsonObject jObj = w.getValue().getAsJsonObject();

                    String path = jObj.get("Path").getAsString();

                    File file = new File(path);

                    if (file.exists()) {
                        StaticTextureWidget stw = new StaticTextureWidget(file);
                        stw.loadConfig(jObj);
                        WidgetsManager.getWidgets().add(stw);
                    }
                } else if (w.getKey().equals("GifTexture")) {
                    JsonObject jObj = w.getValue().getAsJsonObject();

                    String path = jObj.get("Path").getAsString();

                    File file = new File(path);

                    if (file.exists()) {
                        GifTextureWidget gte = new GifTextureWidget(file);
                        gte.loadConfig(jObj);
                        WidgetsManager.getWidgets().add(gte);
                    }
                } else {
                    Optional<Widget> widget = client.getWidgetsManager().getWidgetByName(w.getKey());


                    widget.ifPresent(wid -> {
                        wid.loadConfig(w.getValue().getAsJsonObject());
                    });

                    if (!widget.isPresent()) {
                        this.logger.error("Widget {} is missing!", w.getKey());
                    }
                }
            });
        } catch (Throwable ignored) {

        }

    }

    @Override
    @SneakyThrows
    public void stop() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
        }

        File curConfigFile = new File(configDir, "Config.json");
        curConfigFile.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(curConfigFile), StandardCharsets.UTF_8));

        JsonObject obj = new JsonObject();

        obj.addProperty("Config", currentConfig);

        obj.addProperty("CMCookie", OptionsUtil.getCookie());
        writer.write(gson.toJson(obj));

        writer.flush();
        writer.close();

        //模块配置目录
        File configsFile = new File(configDir, "Profiles");
        File configFile = new File(configsFile, currentConfig + ".json");
        JsonObject jsonObject = new JsonObject();

        JsonObject modules = new JsonObject();
        ModuleManager.getModules().forEach(module -> {
            modules.add(module.getInternalName(), module.saveConfig());
        });
        jsonObject.add("Modules", modules);

        JsonObject widgets = new JsonObject();
        WidgetsManager.getWidgets().forEach(widget -> {
            widgets.add(widget.getInternalName(), widget.saveConfig());
        });
        jsonObject.add("Widgets", widgets);

        JsonObject settings = new JsonObject();
        GlobalSettings.getSettings().forEach(val -> {
            settings.addProperty(val.getInternalName(), val.getValueForConfig());
        });
        jsonObject.add("Settings", settings);

        configFile.createNewFile();

        Files.write(configFile.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        this.saveAlts();

        if (!Phosphate.getInstance().isObfuscated()) {
            logger.info("Config saved.");
        }

    }

    public void loadAlts() {
        try {
            if (!ALT.exists()) {
                PrintWriter printWriter = new PrintWriter(ALT, "UTF-8");
                printWriter.println("[]");
                printWriter.close();
            } else {
                AltManager.getAlts().clear();
                Gson gson = (new GsonBuilder()).create();
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ALT), StandardCharsets.UTF_8));
                JsonArray array = gson.fromJson(reader, JsonArray.class);
                AltManager.getAlts().clear();
                for (JsonElement jsonElement : array) {
                    Alt alt = gson.fromJson(jsonElement, Alt.class);
                    AltManager.getAlts().add(alt);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            if (ALT.exists())
                ALT.delete();
        }
    }

    @SneakyThrows
    public void saveAlts() {
        try {
            PrintWriter printWriter = new PrintWriter(ALT, "UTF-8");
            Gson gson = (new GsonBuilder().setPrettyPrinting()).create();
            printWriter.println(gson.toJson(AltManager.getAlts()));
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
