package net.optifine.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL31;

public enum TextureType {
    TEXTURE_1D(GL11.GL_TEXTURE_1D),
    TEXTURE_2D(GL11.GL_TEXTURE_2D),
    TEXTURE_3D(GL12.GL_TEXTURE_3D),
    TEXTURE_RECTANGLE(GL31.GL_TEXTURE_RECTANGLE);

    private final int id;

    TextureType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
