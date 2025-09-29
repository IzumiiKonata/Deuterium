package tech.konata.phosphate.rendering.notification;

import net.minecraft.util.Location;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.widget.impl.MusicWidget;

import java.awt.*;

public class NotificationCurrentPlaying extends Notification {

    final Music playing;

    public NotificationCurrentPlaying(String title, String message, Type type, long stayTime, Music playing) {
        super(title, message, type, stayTime);
        this.playing = playing;
    }

    // no stay time = show forever
    public NotificationCurrentPlaying(String title, String message, Type type, Music playing) {
        super(title, message, type);
        this.playing = playing;
    }

    @Override
    public void draw(double offsetX, double offsetY) {
        renderY = Interpolations.interpBezier(renderY, offsetY, 0.2);

        renderX = RenderSystem.getWidth() * 0.5 - width * 0.5;

        boolean closing = (timer.delayed().toMillis() > stayTime && !forever);

        openCloseAnimation.run(closing ? 0 : width * 0.5);

        NORMAL.add(() -> {
            RenderSystem.doScissor(renderX + width * 0.5 - openCloseAnimation.getValue(), renderY, openCloseAnimation.getValue() * 2, height + 2);
            roundedRect(renderX, renderY, width, height, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, 255));

            int fontColor;
            switch (this.type) {
                case INFO:
                    fontColor = ThemeManager.get(ThemeManager.ThemeColor.Text);
                    break;
                case WARNING:
                    fontColor = 0xffefbc12;
                    break;
                case ERROR:
                    fontColor = 0xfff04747;
                    break;
                case SUCCESS:
                    fontColor = 0xff23ad5c;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            double coverSize = 64;

            CFontRenderer frTitle = FontManager.pf25bold;
            frTitle.drawCenteredString(this.title, renderX + width * 0.5, renderY + 4, fontColor);

            if (!forever) {
                progress = ((double) timer.delayed().toMillis() / this.stayTime);
            }

            Location cover = MusicWidget.getMusicCover(this.playing);
            if (mc.getTextureManager().getTexture(cover) != null) {
                mc.getTextureManager().bindTexture(cover);
                roundedRectTextured(renderX + width * 0.5 - coverSize * 0.5, renderY + 4 + frTitle.getHeight() + 4, coverSize, coverSize, 8);
            } else {
                roundedRect(renderX + 4, renderY + 4, coverSize, coverSize, 8, Color.GRAY);
            }

            CFontRenderer frContent = FontManager.pf20bold;

            frContent.drawCenteredStringMultiLine(this.message, renderX + width * 0.5, renderY + 4 + frTitle.getHeight() + 4 + coverSize + 4, fontColor);
            width = Math.max(frContent.getStringWidth(this.message) + 16, 100);
            RenderSystem.endScissor();

            height = 4 + frTitle.getHeight() + 4 + coverSize + 4 + frContent.getStringHeight(this.message) + 6;

        });
    }
}
