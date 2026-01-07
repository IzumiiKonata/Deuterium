package tritium.utils.other;

import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import tritium.Tritium;
import tritium.command.CommandValues;
import tritium.management.CommandManager;
import tritium.management.ModuleManager;
import tritium.screens.ConsoleScreen;
import tritium.utils.other.info.Version;

import java.util.Base64;

/**
 * @author IzumiiKonata
 * @since 6/19/2023 10:36 AM
 */
public class DevUtils {


    public static void printCurrentInvokeStack() {

        if (Tritium.getInstance().isObfuscated())
            return;

        System.out.println("Current Invoke Stack From Thread " + Thread.currentThread().getName() + ":... ");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement s : elements) {
            System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

    public static String getCurrentInvokeStack() {

        if (Tritium.getInstance().isObfuscated())
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
            Class.forName(new String(Base64.getDecoder().decode("dHJpdGl1bS5Ucml0aXVt")));
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

    public static void registerDevCommand() {
        CommandManager.registerCommand("setup_dev_settings", () -> {
            CommandValues.Values values = CommandValues.getValues();
            values.cl_skip_world_rendering = true;
            values.cl_show_crosshair = values.cl_show_hotbar = false;
            ModuleManager.openNCMScreen.setKeyBind(Keyboard.KEY_L);
            ConsoleScreen.log("{}ok", EnumChatFormatting.GREEN);
        });
    }

}
