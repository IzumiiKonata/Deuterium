package tech.konata.utils.logging;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Getter
public class Logger {

    private final String name;

    @Getter
    @Setter
    private LogLevel overrideLevel = null;

    public Logger() {
        this(Thread.currentThread().getName());
    }

    @SneakyThrows
    public Logger(String name) {
        this.name = name;
    }

    public void debug(String message, Object... objects) {
        LogManager.print(this, LogLevel.DEBUG, LogManager.parse(message, objects), name);
    }

    public void info(String message, Object... objects) {
        LogManager.print(this, LogLevel.INFO, LogManager.parse(message, objects), name);
    }

    public void warn(String message, Object... objects) {
        LogManager.print(this, LogLevel.WARN, LogManager.parse(message, objects), name);
    }

    public void error(Throwable t) {
        this.error(t.getMessage(), t);
    }

    public void error(String message, Object... objects) {
        LogManager.print(this, LogLevel.ERROR, LogManager.parse(message, objects), name);
    }

    public void fatal(Throwable t) {
        this.fatal(t.getMessage(), t);
    }

    public void fatal(String message, Object... objects) {
        LogManager.print(this, LogLevel.CRITICAL, LogManager.parse(message, objects), name);
    }

    public boolean isDebugEnabled() {
        return LogManager.getDefaultLogLevel().getLevel() >= LogLevel.DEBUG.getLevel();
    }

}
