package tritium.command.impl;

import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import tritium.command.Command;
import tritium.command.CommandHandler;
import tritium.management.Localizer;
import tritium.management.ModuleManager;
import tritium.module.Module;


/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:49 PM
 */
public class Bind extends Command {

    public Bind() {
        super("Bind", "Binds a module to a key.", "bind <module> <key>", "bind");
    }

    @CommandHandler(paramNames = {"list/<module name>"})
    public void operation(String op) {
        // list all binds
        if ("list".equals(op)) {
            for (Module module : ModuleManager.getModules()) {
                if (module.getKeyBind() != 0) {
                    this.print(EnumChatFormatting.GOLD + module.getInternalName() + EnumChatFormatting.GREEN + ": " + EnumChatFormatting.RESET + Keyboard.getKeyName(module.getKeyBind()));
                }
            }
        } else {
            // else we're going to print the key of the specified module
            client.getModuleManager().getModuleByName(op).ifPresentOrElse(
                    md -> this.print(EnumChatFormatting.GOLD + md.getInternalName() + EnumChatFormatting.GREEN + ": " + EnumChatFormatting.RESET + Keyboard.getKeyName(md.getKeyBind())), () -> this.print(EnumChatFormatting.RED + Localizer.format("command.bind.module not found", EnumChatFormatting.GOLD + op + EnumChatFormatting.RED))
            );

        }
    }

    @CommandHandler(paramNames = {"module name", "key name"})
    public void bind(String moduleName, String keyName) {
        // bind the specified module to the specified key

        client.getModuleManager().getModuleByName(moduleName)
                .ifPresentOrElse(
            md -> {
                        if (keyName.startsWith("mouse")) {
                            try {
                                int i = Integer.parseInt(keyName.substring(5));
                                md.setKeyBind(i - 101);
                                this.print(
                                        EnumChatFormatting.GREEN +
                                                Localizer.format(
                                                        "command.bind.module bound to mouse button",
                                                        EnumChatFormatting.GOLD + md.getInternalName() + EnumChatFormatting.GREEN,
                                                        EnumChatFormatting.RESET + String.valueOf(i) + EnumChatFormatting.GREEN
                                                )
                                );
                                return;
                            } catch (Exception e) {
                            }
                        }

                        md.setKeyBind(Keyboard.getKeyIndex(keyName.toUpperCase()));

                        this.print(EnumChatFormatting.GREEN + Localizer.format("command.bind.module bound to key",
                                EnumChatFormatting.GOLD + md.getInternalName() + EnumChatFormatting.GREEN,
                                EnumChatFormatting.RESET + Keyboard.getKeyName(Keyboard.getKeyIndex(keyName.toUpperCase())
                                )));
                    }, () -> this.print(EnumChatFormatting.RED + Localizer.format("command.bind.module not found", EnumChatFormatting.GOLD + moduleName + EnumChatFormatting.RED))
                );

    }

}
