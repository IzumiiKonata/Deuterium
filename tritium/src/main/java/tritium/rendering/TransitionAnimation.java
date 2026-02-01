package tritium.rendering;

import lombok.Getter;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.other.DevUtils;

import java.time.Duration;

/**
 * the transition animation for the guis
 *
 * @author IzumiiKonata
 * @since 6/20/2023 7:15 PM
 */
public class TransitionAnimation {

    static float screeMaskAlpha = 0;
    static boolean increasing = false, running = false;
    static Task task;
    static Animation fadeAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(250L));
    /**
     * render the transition mask on top of the screen
     */
    public static void render() {

        screeMaskAlpha = (float) fadeAnimation.run(increasing ? 1f : 0f);

        if (/*task == null || */(!increasing && fadeAnimation.isFinished()))
            return;

        Rect.draw(
                0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RGBA.color(0, 0, 0, (int) (screeMaskAlpha * 255)), Rect.RectType.EXPAND
        );

        if (increasing && screeMaskAlpha > 0.99) {
            increasing = false;
            task.run();
            task = null;
            running = false;
        }

    }

    public static void task(Runnable runnable) {

        runnable.run();
        return;

    }

    @Getter
    private abstract static class Task {

        private final String msg;

        public Task(String msg) {
            this.msg = msg;
        }

        public abstract void run();

    }
}
