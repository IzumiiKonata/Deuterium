package tritium.rendering.async;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import tritium.utils.logging.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * @since 11/25/2023
 */
public class AsyncGLContext {

    private static final Logger logger = LogManager.getLogger("AsyncGLContext");

    public static final Object MULTITHREADING_LOCK = new Object();

    private static final int THREAD_COUNT = Math.max(1, Math.min(2, Runtime.getRuntime().availableProcessors() - 1));

    @Getter
    private static final BlockingQueue<FutureTask<?>> TASK_QUEUE = new LinkedBlockingQueue<>();

    @Getter
    private static final List<Thread> threads = new ArrayList<>();

    private static final ThreadLocal<Boolean> IS_WORKER = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static volatile boolean ready = false;

    public static boolean isReady() {
        return ready;
    }

    public static boolean isWorkerThread() {
        return IS_WORKER.get();
    }

    public static boolean canUploadOnCurrentThread() {
        return Minecraft.getMinecraft().isCallingFromMinecraftThread() || GLFW.glfwGetCurrentContext() != 0L;
    }

    public static void init() {
        if (ready) {
            return;
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            long ctx = GLContextUtils.createContext();

            if (ctx == 0L) {
                logger.warn("Failed to create shared GL context for async texture worker #{}", i);
                continue;
            }

            Thread thread = new Thread(() -> workerLoop(ctx), "Async-GL-Worker-" + i);
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
        }

        ready = !threads.isEmpty();

        if (ready) {
            logger.info("AsyncGLContext initialized with {} worker thread(s)", threads.size());
        } else {
            logger.warn("AsyncGLContext disabled, falling back to main thread texture uploads");
        }
    }

    private static void workerLoop(long ctx) {
        IS_WORKER.set(Boolean.TRUE);
        GLFW.glfwMakeContextCurrent(ctx);
        GL.createCapabilities();

        while (true) {
            FutureTask<?> task;

            try {
                task = TASK_QUEUE.take();
            } catch (InterruptedException e) {
                break;
            }

            task.run();
        }
    }

    public static void submit(Runnable runnable) {
        if (!ready || isWorkerThread()) {
            runSafely(runnable);
            return;
        }

        TASK_QUEUE.add(new FutureTask<>(() -> runSafely(runnable), null));
    }

    public static <T> T callBlocking(Supplier<T> supplier) {
        if (!ready || isWorkerThread()) {
            return supplier.get();
        }

        FutureTask<T> task = new FutureTask<>(supplier::get);
        TASK_QUEUE.add(task);

        try {
            return task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void runBlocking(Runnable runnable) {
        callBlocking(() -> {
            runnable.run();
            return null;
        });
    }

    private static void runSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            logger.error("Async GL task failed", t);
        }
    }
}
