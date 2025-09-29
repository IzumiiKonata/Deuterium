package tech.konata.phosphate.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class TextureReader {

    public static BufferedImage readTextureToImage(int textureId, int width, int height) {
        // 创建一个ByteBuffer来存储读取的数据
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        // 绑定纹理
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // 读取纹理数据到ByteBuffer
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // 创建BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        buffer.rewind();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = buffer.get() & 0xFF;
                int g = buffer.get() & 0xFF;
                int b = buffer.get() & 0xFF;
                int a = buffer.get() & 0xFF;
                pixels[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);

        return image;
    }

    public static void saveImage(BufferedImage image, String filePath) throws IOException {
        // 保存图像到文件
        File file = new File(filePath);
        ImageIO.write(image, "png", file);
    }

    public static void main(String[] args) {
        // 假设textureId是你的纹理ID，width和height是纹理的尺寸
        int textureId = 1; // 示例纹理ID
        int width = 512;   // 示例宽度
        int height = 512;  // 示例高度

        try {
            BufferedImage image = readTextureToImage(textureId, width, height);
            saveImage(image, "output.png");
            System.out.println("图像保存成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}