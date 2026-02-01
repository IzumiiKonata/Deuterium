package tritium.bridge.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import today.opai.api.interfaces.render.Image;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:54
 */
public class ImageImpl implements Image {

    ITextureObject texObj = null;

    public ImageImpl(String b64) {
        this(Base64.getDecoder().decode(b64));
    }

    public ImageImpl(byte[] b) {
        this(new ByteArrayInputStream(b));
    }

    public ImageImpl(InputStream is) {
        NativeBackedImage img = NativeBackedImage.make(is);

        if (img == null)
            throw new IllegalArgumentException("Cannot read image");

        if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            texObj = new DynamicTexture(img, true);
        } else {
            MultiThreadingUtil.runAsync(() -> texObj = new DynamicTexture(img, true));
        }
    }

    @Override
    public void render(float x, float y, float width, float height) {
        if (texObj == null)
            return;

        tritium.rendering.Image.draw(texObj.getGlTextureId(), x, y, width, height, tritium.rendering.Image.Type.Normal);
    }
}
