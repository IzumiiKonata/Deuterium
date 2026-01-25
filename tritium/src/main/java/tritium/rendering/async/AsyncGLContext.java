package tritium.rendering.async;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import tritium.utils.logging.Logger;
import tritium.utils.other.DevUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author IzumiiKonata
 * @since 11/25/2023
 */
public class AsyncGLContext {

    public static final int THREAD_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    public static final Object MULTITHREADING_LOCK = new Object();

    @Getter
    private static final ConcurrentLinkedQueue<GLTask> TASK_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * threads list
     */
    @Getter
    private static final List<Context> threads = Collections.synchronizedList(new ArrayList<>());

    public static final Logger LOGGER = new Logger("AsyncGLContext") {
        {
//            this.setOverrideLevel(LogLevel.DEBUG);
        }
    };

    public static class Context extends Thread {

        final long pContextHandle;

        public final Object lock = new Object();

        public Context(long pContext) {

            if (pContext <= MemoryUtil.NULL) {
                this.pContextHandle = -1;
                return;
            }

            this.pContextHandle = pContext;
        }

        @Override
        @SneakyThrows
        public void run() {
//            System.out.println("Context: " + pContextHandle);
            GLFW.glfwMakeContextCurrent(this.pContextHandle);
            GL.createCapabilities();

            while (true) {
                if (!TASK_QUEUE.isEmpty()) {
                    // poll the task from the queue
                    GLTask task = TASK_QUEUE.poll();

                    try {
                        // this shouldn't happen but in case it happens we'll just assert it is not null
                        if (task == null)
                            continue;

                        LOGGER.debug("ID: {} on thread {}", task.id, Thread.currentThread().getName());

                        // execute the task
                        task.runnable.run();

                        // complete the future
                        task.future.complete(null);

                        // sync gl commands among all the threads
                        GL11.glFlush();
                        GL11.glFinish();

                        LOGGER.debug("ID: {} finished", task.id);
                        LOGGER.debug("ID: {}", task.id);
                    } catch (Exception e) {
                        // complete the future exceptionally
                        task.future.completeExceptionally(e);
                        // LEAVE ME ALONE
                        e.printStackTrace();
                    }
                } else {
                    synchronized (lock) {

                        LOGGER.debug("Thread {} started waiting!", Thread.currentThread().getName());

                        // let the thread waits if there aren't any tasks to load
                        lock.wait();
                    }
                }
            }
        }
    }

    @AllArgsConstructor
    public static class GLTask {

        public final int id;
        public final Runnable runnable;
        public final String callStack;
        public CompletableFuture<Void> future;

        public GLTask(int id, Runnable runnable, String callStack) {
            this.id = id;
            this.runnable = runnable;
            this.callStack = callStack;
            this.future = new CompletableFuture<>();
        }
    }

    /**
     * initializes the thread.
     */
    public static void init() {

        for (int i = 0; i < AsyncGLContext.THREAD_COUNT; i++) {
            long subWindow = GLContextUtils.createContext();

            Context thread = new Context(subWindow);
            thread.setName("GL Context " + i);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());

            thread.setDaemon(true);
            thread.start();
            threads.add(thread);
        }

    }

    static int id = 0;

    @SneakyThrows
    public static CompletableFuture<Void> submit(Runnable runnable) {

        if (runnable == null) {
            System.err.println("Got Null Runnable!");
            System.err.println(DevUtils.getCurrentInvokeStack());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Runnable is null"));
            return future;
        }

        GLTask task = new GLTask(id++, runnable, /*DevUtils.getCurrentInvokeStack()*/"");

        TASK_QUEUE.add(task);

        for (Context thread : threads) {

            if (thread.getState() == Thread.State.WAITING)
                synchronized (thread.lock) {
                    // change the thread's state (WAITING -> RUNNING) to continue loading tasks
                    thread.lock.notifyAll();
                }

        }
        
        return task.future;
    }

    public static boolean isAllTasksFinished() {
        return TASK_QUEUE.isEmpty();
    }

}
