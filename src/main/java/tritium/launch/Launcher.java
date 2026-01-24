package tritium.launch;

import net.minecraft.launchwrapper.Launch;
import org.lwjgl.system.Configuration;
import tritium.mixin.Mixin;
import tritium.utils.logging.ConsoleOutputRedirector;

/**
 * @author IzumiiKonata
 * Date: 2025/10/14 10:16
 */
public class Launcher {

    public static final long startupTime = System.currentTimeMillis();

    public static void main(String[] args) {
        Configuration.MEMORY_ALLOCATOR.set("jemalloc");
        ConsoleOutputRedirector.init();

        Configuration.DISABLE_CHECKS.set(true);
        Configuration.DISABLE_FUNCTION_CHECKS.set(true);
        Configuration.DISABLE_HASH_CHECKS.set(true);
        Configuration.DEBUG.set(false);
        Configuration.DEBUG_FUNCTIONS.set(false);

        Launch launch = new Launch();

        Mixin.setup();

        System.setProperty("java.net.preferIPv4Stack", "true");
        launch.launch(args);
//        MinecraftBootstrap.launchMinecraft(args);
    }

}
