package tritium.management;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.util.EnumChatFormatting;
import tritium.bridge.ExtensionCommandWrapper;
import tritium.command.Command;
import tritium.command.CommandHandler;
import tritium.command.impl.*;
import tritium.screens.ConsoleScreen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
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
        // 神必正则
        // 用于匹配引号中括起来的内容
        String[] split = unformattedCommand.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        // 然后去除所有引号
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("\"", "");
        }

        // 第一个是命令名
        String commandName = split[0];

        // 找命令
        boolean foundCommand = false;
        for (Command command : commands) {
            if (commandName.equalsIgnoreCase(command.getName()) || Arrays.asList(command.getAlias()).contains(commandName.toLowerCase())) {
                foundCommand = true;
                String[] args = new String[split.length - 1];
                System.arraycopy(split, 1, args, 0, split.length - 1);

                try {

                    if (command instanceof ExtensionCommandWrapper wrapper) {
                        wrapper.execute(args);
                    } else {
                        command.tryExecute(args);
                    }

                } catch (Exception e) {
                    StringWriter sw = new StringWriter();

                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();

                    ConsoleScreen.log(EnumChatFormatting.RED + sw.toString());
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
    NameSpoof nSpoof = new NameSpoof();
    Reload reload = new Reload();

    private static Command getOrCreateNew(String name, String[] alias) {
        for (Command cmd : CommandManager.getCommands()) {
            if (cmd.getName().equals(name))
                return cmd;
        }

        Command command;
        commands.add(command = new Command(name, name, name, alias));
        return command;
    }

    // 命令注册
    // 只实现了无参数、一个参数、两个参数、三个参数的命令注册
    // 而且还得指定参数类型
    // 麻烦得很

    public interface SimpleCommandCallback {
        void execute();
    }

    public interface SingleArgumentCommandCallback<T> {
        void execute(T arg);
    }

    public interface CommandRegisteredCallback {
        void setDescription(String description);
    }

    public static <T> CommandRegisteredCallback registerCommand(String name, SingleArgumentCommandCallback<T> callback, Class<T> argType, String argDesc) {
        return registerCommand(name, new String[0], callback, argType, argDesc);
    }

    @SneakyThrows
    public static <T> CommandRegisteredCallback registerCommand(String name, String[] alias, SingleArgumentCommandCallback<T> callback, Class<T> argType, String argDesc) {
        Command command = getOrCreateNew(name, alias);

        Method execute = callback.getClass().getDeclaredMethod("execute", Object.class);
        execute.setAccessible(true);
        Command.InvokeInfo invokeInfo = new Command.InvokeInfo(new CommandHandler() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandHandler.class;
            }

            final String[] desc = new String[] { argDesc };

            @Override
            public String[] paramNames() {
                return desc;
            }
        }, callback, execute, new Class[] {argType});
        command.registerInvokeInfo(invokeInfo);
        return invokeInfo::setDesc;
    }

    public interface TwoArgumentCommandCallback<T1, T2> {
        void execute(T1 arg1, T2 arg2);
    }

    public static <T1, T2> CommandRegisteredCallback registerCommand(String name, TwoArgumentCommandCallback<T1, T2> callback, Class<T1> argType1, Class<T2> argType2, String argDesc1, String argDesc2) {
        return registerCommand(name, new String[0], callback, argType1, argType2, argDesc1, argDesc2);
    }

    @SneakyThrows
    public static <T1, T2> CommandRegisteredCallback registerCommand(String name, String[] alias, TwoArgumentCommandCallback<T1, T2> callback, Class<T1> argType1, Class<T2> argType2, String argDesc1, String argDesc2) {
        Command command = getOrCreateNew(name, alias);

        Method execute = callback.getClass().getDeclaredMethod("execute", Object.class, Object.class);
        execute.setAccessible(true);
        Command.InvokeInfo invokeInfo = new Command.InvokeInfo(new CommandHandler() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandHandler.class;
            }

            final String[] desc = new String[] { argDesc1, argDesc2 };

            @Override
            public String[] paramNames() {
                return desc;
            }
        }, callback, execute, new Class[] {argType1, argType2});
        command.registerInvokeInfo(invokeInfo);

        return invokeInfo::setDesc;
    }

    public interface ThreeArgumentCommandCallback<T1, T2, T3> {
        void execute(T1 arg1, T2 arg2, T3 arg3);
    }

    public static <T1, T2, T3> CommandRegisteredCallback registerCommand(String name, ThreeArgumentCommandCallback<T1, T2, T3> callback, Class<T1> argType1, Class<T2> argType2, Class<T3> argType3, String argDesc1, String argDesc2, String argDesc3) {
        return registerCommand(name, new String[0], callback, argType1, argType2, argType3, argDesc1, argDesc2, argDesc3);
    }

    @SneakyThrows
    public static <T1, T2, T3> CommandRegisteredCallback registerCommand(String name, String[] alias, ThreeArgumentCommandCallback<T1, T2, T3> callback, Class<T1> argType1, Class<T2> argType2, Class<T3> argType3, String argDesc1, String argDesc2, String argDesc3) {
        Command command = getOrCreateNew(name, alias);

        Method execute = callback.getClass().getDeclaredMethod("execute", Object.class, Object.class, Object.class);
        execute.setAccessible(true);
        Command.InvokeInfo invokeInfo = new Command.InvokeInfo(new CommandHandler() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandHandler.class;
            }

            final String[] desc = new String[] { argDesc1, argDesc2, argDesc3 };

            @Override
            public String[] paramNames() {
                return desc;
            }
        }, callback, execute, new Class[] {argType1, argType2, argType3});
        command.registerInvokeInfo(invokeInfo);
        return invokeInfo::setDesc;
    }

    public static CommandRegisteredCallback registerSimpleCommand(String name, SimpleCommandCallback callback) {
        return registerSimpleCommand(name, new String[0], callback);
    }

    @SneakyThrows
    public static CommandRegisteredCallback registerSimpleCommand(String name, String[] alias, SimpleCommandCallback callback) {
        Command command = getOrCreateNew(name, alias);

        Method execute = callback.getClass().getDeclaredMethod("execute");
        execute.setAccessible(true);
        Command.InvokeInfo invokeInfo = new Command.InvokeInfo(new CommandHandler() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandHandler.class;
            }

            @Override
            public String[] paramNames() {
                return new String[0];
            }
        }, callback, execute, new Class[0]);
        command.registerInvokeInfo(invokeInfo);
        return invokeInfo::setDesc;
    }

    public void print(String message) {
        ConsoleScreen.log(message);
    }

    @Override
    @SneakyThrows
    public void init() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (Command.class.isAssignableFrom(field.getType())) {
                Command command = (Command) field.get(this);
                CommandManager.getCommands().add(command);
            }
        }
    }

    @Override
    public void stop() {

    }

}
