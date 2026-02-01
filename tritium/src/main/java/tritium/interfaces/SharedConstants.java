package tritium.interfaces;

import net.minecraft.client.Minecraft;
import tritium.Tritium;

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
