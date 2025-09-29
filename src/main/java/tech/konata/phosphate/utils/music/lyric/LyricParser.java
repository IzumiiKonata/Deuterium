package tech.konata.phosphate.utils.music.lyric;

import com.google.gson.JsonObject;
import tech.konata.phosphate.widget.impl.MusicLyrics;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网易云音乐歌词解析器
 * NetEase Cloud Music Lyric Parser
 */
public class LyricParser {

    public static List<LyricLine> parse(JsonObject input) {
        // 处理未收集歌词的情况
        if (input.has("uncollected") || !input.has("lrc")) {
            return new ArrayList<>();
        }

        // 解析主歌词
        List<LyricLine> lyricLines = parseSingleLine(input.getAsJsonObject("lrc").get("lyric").getAsString());

        // 处理翻译歌词
        processTranslationLyrics(input, lyricLines);

        // 处理罗马音歌词
        processRomanizationLyrics(input, lyricLines);

        // 处理逐字歌词(YRC)
        if (input.has("yrc")) {
            String yrc = input.getAsJsonObject("yrc").get("lyric").getAsString();
            parseYrc(yrc);
        }

        return lyricLines;
    }

    // 处理翻译歌词
    private static void processTranslationLyrics(JsonObject input, List<LyricLine> lyricLines) {
        if (!input.has("tlyric")) return;

        String tLyric = input.getAsJsonObject("tlyric").get("lyric").getAsString();
        if (tLyric.trim().isEmpty()) return;

        MusicLyrics.hasTransLyrics = true;
        List<LyricLine> translates = parseSingleLine(tLyric);

        // 创建翻译映射
        Map<Long, String> transMap = new HashMap<>();
        for (LyricLine t : translates) {
            transMap.put(t.timeStamp, t.lyric);
        }

        // 匹配主歌词和翻译
        for (LyricLine l : lyricLines) {
            String translation = transMap.get(l.timeStamp);
            if (translation != null && l.translationText == null) {
                l.translationText = translation;
            }
        }
    }

    // 处理罗马音歌词
    private static void processRomanizationLyrics(JsonObject input, List<LyricLine> lyricLines) {
        if (!input.has("romalrc")) return;

        String romanization = input.getAsJsonObject("romalrc").get("lyric").getAsString();
        if (romanization.isEmpty()) return;

        MusicLyrics.hasRomanization = true;
        List<LyricLine> romanizations = parseSingleLine(romanization);

        // 创建罗马音映射
        Map<Long, String> romaMap = new HashMap<>();
        for (LyricLine r : romanizations) {
            romaMap.put(r.timeStamp, r.lyric);
        }

        // 匹配主歌词和罗马音
        for (LyricLine l : lyricLines) {
            String roma = romaMap.get(l.timeStamp);
            if (roma != null && l.romanizationText == null) {
                l.romanizationText = roma;
            }
        }
    }

    // 解析单行歌词文本
    private static List<LyricLine> parseSingleLine(String input) {
        List<LyricLine> lyricLines = new ArrayList<>();
        String[] lines = input.split("\\n");
        if (lines.length == 1) lines = input.split("\\\\n");

        for (String line : lines) {
            List<LyricLine> parsedLines = parseLine(line);
            if (parsedLines != null) {
                lyricLines.addAll(parsedLines);
            }
        }

        // 按时间戳排序
        lyricLines.sort(Comparator.comparingLong(LyricLine::getTimeStamp));
        return lyricLines;
    }

    // 解析单行歌词
    private static List<LyricLine> parseLine(String input) {
        if (input.isEmpty()) return null;

        input = input.trim();
        // 匹配时间标签和歌词内容
        Matcher lineMatcher = Pattern.compile("((?:\\[\\d{2}:\\d{2}\\.\\d{2,3}])+)(.*)").matcher(input);
        if (!lineMatcher.matches()) return null;

        String times = lineMatcher.group(1);
        String text = lineMatcher.group(2).trim();
        if (text.isEmpty()) return null;

        List<LyricLine> entryList = new ArrayList<>();
        Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]").matcher(times);

        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));  // 分钟
            long sec = Long.parseLong(timeMatcher.group(2));  // 秒钟
            long mil = Long.parseLong(timeMatcher.group(3));  // 毫秒

            // 处理不同精度的毫秒值
            int scaleFactor = (mil > 100) ? 1 : 10;
            long time = min * 60000 + sec * 1000 + mil * scaleFactor;

            entryList.add(new LyricLine(time, times, text));
        }
        return entryList;
    }

    // 解析逐字歌词(YRC格式)
    public static void parseYrc(String yrc) {
        String[] lines = yrc.split("\n");
        if (lines.length == 1) lines = yrc.split("\\\\n");

        for (String line : lines) {
            if (!line.startsWith("[")) continue;

            // 解析时间信息
            String timeData = line.substring(1, line.indexOf("]"));
            String[] timeParts = timeData.split(",");
            long startDuration = Long.parseLong(timeParts[0]);
            long duration = Long.parseLong(timeParts[1]);

            // 创建时间轴对象
            MusicLyrics.ScrollTiming timing = new MusicLyrics.ScrollTiming();
            timing.start = startDuration;
            timing.duration = duration;
            timing.text = line.substring(line.indexOf("]") + 1);

            if (timing.text.isEmpty())
                continue;

            // 解析逐字时间信息
            parseWordTimings(timing);
            MusicLyrics.timings.add(timing);
        }
    }

    // 解析逐字时间信息
    private static void parseWordTimings(MusicLyrics.ScrollTiming timing) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+),0\\)((?!\\(\\d+,\\d+,0\\)|\\(\\d+,\\d+,0\\\\).)*");
        Matcher matcher = pattern.matcher(timing.text);
        long sumDuration = 0;

        while (matcher.find()) {
            String group = matcher.group();
            String metadata = group.substring(0, group.indexOf(")") + 1);
            String[] metadataParts = metadata.split(",");
            String lyric = group.substring(group.indexOf(")") + 1);

            // 创建单词时间对象
            MusicLyrics.WordTiming wordTiming = new MusicLyrics.WordTiming();
            wordTiming.word = lyric;
            sumDuration += Long.parseLong(metadataParts[1]);  // 累加持续时间
            wordTiming.timing = sumDuration;
            timing.timings.add(wordTiming);
        }
    }
}
