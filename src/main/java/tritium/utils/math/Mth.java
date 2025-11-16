package tritium.utils.math;

import lombok.experimental.UtilityClass;

/**
 * @author IzumiiKonata
 * Date: 2025/11/16 12:28
 */
@UtilityClass
public class Mth {

    public static int floor(float v) {
        int i = (int)v;
        return v < (float)i ? i - 1 : i;
    }

    public static int floor(double v) {
        int i = (int)v;
        return v < (double)i ? i - 1 : i;
    }

}
