package tech.konata.phosphate.command.impl;

import net.minecraft.util.EnumChatFormatting;
import tech.konata.phosphate.command.Command;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.settings.Setting;

/**
 * @author IzumiiKonata
 * @since 6/24/2023 10:21 PM
 */
public class Set extends Command {

    public Set() {
        super("Set", "set <module> <setting> <value>", "set <module> <setting> <value>", "s", "set");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            this.print(EnumChatFormatting.RED + "Usage: " + this.getUsage());
        } else {
            String moduleName = args[0]/*.replaceAll("-", " ")*/;
            boolean moduleFound = false;
            for (Module module : ModuleManager.getModules()) {
                if (module.nameEquals(moduleName)) {
                    moduleFound = true;
                    this.iterInto(module, args);
                }
            }

            if (!moduleFound) {

                for (Module module : WidgetsManager.getWidgets()) {
                    if (module.nameEquals(moduleName)) {
                        moduleFound = true;
                        this.iterInto(module, args);
                    }
                }

                if (!moduleFound) {
                    this.print(EnumChatFormatting.RED + "Module " + moduleName + " not found!");
                }
            }
        }
    }

    private void iterInto(Module module, String[] args) {
        String settingName = args[1]/*.replaceAll("\\^", " ")*/;
        boolean settingFound = false;
        for (Setting<?> setting : module.getSettings()) {
            if (setting.getInternalName().equalsIgnoreCase(settingName)) {

                settingFound = true;
                if (setting instanceof BooleanSetting) {
                    ((BooleanSetting) setting).setValue(Boolean.parseBoolean(args[2]));
                    this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                } else if (setting instanceof ModeSetting) {
                    ((ModeSetting<?>) setting).setMode(args[2]);
                    this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                } else if (setting instanceof NumberSetting) {
                    setting.loadValue(args[2]);
                    this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                } else {
                    setting.loadValue(args[2]);
                    this.print("Set setting " + setting.getName().get() + "'s value to " + setting.getValue());
                }

            }
        }

        if (!settingFound) {
            this.print(EnumChatFormatting.RED + "Setting " + settingName + " not found!");
        }
    }
}
