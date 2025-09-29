package tech.konata.phosphate.rendering.music.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.music.PVRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

/**
 * @author IzumiiKonata
 * Date: 2025/1/22 12:29
 */
public class OchameKino extends PVRenderer {

    Location[] texturesLeft;
    Location[] texturesRight;
    
    @Override
    public boolean isApplicable(long id) {
        return id == 4887947 || id == 34586069 || id == 33471226 || id == 2078501348 || id == 1977093242;
    }

    @Override
    public void onInit() {
        texturesLeft = this.loadTextureFrom("OchameKino/Cat (", ").png", 13, 20);
        texturesRight = this.loadTextureFrom("OchameKino/Cat (", ").png", 2, 13);
    }

    int curIdx = 0;

    @Override
    public void onRender(float playBackTime, long musicID) {

        boolean switchSide = (int) this.beatCount(playBackTime) % 2 == 0;
        Location[] textures = switchSide ? texturesLeft : texturesRight;

        // 贴图切换的速度
        double texDuration = this.getMillisPerBeat() / textures.length;

        curIdx = (int) ((playBackTime / texDuration) % textures.length);

        GlStateManager.pushMatrix();

        this.doScale();
        Image.drawLinear(textures[curIdx], 0, RenderSystem.getHeight() - 64, 64, 64, Image.Type.Normal);

        GlStateManager.popMatrix();

        debug("Cur: " + curIdx);
        debug("Left: " + switchSide);

    }

    @Override
    public double getBPM() {
        return 150;
    }

    @Override
    public long waitTime(long id) {
        long defWaitTime = 250L;

        if (id == 33471226L) {
            defWaitTime = 2050L;
        }

        if (id == 2078501348) {
            defWaitTime = 800L;
        }

        if (id == 1977093242L) {
            defWaitTime = 300L;
        }

        return defWaitTime;
    }
}
