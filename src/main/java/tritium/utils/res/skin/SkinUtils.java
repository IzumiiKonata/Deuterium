package tritium.utils.res.skin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Location;
import tritium.Tritium;

public class SkinUtils {

    private static final Gson gson = (new GsonBuilder()).create();

    public static final Location ALEX_FACE = Location.of("tritium/textures/alex.png");
    public static final Location STEVE_FACE = Location.of("tritium/textures/steve.png");

}
