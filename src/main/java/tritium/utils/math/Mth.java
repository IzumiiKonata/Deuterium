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

    public static float frac(float num) {
        return num - (float)Mth.floor(num);
    }

    public static double frac(double num) {
        return num - (double)Mth.lfloor(num);
    }

    public static long lfloor(double v) {
        long i = (long)v;
        return v < (double)i ? i - 1L : i;
    }

    public static int lerpInt(float alpha1, int p0, int p1) {
        return p0 + Mth.floor(alpha1 * (float)(p1 - p0));
    }

}
