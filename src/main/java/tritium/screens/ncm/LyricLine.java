package tritium.screens.ncm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import tritium.management.FontManager;
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

    public final List<Word> words = new CopyOnWriteArrayList<>();

    public static class Word {
        public String word;
        public long timing;
    }

    public void computeHeight(double width) {

        if (!this.words.isEmpty()) {
            double height = FontManager.pf65bold.getHeight();

            double w = 0;
            for (Word word : words) {
                double wordWidth = FontManager.pf65bold.getStringWidthD(word.word);

                if (w + wordWidth > width) {
                    height += FontManager.pf65bold.getHeight() * .85 + 4;
                    w = wordWidth;
                } else {
                    w += wordWidth;
                }
            }

            if (translationText != null) {
                height += FontManager.pf34bold.getHeight() + 8;
            }

            this.height = height;
        } else {
            int length = FontManager.pf65bold.fitWidth(lyric, (float) width).length;
            this.height = length * FontManager.pf65bold.getHeight() * .85 + (length - 1) * 4;

            if (translationText != null) {
                this.height += FontManager.pf34bold.getHeight() + 4;
            }
        }
    }

}
