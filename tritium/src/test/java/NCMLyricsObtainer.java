import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import tritium.ncm.OptionsUtil;
import tritium.ncm.RequestUtil;
import tritium.ncm.api.CloudMusicApi;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.screens.ncm.LyricLine;
import tritium.screens.ncm.LyricParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * @author IzumiiKonata
 * Date: 2026/2/10 20:41
 */
@UtilityClass
public class NCMLyricsObtainer {

    final Scanner sn = new Scanner(System.in).useDelimiter("\n");

    public void main(String[] args) {
        RequestUtil.RequestAnswer registerAnonimous = CloudMusicApi.registerAnonimous();

        System.out.println(registerAnonimous);
        String[] cookies = registerAnonimous.getCookies();

        if (cookies == null)
            return;

        OptionsUtil.setCookie(String.join(" ",  cookies));

        while (true) {

            String search = readString("Search>> ");

            List<Music> searchResult = CloudMusic.search(search);

            System.out.println("Results: ");

            for (int i = 0; i < Math.min(9, searchResult.size()); i++) {
                Music music = searchResult.get(i);

                System.out.println("\t[" + i + "] " + music.getName() + " - " + music.getArtistsName() + " (" + music.getId() + ")");
            }

            int idx = parse("Index>> ", int.class);

            while (idx < 0 || idx >= searchResult.size()) {
                System.err.println("Invalid index");
                idx = parse("Index>> ", int.class);
            }

            Music music = searchResult.get(idx);

            String translatedNames = music.getTranslatedNames();
            File dest = new File("C:\\Users\\20307\\Desktop\\amll\\", music.getName() + (translatedNames == null ? "" : (" (" + translatedNames + ")")));

            if (dest.exists() && dest.isDirectory()) {
                System.err.println("Folder \"" + dest.getName() + "\" already exists");
                continue;
            }

            dest.mkdir();
            System.out.println("Requesting lyrics...");
            JsonObject result = CloudMusicApi.lyricNew(music.getId()).toJsonObject();

            String lyric = result.get("lrc").getAsJsonObject().get("lyric").getAsString();
            while (!(lyric.startsWith("[") && isNumber(lyric.substring(1, 2)))) {
                lyric = lyric.substring(1);
            }

            lyric = lyric.replace("\\n", "\n");

            StringBuilder finalLyrics = new StringBuilder();

            for (String s : lyric.split("\n")) {
                // if the lyrics is empty then continue
                if (s.substring(s.lastIndexOf("]") + 1).isEmpty()) {
                    continue;
                }

                finalLyrics.append(s).append("\n");
            }

            File originalLrc = new File(dest, "original.lrc");
            try {
                Files.write(originalLrc.toPath(), finalLyrics.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                System.err.println("Failed to save original lrc!");
                e.printStackTrace();
            }

            String tlyric = result.get("tlyric").getAsJsonObject().get("lyric").getAsString();
            tlyric = tlyric.replace("\\n", "\n");

            StringBuilder sb = new StringBuilder();

            for (String s : tlyric.split("\n")) {
                s = s.substring(s.lastIndexOf("]") + 1);

                if (s.isEmpty())
                    continue;

                sb.append(s).append("\n");
            }

            File tlyricFile = new File(dest, "translations.txt");
            try {
                Files.write(tlyricFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                System.err.println("Failed to save translations!");
                e.printStackTrace();
            }
        }

    }

    private boolean isNumber(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    private String readString(String hint) {
        System.out.print(hint);
        return sn.next();
    }

    private String readString() {
        return readString("");
    }

    private <T> Function<String, T> getParser(Class<T> type) {

        if (type == String.class) {
            return s -> (T) s;
        }

        if (type == Integer.class || type == int.class) {
            return s -> (T) (Integer) Integer.parseInt(s);
        }

        if (type == Long.class || type == long.class) {
            return s -> (T) (Long) Long.parseLong(s);
        }

        if (type == Boolean.class || type == boolean.class) {
            return s -> (T) (Boolean) Boolean.parseBoolean(s);
        }

        if (type == Double.class || type == double.class) {
            return s -> (T) (Double) Double.parseDouble(s);
        }

        if (type == Float.class || type == float.class) {
            return s -> (T) (Float) Float.parseFloat(s);
        }

        if (type == Character.class || type == char.class) {
            return s -> (T) (Character) s.charAt(0);
        }

        if (type == Byte.class || type == byte.class) {
            return s -> (T) (Byte) Byte.parseByte(s);
        }

        if (type == Short.class || type == short.class) {
            return s -> (T) (Short) Short.parseShort(s);
        }

        return null;
    }

    private <T> T parse(String hint, Class<T> type) {
        String input = readString(hint);

        Function<String, T> parser = getParser(type);

        if (parser == null) {
            throw new IllegalArgumentException("Cannot parse type: " + type);
        }

        try {
            return parser.apply(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
