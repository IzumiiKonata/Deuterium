package net.minecraft.launchwrapper;

import org.apache.logging.log4j.Level;
import tritium.utils.logging.Logger;

public class LogWrapper {
    static final Logger log = new Logger("LaunchWrapper");

    private static void configureLogging() {

    }

    public static void log(Level level, String format, Object... data) {
        log.log(level, String.format(format, data));
    }

    public static void log(Level level, Throwable ex, String format, Object... data) {
        log.log(level, String.format(format, data), ex);
    }

    public static void severe(String format, Object... data) {
        log(Level.ERROR, format, data);
    }

    public static void warning(String format, Object... data) {
        log(Level.WARN, format, data);
    }

    public static void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }

    public static void fine(String format, Object... data) {
        log(Level.DEBUG, format, data);
    }

    public static void finest(String format, Object... data) {
        log(Level.TRACE, format, data);
    }

}
