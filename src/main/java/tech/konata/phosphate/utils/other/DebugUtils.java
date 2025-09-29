package tech.konata.phosphate.utils.other;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author IzumiiKonata
 * Date: 2025/1/22 09:03
 */
@UtilityClass
public class DebugUtils {

    PublicKey pubKey;

    static {
        try {
            byte[] keyBytes = Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2MBN8jPVkx0Xwg4DXJMXrcxzx+U1bLVVYfmo48aGK+JcNYeD5oXzTrNKUQAkVM+sSiM96qOdc8G1NJxSQCCwSXjM32qe9wgsj7GWR77aQffM/hGu1/vZIQWaGzNheKhTddDoDh1cfigSACUPlcJDnc4zWW+E0xlMrHPZP5cAWjyOlur2cqb6AionyB9AFgQEA6JTOByDJeS+1fQZf3uMArnVDvZgQtWm699bihjCAqWMGD8rt33m5JekdommYb+61Z1bFiDehXuNkYDXhMaIVVIroPt1IKzBzvwccgFHa4biAvJy9nDM9OnJVqaDgsTx/tTjMgJLxMBdpE9RjAEolQIDAQAB");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory;

            keyFactory = KeyFactory.getInstance("RSA");
            pubKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {

        }
    }

    @SneakyThrows
    public void debugThis(Object o) {

        if (pubKey == null)
            return;

        String string = o.toString();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] texts = string.getBytes(StandardCharsets.UTF_8);

        int MAX_ENCRYPT_BLOCK = 2048 / 8 - 11;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;

        while (texts.length - offset > 0) {
            int blockSize = Math.min(MAX_ENCRYPT_BLOCK, texts.length - offset);
            byte[] block = new byte[blockSize];
            System.arraycopy(texts, offset, block, 0, blockSize);
            byte[] encryptedBlock = cipher.doFinal(block);
            out.write(encryptedBlock);
            offset += blockSize;
        }

        byte[] result = out.toByteArray();
        out.close();
        System.out.println(Base64.getEncoder().encodeToString(result));
    }

}
