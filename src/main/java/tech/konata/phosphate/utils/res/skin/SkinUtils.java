package tech.konata.phosphate.utils.res.skin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Location;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.utils.network.HttpUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Base64;

public class SkinUtils {

    private static final Gson gson = (new GsonBuilder()).create();

    public static final Location ALEX_FACE = Location.of(Phosphate.NAME + "/textures/alex.png");
    public static final Location STEVE_FACE = Location.of(Phosphate.NAME + "/textures/steve.png");

}
