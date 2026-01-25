package tritium.launch;

import net.minecraft.launchwrapper.Launch;

/**
 * @author IzumiiKonata
 * Date: 2025/10/14 10:16
 */
public class Launcher {

    public static void main(String[] args) {
        System.setProperty("tritium.startupTime", String.valueOf(System.currentTimeMillis()));

        Launch launch = new Launch(args);

        System.setProperty("java.net.preferIPv4Stack", "true");
        launch.launch(args);
    }

}
