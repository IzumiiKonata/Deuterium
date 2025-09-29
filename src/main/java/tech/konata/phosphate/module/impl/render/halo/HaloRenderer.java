package tech.konata.phosphate.module.impl.render.halo;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.entities.impl.Image;

import java.time.Duration;
import java.util.*;

/**
 * @author IzumiiKonata
 * @since 2024/11/22 17:44
 */
public class HaloRenderer {

    @Getter
    private final Map<String, HaloData> haloDataMap = new HashMap<>();

    @Getter
    private final List<String> haloNames = new ArrayList<>();

    @Getter
    @Setter
    private HaloData currentHalo = null;

    public HaloRenderer() {

    }

    final Animation floatAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofSeconds(2));
    boolean animationBackwards = false;


    public void render() {
        double animationHeight = 0.2;
        floatAnimation.run(animationBackwards ? 0 : animationHeight);

        if (floatAnimation.getValue() == 0) animationBackwards = false;
        if (floatAnimation.getValue() == animationHeight) animationBackwards = true;

        if (this.currentHalo == null) {

            if (this.haloDataMap.isEmpty())
                return;

            this.currentHalo = this.haloDataMap.values().iterator().next();

        }

        GlStateManager.rotate(-90, 1, 0, 0);
        GlStateManager.translate(0, floatAnimation.getValue(), 0);
        GlStateManager.rotate(90, 1, 0, 0);

        double imgWidth = ModuleManager.halo.size.getValue(), imgHeight = ModuleManager.halo.size.getValue();

        if (!this.currentHalo.layered) {
            Image.drawLinear(this.currentHalo.textureLocation.get(0), -imgWidth * 0.5, -imgHeight * 0.5, imgWidth, imgHeight, Image.Type.Normal);
        } else {

            boolean maskEnabled = GlStateManager.depthState.maskEnabled;

            if (maskEnabled)
                GlStateManager.depthMask(false);

            for (Location location : this.currentHalo.textureLocation) {
                Image.drawLinear(location, -imgWidth * 0.5, -imgHeight * 0.5, imgWidth, imgHeight, Image.Type.Normal);

                GlStateManager.rotate(-90, 1, 0, 0);
                GlStateManager.translate(0, this.currentHalo.spacing * ModuleManager.halo.size.getValue().floatValue(), 0);
                GlStateManager.rotate(90, 1, 0, 0);
            }
            if (maskEnabled)
                GlStateManager.depthMask(true);

        }

    }

    public void addHalo(String name, HaloData data) {
        haloDataMap.put(name, data);
        this.haloNames.add(name);
    }

    public static class HaloData {

        public final boolean layered;

        public final List<Location> textureLocation = new ArrayList<>();

        // for layered
        public double spacing = 0.0;

        // not layered
        public HaloData(Location textureLocation) {
            this.textureLocation.add(textureLocation);
            this.layered = false;
        }

        // layered
        public HaloData(double spacing, Location... textureLocations) {
            this.spacing = spacing;
            this.layered = true;
            this.textureLocation.addAll(Arrays.asList(textureLocations));
        }

    }

}
