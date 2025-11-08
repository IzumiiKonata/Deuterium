import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/27 21:28
 */
public class A {

    @SneakyThrows
    private static String runProcess(String command) {
//        Process process = Runtime.getRuntime().exec(command);
//        process.waitFor();

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = reader.readLine();
        process.waitFor();
        return result;
    }

    public static void main(String[] args) {
        System.out.println(runProcess("AA"));
    }
}
