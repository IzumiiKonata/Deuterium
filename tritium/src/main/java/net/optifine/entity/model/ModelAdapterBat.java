package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBat;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityBat;


public class ModelAdapterBat extends ModelAdapter {
    public ModelAdapterBat() {
        super(EntityBat.class, "bat", 0.25F);
    }

    public ModelBase makeModel() {
        return new ModelBat();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (model instanceof ModelBat) {
            switch (modelPart) {
                case "head" -> {
                    return ((ModelBat) model).batHead;
                }
                case "body" -> {
                    return ((ModelBat) model).batBody;
                }
                case "right_wing" -> {
                    return ((ModelBat) model).batRightWing;
                }
                case "left_wing" -> {
                    return ((ModelBat) model).batLeftWing;
                }
                case "outer_right_wing" -> {
                    return ((ModelBat) model).batOuterRightWing;
                }
                case "outer_left_wing" -> {
                    return ((ModelBat) model).batOuterLeftWing;
                }
            }
        }
        return null;
    }

    public String[] getModelRendererNames() {
        return new String[]{"head", "body", "right_wing", "left_wing", "outer_right_wing", "outer_left_wing"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        RenderBat renderbat = new RenderBat(rendermanager);
        renderbat.mainModel = modelBase;
        renderbat.shadowSize = shadowSize;
        return renderbat;
    }
}
