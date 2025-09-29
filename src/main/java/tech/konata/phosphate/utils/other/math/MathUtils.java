package tech.konata.phosphate.utils.other.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    public static double round(double num, double increment) {
        if (increment < 0.0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale((int) increment, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double range(double max, double min, double input) {
        return Math.min(max, Math.max(min, input));
    }

    public static double randomNumber(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static double getIncremental(double val, double inc) {
        double one = 1.0 / inc;
        return (double) Math.round(val * one) / one;
    }

    public static float clamp(float input, float min, float max) {
        if (input > max)
            input = max;
        if (input < min)
            input = min;
        return input;
    }

    public static double clamp(double value, double minimum, double maximum) {
        return value > maximum ? maximum : Math.max(value, minimum);
    }

    public static double roundToPlace(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean isInError(double n1, double n2, double error) {
        return Math.abs(n1 - n2) <= Math.abs(error);
    }
}
