package tritium.widget.impl;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.minecraft.util.Location;
import tritium.Tritium;
import tritium.rendering.Image;
import tritium.rendering.texture.Textures;
import tritium.utils.other.gif.GifDecoder;
import tritium.utils.timing.Timer;
import tritium.widget.Widget;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author IzumiiKonata
 * Date: 2025/2/4 11:08
 */
public class GifTextureWidget extends Widget {

    private final Location[] location;
    private final File imgFile;

    private final int defWidth, defHeight;

    private final GifDecoder decoder;

    @SneakyThrows
    public GifTextureWidget(File gifFile) {
        super("GifTexture");

        this.imgFile = gifFile;

        decoder = new GifDecoder();
        decoder.read(new FileInputStream(imgFile));

        location = new Location[decoder.getFrameCount()];

        for (int i = 0; i < decoder.getFrameCount(); i++) {
            location[i] = this.randomIdentifier();

            Textures.loadTextureAsyncly(location[i], decoder.getFrame(i));
        }

        defWidth = decoder.getWidth() / 4;
        defHeight = decoder.getHeight() / 4;
        super.setWidth(defWidth);
        super.setHeight(defHeight);
        super.setResizable(true, defWidth, defHeight);
        super.setLockResizeRatio(true, (double) defWidth / defHeight);
        super.setEnabled(true);
        super.setShouldRender(() -> false);
    }

    int index = 0;

    Timer timer = new Timer();

    @Override
    public void onRender(boolean editing) {
        NORMAL.add(() -> {

            if (timer.isDelayed(decoder.getDelay(index))) {
                timer.reset();
                index += 1;

                if (index >= location.length) {
                    index = 0;
                }
            }

            if (mc.getTextureManager().getTexture(location[index]) != null) {
                Image.drawLinear(location[index], this.getX(), this.getY(), this.getWidth(), this.getHeight(), Image.Type.Normal);
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
