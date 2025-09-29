package tech.konata.phosphate.rendering.texture;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.phosphate.interfaces.SharedConstants;
import tech.konata.phosphate.rendering.async.AsyncGLContext;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * @author IzumiiKonata
 * @since 11/25/2023
 */
@UtilityClass
public class Textures implements SharedConstants {

    public Optional<ITextureObject> getTextureOrLoadAsyncly(Location location, BufferedImage img) {

        TextureManager textureManager = mc.getTextureManager();

        ITextureObject texture = textureManager.getTexture(location);

        if (texture == null) {
            Textures.loadTextureAsyncly(location, img);
        }

        return Optional.ofNullable(texture);
    }

    public DynamicTexture loadTexture(Location location, BufferedImage img) {
        return Textures.loadTexture(location, img, true, false);
    }

    public DynamicTexture loadTexture(Location location, BufferedImage img, boolean clearable, boolean linear) {

            DynamicTexture dynamicTexture = new DynamicTexture(img, clearable, linear);

            mc.getTextureManager().loadTexture(location, dynamicTexture);

            return dynamicTexture;

    }

    public void loadTextureAsyncly(Location location, BufferedImage img, boolean flush, boolean clearable, boolean linear, Runnable after) {

        AsyncGLContext.submit(
                () -> {
                    Textures.loadTexture(location, img, clearable, linear);

                    if (flush) {
                        img.flush();
                    }

                    if (after != null) {
                        after.run();
                    }
                }
        );

    }

    public void loadTextureAsyncly(Location location, BufferedImage img, boolean flush, boolean clearable, boolean linear) {
        Textures.loadTextureAsyncly(location, img, flush, clearable, linear, null);
    }

    public void loadTextureAsyncly(Location location, BufferedImage img, boolean flush) {
        Textures.loadTextureAsyncly(location, img, flush, true, false, null);
    }

    public void loadTextureAsyncly(Location location, BufferedImage img, Runnable after) {
        Textures.loadTextureAsyncly(location, img, true, true, false, after);
    }

    public void loadTextureAsyncly(Location location, BufferedImage img) {
        Textures.loadTextureAsyncly(location, img, true, true, false, null);
    }

    public void triggerLoad(Location location, ITextureObject object) {
        Minecraft.getMinecraft().getTextureManager().loadTexture(location, object);
    }

    public void triggerLoad(Location location) {
        Minecraft.getMinecraft().getTextureManager().triggerLoad(location);
    }

}
