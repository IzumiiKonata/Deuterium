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
            if (modelPart.equals("head"))
                return ((ModelBat) model).batHead;
            if (modelPart.equals("body"))
                return ((ModelBat) model).batBody;
            if (modelPart.equals("right_wing"))
                return ((ModelBat) model).batRightWing;
            if (modelPart.equals("left_wing"))
                return ((ModelBat) model).batLeftWing;
            if (modelPart.equals("outer_right_wing"))
                return ((ModelBat) model).batOuterRightWing;
            if (modelPart.equals("outer_left_wing"))
                return ((ModelBat) model).batOuterLeftWing;
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
