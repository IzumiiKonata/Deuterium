package tech.konata.phosphate.interfaces;

import net.minecraft.client.Minecraft;
import tech.konata.phosphate.Phosphate;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Commonly shared constants between the classes.
 *
 * @author IzumiiKonata
 * @since 11/19/2023
 */
public interface SharedConstants {

    Minecraft mc = Minecraft.getMinecraft();

    Phosphate client = Phosphate.getInstance();

    AtomicInteger COUNTER = new AtomicInteger(0);

    ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 30, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r ->
            new Thread(r, String.format("Thread %s", COUNTER.incrementAndGet())));

}
