package tech.konata.utils.logging;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    @Getter
    private static final LogLevel defaultLogLevel = LogLevel.DEBUG;

    private static final Logger unknown = new Logger("Unknown");

    private static final PrintStream SYSOUT = System.out;

    public static Logger getLogger() {
        return unknown;
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

            pw.flush();
            pw.close();

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
                count ++;
            }

            return template;
        } catch (Exception e) {
            SYSOUT.println(template);
            e.printStackTrace();
        }

        return "";
    }
}
