package net.minecraft.client.renderer;

import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.src.Config;

public class VertexBufferUploader extends WorldVertexBufferUploader {
    private VertexBuffer vertexBuffer = null;

    public void draw(WorldRenderer buffer) {
        if (buffer.getDrawMode() == 7 && Config.isQuadsToTriangles()) {
            buffer.quadsToTriangles();
            this.vertexBuffer.setDrawMode(buffer.getDrawMode());
        }

        this.vertexBuffer.bufferData(buffer.getByteBuffer());
        buffer.reset();
    }

    public void setVertexBuffer(VertexBuffer vertexBufferIn) {
        this.vertexBuffer = vertexBufferIn;
    }
}
