package tritium.rendering.entities.impl;

import lombok.Setter;
import tritium.rendering.Stencil;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.settings.ClientSettings;
import tritium.utils.timing.Timer;

import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2024/9/17 22:32
 */
public class ScrollText {

    public ScrollText() {

    }

    Timer t = new Timer();

    double scrollOffset = 0;

    String cachedText = "";

    @Setter
    long waitTime = 2500;

    @Setter
    boolean oneShot = false;

    public Animation anim = new Animation(Easing.LINEAR, Duration.ofMillis(0));

    public void reset() {
        t.reset();
        scrollOffset = 0;
        anim.reset();
        anim.setStartValue(0);
        anim.setValue(0);
    }

    public void render(CFontRenderer fr, String text, double x, double y, double width, int color) {

        if (!cachedText.equals(text)) {
            cachedText = text;

            this.reset();
        }

        StencilClipManager.beginClip(() -> {
            double exp = 2;
            Rect.draw(x, y - exp, width, fr.getHeight() + exp * 2, -1, Rect.RectType.EXPAND);
        });

        fr.drawString(text, x + scrollOffset, y, color);

        int w = fr.getStringWidth(text);

        if (w > width) {

            // wait time
//            if (t.isDelayed(waitTime)) {
//
//            }

            double dest = -(w - width + 4);

            if (anim.getDuration() != 0) {
                scrollOffset = anim.run(dest);
            } else {

                String s = "    ";

                dest = -(w + fr.getStringWidth(s));

                if (t.isDelayed(waitTime)) {
                    scrollOffset = Interpolations.interpLinear((float) scrollOffset, (float) dest, 2f);
                }

                fr.drawString(s + text, x + w + scrollOffset, y, color);

                if (Math.abs(dest - scrollOffset) == 0) {
                    scrollOffset = 0;
                    t.reset();
                }

            }

//            if (scrollOffset - dest < 0.2) {
//                if (!oneShot) {
//                    t.reset();
//                }
//            }

        }


        StencilClipManager.endClip();

        if (ClientSettings.DEBUG_MODE.getValue()) {
            fr.drawString("" + scrollOffset, x, y - fr.getHeight(), color);
        }

    }

}
