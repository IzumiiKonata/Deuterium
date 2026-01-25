package tritium.screens.mainmenu.skybox;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import tritium.rendering.rendersystem.RenderSystem;

public class RenderSkybox {
    private final Minecraft mc;
    private final RenderSkyboxCube renderer;

    private float timer = 0.0F;

    public RenderSkybox(RenderSkyboxCube rendererIn) {
        this.renderer = rendererIn;
        this.mc = Minecraft.getMinecraft();
    }

    public void render(float alpha) {
        timer += (float) (RenderSystem.getFrameDeltaTime());
        this.renderer.render(this.mc, MathHelper.sin((timer * 0.00625f) % 360.0f) * 2.5f + 10.0F, timer * 0.03F, alpha);
    }
}
