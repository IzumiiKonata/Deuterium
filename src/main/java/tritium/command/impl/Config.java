package tritium.command.impl;

import lombok.SneakyThrows;
import tritium.command.CommandHandler;
import tritium.ncm.music.CloudMusic;
import tritium.command.Command;
import tritium.management.ModuleManager;
import tritium.module.Module;
import tritium.settings.ClientSettings;
import tritium.settings.Setting;

public class Config extends Command {


    public Config() {
        super("Config", "Save or reload config.", "config <save/reload/load <profile name>>", "c", "config");
    }

    @SneakyThrows
    @CommandHandler(paramNames = "save/reload")
    public void execute(String op) {
        switch (op) {
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
                CloudMusic.initNCM();
                this.print("Config reloaded.");
                break;
            }
        }
    }
}
