package tech.konata.phosphate.utils.logging;

import net.minecraft.util.EnumChatFormatting;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * @since 2024/8/31 12:15
 */
public class ConsoleOutputRedirector {

    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;

    public static final List<String> SYSTEM_OUT = new CopyOnWriteArrayList<>();

    public static void init() {

        System.setOut(new LoggingPrintStream(OUT));
        System.setErr(new LoggingPrintStream(ERR));

    }

    private static class LoggingPrintStream extends PrintStream {
        private final PrintStream stream;

        public LoggingPrintStream(PrintStream outStream) {
            super(outStream);
            this.stream = outStream;
        }

        public void println(String p_println_1_) {
            this.logString(p_println_1_);
        }

        public void println(Object p_println_1_) {
            this.logString(String.valueOf(p_println_1_));
        }

        protected void logString(String string) {

            if (stream == OUT) {
                String noColor = string;
                while (noColor.contains("\033")) {
                    noColor = noColor.substring(0, noColor.indexOf("\033")) + noColor.substring(noColor.indexOf("m", noColor.indexOf("\033")) + 1);
                }

                OUT.println(string);

                String colorCodes = string;

                while (colorCodes.contains("\033")) {
                    String c = colorCodes.substring(colorCodes.indexOf("\033") + 1, colorCodes.indexOf("m", colorCodes.indexOf("\033")) + 1);
//                    OUT.println("Color: " + c);

                    colorCodes = colorCodes.replace("\033" + c, this.consoleColorToFormatting(c));
                }

                SYSTEM_OUT.add(colorCodes);

            } else {
                ERR.println(string);

                for (String s : string.replaceAll("\t", "    ").split("\n")) {
                    SYSTEM_OUT.add(EnumChatFormatting.RED + s);
                }

            }

        }

        private String consoleColorToFormatting(String input) {

            if (input.equals("[0;92m")) {
                return EnumChatFormatting.GREEN.toString();
            } else if (input.equals("[0m")) {
                return EnumChatFormatting.GRAY.toString();
            } else if (input.equals("[0;97m") || input.equals("[0;37m")) {
                return EnumChatFormatting.WHITE.toString();
            } else if (input.equals("[0;93m")) {
                return EnumChatFormatting.YELLOW.toString();
            } else if (input.equals("[0;91m")) {
                return EnumChatFormatting.RED.toString();
            } else {
                return "(IDK)";
            }

        }
    }

}
