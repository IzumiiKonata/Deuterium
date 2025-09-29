package tech.konata.phosphate.utils.music.dto;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 滚动歌词
 */
public class LyricDTO {
    private final Map<Long, String> lyric;
    private final Map<Long, String> tlyric;
    private final boolean loopIn = true;
    private final boolean load = true;
    private final String[] toLyric = {};

    public LyricDTO(JsonObject data) {
        String lyric = data.getAsJsonObject("lrc").get("lyric").getAsString();
        if (lyric.isEmpty()) {
            this.lyric = new LinkedHashMap<>();
            this.tlyric = this.lyric;
            return;
        }

        this.lyric = lyricToMap(lyric);
        if (!data.has("tlyric")) {
            this.tlyric = new LinkedHashMap<>();
            return;
        }

        String tlyric = data.getAsJsonObject("tlyric").get("lyric").getAsString();
        if (tlyric.isEmpty()) {
            this.tlyric = new LinkedHashMap<>();
            return;
        }

        this.tlyric = lyricToMap(data.getAsJsonObject("tlyric").get("lyric").getAsString());
    }

    /**
     * 将歌词时间字符串转换为毫秒
     *
     * @param n 歌词时间字符串
     * @return 歌词时间毫秒
     */
    public static long timeStrToTime(String n) {
        try {
            String[] timeStr = n.split(":");
            String[] secondStr = timeStr[1].split("\\.");

            int minute = Integer.parseInt(timeStr[0]) * 60 * 1000;
            int second = Integer.parseInt(secondStr[0]) * 1000;
            int millisecond = Integer.parseInt(secondStr[1]) * 10;
            return minute + second + millisecond;
        } catch (Exception err) {
            return 0;
        }
    }

    /**
     * 处理歌词字符串
     *
     * @param lyric 歌词字符串
     * @return 歌词 Map
     */
    public static Map<Long, String> lyricToMap(String lyric) {
        Map<Long, String> lyricMap = new LinkedHashMap<>();
        for (String lyricRow : lyric.split("\n")) {
            try {
                String[] lyricRows = lyricRow.substring(1).split("]", 2);
                if (lyricRows.length < 2) {
                    continue;
                }

                lyricMap.put(timeStrToTime(lyricRows[0]), lyricRows[1]);
            } catch (Exception err) {
                continue;
            }
        }
        return lyricMap;
    }

    /**
     * 获取当前滚动到的歌词
     *
     * @return 歌词
     */
    public String[] getToLyric() {
        return toLyric;
    }

}
