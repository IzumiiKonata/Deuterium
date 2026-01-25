package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.Location;
import net.minecraft.world.ColorizerFoliage;

import java.io.IOException;

public class FoliageColorReloadListener implements IResourceManagerReloadListener {
    private static final Location LOC_FOLIAGE_PNG = Location.of("textures/colormap/foliage.png");

    public void onResourceManagerReload(IResourceManager resourceManager) {
        try {
            ColorizerFoliage.setFoliageBiomeColorizer(TextureUtil.readImageData(resourceManager, LOC_FOLIAGE_PNG));
        } catch (IOException var3) {
        }
    }
}
