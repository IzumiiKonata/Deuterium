package tritium.command.impl;

import net.minecraft.util.EnumChatFormatting;
import tritium.command.Command;
import tritium.command.CommandHandler;
import tritium.management.ModuleManager;
import tritium.management.WidgetsManager;
import tritium.module.Module;
import tritium.settings.BooleanSetting;
import tritium.settings.ModeSetting;
import tritium.settings.Setting;

/**
 * @author IzumiiKonata
 * @since 6/24/2023 10:21 PM
 */
public class Set extends Command {

    public Set() {
        super("Set", "set <module> <setting> <value>", "set <module> <setting> <value>", "s", "set");
    }

    @CommandHandler(paramNames = {"module name", "setting name", "value"})
    public void execute(String moduleName, String settingName, String value) {
        boolean moduleFound = false;
        for (Module module : ModuleManager.getModules()) {
            if (module.nameEquals(moduleName)) {
                moduleFound = true;
                this.iterInto(module, settingName, value);
            }
        }

        if (!moduleFound) {

            for (Module module : WidgetsManager.getWidgets()) {
                if (module.nameEquals(moduleName)) {
                    moduleFound = true;
                    this.iterInto(module, settingName, value);
                }
            }

            if (!moduleFound) {
                this.print(EnumChatFormatting.RED + "Module " + moduleName + " not found!");
            }
        }
    }

    private void iterInto(Module module, String settingName, String value) {
        boolean settingFound = false;
        for (Setting<?> setting : module.getSettings()) {
            if (setting.getInternalName().equalsIgnoreCase(settingName)) {

                settingFound = true;
                switch (setting) {
                    case BooleanSetting booleanSetting -> {
                        booleanSetting.setValue(Boolean.parseBoolean(value));
                        this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                    }
                    case ModeSetting modeSetting -> {
                        modeSetting.setMode(value);
                        this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                    }
                    default -> {
                        setting.loadValue(value);
                        this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                    }
                }

            }
        }

        if (!settingFound) {
            this.print(EnumChatFormatting.RED + "Setting " + settingName + " not found!");
        }
    }
}
