package tritium.command.impl;

import lombok.SneakyThrows;
import tritium.command.Command;
import tritium.management.ConfigManager;
import tritium.management.ModuleManager;
import tritium.module.Module;
import tritium.settings.ClientSettings;
import tritium.settings.Setting;


import java.io.File;
import java.io.FileWriter;

public class Config extends Command {


    public Config() {
        super("Config", "Save or reload config.", "config <save/reload/load <profile name>>", "c", "config");
    }

    @Override
    @SneakyThrows
    public void execute(String[] args) {
        if (args.length == 0) {
            this.printUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "create": {
                // creates a new config.
                if (args.length < 2) {
                    this.printUsage();
                    return;
                }

                String config = args[1];

                File configsFile = new File(ConfigManager.configDir, "Profiles");
                File configFile = new File(configsFile, config + ".json");
                if (configFile.exists()) {
                    this.print("That profile is already exist!");
                    return;
                } else {
                    configFile.createNewFile();

                    FileWriter writer = new FileWriter(configFile);

                    // dummy config
                    writer.write("{\n" +
                            "\t\"Modules\": {\n" +
                            "\n" +
                            "\t},\n" +
                            "\t\"Widgets\": {\n" +
                            "\n" +
                            "\t},\n" +
                            "\t\"Settings\": {\n" +
                            "\t\n" +
                            "\t}\n" +
                            "}");
                    writer.flush();
                    writer.close();
                }

                //
                client.getConfigManager().stop();
                for (Module module : ModuleManager.getModules()) {
                    if (module.isEnabled())
                        module.setEnabled(false);
                    module.setKeyBind(0);
                    for (Setting<?> setting : module.getSettings()) {
                        setting.reset();
                    }
                }
                client.getConfigManager().currentConfig = config;
                client.getConfigManager().loadConfig();
                client.getConfigManager().loadAlts();

                this.print("Config created: " + config);
                break;
            }

            case "save": {
                if (args.length > 1) {
                    String config = args[1];

                    client.getConfigManager().stop();
                    client.getConfigManager().currentConfig = config;
                    client.getConfigManager().stop();
                    client.getConfigManager().saveAlts();
                    this.print("Config saved: " + config);
                } else {
                    client.getConfigManager().stop();
                    this.print("Config saved: " + client.getConfigManager().currentConfig);
                }
                break;
            }

            case "load": {
                if (args.length < 2) {
                    this.printUsage();
                    return;
                }

                String config = args[1];

                File configsFile = new File(client.getConfigManager().configDir, "Profiles");
                File configFile = new File(configsFile, config + ".json");
                if (!configFile.exists()) {
                    this.print("That profile isn't exist!");
                    return;
                }

                client.getConfigManager().stop();
                for (Module module : ModuleManager.getModules()) {
                    if (module.isEnabled())
                        module.setEnabled(false);
                    module.setKeyBind(0);
                    for (Setting<?> setting : module.getSettings()) {
                        setting.reset();
                    }
                }
                client.getConfigManager().currentConfig = config;
                client.getConfigManager().loadConfig();
                client.getConfigManager().loadAlts();

                ClientSettings.initialize();
                this.print("Config loaded: " + config);
                break;
            }

            case "reload": {
                for (Module module : ModuleManager.getModules()) {
                    if (module.isEnabled())
                        module.setEnabled(false);
                    module.setKeyBind(0);
                    for (Setting<?> setting : module.getSettings()) {
                        setting.reset();
                    }
                }
                client.getConfigManager().init();

                ClientSettings.initialize();
                this.print("Config reloaded: " + client.getConfigManager().currentConfig);
                break;
            }
        }
    }
}
