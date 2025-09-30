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
            case "save": {
                client.getConfigManager().stop();
                client.getConfigManager().saveAlts();
                this.print("Config saved.");
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
                this.print("Config reloaded.");
                break;
            }
        }
    }
}
