package tritium.utils.res;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author IzumiiKonata
 * @since 2024/11/16 21:22
 */
@UtilityClass
public class InputStreams {

    @SneakyThrows
    public byte[] readInputStream(InputStream inputStream) {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    @SneakyThrows
    public void writeTo(InputStream src, OutputStream dest) {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = src.read(buffer)) != -1) {
            dest.write(buffer, 0, len);
        }
        dest.flush();
    }

}
