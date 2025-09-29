package tech.konata.phosphate.rendering;

import net.minecraft.client.renderer.GlStateManager;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public enum GLAction {

    Matrix(GlStateManager::pushMatrix, GlStateManager::popMatrix),
    Attrib(GlStateManager::pushAttrib, GlStateManager::popAttrib);

    public final Runnable before, after;

    GLAction(Runnable before, Runnable after) {
        this.before = before;
        this.after = after;
    }

}
