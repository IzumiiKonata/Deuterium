package tritium.module.impl.render;

import net.minecraft.util.EnumChatFormatting;
import tritium.Tritium;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.Render2DEvent;
import tritium.management.FontManager;
import tritium.module.Module;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.font.GlyphCache;
import tritium.rendering.notification.NotificationManager;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.settings.ClientSettings;
import tritium.settings.NumberSetting;
import tritium.settings.StringSetting;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class Interface extends Module {

    public Interface() {
        super("Interface", Category.RENDER);
        super.setEnabled(true);
    }

    public BooleanSetting notifications = new BooleanSetting("Notifications", true);
    public BooleanSetting waterMark = new BooleanSetting("Water Mark", false);
    public StringSetting waterMarkValue = new StringSetting("Title", Tritium.NAME + "", () -> waterMark.getValue());
    public NumberSetting<Double> pvScale = new NumberSetting<>("<..?> Scale", 1.5, 0.8, 3.0, 0.05);

    @Override
    public void onEnable() {

    }

    @Handler
    public final void onRender2D(Render2DEvent.Render2DBeforeInventoryEvent e) {

        if (this.notifications.getValue())
            NotificationManager.doRender(RenderSystem.getWidth() * 0.5, 4);

        if (this.waterMark.getValue()) {
            this.renderWaterMark();
        }

        if (ClientSettings.DEBUG_MODE.getValue()) {
            x = 300;
            y = 100;

            debug("MultiThreadingUtil:");
            debug("Tasks: " + MultiThreadingUtil.getTASK_QUEUE().size());

            for (int i = 0; i < MultiThreadingUtil.getThreads().size(); i++) {
                MultiThreadingUtil.WorkerThread workerThread = MultiThreadingUtil.getThreads().get(i);

                debug("Thread #" + i + ": " + workerThread.getState());
            }

            debug("CallLists: " + GlyphCache.CALL_LIST_COUNTER.get());

            x = 500;
            y = 100;

            debug("AsyncGLContext:");
            debug("Tasks: " + AsyncGLContext.getTASK_QUEUE().size());

            for (int i = 0; i < AsyncGLContext.getThreads().size(); i++) {
                AsyncGLContext.Context workerThread = AsyncGLContext.getThreads().get(i);

                debug("Thread #" + i + ": " + workerThread.getState());
            }
        }

    }

    double x, y;
    private void debug(String text) {
        FontManager.pf25.drawString(text, x, y, -1);
        y += FontManager.pf25.getHeight() + 2;
    }

    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private void renderWaterMark() {
        double posX = 4, posY = 4 + (double) 0;

        String text = mc.getSession().getUsername() + " | " + sdf.format(new Date());

        if (!waterMarkValue.getValue().isEmpty()) {
            text = waterMarkValue.getValue().replaceAll("&", "\247") + EnumChatFormatting.RESET + " | " + text;
        }

        CFontRenderer fr = FontManager.pf20bold;

        roundedRect(posX, posY, fr.getStringWidth(text) + 8, fr.getHeight() + 8, 4, new Color(12, 12, 12, 60));

        fr.drawString(text, posX + 4, posY + 4, -1);
    }

}
