import lombok.SneakyThrows;
import net.minecraft.client.main.Main;
import tritium.Tritium;
import tritium.launch.Launcher;

import java.util.Arrays;

public class Start {

    @SneakyThrows
    public static void main(String[] args) {
        String[] launchArgs = concat(new String[]{ "--version", Tritium.NAME, "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}", "--width", "854", "--height", "480" }, args);
        Launcher.main(launchArgs);
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
