package tritium.management;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import org.lwjglx.input.Mouse;
import tritium.Tritium;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.DisplayResizedEvent;
import tritium.event.events.rendering.Render2DEvent;
import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;

import tritium.screens.MoveWidgetsScreen;
import tritium.settings.ClientSettings;
import tritium.settings.Setting;
import tritium.widget.Widget;
import tritium.widget.direction.HorizontalDirection;
import tritium.widget.direction.VerticalDirection;
import tritium.widget.impl.*;

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

    public static final Armor armor = new Armor();
    public static final Inventory inventory = new Inventory();
    public static final KeyStrokes keyStrokes = new KeyStrokes();
    public static final PaperDoll paperDoll = new PaperDoll();
    public static final ScoreBoard scoreBoard = new ScoreBoard();
    public static final FPSDisplay fpsDisplay = new FPSDisplay();
    public static final CPSDisplay cpsDisplay = new CPSDisplay();
    public static final ComboDisplay comboDisplay = new ComboDisplay();
    public static final PotionDisplay potionDisplay = new PotionDisplay();
    public static final PingDisplay pingDisplay = new PingDisplay();
    public static final Compass compass = new Compass();
    public static final Coords coords = new Coords();
    public static final MouseStrokes mouseStrokes = new MouseStrokes();
    public static final MusicInfoWidget musicInfo = new MusicInfoWidget();
    public static final MusicLyricsWidget musicLyrics = new MusicLyricsWidget();
    public static final MusicSpectrumWidget musicSpectrum = new MusicSpectrumWidget();

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
