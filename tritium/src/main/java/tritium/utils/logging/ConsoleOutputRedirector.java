package tritium.utils.logging;

import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ConsoleOutputRedirector {

    public static final Queue<String> SYSTEM_OUT = new ConcurrentLinkedQueue<>();

    private static final PrintStream REAL_OUT = System.out;
    private static final PrintStream REAL_ERR = System.err;

    public static void init() {
        System.setOut(new PrintStream(
                new LineInterceptingStream(REAL_OUT, true),
                true,
                StandardCharsets.UTF_8
        ));

        System.setErr(new PrintStream(
                new LineInterceptingStream(REAL_ERR, false),
                true,
                StandardCharsets.UTF_8
        ));
    }

    private static final class LineInterceptingStream extends OutputStream {

        private final PrintStream target;
        private final boolean stdout;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);

        LineInterceptingStream(PrintStream target, boolean stdout) {
            this.target = target;
            this.stdout = stdout;
        }

        @Override
        public synchronized void write(int b) {
            buffer.write(b);

            if (b == '\n') {
                flushLine();
            }
        }

        @Override
        public synchronized void flush() {
            if (buffer.size() > 0) {
                flushLine();
            }
            target.flush();
        }

        @Override
        public void close() {
            flush();
        }

        private void flushLine() {
            String line = buffer.toString(StandardCharsets.UTF_8);
            buffer.reset();

            target.print(line);

            String processed = stdout
                    ? convertAnsiToMc(stripTrailingNewline(line))
                    : EnumChatFormatting.RED + stripTrailingNewline(line);

            SYSTEM_OUT.add(processed);
        }
    }

    private static String stripTrailingNewline(String s) {
        if (s.endsWith("\r\n")) return s.substring(0, s.length() - 2);
        if (s.endsWith("\n")) return s.substring(0, s.length() - 1);
        return s;
    }

    private static String convertAnsiToMc(String input) {
        StringBuilder out = new StringBuilder(input.length());
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            if (c == '\033' && i + 1 < input.length() && input.charAt(i + 1) == '[') {
                int end = input.indexOf('m', i);
                if (end == -1) {
                    i++;
                    continue;
                }

                String code = input.substring(i + 2, end);
                out.append(mapAnsi(code));
                i = end + 1;
                continue;
            }

            out.append(c);
            i++;
        }

        return out.toString();
    }

    private static String mapAnsi(String code) {
        return switch (code) {
            case "0", "0;0" -> EnumChatFormatting.GRAY.toString();
            case "0;91", "31" -> EnumChatFormatting.RED.toString();
            case "0;92", "32" -> EnumChatFormatting.GREEN.toString();
            case "0;93", "33" -> EnumChatFormatting.YELLOW.toString();
            case "0;97", "37" -> EnumChatFormatting.WHITE.toString();
            default -> "";
        };
    }
}
