package tritium.screens.ncm;

import com.google.gson.JsonObject;

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

        // 处理逐字歌词(YRC)
        if (input.has("yrc")) {
            String yrc = input.getAsJsonObject("yrc").get("lyric").getAsString();
            parseYrc(yrc, lyricLines);
        }

        // 处理翻译歌词
        processTranslationLyrics(input, lyricLines);

        // 处理罗马音歌词
        processRomanizationLyrics(input, lyricLines);

        return lyricLines;
    }

    // 处理翻译歌词
    private static void processTranslationLyrics(JsonObject input, List<LyricLine> lyricLines) {
        if (!input.has("tlyric")) return;

        String tLyric = input.getAsJsonObject("tlyric").get("lyric").getAsString();
        if (tLyric.trim().isEmpty()) return;

        List<LyricLine> translates = parseSingleLine(tLyric);

        for (int i = 0; i < lyricLines.size() && i < translates.size(); i++) {
            LyricLine t = lyricLines.get(i);
            t.translationText = translates.get(i).lyric;
        }
    }

    // 处理罗马音歌词
    private static void processRomanizationLyrics(JsonObject input, List<LyricLine> lyricLines) {
        if (!input.has("romalrc")) return;

        String romanization = input.getAsJsonObject("romalrc").get("lyric").getAsString();
        if (romanization.isEmpty()) return;

        List<LyricLine> romanizations = parseSingleLine(romanization);

        for (int i = 0; i < lyricLines.size() && i < romanizations.size(); i++) {
            LyricLine t = lyricLines.get(i);
            t.romanizationText = romanizations.get(i).lyric;
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
        if (input.isEmpty()) {
            return null;
        }
        boolean alt = false;
        // 去除空格
        input = input.trim();
//        System.out.println("input = " + input);
        // 正则表达式，匹配可能有多个时间标签的行，允许歌词文本为空
        Matcher lineMatcher = Pattern.
                compile("((?:\\[\\d{2}:\\d{2}\\.\\d{2,3}])+)(.*)").matcher(input);
        // 如果没匹配到则返回 null
        if (!lineMatcher.matches()) {
            lineMatcher = Pattern.
                    compile("((?:\\[\\d{2}:\\d{2}:\\d{2,3}])+)(.*)").matcher(input);
            if (!lineMatcher.matches()) {
                return null;
            }
            alt = true;
        }
        // 得到时间标签
        String times = lineMatcher.group(1);
//        System.out.println("times = " + times);
        // 得到歌词文本内容，可能为空
        String text = lineMatcher.group(2).trim();
        if (text.isEmpty()) {
            return null;
        }/* else {
            System.out.println("text = " + text);
        }*/
        List<LyricLine> entryList = new ArrayList<>();
        Matcher timeMatcher = Pattern.compile(alt ? "\\[(\\d\\d):(\\d\\d):(\\d{2,3})]" : "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]").matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));// 分
            long sec = Long.parseLong(timeMatcher.group(2));// 秒
            long mil = Long.parseLong(timeMatcher.group(3));// 毫秒
            // 转换为long型时间
            int scale_mil = mil > 100 ? 1 : 10;//如果毫秒是3位数则乘以1，反正则乘以10
            // 转换为long型时间
            long time =
                    min * 60000 +
                            sec * 1000 +
                            mil * scale_mil;
            // 最终解析得到一个list
            entryList.add(new LyricLine(time, text));
        }
        return entryList;
    }

    // 解析逐字歌词
    public static void parseYrc(String yrc, List<LyricLine> lyricLines) {
        String[] lines = yrc.split("\n");
        if (lines.length == 1) lines = yrc.split("\\\\n");

        lyricLines.clear();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.startsWith("[")) continue;

            // 解析时间信息
            String timeData = line.substring(1, line.indexOf("]"));
            String[] timeParts = timeData.split(",");
            long startDuration = Long.parseLong(timeParts[0]);
            long duration = Long.parseLong(timeParts[1]);

            LyricLine l = new LyricLine(startDuration, "");

            parseWordTimings(l, line.substring(line.indexOf("]") + 1));

            // 创建时间轴对象
//            MusicLyricsWidget.ScrollTiming timing = new MusicLyricsWidget.ScrollTiming();
//            timing.start = startDuration;
//            timing.duration = duration;
//            timing.text = line.substring(line.indexOf("]") + 1);

//            if (timing.text.isEmpty())
//                continue;

            // 解析逐字时间信息
//            parseWordTimings(timing);
//            MusicLyricsWidget.timings.add(timing);
            lyricLines.add(l);
        }
    }

    // 解析逐字时间信息
    private static void parseWordTimings(LyricLine l, String text) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+),0\\)((?!\\(\\d+,\\d+,0\\)|\\(\\d+,\\d+,0\\\\).)*");
        Matcher matcher = pattern.matcher(text);
        long sumDuration = 0;

        while (matcher.find()) {
            String group = matcher.group();
            String metadata = group.substring(0, group.indexOf(")") + 1);
            String[] metadataParts = metadata.split(",");
            String lyric = group.substring(group.indexOf(")") + 1);

            // 创建单词时间对象
            LyricLine.Word wordTiming = new LyricLine.Word();
            wordTiming.word = lyric;
            sumDuration += Long.parseLong(metadataParts[1]);  // 累加持续时间
            wordTiming.timing = sumDuration;
            l.words.add(wordTiming);
        }
    }
}
