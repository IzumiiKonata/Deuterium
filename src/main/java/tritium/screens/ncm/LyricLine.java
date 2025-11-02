package tritium.screens.ncm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;
import tritium.utils.timing.Timer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * Date: 2025/10/18 10:55
 */
@RequiredArgsConstructor
public class LyricLine {
    @Getter
    @NonNull
    public Long timeStamp;  // 时间戳(ms)

    @Getter
    @Setter
    @NonNull public String lyric;  // 歌词文本

    @Getter public String translationText;  // 翻译文本
    @Getter public String romanizationText; // 罗马音文本

    public double posY = 0;
    public double height = 0;
    public float alpha = .4f;
    public float hoveringAlpha = 0f;
    public float blurAlpha = 0f;
    public boolean shouldUpdatePosition = false;
    public double reboundAnimation = 0;
    public Timer delayTimer = new Timer();

    public final List<Word> words = new CopyOnWriteArrayList<>();

    public static class Word {
        public final String word;
        public final long timing;
        public final double[] emphasizes;

        public Word(String word, long timing) {
            this.word = word;
            this.timing = timing;
            this.emphasizes = new double[word.length()];
        }
    }

    private boolean heightComputed = false;

    public void computeHeight(double width) {

        if (heightComputed) return;

        CFontRenderer fr = FontManager.pf65bold;

        boolean canSetComputed = true;

        if (!this.words.isEmpty()) {
            double height = fr.getHeight();

            double w = 0;
            for (Word word : words) {

                if (!fr.areGlyphsLoaded(word.word)) {
                    canSetComputed = false;
                }

                double wordWidth = fr.getStringWidthD(word.word);

                if (w + wordWidth > width) {
                    height += fr.getHeight() * .85 + 4;
                    w = wordWidth;
                } else {
                    w += wordWidth;
                }
            }

            this.height = height;
        } else {

            if (!fr.areGlyphsLoaded(lyric)) {
                canSetComputed = false;
            }

            int length = fr.fitWidth(lyric, (float) width - 12).length;
            this.height = length * fr.getHeight() * .85 + length * 4;
        }

        if (translationText != null) {

            if (!fr.areGlyphsLoaded(translationText)) {
                canSetComputed = false;
            }

            CFontRenderer frTranslation = FontManager.pf34bold;
            String[] strings = frTranslation.fitWidth(translationText, width - 12);
            height += frTranslation.getHeight() * strings.length + 4 * (strings.length - 1)/* + 8*/;
        } else {
            height -= 4;
        }

        heightComputed = canSetComputed;
    }

}
