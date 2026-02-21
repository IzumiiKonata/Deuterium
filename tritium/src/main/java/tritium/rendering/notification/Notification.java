package tritium.rendering.notification;

import net.minecraft.client.Minecraft;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.Stencil;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.timing.Timer;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;

public class Notification implements SharedRenderingConstants {
    public static Minecraft mc = Minecraft.getMinecraft();

    public String message, title;
    public String icon;
    public Type type;
    public long stayTime;
    public boolean forever = false;
    public double renderX = Double.MAX_VALUE;
    public double renderY = -50;
    public double progress = 0;
    public Timer timer;

    public Notification(String title, String message, Type type, long stayTime) {
        this.message = message;
        this.title = title;

        switch (type) {
            case INFO:
                this.icon = "A";
                break;
            case WARNING:
                this.icon = "B";
                break;
            case ERROR:
                this.icon = "C";
                break;
            case SUCCESS:
                this.icon = "D";
                break;
        }
        this.type = type;
        this.stayTime = stayTime;

        this.timer = new Timer();
        timer.reset();
    }

    // no stay time = show forever
    public Notification(String title, String message, Type type) {
        this.message = message;
        this.title = title;

        switch (type) {
            case INFO:
                this.icon = "A";
                break;
            case WARNING:
                this.icon = "B";
                break;
            case ERROR:
                this.icon = "C";
                break;
            case SUCCESS:
                this.icon = "D";
                break;
        }
        this.type = type;
        this.forever = true;

        this.timer = new Timer();
        timer.reset();
    }

    public void show() {
        NotificationManager.getNotifications().add(this);
    }

    DecimalFormat df = new DecimalFormat("##.#");

    double width = 160;
    double height = 40;

    Animation openCloseAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(500L));

    public void draw(double offsetX, double offsetY) {

        renderY = Interpolations.interpolate(renderY, offsetY, 0.2);

        renderX = RenderSystem.getWidth() * 0.5 - width * 0.5;

        boolean closing = (timer.delayed().toMillis() > stayTime && !forever);

        openCloseAnimation.run(closing ? 0 : width * 0.5);

        RenderSystem.doScissor(renderX + width * 0.5 - openCloseAnimation.getValue(), renderY, openCloseAnimation.getValue() * 2, height + 4);
        roundedRect(renderX, renderY, width, height, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, 255));

        int fontColor = switch (this.type) {
            case INFO -> ThemeManager.get(ThemeManager.ThemeColor.Text);
            case WARNING -> 0xffefbc12;
            case ERROR -> 0xfff04747;
            case SUCCESS -> 0xff23ad5c;
        };

        FontManager.pf20bold.drawCenteredString(this.title, renderX + width * 0.5, renderY + 4, fontColor);

        if (!forever) {
            double leftTime = (this.stayTime - timer.delayed().toMillis()) / 1000.0;

//                String fmt = df.format(leftTime);
//
//                if (fmt.length() == 1)
//                    fmt += ".0";

//                FontManager.pf20.drawString(" (" + fmt + "s)", renderX + 8 + FontManager.pf20bold.getStringWidth(this.title), renderY + 4.5, fontColor);

            progress = ((double) timer.delayed().toMillis() / this.stayTime);
        }

        Stencil.write();
        Rect.draw(renderX, renderY + height - 3, width * progress, 3, -1, Rect.RectType.EXPAND);
        Stencil.erase();
        roundedRect(renderX, renderY, width, height, 6, new Color(fontColor));
        Stencil.dispose();

        CFontRenderer fontRenderer = FontManager.pf18bold;

        fontRenderer.drawCenteredString(this.message, renderX + width * 0.5, renderY + 10 + fontRenderer.getHeight(), fontColor);
        width = Math.max(fontRenderer.getStringWidth(this.message) + 8, 160);
        RenderSystem.endScissor();

    }

    public boolean shouldDelete() {
        if (forever)
            return false;

        return isFinished() && openCloseAnimation.getValue() <= 0;
    }

    private boolean isFinished() {
        return timer.isDelayed(stayTime);
    }

    public enum Type {
        INFO, WARNING, ERROR, SUCCESS
    }

}
