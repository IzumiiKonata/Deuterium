package tech.konata.phosphate.utils.other.multithreading;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;

import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.utils.logging.Logger;
import tech.konata.phosphate.utils.other.DevUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author IzumiiKonata
 * @since 2024/12/28 17:57
 */
public class MultiThreadingUtil {

    public static final int THREAD_COUNT = 4;

    @Getter
    private static final ConcurrentLinkedQueue<Runnable> TASK_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * threads list
     */
    @Getter
    private static final List<WorkerThread> threads = Collections.synchronizedList(new ArrayList<>());

    /**
     * a list to store tasks when the loader threads aren't loaded.
     */
    public static final Logger LOGGER = new Logger("MultiThreadingUtil") {
        {
//            this.setOverrideLevel(LogLevel.DEBUG);
        }
    };

    public static class WorkerThread extends Thread {

        public final Object lock = new Object();

        public WorkerThread() {

        }

        @Override
        @SneakyThrows
        public void run() {

            while (true) {
                if (!TASK_QUEUE.isEmpty()) {
                    // poll the task from the queue
                    Runnable task = TASK_QUEUE.poll();

                    try {
                        // this shouldn't happen but in case it happens we'll just assert it is not null
                        if (task == null)
                            continue;

                        LOGGER.debug("[Thread {}] Started executing the task", Thread.currentThread().getName());

                        // execute the task
                        task.run();

                        LOGGER.debug("[Thread {}] Finished", Thread.currentThread().getName());
                    } catch (Throwable t) {
                        // LEAVE ME ALONE
                        t.printStackTrace();
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

    /**
     * initializes the thread.
     */
    static {

        for (int i = 0; i < MultiThreadingUtil.THREAD_COUNT; i++) {

            WorkerThread thread = new WorkerThread();
            thread.setName("[MTU] " + i);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());

            thread.setDaemon(true);
            thread.start();
            threads.add(thread);
        }

//        MultiThreadingUtil.startDaemonThread();

    }

    // 防止出现奇怪的问题导致线程没被踢醒 创建一个守护进程一秒遍历一次
    // *Edit: okay i tested it, and it's not necessary
    private static void startDaemonThread() {
        new Thread(
                () -> {

                    while (true) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        if (threads.isEmpty())
                            continue;

                        if (!TASK_QUEUE.isEmpty()) {
                            for (WorkerThread thread : threads) {

                                if (thread.getState() == Thread.State.WAITING)
                                    synchronized (thread.lock) {
                                        // change the thread's state (WAITING -> RUNNING) to continue loading tasks
                                        thread.lock.notifyAll();
                                    }

                            }
                        }


                    }
                },
                "MTU Daemon Thread"
        ).start();
    }

    static int id = 0;

    @SneakyThrows
    public static void runAsync(Runnable runnable) {

        if (runnable == null) {
            System.err.println("Got Null Runnable!");
            System.err.println(DevUtils.getCurrentInvokeStack());
            return;
        }

        TASK_QUEUE.add(runnable);

        for (MultiThreadingUtil.WorkerThread thread : threads) {

            if (thread.getState() == Thread.State.WAITING)
                synchronized (thread.lock) {
                    // change the thread's state (WAITING -> RUNNING) to continue loading tasks
                    thread.lock.notifyAll();
                }

        }
    }

}
