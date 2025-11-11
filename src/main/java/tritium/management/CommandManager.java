package tritium.management;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import tritium.Tritium;
import tritium.command.Command;
import tritium.command.impl.*;
import tritium.screens.ConsoleScreen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:22 PM
 */
public class CommandManager extends AbstractManager {

    @Getter
    private static final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        super("CommandManager");
    }

    public void execute(String unformattedCommand) {
        String[] split = unformattedCommand.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("\"", "");
        }

        String commandName = split[0];

        boolean foundCommand = false;
        for (Command command : commands) {
            if (commandName.equalsIgnoreCase(command.getName()) || Arrays.asList(command.getAlias()).contains(commandName.toLowerCase())) {
                foundCommand = true;
                String[] args = new String[split.length - 1];
                System.arraycopy(split, 1, args, 0, split.length - 1);

                try {
                    command.execute(args);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();

                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();

                    Minecraft.getMinecraft().thePlayer.addChatMessage(EnumChatFormatting.RED + sw.toString());
                }
            }
        }

        if (!foundCommand) {
            this.print(EnumChatFormatting.RED + Localizer.format("command.command not found", EnumChatFormatting.GOLD + commandName + EnumChatFormatting.RED));
        }
    }

    Bind bind = new Bind();

    Rot rot = new Rot();
    Toggle toggle = new Toggle();
    Set set = new Set();
    Config config = new Config();
    GetSelfHead getSelfHead = new GetSelfHead();
    NSpoof nSpoof = new NSpoof();
    Reload reload = new Reload();

    public interface SimpleCommandCallback {
        void execute(String[] args);
    }

    public static void registerSimpleCommand(String name, String[] alias, SimpleCommandCallback callback) {
        // 这里只需要 new 一个 Command 对象就可以了
        // 因为在 Command 的类构造器里会自动将自己添加到命令列表中
        new Command(name, name, name, alias) {
            @Override
            public void execute(String[] args) {
                callback.execute(args);
            }
        };
    }

    public void print(String message) {
        ConsoleScreen.log(message);
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }

}
