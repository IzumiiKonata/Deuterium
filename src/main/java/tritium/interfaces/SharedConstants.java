package tritium.interfaces;

import net.minecraft.client.Minecraft;
import tritium.Tritium;

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

    Tritium client = Tritium.getInstance();

}
