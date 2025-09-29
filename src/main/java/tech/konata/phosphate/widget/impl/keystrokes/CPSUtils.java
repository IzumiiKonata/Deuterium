package tech.konata.phosphate.widget.impl.keystrokes;

import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.utils.timing.Counter;

public class CPSUtils {

    public static final Counter.SecondCounter left = new Counter.SecondCounter(), right = new Counter.SecondCounter();

    public static void addLeftCPS() {
        left.add();
        WidgetsManager.keyStrokes.keys[5].circles.add(new Circle());
    }

    public static void addRightCPS() {
        right.add();
        WidgetsManager.keyStrokes.keys[6].circles.add(new Circle());
    }

}