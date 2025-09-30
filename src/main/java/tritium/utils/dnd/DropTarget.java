package tritium.utils.dnd;

import lombok.Getter;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author IzumiiKonata
 * Date: 2025/2/4 09:09
 */
public class DropTarget {

    @Getter
    private static final DropTarget instance = new DropTarget();

    public DropTarget() {
        loadNative();
    }

    public native void registerDropTarget(long hwnd);

    public void onDragEnter(int x, int y, int effect, String[] formats) {
        DropTargetHandler.getInstance().onDragEnterImpl(x, y, effect, formats);
    }

    public void onDragLeave() {
        DropTargetHandler.getInstance().onDragLeaveImpl();
    }

    public void onDragOver(int x, int y, int effect) {
        DropTargetHandler.getInstance().onDragOverImpl(x, y, effect);
    }

    public void onDrop(int x, int y, int effect, String[] formats) {
        DropTargetHandler.getInstance().onDropImpl(x, y, effect, formats);
    }

    public static void loadNative() {

        if (Util.getOSType() != Util.EnumOS.WINDOWS) {
            return;
        }

        File libFile;
        String libFileName = "/assets/minecraft/tritium/DropTarget.dll";
        try {
            libFile = File.createTempFile("lib", null);
            libFile.deleteOnExit();
            if (!libFile.exists()) {
                throw new IOException();
            }
        } catch (IOException iOException) {
            throw new UnsatisfiedLinkError("Failed to create temp file");
        }
        byte[] arrayOfByte = new byte[2048];
        try {
            InputStream inputStream = DropTarget.class.getResourceAsStream(libFileName);
            if (inputStream == null) {
                throw new UnsatisfiedLinkError(String.format("Failed to open lib file: %s", libFileName));
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(libFile)) {
                int size;
                while ((size = inputStream.read(arrayOfByte)) != -1) {
                    fileOutputStream.write(arrayOfByte, 0, size);
                }
            } catch (Throwable throwable) {
                try {
                    inputStream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException exception) {
            throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", exception.getMessage()));
        }
        try {
            System.load(libFile.getAbsolutePath());
        } catch (UnsatisfiedLinkError wtf) {
            wtf.printStackTrace();
        }
    }

}
