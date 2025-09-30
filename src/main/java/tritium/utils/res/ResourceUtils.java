package tritium.utils.res;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.util.Location;
import org.apache.commons.io.IOUtils;
import tritium.Tritium;

import java.io.InputStream;

/**
 * @author IzumiiKonata
 * @since 11/19/2023
 */
@UtilityClass
public class ResourceUtils {

    /**
     * Load a resource from the given path.
     *
     * @param path path of the resource e.g. textures/res.png
     * @return Location
     */
    public Location loadRes(String path) {
        return Location.of(Tritium.NAME + "/" + path);
    }

    @SneakyThrows
    public byte[] getResourceAsBytes(String path) {
        InputStream is = ResourceUtils.class.getResourceAsStream(path);

        if (is == null)
            throw new NullPointerException("Resource not found: " + path);

        return IOUtils.toByteArray(is);
    }

}
