package tritium.utils.logging;

import lombok.SneakyThrows;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * @since 2024/8/31 12:15
 */
public class ConsoleOutputRedirector {

    public static final List<String> SYSTEM_OUT = new CopyOnWriteArrayList<>();

    static PrintStream OUT, ERR;
    public static void init() {

        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        FileOutputStream fdErr = new FileOutputStream(FileDescriptor.err);

        System.setOut(new LoggingPrintStream(OUT = newPrintStream(fdOut, System.getProperty("stdout.encoding")), true));
        System.setErr(new LoggingPrintStream(ERR = newPrintStream(fdErr, System.getProperty("stdout.encoding")), false));

    }

    private static PrintStream newPrintStream(OutputStream out, String enc) {
        if (enc != null) {
            return new PrintStream(new BufferedOutputStream(out, 128), true,
                    Charset.forName(enc, StandardCharsets.UTF_8));
        }
        return new PrintStream(new BufferedOutputStream(out, 128), true);
    }

    private static class LoggingPrintStream extends PrintStream {
        private final OutputStream stream;
        private final boolean stdOut;

        public LoggingPrintStream(PrintStream outStream, boolean stdOut) {
            super(outStream);
            this.stream = outStream;
            this.stdOut = stdOut;
        }

        public void println(String p_println_1_) {
            this.logString(p_println_1_);
        }

        public void println(Object p_println_1_) {
            this.logString(String.valueOf(p_println_1_));
        }

        @SneakyThrows
        protected void logString(String string) {

            if (stdOut) {
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
