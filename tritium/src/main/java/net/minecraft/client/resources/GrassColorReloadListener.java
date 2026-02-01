package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.Location;
import net.minecraft.world.ColorizerGrass;

import java.io.IOException;

public class GrassColorReloadListener implements IResourceManagerReloadListener {
    private static final Location LOC_GRASS_PNG = Location.of("textures/colormap/grass.png");

    public void onResourceManagerReload(IResourceManager resourceManager) {
        ColorizerGrass.setGrassBiomeColorizer(TextureUtil.readImageData(resourceManager, LOC_GRASS_PNG));


    }
}
