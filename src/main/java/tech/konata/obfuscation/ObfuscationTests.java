package tech.konata.obfuscation;

import javax.swing.*;

/**
 * @author IzumiiKonata
 * Date: 2025/1/19 19:07
 */
@Flow(strength = ObfuscationStrength.Heavy)
@Indy(strength = ObfuscationStrength.Heavy)
@NumberObfuscation(strength = ObfuscationStrength.Heavy)
@StringObfuscation(strength = ObfuscationStrength.Heavy)
public class ObfuscationTests {

    public static final String SOME_STRINGS = "I hate niggers";

    public static final int SOME_RANDOM_INTEGER = 19890604;

    public static void staticMethod() {

        Object o = new Object();

        synchronized (o) {
            System.out.println("HAHA I GOT YA");
            System.out.println("STRING: " + SOME_STRINGS);
            System.out.println("NUMBER: " + SOME_RANDOM_INTEGER);
        }

        JFrame jf = new JFrame();

        jf.setLocationRelativeTo(null);
        jf.dispose();

    }

    public void niggers(int a, int b) {

        a = a + 19890604;
        b = b + 89648964;

        System.out.println(a + b);

    }


}
