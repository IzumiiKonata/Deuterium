package tritium.utils.optimization;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.util.Tuple;
import tritium.Tritium;
import tritium.utils.other.DevUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author IzumiiKonata
 * Date: 2025/10/26 12:05
 */
@UtilityClass
public class InputStreamLeakageTracker {

    @Getter
    private static final Map<TrackedInputStream, Tuple<Long, String>> leakageMap = new ConcurrentHashMap<>();

    private static void check() {
        leakageMap.entrySet().removeIf(entry -> {
            TrackedInputStream trackedInputStream = entry.getKey();
            if (trackedInputStream.isClosed()) {
                return true;
            }
            Tuple<Long, String> tuple = entry.getValue();
//            long time = tuple.getA();
//            long now = System.currentTimeMillis();
            System.out.println("InputStreamLeakageTracker 泄漏: " + trackedInputStream);
            System.out.println("Call stack: " + tuple.getB());
            return false;
        });

        if (leakageMap.isEmpty())
            System.out.println("No leakage.");
    }

    static {
        if (!Tritium.getInstance().isObfuscated()) {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        check();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    public static InputStream wrap(InputStream inputStream) {

        if (inputStream == null)
            return null;

        if (!Tritium.getInstance().isObfuscated()) {
            return inputStream;
        }

        TrackedInputStream trackedInputStream = new TrackedInputStream(inputStream);
        leakageMap.put(trackedInputStream, Tuple.of(System.currentTimeMillis(), DevUtils.getCurrentInvokeStack()));
        return trackedInputStream;
    }

    public static class TrackedInputStream extends InputStream {

        @Getter
        private boolean closed = false;

        private final InputStream inputStream;

        public TrackedInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public void close() throws IOException {
            closed = true;
            inputStream.close();
        }

    }
}
