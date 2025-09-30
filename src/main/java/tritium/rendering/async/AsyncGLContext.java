package tritium.rendering.async;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import tritium.utils.logging.Logger;
import tritium.utils.other.DevUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author IzumiiKonata
 * @since 11/25/2023
 */
public class AsyncGLContext {

    public static final int THREAD_COUNT = 4;

    public static final Object MULTITHREADING_LOCK = new Object();

    @Getter
    private static final ConcurrentLinkedQueue<GLTask> TASK_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * threads list
     */
    @Getter
    private static final List<Context> threads = Collections.synchronizedList(new ArrayList<>());

    /**
     * a list to store tasks when the loader threads aren't loaded.
     */
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

                        // sync gl commands among all the threads
                        GL11.glFlush();
                        GL11.glFinish();

                        LOGGER.debug("ID: {} finished", task.id);
                        LOGGER.debug("ID: {}", task.id);
                    } catch (Exception e) {
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

    // 防止出现奇怪的问题导致线程没被踢醒 创建一个守护进程一秒遍历一次
    private static void startDaemonThread() {
        new Thread(
                () -> {

                    while (true) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                },
                "AsyncGLContentLoader Daemon Thread"
        ).start();
    }

    static int id = 0;

    @SneakyThrows
    public static void submit(Runnable runnable) {

        if (runnable == null) {
            System.err.println("Got Null Runnable!");
            System.err.println(DevUtils.getCurrentInvokeStack());
            return;
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
    }

    public static boolean isAllTasksFinished() {
        return TASK_QUEUE.isEmpty();
    }

}
