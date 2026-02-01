package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.RenderEnderCrystal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.src.Config;


public class ModelAdapterEnderCrystal extends ModelAdapter {
    public ModelAdapterEnderCrystal() {
        this("end_crystal");
    }

    protected ModelAdapterEnderCrystal(String name) {
        super(EntityEnderCrystal.class, name, 0.5F);
    }

    public ModelBase makeModel() {
        return new ModelEnderCrystal(0.0F, true);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (model instanceof ModelEnderCrystal) {
            ModelEnderCrystal modelendercrystal = (ModelEnderCrystal) model;
            if (modelPart.equals("cube"))
                return modelendercrystal.cube;
            if (modelPart.equals("glass"))
                return modelendercrystal.glass;
            if (modelPart.equals("base"))
                return modelendercrystal.base;
        }
        return null;
    }

    public String[] getModelRendererNames() {
        return new String[]{"cube", "glass", "base"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        Render<? extends Entity> render = rendermanager.getEntityRenderMap().get(EntityEnderCrystal.class);

        if (!(render instanceof RenderEnderCrystal)) {
            Config.warn("Not an instance of RenderEnderCrystal: " + render);
            return null;
        } else {
            RenderEnderCrystal renderendercrystal = (RenderEnderCrystal) render;

            renderendercrystal.modelEnderCrystal = modelBase;
            renderendercrystal.shadowSize = shadowSize;
            return renderendercrystal;
        }
    }
}
