package tech.konata.phosphate.command.impl;

import net.minecraft.util.EnumChatFormatting;
import tech.konata.phosphate.command.Command;
import tech.konata.phosphate.module.Module;

import java.util.Optional;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:43 PM
 */
public class Toggle extends Command {

    public Toggle() {
        super("Toggle", "Toggles the module.", "toggle <module>", "t");
    }

    @Override
    public void execute(String[] args) {

        if (args.length < 1) {
            this.printUsage();
            return;
        }

        String moduleName = args[0];

        Optional<Module> m = client.getModuleManager().getModuleByName(moduleName);

        m.ifPresent(md -> {
            md.toggle();
            this.print((md.isEnabled() ? (EnumChatFormatting.GREEN + "Enabled ") : (EnumChatFormatting.RED + "Disabled ")) + EnumChatFormatting.RESET + "module " + EnumChatFormatting.GOLD + md.getInternalName() + EnumChatFormatting.RESET + ".");

        });

        if (!m.isPresent()) {
            this.print(EnumChatFormatting.RED + "Module " + EnumChatFormatting.GOLD + moduleName + EnumChatFormatting.RED + " not found!");
        }

    }
}
