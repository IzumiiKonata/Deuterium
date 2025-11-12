package tritium.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import tritium.interfaces.SharedConstants;
import tritium.screens.ConsoleScreen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:18 PM
 */
public class Command implements SharedConstants {
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
    }

    final List<InvokeInfo> hardDeclaredInvokeInfos = new ArrayList<>();

    public void registerInvokeInfo(InvokeInfo invokeInfo) {
        hardDeclaredInvokeInfos.add(invokeInfo);
    }

    public void tryExecute(String[] args) {
        List<InvokeInfo> invokeInfos = new ArrayList<>(hardDeclaredInvokeInfos);

        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            declaredMethod.setAccessible(true);

            if (declaredMethod.isAnnotationPresent(CommandHandler.class)) {
                invokeInfos.add(new InvokeInfo(declaredMethod.getAnnotation(CommandHandler.class), this, declaredMethod, declaredMethod.getParameterTypes()));
            }
        }

        boolean found = false;
        for (InvokeInfo invokeInfo : invokeInfos) {
            if (invokeInfo.isArgsLengthMatch(args)) {
                found = true;
                invokeInfo.tryFitArgsAndInvoke(args);
                break;
            }
        }

        if (!found) {
            ConsoleScreen.log("Mismatched arguments");

            for (InvokeInfo invokeInfo : invokeInfos) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.getName());

                for (int j = 0; j < invokeInfo.methodToInvoke.getParameters().length; j++) {
                    sb.append(" ").append("<").append(invokeInfo.annotation.paramNames().length > 0 ? (invokeInfo.annotation.paramNames()[j]) : ("arg" + (j + 1))).append(">");
                }

                ConsoleScreen.log("    {}", sb.toString());
            }
        }
    }

    @RequiredArgsConstructor
    public static class InvokeInfo {

        public final CommandHandler annotation;
        public final Object objInstance;
        public final Method methodToInvoke;
        public final Class<?>[] parameterTypes;

        public boolean isArgsLengthMatch(String[] args) {
            return parameterTypes.length == args.length;
        }

        @SneakyThrows
        public void tryFitArgsAndInvoke(String[] args) {
            List<Object> arguments = new ArrayList<>();

            for (int i = 0; i < parameterTypes.length; i++) {
                Object arg;
                try {
                    arg = this.parseType(parameterTypes[i], args[i]);
                } catch (NumberFormatException e) {
                    ConsoleScreen.log(EnumChatFormatting.RED + "Unable to parse arg {} to type {}.", args[i], parameterTypes[i].getSimpleName());
                    ConsoleScreen.log(EnumChatFormatting.RED + "Illegal Arguments!");
                    return;
                }

                if (arg == null) {
                    throw new IllegalArgumentException("Unable to parse arg " + args[i] + " (" + (i) + ") to type " + parameterTypes[i].getSimpleName() + ".");
                }

                if (!(arg instanceof Number) && !(parameterTypes[i].isAssignableFrom(arg.getClass())) && parameterTypes[i] != arg.getClass()) {
                    throw new IllegalArgumentException("Unable to parse arg " + args[i] + " (" + (i) + ") to type " + parameterTypes[i].getSimpleName() + ".\n" +
                            "Expected: " + parameterTypes[i].getSimpleName() + ", Got: " + arg.getClass());
                }

                arguments.add(arg);
            }
            this.methodToInvoke.invoke(objInstance, arguments.toArray());
        }

        private Object parseType(Class<?> type, String rawData) throws NumberFormatException {
            if (type == String.class)
                return rawData;

            if (type == Integer.class || type == int.class)
                return Integer.parseInt(rawData);

            if (type == Float.class || type == float.class)
                return Float.parseFloat(rawData);

            if (type == Double.class || type == double.class)
                return Double.parseDouble(rawData);

            if (type == Long.class || type == long.class)
                return Long.parseLong(rawData);

            if (type == Boolean.class || type == boolean.class)
                return Boolean.parseBoolean(rawData);

            return null;
        }
    }

    /**
     * prints a string to player's chat hud
     *
     * @param format string format
     * @param args   format arguments
     */
    public void print(String format, Object... args) {
        ConsoleScreen.log(format, args);
    }

    /**
     * prints the usage to the player's chat hud
     */
    public void printUsage() {
        print(EnumChatFormatting.RED + "Usage: " + EnumChatFormatting.RESET + this.getUsage());
    }

}
