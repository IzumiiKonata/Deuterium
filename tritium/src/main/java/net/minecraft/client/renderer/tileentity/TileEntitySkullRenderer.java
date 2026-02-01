package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TileEntitySkullRenderer extends TileEntitySpecialRenderer<TileEntitySkull> {
    private static final Location SKELETON_TEXTURES = Location.of("textures/entity/skeleton/skeleton.png");
    private static final Location WITHER_SKELETON_TEXTURES = Location.of("textures/entity/skeleton/wither_skeleton.png");
    private static final Location ZOMBIE_TEXTURES = Location.of("textures/entity/zombie/zombie.png");
    private static final Location CREEPER_TEXTURES = Location.of("textures/entity/creeper/creeper.png");
    public static TileEntitySkullRenderer instance;
    private final ModelSkeletonHead skeletonHead = new ModelSkeletonHead(0, 0, 64, 32);
    public ModelSkeletonHead humanoidHead = new ModelHumanoidHead();
    
    // Cache for player skin textures to avoid repeated loading and memory allocation
    private final Map<GameProfile, Location> skinTextureCache = new ConcurrentHashMap<>();

    public void renderTileEntityAt(TileEntitySkull te, double x, double y, double z, float partialTicks, int destroyStage) {
        EnumFacing enumfacing = EnumFacing.getFront(te.getBlockMetadata() & 7);
        this.renderSkull((float) x, (float) y, (float) z, enumfacing, (float) (te.getSkullRotation() * 360) / 16.0F, te.getSkullType(), te.getPlayerProfile(), destroyStage);
    }

    public void setRendererDispatcher(TileEntityRendererDispatcher rendererDispatcherIn) {
        super.setRendererDispatcher(rendererDispatcherIn);
        instance = this;
    }

    public void renderSkull(float x, float y, float z, EnumFacing facing, float rot, int skullType, GameProfile profile, int destroyStage) {
        ModelBase modelbase = this.skeletonHead;

        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        } else {
            switch (skullType) {
                case 1:
                    this.bindTexture(WITHER_SKELETON_TEXTURES);
                    break;

                case 2:
                    this.bindTexture(ZOMBIE_TEXTURES);
                    modelbase = this.humanoidHead;
                    break;

                case 3:
                    modelbase = this.humanoidHead;
                    Location resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();

                    if (profile != null) {
                        resourcelocation = skinTextureCache.get(profile);
                        
                        if (resourcelocation == null) {
                            Minecraft minecraft = Minecraft.getMinecraft();
                            Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);

                            if (map.containsKey(Type.SKIN)) {
                                resourcelocation = minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
                                skinTextureCache.put(profile, resourcelocation);
                            } else {
                                UUID uuid = EntityPlayer.getUUID(profile);
                                resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                                skinTextureCache.put(profile, resourcelocation);
                            }
                        }
                    }

                    this.bindTexture(resourcelocation);
                    break;

                case 4:
                    this.bindTexture(CREEPER_TEXTURES);
                    break;

                case 0:
                default:
                    this.bindTexture(SKELETON_TEXTURES);
                    break;
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        if (facing != EnumFacing.UP) {
            switch (facing) {
                case NORTH:
                    GlStateManager.translate(x + 0.5F, y + 0.25F, z + 0.74F);
                    break;

                case SOUTH:
                    GlStateManager.translate(x + 0.5F, y + 0.25F, z + 0.26F);
                    rot = 180.0F;
                    break;

                case WEST:
                    GlStateManager.translate(x + 0.74F, y + 0.25F, z + 0.5F);
                    rot = 270.0F;
                    break;

                case EAST:
                default:
                    GlStateManager.translate(x + 0.26F, y + 0.25F, z + 0.5F);
                    rot = 90.0F;
            }
        } else {
            GlStateManager.translate(x + 0.5F, y, z + 0.5F);
        }

        float f = 0.0625F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.enableAlpha();
        modelbase.render(null, 0.0F, 0.0F, 0.0F, rot, 0.0F, f);
        GlStateManager.popMatrix();

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
    }
}
