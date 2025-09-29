package tech.konata.phosphate.management;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.DisplayResizedEvent;
import tech.konata.phosphate.event.events.rendering.Render2DEvent;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

import tech.konata.phosphate.screens.MoveWidgetsScreen;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.Setting;
import tech.konata.phosphate.widget.Widget;
import tech.konata.phosphate.widget.direction.HorizontalDirection;
import tech.konata.phosphate.widget.direction.VerticalDirection;
import tech.konata.phosphate.widget.impl.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * @since 2023/12/23
 */
public class WidgetsManager extends AbstractManager implements SharedRenderingConstants {

    public WidgetsManager() {
        super("WidgetsManager");
    }

    @Getter
    private static final List<Widget> widgets = new ArrayList<>();

    public static final tech.konata.phosphate.widget.impl.ArrayList arrayList = new tech.konata.phosphate.widget.impl.ArrayList();
    public static final Armor armor = new Armor();
    public static final Inventory inventory = new Inventory();
    public static final KeyStrokes keyStrokes = new KeyStrokes();
    public static final PaperDoll paperDoll = new PaperDoll();
    public static final ScoreBoard scoreBoard = new ScoreBoard();
    public static final TargetHud targetHud = new TargetHud();
    public static final FPSDisplay fpsDisplay = new FPSDisplay();
    public static final CPSDisplay cpsDisplay = new CPSDisplay();
    public static final ComboDisplay comboDisplay = new ComboDisplay();
    public static final PotionDisplay potionDisplay = new PotionDisplay();
    public static final PingDisplay pingDisplay = new PingDisplay();
    public static final Compass compass = new Compass();
    public static final Coords coords = new Coords();
    public static final MouseStrokes mouseStrokes = new MouseStrokes();
//    public static final BlockInfo blockInfo = new BlockInfo();

    // should before music and musiclyrics
    public static final MusicSpectrum musicSpectrum = new MusicSpectrum();
    public static final MusicWidget music = new MusicWidget();
    public static final MusicLyrics musicLyrics = new MusicLyrics();


    @Handler
    public void onRender2D(Render2DEvent event) {

        for (Widget widget : WidgetsManager.getWidgets()) {

            if (!widget.isEnabled())
                continue;

            if (widget.isMovable()) {
                this.doAdsorption(widget);
            }

            GlStateManager.pushMatrix();

            widget.doScale();

            widget.onRender(mc.currentScreen instanceof MoveWidgetsScreen);
            GlStateManager.popMatrix();

        }

        if (mc.currentScreen instanceof GuiChat) {
            ScaledResolution scaledResolution = ScaledResolution.get();
            double mouseX = Mouse.getX() * scaledResolution.getScaledWidth() / (mc.displayWidth * 1.0);
            double mouseY = scaledResolution.getScaledHeight() - Mouse.getY() * scaledResolution.getScaledHeight() / (mc.displayHeight * 1.0) - 1;
            mouseX = mouseX * (GlobalSettings.FIXED_SCALE.getValue() ? RenderSystem.getScaleFactor() : 1);
            mouseY = mouseY * (GlobalSettings.FIXED_SCALE.getValue() ? RenderSystem.getScaleFactor() : 1);

            this.renderWidgetButton(mouseX, mouseY);
        }

    }

    private void doAdsorption(Widget widget) {

        if (widget.horizontalDirection == HorizontalDirection.Left) {

            if (widget.getWidth() < 0)
                widget.setX(-widget.getWidth());
            else
                widget.setX(0);

        } else if (widget.horizontalDirection == HorizontalDirection.Center) {
            widget.setX(RenderSystem.getWidth() * 0.5 - widget.getWidth() * 0.5);
        } else if (widget.horizontalDirection == HorizontalDirection.Right) {
            widget.setX(RenderSystem.getWidth() - widget.getWidth());

            if (widget.getWidth() < 0)
                widget.setX(RenderSystem.getWidth());
        }

        if (widget.verticalDirection == VerticalDirection.Top) {

            if (widget.getHeight() < 0)
                widget.setY(-widget.getHeight());
            else
                widget.setY(0);

        } else if (widget.verticalDirection == VerticalDirection.Center) {
            widget.setY(RenderSystem.getHeight() * 0.5 - widget.getHeight() * 0.5);
        } else if (widget.verticalDirection == VerticalDirection.Bottom) {
            widget.setY(RenderSystem.getHeight() - widget.getHeight());

            if (widget.getHeight() < 0)
                widget.setY(RenderSystem.getHeight());
        }

    }

    public float rwbHoverAlpha = 0.0f;

    public void renderWidgetButton(double mouseX, double mouseY) {

        double size = 26;

        double offsetX = RenderSystem.getWidth() - 4 - size, offsetY = RenderSystem.getHeight() - 4 - size - 12 * RenderSystem.getScaleFactor();

        this.roundedRectAccentColor(offsetX, offsetY, size, size, 6, 255);
//        this.roundedRect(offsetX, offsetY, size, size, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, 100));

        SVGImage.draw(Location.of(Phosphate.NAME + "/textures/clickgui/panel/edit.svg"), offsetX + 4, offsetY + 4, 18, 18);

        boolean hovered = isHovered(mouseX, mouseY, offsetX, offsetY, size, size);

        if (hovered) {
            rwbHoverAlpha = Interpolations.interpBezier(rwbHoverAlpha, 0.2f, 0.4f);

            if (Mouse.isButtonDown(0)) {
                mc.displayGuiScreen(new MoveWidgetsScreen());
            }

        } else {
            rwbHoverAlpha = Interpolations.interpBezier(rwbHoverAlpha, 0.0f, 0.4f);
        }

        if (rwbHoverAlpha > 0.02) {
            this.roundedRect(offsetX, offsetY, size, size, 6, new Color(1, 1, 1, rwbHoverAlpha));
        }
    }

    @Override
    @SneakyThrows
    public void init() {
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (Widget.class.isAssignableFrom(field.getType())) {
                Widget widget = (Widget) field.get(null);

                if (widget == null)
                    continue;

                widgets.add(widget);
            }
        }

        List<Widget> collect = widgets.stream().distinct().collect(Collectors.toList());

        widgets.clear();
        widgets.addAll(collect);

        for (Widget widget : widgets) {
            // clear settings for reload command
            widget.getSettings().clear();

            for (Field f : widget.getClass().getDeclaredFields()) {
                f.setAccessible(true);

                if (Setting.class.isAssignableFrom(f.getType())) {
                    widget.addSettings((Setting<?>) f.get(widget));
                }
            }
        }

    }

    @Handler
    public void onResize(DisplayResizedEvent event) {

        if (true)
            return;


        for (Widget widget : widgets) {

            if (widget.horizontalDirection == HorizontalDirection.None) {
                widget.setX((widget.getX() / event.getBeforeWidth()) * event.getNowWidth());
            }

            if (widget.verticalDirection == VerticalDirection.None) {
                widget.setY((widget.getY() / event.getBeforeHeight()) * event.getNowHeight());
            }

        }

    }

    @Override
    public void stop() {

    }


    public Optional<Widget> getWidgetByName(String name) {

        for (Widget w : WidgetsManager.getWidgets()) {
            if (w.nameEquals(name))
                return Optional.of(w);
        }

        return Optional.empty();

    }

}
