package tritium.command.impl;

import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import tritium.command.Command;
import tritium.management.Localizer;
import tritium.management.ModuleManager;
import tritium.module.Module;

import java.util.Optional;


/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:49 PM
 */
public class Bind extends Command {

    public Bind() {
        super("Bind", "Binds a module to a key.", "bind <module> <key>", "bind");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {

            String option = args[0];

            // list all binds
            if ("list".equals(option)) {
                for (Module module : ModuleManager.getModules()) {
                    if (module.getKeyBind() != 0) {
                        this.print(EnumChatFormatting.GOLD + module.getInternalName() + EnumChatFormatting.GREEN + ": " + EnumChatFormatting.RESET + Keyboard.getKeyName(module.getKeyBind()));
                    }
                }
            } else {
                // else we're going to print the bounded key of the specified module
                String moduleName = args[0];

                Optional<Module> m = client.getModuleManager().getModuleByName(moduleName);

                m.ifPresent(md -> {
                    this.print(EnumChatFormatting.GOLD + md.getInternalName() + EnumChatFormatting.GREEN + ": " + EnumChatFormatting.RESET + Keyboard.getKeyName(md.getKeyBind()));
                });

                if (!m.isPresent()) {
                    this.print(EnumChatFormatting.RED + Localizer.format("command.bind.module not found", EnumChatFormatting.GOLD + moduleName + EnumChatFormatting.RED));
                }

            }

            return;
        } else if (args.length < 2) {
            this.printUsage();
            return;
        }

        // bind the specified module to the specified key
        String moduleName = args[0];

        Optional<Module> m = client.getModuleManager().getModuleByName(moduleName);

        m.ifPresent(md -> {
            String keyName = args[1];

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
        });

        if (!m.isPresent()) {
            this.print(EnumChatFormatting.RED + Localizer.format("command.bind.module not found", EnumChatFormatting.GOLD + moduleName + EnumChatFormatting.RED));
        }
    }
}
