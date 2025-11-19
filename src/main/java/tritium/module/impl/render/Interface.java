package tritium.module.impl.render;

import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.Render2DEvent;
import tritium.management.FontManager;
import tritium.module.Module;
import tritium.ncm.music.CloudMusic;
import tritium.rendering.MusicToast;
import tritium.rendering.Rect;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.music.PVRenderer;
import tritium.rendering.music.impl.*;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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

//    public BooleanSetting notifications = new BooleanSetting("Notifications", true);
//    public BooleanSetting waterMark = new BooleanSetting("Water Mark", false);
//    public StringSetting waterMarkValue = new StringSetting("Title", Tritium.NAME + "", () -> waterMark.getValue());
//    public NumberSetting<Double> pvScale = new NumberSetting<>("<..?> Scale", 1.5, 0.8, 3.0, 0.05);

    @Override
    public void onEnable() {

    }

    final List<PVRenderer> pvRenderers = Arrays.asList(
            new LagTrain(),
            new OchameKino(),
            new KyuKurarin()
    );

    @Handler
    public final void onRender2D(Render2DEvent e) {

        if (CloudMusic.currentlyPlaying != null && CloudMusic.player != null) {
            for (PVRenderer pvRenderer : pvRenderers) {
                if (pvRenderer.isApplicable(CloudMusic.currentlyPlaying.getId())) {
                    pvRenderer.render();
                    break;
                }
            }
        }

//        NORMAL.add(() -> {
//            double offsetX = 4, offsetY = 4;
//            for (CFontRenderer fr : FontManager.getAllFontRenderers()) {
//                String text = "AaBbGg你好";
//                double stringWidth = fr.getStringWidthD(text);
//                Rect.draw(offsetX, offsetY, stringWidth, fr.getFontHeight(), -1);
//                fr.drawString(text, offsetX, offsetY, hexColor(0, 0, 0f));
//                Rect.draw(offsetX + stringWidth, offsetY + fr.getFontHeight() * .5, stringWidth, fr.getFontHeight(), -1);
//                fr.drawString(text, offsetX + stringWidth, offsetY + fr.getFontHeight() * .5, hexColor(0, 0, 0f));
//                offsetY += fr.getFontHeight() + 2;
//
//                if (offsetY > RenderSystem.getHeight() - 20) {
//                    offsetX += 400;
//                    offsetY = 4;
//                }
//            }
//        });

//        mc.fontRendererObj.drawString("ABCDEFGabcdefg操操你妈逼", 100, 100, -1);

//        if (this.notifications.getValue())
//            NotificationManager.doRender(RenderSystem.getWidth() * 0.5, 4);
//
//        if (this.waterMark.getValue()) {
//            this.renderWaterMark();
//        }

        if (ClientSettings.DEBUG_MODE.getValue()) {
            x = 300;
            y = 100;

//            debug("CallLists: " + GlyphCache.CALL_LIST_COUNTER.get());

            x = 500;
            y = 100;

//                debug("AsyncGLContext:");
//                debug("Tasks: " + AsyncGLContext.getTASK_QUEUE().size());
//
//                for (int i = 0; i < AsyncGLContext.getThreads().size(); i++) {
//                    AsyncGLContext.Context workerThread = AsyncGLContext.getThreads().get(i);
//
//                    debug("Thread #" + i + ": " + workerThread.getState());
//                }
        }

//        Rect.draw(0, RenderSystem.getHeight() * .5 - .5, RenderSystem.getWidth(), 1, -1);
//        Rect.draw(RenderSystem.getWidth() * .5 - .5, 0, 1, RenderSystem.getHeight(), -1);
    }

    double x, y;
    private void debug(String text) {
        FontManager.pf25.drawString(text, x, y, -1);
        y += FontManager.pf25.getHeight() + 2;
    }

    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//    private void renderWaterMark() {
//        double posX = 4, posY = 4 + (double) 0;
//
//        String text = mc.getSession().getUsername() + " | " + sdf.format(new Date());
//
//        if (!waterMarkValue.getValue().isEmpty()) {
//            text = waterMarkValue.getValue().replaceAll("&", "\247") + EnumChatFormatting.RESET + " | " + text;
//        }
//
//        CFontRenderer fr = FontManager.pf20bold;
//
//        roundedRect(posX, posY, fr.getStringWidth(text) + 8, fr.getHeight() + 8, 4, new Color(12, 12, 12, 60));
//
//        fr.drawString(text, posX + 4, posY + 4, -1);
//    }

}
