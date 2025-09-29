package tech.konata.phosphate.utils.logging;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    @Getter
    private static final LogLevel defaultLogLevel = LogLevel.INFO;

    private static final Logger unknown = new Logger("Unknown");

    private static final PrintStream SYSOUT = System.out;

    public static Logger getLogger() {
        return new Logger(getClassName(2));
    }

    private static String getClassName(int depth) {
        String className = (new Throwable()).getStackTrace()[depth].getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    private static String getLocalTime() {
        return new SimpleDateFormat("hh:mm:ss").format(new Date());
    }

    @SneakyThrows
    public static void print(Logger loggerIn, LogLevel levelIn, String message, String name) {

        if (!(levelIn.getLevel() >= (loggerIn.getOverrideLevel() != null ? loggerIn.getOverrideLevel() : LogManager.getDefaultLogLevel()).getLevel()))
            return;

        String msg = ConsoleColors.GREEN_BRIGHT + "[" + getLocalTime() + "]" + ConsoleColors.RESET
                + " [" + ConsoleColors.WHITE_BRIGHT + name + ConsoleColors.RESET + "/"
                + getSymbolColor(levelIn) + levelIn + ConsoleColors.RESET + "] "
                + getSymbolColor(levelIn) + message + ConsoleColors.RESET;

        SYSOUT.println(msg);

    }

    private static String getSymbolColor(LogLevel symbol) {

        switch (symbol) {
            case DEBUG:
                return ConsoleColors.WHITE;
            case INFO:
                return ConsoleColors.RESET;
            case WARN:
                return ConsoleColors.YELLOW_BRIGHT;
            case ERROR:
                return ConsoleColors.RED_BRIGHT;
            case CRITICAL:
                return ConsoleColors.RED_BACKGROUND_BRIGHT;
            default:
                throw new IllegalArgumentException();
        }

    }

    private static String toString(Object o) {

        if (o == null)
            return "";

        if (o instanceof Throwable) {
            Throwable t = (Throwable) o;

            StringWriter sw = new StringWriter();

            PrintWriter pw = new PrintWriter(sw);

            t.printStackTrace(pw);

            return sw.toString();
        }

        String string = o.toString();

        return string.replaceAll("\\$", "?");
    }

    // "{}", "a"
    public static String parse(String template, Object... args) {

        try {
            int count = 0;
            while (template.contains("{}")) {
                template = template.replaceFirst("\\{}", LogManager.toString(args[count]));
                count++;
            }

            for (Object arg : args) {
                if (arg instanceof Throwable) {
                    Throwable t = (Throwable) arg;
                    String string = LogManager.toString(t);
                    System.err.println(string);
                }
            }

            return template;
        } catch (Exception e) {
            SYSOUT.println(template);
            e.printStackTrace();
        }

        return "";
    }
}
