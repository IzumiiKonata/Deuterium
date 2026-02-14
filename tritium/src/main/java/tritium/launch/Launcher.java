package tritium.launch;

import net.minecraft.launchwrapper.Launch;

/**
 * @author IzumiiKonata
 * Date: 2025/10/14 10:16
 */
public class Launcher {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.setProperty("tritium.startupTime", String.valueOf(startTime));

        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(Runtime.getRuntime().availableProcessors() - 1));

        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("log4j2.disable.jmx", "true");
        System.setProperty("log4j2.includeLocation", "false");
        System.setProperty("log4j2.asyncLoggerRingBufferSize", "262144");
        System.setProperty("log4j2.asyncLoggerWaitTimeout", "30000");

        System.setProperty("mixin.env.disableRefMap", "true");
        System.setProperty("mixin.env.remapRefMap", "false");
        System.setProperty("mixin.debug.verbose", "false");
        System.setProperty("mixin.debug.export", "false");

        new Launch(args).launch(args);
    }

}