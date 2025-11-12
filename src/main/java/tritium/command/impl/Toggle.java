package tritium.command.impl;

import net.minecraft.util.EnumChatFormatting;
import tritium.command.Command;
import tritium.command.CommandHandler;
import tritium.module.Module;

import java.util.Optional;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:43 PM
 */
public class Toggle extends Command {

    public Toggle() {
        super("Toggle", "Toggles the module.", "toggle <module>", "t");
    }

    @CommandHandler(paramNames = {"module name"})
    public void execute(String moduleName) {
        client.getModuleManager().getModuleByName(moduleName).ifPresentOrElse(md -> {
            md.toggle();
            this.print((md.isEnabled() ? (EnumChatFormatting.GREEN + "Enabled ") : (EnumChatFormatting.RED + "Disabled ")) + EnumChatFormatting.RESET + "module " + EnumChatFormatting.GOLD + md.getInternalName() + EnumChatFormatting.RESET + ".");
        }, () -> {
            this.print(EnumChatFormatting.RED + "Module " + EnumChatFormatting.GOLD + moduleName + EnumChatFormatting.RED + " not found!");
        });

    }
}
