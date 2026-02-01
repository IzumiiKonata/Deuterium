package tritium.utils.other.multithreading;

import lombok.SneakyThrows;

import net.minecraft.client.Minecraft;
import tritium.utils.logging.Logger;
import tritium.utils.other.DevUtils;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * @since 2024/12/28 17:57
 */
public class MultiThreadingUtil {

    public static final Logger LOGGER = new Logger("MultiThreadingUtil") {
        {
//            this.setOverrideLevel(LogLevel.DEBUG);
        }
    };

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @SneakyThrows
    public static CompletableFuture<Void> runAsync(Runnable runnable) {

        if (runnable == null) {
            System.err.println("Got Null Runnable!");
            System.err.println(DevUtils.getCurrentInvokeStack());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Runnable is null"));
            return future;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        FutureTaskWrapper wrapper = new FutureTaskWrapper(runnable, future);

        executor.submit(wrapper);

        return future;
    }

    @SneakyThrows
    public static <T> T runOnMainThreadBlocking(Supplier<T> supplier) {
        return Minecraft.getMinecraft().addScheduledTask(supplier::get).get();
    }

    public static void runOnMainThread(Runnable runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }

    private static class FutureTaskWrapper implements Runnable {
        private final Runnable runnable;
        private final CompletableFuture<Void> future;

        public FutureTaskWrapper(Runnable runnable, CompletableFuture<Void> future) {
            this.runnable = runnable;
            this.future = future;
        }

        @Override
        public void run() {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }
}
