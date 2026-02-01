package tritium.management;

import com.google.gson.*;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import tritium.Tritium;
import tritium.command.CommandValues;
import tritium.utils.alt.Alt;
import tritium.utils.alt.AltManager;
import tritium.settings.ClientSettings;
import tritium.utils.json.JsonUtils;
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
    public static final File commandValuesFile = new File(configDir, "Commands.json");
    public static final File keybindCommandsFile = new File(configDir, "KeybindCommands.json");

    static final Timer configSavingScheduler = new Timer();

    static {
        // 定时保存
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
        if (!configDir.exists()) {
            configDir.mkdir();
        }

        this.loadConfig();
        this.loadAlts();
    }

    @SneakyThrows
    public static JsonObject preloadGlobalSettings() {
        if (!configDir.exists()) {
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

        try {
            if (configFile.exists()) {
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
            }


            if (commandValuesFile.exists())
                CommandValues.setValues(JsonUtils.parse(new FileReader(commandValuesFile), CommandValues.Values.class));

            if (keybindCommandsFile.exists()) {
                JsonObject keybinds = JsonUtils.parse(new FileReader(keybindCommandsFile), JsonObject.class);
                for (Map.Entry<String, JsonElement> element : keybinds.entrySet()) {
                    int key = Integer.parseInt(element.getKey());
                    String cmd = element.getValue().getAsString();

                    if (cmd.isEmpty())
                        continue;

                    CommandManager.getKeyToCommandMap().put(key, cmd);
                }
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }

        CommandValues.registerCommands();
    }

    @Override
    @SneakyThrows
    public void stop() {
        if (!configDir.exists()) {
            configDir.mkdir();
        }

        {
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

            Files.writeString(configFile.toPath(), JsonUtils.toJsonString(jsonObject), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }

        this.saveAlts();

        Files.writeString(commandValuesFile.toPath(), JsonUtils.toJsonString(CommandValues.getValues()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        {
            JsonObject objKeys = new JsonObject();

            CommandManager.getKeyToCommandMap().forEach((key, cmd) -> objKeys.addProperty(key.toString(), cmd));
            Files.writeString(keybindCommandsFile.toPath(), JsonUtils.toJsonString(objKeys), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }

        if (!Tritium.getInstance().isObfuscated()) {
            logger.info("Config saved.");
        }

    }

    public void loadAlts() {
        try {
            if (!ALT.exists()) {
                PrintWriter printWriter = new PrintWriter(ALT, StandardCharsets.UTF_8);
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
            PrintWriter printWriter = new PrintWriter(ALT, StandardCharsets.UTF_8);
            printWriter.println(JsonUtils.toJsonString(AltManager.getAlts()));
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
