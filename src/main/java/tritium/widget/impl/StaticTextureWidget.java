package tritium.widget.impl;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.util.Location;
import tritium.Tritium;
import tritium.rendering.Image;
import tritium.rendering.texture.Textures;
import tritium.widget.Widget;
import tritium.interfaces.SharedRenderingConstants;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author IzumiiKonata
 * Date: 2025/2/4 11:08
 */
public class StaticTextureWidget extends Widget {

    private final Location location = this.randomIdentifier();
    private final File imgFile;

    private final int defWidth, defHeight;

    @SneakyThrows
    public StaticTextureWidget(File imgFile) {
        super("Texture");

        this.imgFile = imgFile;
        BufferedImage image = null;
        try {
            image = NativeBackedImage.make(Files.newInputStream(imgFile.toPath()));
        } catch (Exception e) {
            System.err.println("File \"" + imgFile.getAbsolutePath() + "\" is not an image!");
            e.printStackTrace();
        }
        Textures.loadTextureAsyncly(location, image);
        defWidth = image.getWidth() / 8;
        defHeight = image.getHeight() / 8;
        super.setWidth(defWidth);
        super.setHeight(defHeight);
        super.setResizable(true, defWidth, defHeight);
        super.setLockResizeRatio(true, (double) defWidth / defHeight);
        super.setEnabled(true);
        super.setShouldRender(() -> false);
    }

    @Override
    public void onRender(boolean editing) {
        SharedRenderingConstants.NORMAL.add(() -> {

            if (mc.getTextureManager().getTexture(location) != null) {
                Image.drawLinear(location, this.getX(), this.getY(), this.getWidth(), this.getHeight(), Image.Type.Normal);
            }

        });
    }

    public Location randomIdentifier() {
        return Location.of(Tritium.NAME, "temp/" + randomString());
    }

    private String randomString() {
        return IntStream.range(0, 32)
                .mapToObj(operand -> String.valueOf((char) ('a' + new Random().nextInt('z' + 1 - 'a'))))
                .collect(Collectors.joining());
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject jsonObject = super.saveConfig();

        jsonObject.addProperty("Path", this.imgFile.getAbsolutePath());

        return jsonObject;
    }
}
