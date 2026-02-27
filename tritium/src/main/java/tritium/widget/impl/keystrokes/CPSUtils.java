package tritium.widget.impl.keystrokes;

import tritium.management.WidgetsManager;
import tritium.utils.timing.Counter;

public class CPSUtils {

    public static final Counter.SecondCounter left = new Counter.SecondCounter(), right = new Counter.SecondCounter();

    public static void addLeftCPS() {

        if (!WidgetsManager.keyStrokes.isEnabled())
            return;

        left.add();
        WidgetsManager.keyStrokes.keys[5].circles.add(new Circle());
    }

    public static void addRightCPS() {

        if (!WidgetsManager.keyStrokes.isEnabled())
            return;

        right.add();
        WidgetsManager.keyStrokes.keys[6].circles.add(new Circle());
    }

}