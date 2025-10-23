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
        if (input.has("uncollected") || !input.has("lrc")) {
            return new ArrayList<>();
        }

        List<LyricLine> lyricLines = new ArrayList<>();

        if (input.has("yrc")) {
            String yrc = input.getAsJsonObject("yrc").get("lyric").getAsString();
            parseYrc(yrc, lyricLines);
        }

        if (lyricLines.isEmpty()) {
            lyricLines.addAll(parseSingleLine(input.getAsJsonObject("lrc").get("lyric").getAsString()));
        }

        processTranslationLyrics(input, lyricLines);

        processRomanizationLyrics(input, lyricLines);

        return lyricLines;
    }

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

        lyricLines.sort(Comparator.comparingLong(LyricLine::getTimeStamp));
        return lyricLines;
    }

    private static List<LyricLine> parseLine(String input) {
        if (input.isEmpty()) {
            return null;
        }
        boolean alt = false;
        input = input.trim();
//        System.out.println("input = " + input);
        Matcher lineMatcher = Pattern.
                compile("((?:\\[\\d{2}:\\d{2}\\.\\d{2,3}])+)(.*)").matcher(input);
        if (!lineMatcher.matches()) {
            lineMatcher = Pattern.
                    compile("((?:\\[\\d{2}:\\d{2}:\\d{2,3}])+)(.*)").matcher(input);
            if (!lineMatcher.matches()) {
                return null;
            }
            alt = true;
        }
        String times = lineMatcher.group(1);
//        System.out.println("times = " + times);
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
            int scale_mil = mil > 100 ? 1 : 10;
            long time =
                    min * 60000 +
                            sec * 1000 +
                            mil * scale_mil;
            entryList.add(new LyricLine(time, text));
        }
        return entryList;
    }

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

            StringBuilder sb = new StringBuilder();
            for (LyricLine.Word word : l.words) {
                sb.append(word.word);
            }

            l.lyric = sb.toString();

//            MusicLyricsWidget.ScrollTiming timing = new MusicLyricsWidget.ScrollTiming();
//            timing.start = startDuration;
//            timing.duration = duration;
//            timing.text = line.substring(line.indexOf("]") + 1);

//            if (timing.text.isEmpty())
//                continue;

//            parseWordTimings(timing);
//            MusicLyricsWidget.timings.add(timing);
            lyricLines.add(l);
        }
    }

    private static void parseWordTimings(LyricLine l, String text) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+),0\\)((?!\\(\\d+,\\d+,0\\)|\\(\\d+,\\d+,0\\\\).)*");
        Matcher matcher = pattern.matcher(text);
        long sumDuration = 0;

        while (matcher.find()) {
            String group = matcher.group();
            String metadata = group.substring(0, group.indexOf(")") + 1);
            String[] metadataParts = metadata.split(",");
            String lyric = group.substring(group.indexOf(")") + 1);

            LyricLine.Word wordTiming = new LyricLine.Word();
            wordTiming.word = lyric;
            sumDuration += Long.parseLong(metadataParts[1]);  // 累加持续时间
            wordTiming.timing = sumDuration;
            l.words.add(wordTiming);
        }
    }
}
