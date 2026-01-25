package tritium.rendering;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Location;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.timing.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author IzumiiKonata
 * Date: 2025/10/12 21:37
 */
public class ResPackPreview {

    private final IResourcePack pack;
    private final String path;
    private final AnimatedTexture animatedTexture;

    @SneakyThrows
    public ResPackPreview(IResourcePack pack, NativeBackedImage img, String path) {
        this.pack = pack;
        this.path = path;
        InputStream meta = null;
        try {
            meta = pack.getInputStream(Location.of(path + ".mcmeta"));
        } catch (IOException ignored) {
        }
        animatedTexture = new AnimatedTexture(img, meta);
    }

    public void cleanUp() {

    }

    public void render(double x, double y, double width, double height) {
        animatedTexture.render(x, y, width, height);
    }

}
