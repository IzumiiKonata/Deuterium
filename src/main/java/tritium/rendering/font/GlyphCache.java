package tritium.rendering.font;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author IzumiiKonata
 * Date: 2024/12/28 19:22
 *
 * 这个类是减少Glyph的CallList数量的
 * 如果一个Glyph的长和宽相等 就可以复用这个CallList
 *
 * 命中率很高 你可以把 Glyph.java 的 init() 方法中我注释掉的那一行代码取消注释 看看能命中多少次缓存
 */
@UtilityClass
public class GlyphCache {

    public static final AtomicReference<Integer> CALL_LIST_COUNTER = new AtomicReference<>(0);

    private final HashMap<GlyphSize, Integer> CALL_LIST_MAP = new HashMap<>();

    public void clear() {
        CALL_LIST_MAP.clear();
    }

    public boolean containsKey(final GlyphSize size) {
        return CALL_LIST_MAP.containsKey(size);
    }

    public Integer get(final GlyphSize size) {
        return CALL_LIST_MAP.get(size);
    }

    public void put(final GlyphSize size, final Integer value) {
        CALL_LIST_MAP.put(size, value);
    }

    public void remove(final GlyphSize size) {
        CALL_LIST_MAP.remove(size);
    }

    @AllArgsConstructor
    public class GlyphSize {
        public final float width, height;
        public final int imgWidth, imgHeight;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GlyphSize other = (GlyphSize) o;
            return width == other.width &&
                    height == other.height && imgWidth == other.imgWidth && imgHeight == other.imgHeight;
        }

        @Override
        public int hashCode() {
            return Float.floatToIntBits(width) + Float.floatToIntBits(height) + 3 * imgWidth + imgHeight;
        }
    }

}
