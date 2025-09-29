import lombok.SneakyThrows;
import net.minecraft.client.main.Main;
import tech.konata.phosphate.Phosphate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Start {

    @SneakyThrows
    public static void main(String[] args) {
        String[] launchArgs = concat(new String[]{ "--version", Phosphate.NAME, "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}", "--width", "854", "--height", "480" }, args);
        Main.main(launchArgs);
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
