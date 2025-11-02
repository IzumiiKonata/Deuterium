package tritium.ncm.music.lyric;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LyricLine {
    @Getter @NonNull
    public Long timeStamp;  // 时间戳(ms)

    @Getter final String time;  // 原始时间字符串
    @Getter @NonNull public String lyric;  // 歌词文本

    @Getter public String translationText;  // 翻译文本
    @Getter public String romanizationText; // 罗马音文本

    public float alpha = 160 / 255.0f;
    public double scale = 0.8;
    public float blur = 0.0f;
    public double scrollWidth = 0;
    public double offsetX = 0;
    public double targetOffsetX = 0;

    public double offsetY = Double.MIN_VALUE;
}