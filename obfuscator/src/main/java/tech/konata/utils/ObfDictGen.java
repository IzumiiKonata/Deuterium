package tech.konata.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author IzumiiKonata
 * @since 2024/12/6 21:24
 */
public class ObfDictGen {

    private static final List<String> list = Arrays.asList(
            "T", "R", "I", "U", "M"
    );

    private static final Random random = new SecureRandom();

    private static String genStr(int repeat) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < repeat; i++) {
            result.append(list.get(Math.abs(random.nextInt() % list.size())));
        }

        return result.toString();
    }

    public static List<String> gen() {
        List<String> used = new ArrayList<>();
        List<String> result = new ArrayList<>();

        int repeat = 48;

        for (int i = 0; i < 40000; i++) {

            String rand = genStr(repeat);

            while (used.contains(rand.toLowerCase())) {
                rand = genStr(repeat);
            }

            used.add(rand.toLowerCase());
            result.add(rand);

        }

        return result;
    }

}
