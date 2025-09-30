package tritium.utils.timing;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/1/25
 */
@UtilityClass
public class Counter {

    public static final SecondCounter pps = new SecondCounter();

    public class SecondCounter {
        private final List<Long> counted = new ArrayList<>();

        public void add() {
            counted.add(System.currentTimeMillis());
        }

        public int get() {
            final Iterator<Long> iterator = counted.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() >= System.currentTimeMillis() - 1000L) {
                    continue;
                }
                iterator.remove();
            }
            return counted.size();
        }

    }

}
