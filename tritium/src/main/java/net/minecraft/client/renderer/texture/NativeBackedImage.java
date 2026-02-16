package net.minecraft.client.renderer.texture;

import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import tritium.utils.other.MemoryTracker;

import java.awt.image.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

public class NativeBackedImage extends BufferedImage implements AutoCloseable {

    private final int width;
    private final int height;
    private long pointer;

    private NativeBackedImage(int width, int height, long pointer) {
        super(1, 1, TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        this.pointer = pointer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return width;
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return height;
    }

    @Override
    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {

        if (rgbArray == null) {
            int requiredSize = offset + h * scansize;
            rgbArray = new int[requiredSize];
        }

        long basePointer = this.pointer + ((startX + (long) startY * width) * 4);
        
        for (int z = 0; z < h; z++) {
            long rowPointer = basePointer + ((long) z * width * 4);
            for (int x = 0; x < w; x++) {
                int color = MemoryUtil.memGetInt(rowPointer + (x * 4));
                // ABGR -> ARGB
                int a = (color >> 24) & 0xFF;
                int b = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int r = (color >> 0) & 0xFF;

                int finalColor = (a << 24) | (r << 16) | (g << 8) | b;

                rgbArray[offset + x + (z * scansize)] = finalColor;
            }
        }

        return rgbArray;
    }

    @Override
    public int getRGB(int x, int z) {
        checkBounds(x, z);

        return MemoryUtil.memGetInt(this.pointer + ((x + (long) z * width) * 4));
    }

    @Override
    public void setRGB(int x, int z, int rgb) {
        checkBounds(x, z);

        MemoryUtil.memPutInt(this.pointer + ((x + (long) z * width) * 4), rgb);
    }

    @Override
    public BufferedImage getSubimage(int x, int y, int w, int h) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() {
        if (this.pointer != 0) {
            STBImage.nstbi_image_free(this.pointer);

            this.pointer = 0;
        }
    }

    private void checkBounds(int x, int z) {
        if (x < 0 || x >= this.width || z < 0 || z >= this.height) {
            throw new IllegalStateException(
                    "Out of bounds: " + x + ", " + z + " (width: " + this.width + ", height: " + this.height + ")");
        }
    }

    // Parsing

    public static NativeBackedImage make(InputStream stream) {
        ByteBuffer imgBuf = null;

        try {
            imgBuf = readResource(stream);
            imgBuf.rewind();

            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                IntBuffer width = memoryStack.mallocInt(1);
                IntBuffer height = memoryStack.mallocInt(1);
                IntBuffer channels = memoryStack.mallocInt(1);

                // 4 channels: RGBA
                ByteBuffer buf = STBImage.stbi_load_from_memory(imgBuf, width, height, channels, 4);
                if (buf == null) {
                    throw new RuntimeException("Could not load image: " + STBImage.stbi_failure_reason());
                }

                return new NativeBackedImage(width.get(0), height.get(0), MemoryUtil.memAddress(buf));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // free
            MemoryTracker.memFree(imgBuf);
            IOUtils.closeQuietly(stream);
        }

        return null;
    }

    public static NativeBackedImage makeNoClosing(InputStream stream) {
        ByteBuffer imgBuf = null;

        try {
            imgBuf = readResource(stream);
            imgBuf.rewind();

            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                IntBuffer width = memoryStack.mallocInt(1);
                IntBuffer height = memoryStack.mallocInt(1);
                IntBuffer channels = memoryStack.mallocInt(1);

                // 4 channels: RGBA
                ByteBuffer buf = STBImage.stbi_load_from_memory(imgBuf, width, height, channels, 4);
                if (buf == null) {
                    throw new RuntimeException("Could not load image: " + STBImage.stbi_failure_reason());
                }

                return new NativeBackedImage(width.get(0), height.get(0), MemoryUtil.memAddress(buf));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // free
            MemoryTracker.memFree(imgBuf);
        }

        return null;
    }

    private static final int TRANSFER_SIZE = 16384;
    private static ByteBuffer readResource(InputStream inputStream) throws IOException {

        ByteBuffer byteBuffer;
        if (inputStream instanceof FileInputStream) {
            FileChannel fileChannel = ((FileInputStream) inputStream).getChannel();
            long fileSize = fileChannel.size();
            
            byteBuffer = MemoryTracker.memAlloc((int) fileSize);

            int totalRead = 0;
            int read;
            while (totalRead < fileSize && (read = fileChannel.read(byteBuffer)) != -1) {
                totalRead += read;
            }
        } else {
            int sizeGuess = 4096;
            try {
                sizeGuess = Math.max(4096, inputStream.available());
            } catch (IOException ignored) {
            }

            byteBuffer = MemoryTracker.memAlloc(sizeGuess);

            while (true) {
                int read = read(byteBuffer, inputStream);
                if (read == -1) {
                    break;
                }
                
                if (byteBuffer.remaining() == 0) {
                    int newCapacity = byteBuffer.capacity() + Math.max(4096, byteBuffer.capacity() / 2);
                    byteBuffer = MemoryTracker.memRealloc(byteBuffer, newCapacity);
                }
            }
        }

        return byteBuffer;
    }

    private static int read(ByteBuffer dst, InputStream in) throws IOException {
        int len = dst.remaining();
        int totalRead = 0;
        int bytesRead = 0;
        byte[] buf = new byte[TRANSFER_SIZE];
        while (totalRead < len) {
            int bytesToRead = Math.min((len - totalRead), TRANSFER_SIZE);

            if ((totalRead > 0) && !(in.available() > 0)) {
                break; // block at most once
            }

            bytesRead = in.read(buf, 0, bytesToRead);

            if (bytesRead < 0) {
                break;
            } else {
                totalRead += bytesRead;
            }
            dst.put(buf, 0, bytesRead);
        }

        if ((bytesRead < 0) && (totalRead == 0)) {
            return -1;
        }

        return totalRead;
    }

}