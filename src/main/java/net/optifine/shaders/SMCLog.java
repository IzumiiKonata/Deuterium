package net.optifine.shaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SMCLog {
    private static final Logger LOGGER = LogManager.getLogger("SMCLog");
    private static final String PREFIX = "[SMCLog] ";

    public static void severe(String message) {
        LOGGER.error("[SMCLog] " + message);
    }

    public static void warning(String message) {
        LOGGER.warn("[SMCLog] " + message);
    }

    public static void info(String message) {
        LOGGER.info("[SMCLog] " + message);
    }

    public static void fine(String message) {
        LOGGER.debug("[SMCLog] " + message);
    }

    public static void severe(String format, Object... args) {
        String s = String.format(format, args);
        LOGGER.error("[SMCLog] " + s);
    }

    public static void warning(String format, Object... args) {
        String s = String.format(format, args);
        LOGGER.warn("[SMCLog] " + s);
    }

    public static void info(String format, Object... args) {
        String s = String.format(format, args);
        LOGGER.info("[SMCLog] " + s);
    }

    public static void fine(String format, Object... args) {
        String s = String.format(format, args);
        LOGGER.debug("[SMCLog] " + s);
    }
}
