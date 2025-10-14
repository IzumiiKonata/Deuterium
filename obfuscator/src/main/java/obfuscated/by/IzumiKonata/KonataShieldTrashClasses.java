package obfuscated.by.IzumiKonata;

import lombok.SneakyThrows;

import java.io.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * @author IzumiiKonata
 * Date: 2025/8/20 21:54
 */
public class KonataShieldTrashClasses {

    public static void init() {
    }

    @SneakyThrows
    public static byte[] a(byte[] a) {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        InflaterOutputStream var2 = new InflaterOutputStream(var1, new Inflater());
        var2.write(a);
        var2.flush();
        var2.close();
        return var1.toByteArray();
    }

    public static native void $load(String var1);

    static {
        String string = System.getProperty("os.name").toLowerCase();
        String string2 = System.getProperty("os.arch").toLowerCase();
        StringBuilder stringBuilder = new StringBuilder("KNTASHIELD");
        if (string2.contains("x86_64") || string2.contains("amd64")) {
            stringBuilder.append("64");
        } else if (string2.contains("aarch64")) {
            stringBuilder.append("ARM64");
        } else if (string2.contains("x86")) {
            stringBuilder.append("32");
        }
        if (string.contains("nix") || string.contains("nux") || string.contains("aix")) {
            stringBuilder.insert(0, "lib");
            stringBuilder.append(".so");
        } else if (string.contains("win")) {
            stringBuilder.append(".dll");
        } else if (string.contains("mac")) {
            stringBuilder.insert(0, "lib");
            stringBuilder.append(".dylib");
        } else {
            stringBuilder.insert(0, "lib");
            stringBuilder.append(".so");
        }
        int n = KonataShieldTrashClasses.class.getName().lastIndexOf(".");
        String string3 = KonataShieldTrashClasses.class.getName().substring(0, n).replace(".", "/");
        String string4 = String.format("/%s/%s", string3, stringBuilder);
        try {
            File file = File.createTempFile("lib", null);
            file.deleteOnExit();
            if (!file.exists()) {
                throw new IOException();
            }
            InputStream inputStream = KonataShieldTrashClasses.class.getResourceAsStream(string4);
            if (inputStream == null) {
                throw new UnsatisfiedLinkError(String.format("Failed to open lib file: %s", string4));
            }
            byte[] byteArray = toByteArray(inputStream);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(a(byteArray));
            fileOutputStream.close();

            try {
                System.load(file.getAbsolutePath());
            } catch (Throwable t) {
            }
        } catch (IOException iOException) {
            throw new UnsatisfiedLinkError("Failed to extract file: " + iOException.getMessage());
        }
    }

    public static byte[] toByteArray(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (in.available() > 0) {
                int data = in.read(buffer);
                out.write(buffer, 0, data);
            }

            in.close();
            out.close();
            return out.toByteArray();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
}
