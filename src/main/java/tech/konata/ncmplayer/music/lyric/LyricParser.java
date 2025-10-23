package tech.konata.ncmplayer.music.lyric;

import com.google.gson.JsonObject;
import tritium.widget.impl.MusicLyricsWidget;

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

        List<LyricLine> lyricLines = parseSingleLine(input.getAsJsonObject("lrc").get("lyric").getAsString());

        processTranslationLyrics(input, lyricLines);

        processRomanizationLyrics(input, lyricLines);

        if (input.has("yrc")) {
            String yrc = input.getAsJsonObject("yrc").get("lyric").getAsString();
            parseYrc(yrc);
        }

        return lyricLines;
    }

    private static void processTranslationLyrics(JsonObject input, List<LyricLine> lyricLines) {
        if (!input.has("tlyric")) return;

        String tLyric = input.getAsJsonObject("tlyric").get("lyric").getAsString();
        if (tLyric.trim().isEmpty()) return;

        MusicLyricsWidget.hasTransLyrics = true;
        List<LyricLine> translates = parseSingleLine(tLyric);

        Map<Long, String> transMap = new HashMap<>();
        for (LyricLine t : translates) {
            transMap.put(t.timeStamp, t.lyric);
        }

        for (LyricLine l : lyricLines) {
            String translation = transMap.get(l.timeStamp);
            if (translation != null && l.translationText == null) {
                l.translationText = translation;
            }
        }
    }

    private static void processRomanizationLyrics(JsonObject input, List<LyricLine> lyricLines) {
        if (!input.has("romalrc")) return;

        String romanization = input.getAsJsonObject("romalrc").get("lyric").getAsString();
        if (romanization.isEmpty()) return;

        MusicLyricsWidget.hasRomanization = true;
        List<LyricLine> romanizations = parseSingleLine(romanization);

        Map<Long, String> romaMap = new HashMap<>();
        for (LyricLine r : romanizations) {
            romaMap.put(r.timeStamp, r.lyric);
        }

        for (LyricLine l : lyricLines) {
            String roma = romaMap.get(l.timeStamp);
            if (roma != null && l.romanizationText == null) {
                l.romanizationText = roma;
            }
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

        String text = lineMatcher.group(2).trim();
        if (text.isEmpty()) {
            return null;
        }/* else {
            System.out.println("text = " + text);
        }*/
        List<LyricLine> entryList = new ArrayList<>();
        Matcher timeMatcher = Pattern.compile(alt ? "\\[(\\d\\d):(\\d\\d):(\\d{2,3})]" : "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]").matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));
            long sec = Long.parseLong(timeMatcher.group(2));
            long mil = Long.parseLong(timeMatcher.group(3));
            
            int scale_mil = mil > 100 ? 1 : 10;
            
            long time =
                    min * 60000 +
                            sec * 1000 +
                            mil * scale_mil;
            
            entryList.add(new LyricLine(time, times, text));
        }
        return entryList;
    }

    public static void parseYrc(String yrc) {
        String[] lines = yrc.split("\n");
        if (lines.length == 1) lines = yrc.split("\\\\n");

        for (String line : lines) {
            if (!line.startsWith("[")) continue;

            String timeData = line.substring(1, line.indexOf("]"));
            String[] timeParts = timeData.split(",");
            long startDuration = Long.parseLong(timeParts[0]);
            long duration = Long.parseLong(timeParts[1]);

            MusicLyricsWidget.ScrollTiming timing = new MusicLyricsWidget.ScrollTiming();
            timing.start = startDuration;
            timing.duration = duration;
            timing.text = line.substring(line.indexOf("]") + 1);

            if (timing.text.isEmpty())
                continue;

            parseWordTimings(timing);
            MusicLyricsWidget.timings.add(timing);
        }
    }

    private static void parseWordTimings(MusicLyricsWidget.ScrollTiming timing) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+),0\\)((?!\\(\\d+,\\d+,0\\)|\\(\\d+,\\d+,0\\\\).)*");
        Matcher matcher = pattern.matcher(timing.text);
        long sumDuration = 0;

        while (matcher.find()) {
            String group = matcher.group();
            String metadata = group.substring(0, group.indexOf(")") + 1);
            String[] metadataParts = metadata.split(",");
            String lyric = group.substring(group.indexOf(")") + 1);

            MusicLyricsWidget.WordTiming wordTiming = new MusicLyricsWidget.WordTiming();
            wordTiming.word = lyric;
            sumDuration += Long.parseLong(metadataParts[1]);  
            wordTiming.timing = sumDuration;
            timing.timings.add(wordTiming);
        }
    }
}
