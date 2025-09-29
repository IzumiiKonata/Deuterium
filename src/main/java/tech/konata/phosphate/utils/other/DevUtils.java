package tech.konata.phosphate.utils.other;

import net.minecraft.client.Minecraft;
import tech.konata.phosphate.Phosphate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 6/19/2023 10:36 AM
 */
public class DevUtils {


    public static void printCurrentInvokeStack() {

        if (Phosphate.getInstance().isObfuscated())
            return;

        System.out.println("Current Invoke Stack From Thread " + Thread.currentThread().getName() + ":... ");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement s : elements) {
            System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

    public static String getCurrentInvokeStack() {

        if (Phosphate.getInstance().isObfuscated())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Current Invoke Stack From Thread ").append(Thread.currentThread().getName()).append(":... ").append("\n");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement s : elements) {
            sb.append("\tat ").append(s.getClassName()).append(".").append(s.getMethodName()).append("(").append(s.getFileName()).append(":").append(s.getLineNumber()).append(")").append("\n");
        }

        return sb.toString();
    }

    public static boolean isObfuscated() {
        try {
            Class.forName(new String(Base64.getDecoder().decode("dGVjaC5rb25hdGEucGhvc3BoYXRlLlBob3NwaGF0ZQ==")));
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

}
