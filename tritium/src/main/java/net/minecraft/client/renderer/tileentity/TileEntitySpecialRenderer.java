package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Location;
import net.minecraft.world.World;
import net.optifine.entity.model.IEntityRenderer;

public abstract class TileEntitySpecialRenderer<T extends TileEntity> implements IEntityRenderer {
    protected static final Location[] DESTROY_STAGES = new Location[]{Location.of("textures/blocks/destroy_stage_0.png"), Location.of("textures/blocks/destroy_stage_1.png"), Location.of("textures/blocks/destroy_stage_2.png"), Location.of("textures/blocks/destroy_stage_3.png"), Location.of("textures/blocks/destroy_stage_4.png"), Location.of("textures/blocks/destroy_stage_5.png"), Location.of("textures/blocks/destroy_stage_6.png"), Location.of("textures/blocks/destroy_stage_7.png"), Location.of("textures/blocks/destroy_stage_8.png"), Location.of("textures/blocks/destroy_stage_9.png")};
    protected TileEntityRendererDispatcher rendererDispatcher;
    private Class tileEntityClass = null;
    private Location locationTextureCustom = null;

    public abstract void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage);

    protected void bindTexture(Location location) {
        TextureManager texturemanager = this.rendererDispatcher.renderEngine;

        if (texturemanager != null) {
            texturemanager.bindTexture(location);
        }
    }

    protected World getWorld() {
        return this.rendererDispatcher.worldObj;
    }

    public void setRendererDispatcher(TileEntityRendererDispatcher rendererDispatcherIn) {
        this.rendererDispatcher = rendererDispatcherIn;
    }

    public FontRenderer getFontRenderer() {
        return this.rendererDispatcher.getFontRenderer();
    }

    /**
     * If true the {@link TileEntitySpecialRenderer} will always be rendered while the player is in the render bounding
     * box {@link TileEntity#getRenderBoundingBox()} and his squared distance with the {@link TileEntity} is smaller
     * than {@link TileEntity#getMaxRenderDistanceSquared()}.
     */
    public boolean forceTileEntityRender() {
        return false;
    }

    public void renderTileEntityFast(T p_renderTileEntityFast_1_, double p_renderTileEntityFast_2_, double p_renderTileEntityFast_4_, double p_renderTileEntityFast_6_, float p_renderTileEntityFast_8_, int p_renderTileEntityFast_9_, WorldRenderer p_renderTileEntityFast_10_) {
    }

    public Class getEntityClass() {
        return this.tileEntityClass;
    }

    public void setEntityClass(Class p_setEntityClass_1_) {
        this.tileEntityClass = p_setEntityClass_1_;
    }

    public Location getLocationTextureCustom() {
        return this.locationTextureCustom;
    }

    public void setLocationTextureCustom(Location p_setLocationTextureCustom_1_) {
        this.locationTextureCustom = p_setLocationTextureCustom_1_;
    }
}
