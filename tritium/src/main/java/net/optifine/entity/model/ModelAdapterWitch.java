package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWitch;
import net.minecraft.entity.monster.EntityWitch;


public class ModelAdapterWitch extends ModelAdapter {
    public ModelAdapterWitch() {
        super(EntityWitch.class, "witch", 0.5F);
    }

    public ModelBase makeModel() {
        return new ModelWitch(0.0F);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (!(model instanceof ModelWitch modelwitch)) {
            return null;
        } else {
            switch (modelPart) {
                case "mole" -> {
                    return modelwitch.witchMole;
                }
                case "hat" -> {
                    return modelwitch.witchHat;
                }
                case "head" -> {
                    return modelwitch.villagerHead;
                }
                default -> {
                    if (modelPart.equals("body")) return modelwitch.villagerBody;
                    if (modelPart.equals("arms")) {
                        return modelwitch.villagerArms;
                    } else {
                        if (modelPart.equals("left_leg")) return modelwitch.leftVillagerLeg;
                        if (modelPart.equals("right_leg")) return modelwitch.rightVillagerLeg;
                        return modelPart.equals("nose") ? modelwitch.villagerNose : null;
                    }
                }
            }
        }
    }

    public String[] getModelRendererNames() {
        return new String[]{"mole", "head", "body", "arms", "right_leg", "left_leg", "nose"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        RenderWitch renderwitch = new RenderWitch(rendermanager);
        renderwitch.mainModel = modelBase;
        renderwitch.shadowSize = shadowSize;
        return renderwitch;
    }
}
