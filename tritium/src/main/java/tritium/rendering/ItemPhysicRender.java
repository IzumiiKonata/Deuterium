package tritium.rendering;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ItemPhysicRender {
    public static final Location RES_ITEM_GLINT = Location.of(
            "textures/misc/enchanted_item_glint.png");
    public static Random random = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static RenderItem renderItem = mc.getRenderItem();
    public static long tick;
    public static double rotation;

    public static void doRenderItemPhysic(Entity par1Entity, double x, double y, double z, float par8, float par9) {
        EntityItem item;
        ItemStack itemstack;
        rotation = (double) (System.nanoTime() - tick) / 3000000.0;
        if (!mc.inGameHasFocus) {
            rotation = 0.0;
        }
        if ((itemstack = (item = (EntityItem) par1Entity).getEntityItem()).getItem() != null) {
            random.setSeed(187L);
            boolean flag = false;
            mc.getRenderManager().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            mc.getRenderManager().renderEngine.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false,
                    false);
            flag = true;
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.pushMatrix();
            IBakedModel ibakedmodel = renderItem.getItemModelMesher().getItemModel(itemstack);
            int i = func_177077_a(item, x, y - 0.1f, z, par9, ibakedmodel);
            BlockPos pos = new BlockPos(item);
            if (item.rotationPitch > 360.0f) {
                item.rotationPitch = 0.0f;
            }
            if (!(item.getPosition() == null)) {
                if (item.onGround) {
                    if (item.rotationPitch != 0.0f && item.rotationPitch != 90.0f && item.rotationPitch != 180.0f
                            && item.rotationPitch != 270.0f) {
                        double Abstand0 = formPositiv(item.rotationPitch);
                        double Abstand90 = formPositiv(item.rotationPitch - 90.0f);
                        double Abstand180 = formPositiv(item.rotationPitch - 180.0f);
                        double Abstand270 = formPositiv(item.rotationPitch - 270.0f);
                        if (Abstand0 <= Abstand90 && Abstand0 <= Abstand180 && Abstand0 <= Abstand270) {
                            if (item.rotationPitch < 0.0f) {
                                item.rotationPitch = (float) ((double) item.rotationPitch + rotation);
                            } else {
                                item.rotationPitch = (float) ((double) item.rotationPitch - rotation);
                            }
                        }
                        if (Abstand90 < Abstand0 && Abstand90 <= Abstand180 && Abstand90 <= Abstand270) {
                            if (item.rotationPitch - 90.0f < 0.0f) {
                                item.rotationPitch = (float) ((double) item.rotationPitch + rotation);
                            } else {
                                item.rotationPitch = (float) ((double) item.rotationPitch - rotation);
                            }
                        }
                        if (Abstand180 < Abstand90 && Abstand180 < Abstand0 && Abstand180 <= Abstand270) {
                            if (item.rotationPitch - 180.0f < 0.0f) {
                                item.rotationPitch = (float) ((double) item.rotationPitch + rotation);
                            } else {
                                item.rotationPitch = (float) ((double) item.rotationPitch - rotation);
                            }
                        }
                        if (Abstand270 < Abstand90 && Abstand270 < Abstand180 && Abstand270 < Abstand0) {
                            if (item.rotationPitch - 270.0f < 0.0f) {
                                item.rotationPitch = (float) ((double) item.rotationPitch + rotation);
                            } else {
                                item.rotationPitch = (float) ((double) item.rotationPitch - rotation);
                            }
                        }
                    }
                } else {
                    BlockPos posUp = new BlockPos(item);
                    posUp.add(0, 0.2f, 0);
                    Material m1 = item.worldObj.getBlockState(posUp).getBlock().getMaterial();
                    Material m2 = item.worldObj.getBlockState(pos).getBlock().getMaterial();
                    boolean m3 = item.isInsideOfMaterial(Material.water);
                    boolean m4 = item.isInWater();
                    if (m3 | m1 == Material.water | m2 == Material.water | m4) {
                        item.rotationPitch = (float) ((double) item.rotationPitch + rotation / 4.0);
                    } else {
                        item.rotationPitch = (float) ((double) item.rotationPitch + rotation * 2.0);
                    }
                }
            }
            GL11.glRotatef(item.rotationYaw, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(item.rotationPitch + 90.0f, 1.0f, 0.0f, 0.0f);
            int j = 0;
            while (j < i) {
                if (ibakedmodel.isAmbientOcclusion()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.7f, 0.7f, 0.7f);
                    renderItem.renderItem(itemstack, ibakedmodel);
                    GlStateManager.popMatrix();
                } else {
                    GlStateManager.pushMatrix();
                    if (j > 0 && shouldSpreadItems()) {
                        GlStateManager.translate(0.0f, 0.0f, 0.046875f * (float) j);
                    }
                    renderItem.renderItem(itemstack, ibakedmodel);
                    if (!shouldSpreadItems()) {
                        GlStateManager.translate(0.0f, 0.0f, 0.046875f);
                    }
                    GlStateManager.popMatrix();
                }
                ++j;
            }
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            mc.getRenderManager().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            mc.getRenderManager().renderEngine.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        }
    }

    public static int func_177077_a(EntityItem item, double x, double y, double z, float p_177077_8_,
                                    IBakedModel p_177077_9_) {
        ItemStack itemstack = item.getEntityItem();
        Item item2 = itemstack.getItem();
        if (item2 == null) {
            return 0;
        }
        boolean flag = p_177077_9_.isAmbientOcclusion();
        int i = getModelCount(itemstack);
        float f1 = 0.25f;
        float f2 = 0.0f;
        GlStateManager.translate((float) x, (float) y + f2 + 0.25f, (float) z);
        float f3 = 0.0f;
        if (flag || mc.getRenderManager().renderEngine != null && mc.gameSettings.fancyGraphics) {
            GlStateManager.rotate(f3, 0.0f, 1.0f, 0.0f);
        }
        if (!flag) {
            f3 = -0.0f * (float) (i - 1) * 0.5f;
            float f4 = -0.0f * (float) (i - 1) * 0.5f;
            float f5 = -0.046875f * (float) (i - 1) * 0.5f;
            GlStateManager.translate(f3, f4, f5);
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        return i;
    }

    public static int getModelCount(ItemStack stack) {
        byte b0 = 1;
        if (stack.animationsToGo > 48) {
            b0 = 5;
        } else if (stack.animationsToGo > 32) {
            b0 = 4;
        } else if (stack.animationsToGo > 16) {
            b0 = 3;
        } else if (stack.animationsToGo > 1) {
            b0 = 2;
        }
        return b0;
    }

    public static byte getMiniBlockCount(ItemStack stack, byte original) {
        return original;
    }

    public static byte getMiniItemCount(ItemStack stack, byte original) {
        return original;
    }

    public static boolean shouldSpreadItems() {
        return true;
    }

    public static double formPositiv(float rotationPitch) {
        if (rotationPitch > 0.0f) {
            return rotationPitch;
        }
        return -rotationPitch;
    }

}