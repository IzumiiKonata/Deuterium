package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.boss.EntityDragon;


public class ModelAdapterDragon extends ModelAdapter {
    public ModelAdapterDragon() {
        super(EntityDragon.class, "dragon", 0.5F);
    }

    public ModelBase makeModel() {
        return new ModelDragon(0.0F);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (!(model instanceof ModelDragon)) {
            return null;
        } else {
            ModelDragon modeldragon = (ModelDragon) model;
            if (modelPart.equals("head"))
                return modeldragon.head;
            if (modelPart.equals("spine"))
                return modeldragon.spine;
            if (modelPart.equals("jaw"))
                return modeldragon.jaw;
            if (modelPart.equals("body"))
                return modeldragon.body;
            if (modelPart.equals("rear_leg"))
                return modeldragon.rearLeg;
            if (modelPart.equals("front_leg"))
                return modeldragon.frontLeg;
            if (modelPart.equals("rear_leg_tip"))
                return modeldragon.rearLegTip;
            if (modelPart.equals("front_leg_tip"))
                return modeldragon.frontLegTip;
            if (modelPart.equals("rear_foot"))
                return modeldragon.rearFoot;
            if (modelPart.equals("front_foot"))
                return modeldragon.frontFoot;
            if (modelPart.equals("wing"))
                return modeldragon.wing;
            if (modelPart.equals("wing_tip"))
                return modeldragon.wingTip;
            return null;
        }
    }

    public String[] getModelRendererNames() {
        return new String[]{"head", "spine", "jaw", "body", "rear_leg", "front_leg", "rear_leg_tip", "front_leg_tip", "rear_foot", "front_foot", "wing", "wing_tip"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        RenderDragon renderdragon = new RenderDragon(rendermanager);
        renderdragon.mainModel = modelBase;
        renderdragon.shadowSize = shadowSize;
        return renderdragon;
    }
}
