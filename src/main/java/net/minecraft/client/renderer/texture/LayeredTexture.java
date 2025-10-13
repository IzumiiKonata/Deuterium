package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Config;
import net.minecraft.util.Location;
import net.optifine.shaders.ShadersTex;
import tritium.rendering.async.AsyncGLContext;
import tritium.utils.logging.LogManager;
import tritium.utils.logging.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class LayeredTexture extends AbstractTexture {
    private static final Logger logger = LogManager.getLogger();
    public final List<String> layeredTextureNames;
    private Location textureLocation;

    public LayeredTexture(String... textureNames) {
        this.layeredTextureNames = Lists.newArrayList(textureNames);

        if (textureNames.length > 0 && textureNames[0] != null) {
            this.textureLocation = Location.of(textureNames[0]);
        }
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        BufferedImage bufferedimage = null;

        try {
            for (String s : this.layeredTextureNames) {
                if (s != null) {
                    InputStream inputstream = resourceManager.getResource(Location.of(s)).getInputStream();
                    BufferedImage bufferedimage1 = ImageIO.read(inputstream);

                    if (bufferedimage == null) {
                        bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), 2);
                    }

                    bufferedimage.getGraphics().drawImage(bufferedimage1, 0, 0, null);
                }
            }
        } catch (IOException ioexception) {
            logger.error("Couldn't load layered image", ioexception);
            return;
        }

        if (Config.isShaders()) {
            ShadersTex.loadSimpleTexture(this.getGlTextureId(), bufferedimage, false, false, resourceManager, this.textureLocation, this.getMultiTexID());
        } else {
            TextureUtil.uploadTextureImage(this.getGlTextureId(), bufferedimage);
        }
    }
}
