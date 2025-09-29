package tech.konata.phosphate.management;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.command.Command;
import tech.konata.phosphate.command.impl.*;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.game.ChatEvent;
import tech.konata.phosphate.settings.GlobalSettings;

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

    List<String> turnOn = Arrays.asList(
            "我爱玩原神",
            "我是神里绫华的狗",
            "我想玩原神",
            "我爱原神",
            "打开原神",
            "安装原神"
    );

    List<String> turnOff = Arrays.asList(
            "我不爱玩原神",
            "我不想玩原神了",
            "关闭原神",
            "删除原神"
    );

    @Handler
    public final void onChat(ChatEvent event) {
        String unformatted = event.getMsg();

        //genshin detector
        {
            if (GlobalSettings.GENSHIN_IMPACT_MODE.getValue()) {
                for (String s : turnOff) {
                    if (unformatted.contains(s)) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage("[神必人] 收到! 已关闭原神模式");
                        GlobalSettings.GENSHIN_IMPACT_MODE.setValue(Boolean.FALSE);
                        break;
                    }
                }
            } else {
                for (String s : turnOn) {
                    if (unformatted.contains(s)) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage("[神必人] 收到! 已打开原神模式");
                        GlobalSettings.GENSHIN_IMPACT_MODE.setValue(Boolean.TRUE);
                        break;
                    }
                }
            }
        }

        if (!unformatted.startsWith("."))
            return;

        if (ModuleManager.noCommand.isEnabled())
            return;

        if (unformatted.toLowerCase().startsWith(".say"))
            return;

        event.setCancelled();

        unformatted = unformatted.substring(1);


        String[] split = unformatted.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
            this.print(String.format("%sCommand %s%s%s not found!", EnumChatFormatting.RED, EnumChatFormatting.GOLD, commandName, EnumChatFormatting.RED));
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

    public void print(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(EnumChatFormatting.AQUA + "[" + Phosphate.NAME + "] " + EnumChatFormatting.RESET + message);
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }
}
