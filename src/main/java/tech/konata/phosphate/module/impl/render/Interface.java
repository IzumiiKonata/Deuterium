package tech.konata.phosphate.module.impl.render;

import net.minecraft.util.EnumChatFormatting;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render2DEvent;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.TitleBar;
import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.font.GlyphCache;
import tech.konata.phosphate.rendering.music.PVRenderer;
import tech.konata.phosphate.rendering.music.impl.KyuKurarin;
import tech.konata.phosphate.rendering.music.impl.LagTrain;
import tech.konata.phosphate.rendering.music.impl.LiarDancer;
import tech.konata.phosphate.rendering.music.impl.OchameKino;
import tech.konata.phosphate.rendering.notification.NotificationManager;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.settings.StringSetting;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.music.CloudMusic;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public StringSetting waterMarkValue = new StringSetting("Title", Phosphate.NAME + "", () -> waterMark.getValue());
    public NumberSetting<Double> pvScale = new NumberSetting<>("<..?> Scale", 1.5, 0.8, 3.0, 0.05);

    List<PVRenderer> pvRenderers;

    @Override
    public void onEnable() {

        pvRenderers = Arrays.asList(
                new OchameKino(),
                new LagTrain(),
                new KyuKurarin(),
                new LiarDancer()
        );

        pvRenderers.forEach(PVRenderer::initRenderer);
    }

    @Handler
    public final void onRender2D(Render2DEvent.Render2DBeforeInventoryEvent e) {

        if (this.notifications.getValue())
            NotificationManager.doRender(RenderSystem.getWidth() * 0.5, 4);

        if (this.waterMark.getValue()) {
            this.renderWaterMark();
        }

        if (CloudMusic.currentlyPlaying != null && CloudMusic.player != null) {

            for (PVRenderer pvRenderer : pvRenderers) {
                if (pvRenderer.isApplicable(CloudMusic.currentlyPlaying.getId()))
                    pvRenderer.render();
            }

        }

        if (GlobalSettings.DEBUG_MODE.getValue()) {
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
        double posX = 4, posY = 4 + TitleBar.getTitlebarHeight();

        String text = mc.getSession().getUsername() + " | " + sdf.format(new Date());

        if (!waterMarkValue.getValue().isEmpty()) {
            text = waterMarkValue.getValue().replaceAll("&", "\247") + EnumChatFormatting.RESET + " | " + text;
        }

        CFontRenderer fr = FontManager.pf20bold;

        roundedRect(posX, posY, fr.getStringWidth(text) + 8, fr.getHeight() + 8, 4, new Color(12, 12, 12, 60));

        fr.drawString(text, posX + 4, posY + 4, -1);
    }

}
