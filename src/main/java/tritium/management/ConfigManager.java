package tritium.management;

import com.google.gson.*;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import tritium.Tritium;
import tritium.utils.alt.Alt;
import tritium.utils.alt.AltManager;
import tritium.module.Module;
import tritium.settings.ClientSettings;
import tritium.utils.json.JsonUtils;
import tritium.widget.Widget;
import tritium.widget.impl.GifTextureWidget;
import tritium.widget.impl.StaticTextureWidget;

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

    public static final File configDir = new File(Minecraft.getMinecraft().mcDataDir, Tritium.NAME);
    private final File ALT = new File(configDir, "Alts.json");

    public static final File configFile = new File(configDir, "Config.json");

    static final Timer configSavingScheduler = new Timer();

    static {
        configSavingScheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Tritium.getInstance().getConfigManager().stop();
            }
        }, 1000 * 60 * 5, 1000 * 60 * 5);
    }

    @Override
    @SneakyThrows
    public void init() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
        }

        this.loadConfig();
        this.loadAlts();
    }

    @SneakyThrows
    public static JsonObject preloadGlobalSettings() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
        }

        try {
            File configFile = ConfigManager.configFile;

            if (!configFile.exists()) {
                return new JsonObject();
            }

            @Cleanup
            Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            JsonObject config = JsonUtils.toJsonObject(fileReader);

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

        try {
            @Cleanup
            Reader fileReader = new BufferedReader(new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8));
            JsonObject config = JsonUtils.toJsonObject(fileReader);

            JsonObject modules = config.get("Modules").getAsJsonObject();

            modules.entrySet().forEach(m -> {
                client.getModuleManager().getModuleByName(m.getKey()).ifPresentOrElse(mod -> {
                    mod.loadConfig(m.getValue().getAsJsonObject());
                }, () -> {
                    this.logger.error("Module {} is missing!", m.getKey());
                });
            });

            JsonObject widgets = config.get("Widgets").getAsJsonObject();

            widgets.entrySet().forEach(w -> {

                client.getWidgetsManager().getWidgetByName(w.getKey()).ifPresentOrElse(wid -> {
                    wid.loadConfig(w.getValue().getAsJsonObject());
                }, () -> {
                    this.logger.error("Widget {} is missing!", w.getKey());
                });

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

        File configFile = ConfigManager.configFile;
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
        ClientSettings.getSettings().forEach(val -> {
            settings.addProperty(val.getInternalName(), val.getValueForConfig());
        });
        jsonObject.add("Settings", settings);

        configFile.createNewFile();

        Files.write(configFile.toPath(), JsonUtils.toJsonString(jsonObject).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        this.saveAlts();

        if (!Tritium.getInstance().isObfuscated()) {
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

                Reader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(ALT.toPath()), StandardCharsets.UTF_8));
                JsonArray array = JsonUtils.toJsonArray(reader);
                AltManager.getAlts().clear();
                for (JsonElement jsonElement : array) {
                    Alt alt = JsonUtils.parse(jsonElement, Alt.class);
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
            printWriter.println(JsonUtils.toJsonString(AltManager.getAlts()));
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
