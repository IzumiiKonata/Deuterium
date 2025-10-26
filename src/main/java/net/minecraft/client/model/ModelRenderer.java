package net.minecraft.client.model;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.util.Location;
import net.minecraft.util.Matrix4f;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.model.ModelSprite;
import net.optifine.shaders.Shaders;
import org.lwjglx.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ModelRenderer {
    /**
     * The size of the texture file's width in pixels.
     */
    public float textureWidth;

    /**
     * The size of the texture file's height in pixels.
     */
    public float textureHeight;

    /**
     * The X offset into the texture used for displaying this model
     */
    private int textureOffsetX;

    /**
     * The Y offset into the texture used for displaying this model
     */
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    private boolean compiled;

    private boolean compiledState;

    /**
     * The GL display list rendered by the Tessellator for this model
     */
    private int displayList;
    public boolean mirror;
    public boolean showModel;

    /**
     * Hides the model.
     */
    public boolean isHidden;
    public List<ModelBox> cubeList;
    public List<ModelRenderer> childModels;
    public final String boxName;
    private final ModelBase baseModel;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public List spriteList;
    public boolean mirrorV;
    public float scaleX;
    public float scaleY;
    public float scaleZ;
    private int countResetDisplayList;
    private Location textureLocation;
    private String id;
    private ModelUpdater modelUpdater;
    private final RenderGlobal renderGlobal;

    public ModelRenderer(ModelBase model, String boxNameIn) {
        this.spriteList = new ArrayList();
        this.mirrorV = false;
        this.scaleX = 1.0F;
        this.scaleY = 1.0F;
        this.scaleZ = 1.0F;
        this.textureLocation = null;
        this.id = null;
        this.renderGlobal = Config.getRenderGlobal();
        this.textureWidth = 64.0F;
        this.textureHeight = 32.0F;
        this.showModel = true;
        this.cubeList = Lists.newArrayList();
        this.baseModel = model;
        model.boxList.add(this);
        this.boxName = boxNameIn;
        this.setTextureSize(model.textureWidth, model.textureHeight);
    }

    public ModelRenderer(ModelBase model) {
        this(model, null);
    }

    public ModelRenderer(ModelBase model, int texOffX, int texOffY) {
        this(model);
        this.setTextureOffset(texOffX, texOffY);
    }

    /**
     * Sets the current box's rotation points and rotation angles to another box.
     */
    public void addChild(ModelRenderer renderer) {
        if (this.childModels == null) {
            this.childModels = Lists.newArrayList();
        }

        this.childModels.add(renderer);
    }

    public ModelRenderer setTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
        partName = this.boxName + "." + partName;
        TextureOffset textureoffset = this.baseModel.getTextureOffset(partName);
        this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
        this.cubeList.add((new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F)).setBoxName(partName));
        return this;
    }

    public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
        return this;
    }

    public ModelRenderer addBox(float p_178769_1_, float p_178769_2_, float p_178769_3_, int p_178769_4_, int p_178769_5_, int p_178769_6_, boolean p_178769_7_) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, p_178769_1_, p_178769_2_, p_178769_3_, p_178769_4_, p_178769_5_, p_178769_6_, 0.0F, p_178769_7_));
        return this;
    }

    /**
     * Creates a textured box. Args: originX, originY, originZ, width, height, depth, scaleFactor.
     */
    public void addBox(float p_78790_1_, float p_78790_2_, float p_78790_3_, int width, int height, int depth, float scaleFactor) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, p_78790_1_, p_78790_2_, p_78790_3_, width, height, depth, scaleFactor));
    }

    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
        this.rotationPointX = rotationPointXIn;
        this.rotationPointY = rotationPointYIn;
        this.rotationPointZ = rotationPointZIn;
    }

    public void render(float p_78785_1_) {

        if (!compiledState) {
            this.compiled = false;
        }

        if (!this.isHidden && this.showModel) {
            this.checkResetDisplayList();

            if (!this.compiled) {
                this.compileDisplayList(p_78785_1_);
            }

            int i = 0;

            if (this.textureLocation != null && !this.renderGlobal.renderOverlayDamaged) {
                if (this.renderGlobal.renderOverlayEyes) {
                    return;
                }

                i = GlStateManager.getBoundTexture();
                Config.getTextureManager().bindTexture(this.textureLocation);
            }

            if (this.modelUpdater != null) {
                this.modelUpdater.update();
            }

            GlStateManager.pushMatrix();

            valkyrie$applyTransformation(p_78785_1_, true);

            GlStateManager.callList(displayList);

            if (childModels != null)
                for (final ModelRenderer childModel : childModels)
                    childModel.render(p_78785_1_);

            GlStateManager.popMatrix();

            if (i != 0) {
                GlStateManager.bindTexture(i);
            }
        }
    }

    private static final FloatBuffer valkyrie$buffer = BufferUtils.createFloatBuffer(16);
    private static final org.lwjglx.util.vector.Matrix4f valkyrie$matrix = new Matrix4f();

    private void valkyrie$applyTransformation(final float scale, final boolean applyOffset) {
        valkyrie$matrix.setIdentity();

        boolean flag = this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F;

        if (applyOffset)
            valkyrie$matrix.translate(new Vector3f(offsetX, offsetY, offsetZ));

        if (this.rotateAngleX == 0 && this.rotateAngleY == 0 && this.rotateAngleZ == 0) {

            if (flag) {
                GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
            }

            if (this.rotationPointX != 0 || this.rotationPointY != 0 || this.rotationPointZ != 0)
                valkyrie$matrix.translate(new Vector3f(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale));
        } else {
            valkyrie$matrix.translate(new Vector3f(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale));

            if (rotateAngleZ != 0)
                valkyrie$matrix.rotate(rotateAngleZ, new Vector3f(0, 0, 1));

            if (rotateAngleY != 0)
                valkyrie$matrix.rotate(rotateAngleY, new Vector3f(0, 1, 0));

            if (rotateAngleX != 0)
                valkyrie$matrix.rotate(rotateAngleX, new Vector3f(1, 0, 0));

            if (flag) {
                GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
            }
        }

        valkyrie$matrix.putN(valkyrie$buffer);
        GL11.glMultMatrixf(valkyrie$buffer);
    }

    public void renderWithRotation(float p_78791_1_) {
        if (!this.isHidden && this.showModel) {
            this.checkResetDisplayList();

            if (!this.compiled) {
                this.compileDisplayList(p_78791_1_);
            }

            int i = 0;

            if (this.textureLocation != null && !this.renderGlobal.renderOverlayDamaged) {
                if (this.renderGlobal.renderOverlayEyes) {
                    return;
                }

                i = GlStateManager.getBoundTexture();
                Config.getTextureManager().bindTexture(this.textureLocation);
            }

            if (this.modelUpdater != null) {
                this.modelUpdater.update();
            }

            GlStateManager.pushMatrix();

            valkyrie$applyTransformation(p_78791_1_, false);

            GlStateManager.callList(displayList);

            GlStateManager.popMatrix();

            if (i != 0) {
                GlStateManager.bindTexture(i);
            }
        }
    }

    /**
     * Allows the changing of Angles after a box has been rendered
     */
    public void postRender(float scale) {
        if (!this.isHidden && this.showModel) {
            this.checkResetDisplayList();

            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            valkyrie$applyTransformation(scale, false);
        }
    }

    /**
     * Compiles a GL display list for this model
     */
    private void compileDisplayList(float scale) {
        if (this.displayList == 0) {
            this.displayList = GLAllocation.generateDisplayLists(1);
        }

        GL11.glNewList(this.displayList, GL11.GL_COMPILE);

        this.compiledState = true;

        if (!Config.isShaders()) {
            Tessellator.getInstance().getWorldRenderer().begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        }

        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();

        for (ModelBox modelBox : this.cubeList) {
            modelBox.render(worldrenderer, scale);
        }

        for (Object o : this.spriteList) {
            ModelSprite modelsprite = (ModelSprite) o;
            modelsprite.render(Tessellator.getInstance(), scale);
        }

        if (!Config.isShaders()) {
            Tessellator.getInstance().draw();
        }

        GL11.glEndList();
        this.compiled = true;
    }

    /**
     * Returns the model renderer with the new texture parameters.
     */
    public ModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn) {
        this.textureWidth = (float) textureWidthIn;
        this.textureHeight = (float) textureHeightIn;
        return this;
    }

    public void addSprite(float p_addSprite_1_, float p_addSprite_2_, float p_addSprite_3_, int p_addSprite_4_, int p_addSprite_5_, int p_addSprite_6_, float p_addSprite_7_) {
        this.spriteList.add(new ModelSprite(this, this.textureOffsetX, this.textureOffsetY, p_addSprite_1_, p_addSprite_2_, p_addSprite_3_, p_addSprite_4_, p_addSprite_5_, p_addSprite_6_, p_addSprite_7_));
    }

    public boolean getCompiled() {
        return this.compiled;
    }

    public int getDisplayList() {
        return this.displayList;
    }

    private void checkResetDisplayList() {
        if (this.countResetDisplayList != Shaders.countResetDisplayLists) {
            this.compiled = false;
            this.countResetDisplayList = Shaders.countResetDisplayLists;
        }
    }

    public Location getTextureLocation() {
        return this.textureLocation;
    }

    public void setTextureLocation(Location p_setTextureLocation_1_) {
        this.textureLocation = p_setTextureLocation_1_;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String p_setId_1_) {
        this.id = p_setId_1_;
    }

    public void addBox(int[][] p_addBox_1_, float p_addBox_2_, float p_addBox_3_, float p_addBox_4_, float p_addBox_5_, float p_addBox_6_, float p_addBox_7_, float p_addBox_8_) {
        this.cubeList.add(new ModelBox(this, p_addBox_1_, p_addBox_2_, p_addBox_3_, p_addBox_4_, p_addBox_5_, p_addBox_6_, p_addBox_7_, p_addBox_8_, this.mirror));
    }

    public ModelRenderer getChild(String p_getChild_1_) {
        if (p_getChild_1_ != null) {
            if (this.childModels != null) {
                for (ModelRenderer modelrenderer : this.childModels) {
                    if (p_getChild_1_.equals(modelrenderer.getId())) {
                        return modelrenderer;
                    }
                }
            }

        }
        return null;
    }

    public ModelRenderer getChildDeep(String p_getChildDeep_1_) {
        if (p_getChildDeep_1_ == null) {
            return null;
        } else {
            ModelRenderer modelrenderer = this.getChild(p_getChildDeep_1_);

            if (modelrenderer != null) {
                return modelrenderer;
            } else {
                if (this.childModels != null) {
                    for (ModelRenderer modelrenderer1 : this.childModels) {
                        ModelRenderer modelrenderer2 = modelrenderer1.getChildDeep(p_getChildDeep_1_);

                        if (modelrenderer2 != null) {
                            return modelrenderer2;
                        }
                    }
                }

                return null;
            }
        }
    }

    public void setModelUpdater(ModelUpdater p_setModelUpdater_1_) {
        this.modelUpdater = p_setModelUpdater_1_;
    }

    public String toString() {
        return "id: " + this.id + ", boxes: " + (this.cubeList != null ? this.cubeList.size() : null) + ", submodels: " + (this.childModels != null ? this.childModels.size() : null);
    }
}
