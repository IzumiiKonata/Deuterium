package tech.konata.phosphate.utils.music.lyric;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

@RequiredArgsConstructor
public class LyricLine {
    @Getter
    @NonNull
    public Long timeStamp;

    @Getter
    final String time;
    @Getter
    @NonNull
    public String lyric;
    @Getter
    public String translationText, romanizationText;

    public float alpha = 160 * RenderSystem.DIVIDE_BY_255;
    public double scale = 0.8;
    public float blur = 0.0f;
    public double scrollWidth = 0;
    public double offsetX = 0, targetOffsetX = 0;

    public double offsetY = Double.MIN_VALUE;

}
