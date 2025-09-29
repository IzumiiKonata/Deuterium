package tech.konata.phosphate.module.impl.render;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.Tuple;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.settings.NumberSetting;

public class SmallPlayerModel extends Module {

    public SmallPlayerModel() {
        super("SmallPlayerModel", Category.RENDER);
    }

    public final NumberSetting<Float> bodyScale = new NumberSetting<>("Body Scale", 0.75f, 0.25f, 1.0f, 0.05f);
    public final NumberSetting<Float> headScale = new NumberSetting<>("Head Scale", 1.5f, 0.5f, 2.0f, 0.1f);

    public Tuple<Float, Float> scalePlayerModel(float f, float shadowSize, ModelRenderer bipedHead, ModelRenderer bipedHeadwear) {
        if (this.isEnabled()) {

            bipedHead.scaleX = bipedHead.scaleY = bipedHead.scaleZ = bipedHeadwear.scaleX = bipedHeadwear.scaleY = bipedHeadwear.scaleZ = this.headScale.getValue();

            f = f * this.bodyScale.getValue();
            shadowSize = 0.5f * this.bodyScale.getValue();

            return Tuple.of(f, shadowSize);

        } else {

            bipedHead.scaleX = bipedHead.scaleY = bipedHead.scaleZ = bipedHeadwear.scaleX = bipedHeadwear.scaleY = bipedHeadwear.scaleZ = 1f;

            shadowSize = 0.5F;

            return Tuple.of(f, shadowSize);
        }
    }

}
