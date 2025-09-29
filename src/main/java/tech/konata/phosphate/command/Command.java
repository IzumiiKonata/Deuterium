package tech.konata.phosphate.command;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.interfaces.SharedConstants;
import tech.konata.phosphate.management.CommandManager;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:18 PM
 */
public abstract class Command implements SharedConstants {
    /**
     * Minecraft instance.
     */
    public final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private final String name, description, usage;

    /**
     * another name for the command
     */
    @Getter
    private final String[] alias;

    public Command(String name, String description, String usage, String... alias) {
        this.name = name;
        this.description = description;
        this.usage = usage;

        this.alias = alias;

        // automatically adds the command's instance to the command manager
        CommandManager.getCommands().add(this);
    }

    /**
     * execute the command
     *
     * @param args args
     */
    public abstract void execute(String[] args);

    /**
     * prints a string to player's chat hud
     *
     * @param format string format
     * @param args   format arguments
     */
    public void print(String format, Object... args) {
        mc.thePlayer.addChatMessage(EnumChatFormatting.AQUA + "[" + Phosphate.NAME + "] " + EnumChatFormatting.RESET + String.format(format, args));
    }

    /**
     * prints the usage to the player's chat hud
     */
    public void printUsage() {
        print(EnumChatFormatting.RED + "Usage: " + EnumChatFormatting.RESET + this.getUsage());
    }

}
