package tritium.bridge.rendering;

import lombok.Getter;
import lombok.SneakyThrows;
import today.opai.api.interfaces.render.Font;
import today.opai.api.interfaces.render.FontUtil;
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 17:11
 */
public class FontUtilImpl implements FontUtil {

    @Getter
    private static final FontUtilImpl instance = new FontUtilImpl();

    @Override
    public Font createFont(byte[] data, float size) {
        CFontRenderer fontRenderer = FontManager.create(size, new ByteArrayInputStream(data));

        FontManager.getExtensionCreatedFontRenderers().add(fontRenderer);

        return new FontWrapper(fontRenderer);
    }

    @Override
    public Font createFont(InputStream inputStream, float size) {
        CFontRenderer fontRenderer = FontManager.create(size, inputStream);

        FontManager.getExtensionCreatedFontRenderers().add(fontRenderer);

        return new FontWrapper(fontRenderer);
    }

    @Override
    @SneakyThrows
    public Font createFont(File fontFile, float size) {
        CFontRenderer fontRenderer = FontManager.create(size, Files.newInputStream(fontFile.toPath()));

        FontManager.getExtensionCreatedFontRenderers().add(fontRenderer);

        return new FontWrapper(fontRenderer);
    }

    @Override
    public Font getVanillaFont() {
        return FontManager.vanillaWrapper;
    }

    @Override
    public Font getGoogleSansB18() {
        return FontManager.googleSans18BoldW;
    }

    @Override
    public Font getGoogleSansB16() {
        return FontManager.googleSans16BoldW;
    }

    @Override
    public Font getGoogleSans18() {
        return FontManager.googleSans18W;
    }

    @Override
    public Font getGoogleSans16() {
        return FontManager.googleSans16W;
    }

    @Override
    public Font getProduct18() {
        return FontManager.product18W;
    }

    @Override
    public Font getTahoma18() {
        return FontManager.tahoma18W;
    }
}
