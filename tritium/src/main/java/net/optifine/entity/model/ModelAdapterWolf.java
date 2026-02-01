package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntityWolf;


public class ModelAdapterWolf extends ModelAdapter {
    public ModelAdapterWolf() {
        super(EntityWolf.class, "wolf", 0.5F);
    }

    public ModelBase makeModel() {
        return new ModelWolf();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (model instanceof ModelWolf) {
            ModelWolf modelwolf = (ModelWolf) model;
            if (modelPart.equals("head")) return modelwolf.wolfHeadMain;
            if (modelPart.equals("body")) {
                return modelwolf.wolfBody;
            }

            if (modelPart.equals("leg1")) {
                return modelwolf.wolfLeg1;
            }

            if (modelPart.equals("leg2")) {
                return modelwolf.wolfLeg2;
            }

            if (modelPart.equals("leg3")) {
                return modelwolf.wolfLeg3;
            }

            if (modelPart.equals("leg4")) {
                return modelwolf.wolfLeg4;
            }

            if (modelPart.equals("tail")) {
                return modelwolf.wolfTail;
            }

            if (modelPart.equals("mane"))
                return modelwolf.wolfMane;

        }
        return null;
    }

    public String[] getModelRendererNames() {
        return new String[]{"head", "body", "leg1", "leg2", "leg3", "leg4", "tail", "mane"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        return new RenderWolf(rendermanager, modelBase, shadowSize);
    }
}
