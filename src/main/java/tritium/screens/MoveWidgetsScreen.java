package tritium.screens;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.event.eventapi.Handler;
import tritium.event.events.input.FileDroppedEvent;
import tritium.management.EventManager;
import tritium.utils.i18n.Localizable;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.cursor.CursorUtils;
import tritium.widget.Widget;
import tritium.widget.direction.HorizontalDirection;
import tritium.widget.direction.VerticalDirection;
import tritium.widget.impl.GifTextureWidget;
import tritium.widget.impl.StaticTextureWidget;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/1/12 21:56
 */
public class MoveWidgetsScreen extends BaseScreen {

    private static Widget positioningWidget = null, draggingWidget = null, hoveredWidget = null;

    @Override
    public void initGui() {
        EventManager.register(this);
    }

    @Handler
    public void onFileDropped(FileDroppedEvent event) {
        for (String name : event.getNames()) {
            File file = new File(name);

            if (file.exists()) {

                String lowerCase = file.getName().toLowerCase();
                if (lowerCase.endsWith(".jpg") || lowerCase.endsWith(".png")) {
                    WidgetsManager.getWidgets().add(new StaticTextureWidget(file));
                }

                if (lowerCase.endsWith(".gif")) {
                    WidgetsManager.getWidgets().add(new GifTextureWidget(file));
                }

            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        int dWheel = Mouse.getDWheel();

        hoveredWidget = null;

        if (!Mouse.isButtonDown(0) && positioningWidget != null)
            positioningWidget = null;

        for (Iterator<Widget> iterator = WidgetsManager.getWidgets().iterator(); iterator.hasNext(); ) {
            Widget widget = iterator.next();

            if (!widget.isEnabled())
                continue;

            if (widget.isResizable()) {
                this.resizeWidget(widget, mouseX, mouseY);
            }

            if (widget.isMovable()) {
                this.renderWidgetInfo(widget, mouseX, mouseY);
                this.doCollisions(widget, mouseX, mouseY);
            }

            if (widget.isScalable()) {
                this.scaleWidget(widget, iterator, mouseX, mouseY, dWheel);
            }

        }

        if (draggingWidget != null || hoveredWidget != null) {
            this.setCursor(CursorUtils.RESIZE_NWSE);
        }
    }

    @Override
    public void onGuiClosed() {
        CursorUtils.setCursor(CursorUtils.ARROW);
        EventManager.unregister(this);
    }

    private void scaleWidget(Widget widget, Iterator<Widget> iter, double mouseX, double mouseY, int dWheel) {

        boolean hovered = isHovered(mouseX, mouseY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());

        if (Mouse.isButtonDown(1) && hovered) {
            widget.scaleFactor = 1;
        }

        if (hovered && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Mouse.isButtonDown(0) && (widget instanceof StaticTextureWidget || widget instanceof GifTextureWidget)) {
            iter.remove();
            return;
        }

        if (dWheel != 0) {

            if (hovered) {
                widget.scaleFactor += (dWheel > 0 ? 1 : -1) * 0.1;
            }

        }

        if (widget.scaleFactor < 0.5) {
            widget.scaleFactor = 0.5;
        }

        if (widget.scaleFactor > 2) {
            widget.scaleFactor = 2;
        }

    }

    private void resizeWidget(Widget widget, double mouseX, double mouseY) {
        double posX = widget.getX(), posY = widget.getY(), width = widget.getWidth(), height = widget.getHeight();

        double resizeRange = 8 * widget.scaleFactor;

        if (RenderSystem.isHovered(mouseX, mouseY, posX + width - resizeRange, posY + height - resizeRange, resizeRange * 1.5, resizeRange * 1.5)) {

            if (draggingWidget == null) {
                if (Mouse.isButtonDown(0)) {
                    draggingWidget = widget;
                }
            }

            hoveredWidget = widget;
        }

        if (!Mouse.isButtonDown(0) && draggingWidget == widget)
            draggingWidget = null;

        if (draggingWidget == widget) {
            if (widget.resizeX == 0 && widget.resizeY == 0) {
                widget.resizeX = mouseX - width;
                widget.resizeY = mouseY - height;
            } else {

                double lastWidth = widget.getWidth(), lastHeight = widget.getHeight();

                double w = Math.max(widget.defaultWidth, (mouseX - widget.resizeX) / widget.scaleFactor);
                double h = Math.max(widget.defaultHeight, (mouseY - widget.resizeY) / widget.scaleFactor);

                if (widget.isLockResizeRatio()) {
                    w = h * widget.getRatio();
                    widget.setWidth(w);
                    widget.setHeight(h);
                } else {
                    widget.setWidth(w);
                    widget.setHeight(h);
                }

                widget.onResized(lastWidth, lastHeight);
            }
        } else if (widget.resizeX != 0 || widget.resizeY != 0) {
            widget.resizeX = 0;
            widget.resizeY = 0;
        }

    }

    Localizable lSize = Localizable.of("widgets.size");
    Localizable lScale = Localizable.of("widgets.scale");

    Localizable lHLeft = Localizable.of("widgets.hLeft");
    Localizable lHCenter = Localizable.of("widgets.hCenter");
    Localizable lHRight = Localizable.of("widgets.hRight");

    Localizable lVTop = Localizable.of("widgets.vTop");
    Localizable lVCenter = Localizable.of("widgets.vCenter");
    Localizable lVBottom = Localizable.of("widgets.vBottom");


    DecimalFormat df = new DecimalFormat("##.#");

    private void renderWidgetInfo(Widget widget, double mouseX, double mouseY) {
        double spacingH = 2 * widget.scaleFactor;
        double spacingV = spacingH;

        if (widget.getWidth() < 0) {
            spacingH = -spacingH;
        }

        this.roundedOutline(widget.getX() - spacingH, widget.getY() - spacingV, widget.getWidth() + spacingH * 2, widget.getHeight() + spacingV * 2, 3, 1.5 * widget.scaleFactor, new Color(1, 1, 1, 0.5f + widget.hoveredAlpha));

        boolean hovered = isHovered(mouseX, mouseY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());

        widget.hoveredAlpha = Interpolations.interpBezier(widget.hoveredAlpha, hovered ? 0.3f : 0f, 0.4f);

//        Rect.draw(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), hexColor(0, 0, 0, 40), Rect.RectType.EXPAND);

        List<String> list = new ArrayList<>();

        list.add(widget.getName().get() + " (" + (int) widget.getX() + ", " + (int) widget.getY() + ")");

        if (widget.isResizable()) {
            list.add(lSize.get() + ": (" + df.format(widget.getWidth() / widget.scaleFactor) + ", " + df.format(widget.getHeight() / widget.scaleFactor) + ")");
        }

        if (widget.isMovable()) {

            String align = "";

            switch (widget.horizontalDirection) {
                case Left:
                    align += lHLeft.get();
                    break;
                case Center:
                    align += lHCenter.get();
                    break;
                case Right:
                    align += lHRight.get();
                    break;
            }

            if (widget.verticalDirection != VerticalDirection.None) {

                if (!align.isEmpty()) {
                    align += ", ";
                }

                switch (widget.verticalDirection) {
                    case Top:
                        align += lVTop.get();
                        break;
                    case Center:
                        align += lVCenter.get();
                        break;
                    case Bottom:
                        align += lVBottom.get();
                        break;
                }
            }

            if (!align.isEmpty()) {
                list.add(align);
            }

        }

        if (widget.isScalable()) {
            list.add(lScale.get() + ": " + df.format(widget.scaleFactor));
        }

        double posX = widget.getX() + widget.getWidth();

        if (widget.getWidth() < 0) {
            posX = widget.getX();
        }

        double offsetY = widget.getY() + widget.getHeight() + (Math.abs(spacingH)) * 2;

        for (String s : list) {
            FontManager.pf20bold.drawString(s, posX - FontManager.pf20bold.getStringWidthD(s), offsetY, -1);

            offsetY += FontManager.pf20bold.getHeight() + 2;
        }

    }

    private void doCollisions(Widget widget, double mouseX, double mouseY) {

        if (false) {
            if (widget.getWidth() < 0 && widget.getX() + widget.getWidth() < 0)
                widget.setX(-widget.getWidth());
            else if (widget.getX() < 0) {
                widget.setX(0);
            }

            if (widget.getWidth() < 0 && widget.getX() > RenderSystem.getWidth())
                widget.setX(RenderSystem.getWidth());
            else if (widget.getX() + widget.getWidth() > RenderSystem.getWidth()) {
                widget.setX(RenderSystem.getWidth() - widget.getWidth());
            }

            double limit = 0;

            if (widget.getHeight() < limit && widget.getY() + widget.getHeight() < limit)
                widget.setY(-widget.getHeight());
            else if (widget.getY() < limit)
                widget.setY(limit);

            if (widget.getHeight() < 0 && widget.getY() > RenderSystem.getHeight())
                widget.setY(RenderSystem.getHeight());
            else if (widget.getY() + widget.getHeight() > RenderSystem.getHeight()) {
                widget.setY(RenderSystem.getHeight() - widget.getHeight());
            }
        }

//        this.doAdsorption(widget);

        if ((RenderSystem.isHovered(mouseX, mouseY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()) || positioningWidget == widget) && Mouse.isButtonDown(0) && (positioningWidget == null || positioningWidget == widget) && draggingWidget == null) {
            positioningWidget = widget;
            if (widget.getMoveX() == 0 && widget.getMoveY() == 0) {
                widget.setMoveX(mouseX - widget.getX());
                widget.setMoveY(mouseY - widget.getY());
            } else {

                double widgetWidth = widget.getWidth();
                double widgetHeight = widget.getHeight();

                double x = Math.max(0, Math.min(RenderSystem.getWidth() - widgetWidth, mouseX - widget.getMoveX()));
                double y = Math.max(0, Math.min(RenderSystem.getHeight() - widgetHeight, mouseY - widget.getMoveY()));

                double widgetX = widget.getX();
                double widgetY = widget.getY();

                double widgetCenterX = widgetX + widgetWidth * 0.5;
                double widgetCenterY = widgetY + widgetHeight * 0.5;

                double range = 4;

                if (widgetWidth > 0) {
                    if (widgetX <= range) {
                        widget.horizontalDirection = HorizontalDirection.Left;
                    } else if (this.distanceTo(widgetCenterX, RenderSystem.getWidth() * 0.5) <= range) {
                        widget.horizontalDirection = HorizontalDirection.Center;
                    } else if (RenderSystem.getWidth() - (widgetX + widgetWidth) <= range) {
                        widget.horizontalDirection = HorizontalDirection.Right;
                    } else {
                        widget.horizontalDirection = HorizontalDirection.None;
                    }
                } else {
                    if (widgetX + widgetWidth <= range) {
                        widget.horizontalDirection = HorizontalDirection.Left;
                    } else if (this.distanceTo(widgetCenterX, RenderSystem.getWidth() * 0.5) <= range) {
                        widget.horizontalDirection = HorizontalDirection.Center;
                    } else if (RenderSystem.getWidth() - (widgetX) <= range) {
                        widget.horizontalDirection = HorizontalDirection.Right;
                    } else {
                        widget.horizontalDirection = HorizontalDirection.None;
                    }
                }

                if (widgetHeight > 0) {
                    if (widgetY <= range) {
                        widget.verticalDirection = VerticalDirection.Top;
                    } else if (this.distanceTo(widgetCenterY, RenderSystem.getHeight() * 0.5) <= range) {
                        widget.verticalDirection = VerticalDirection.Center;
                    } else if (RenderSystem.getHeight() - (widgetY + widgetHeight) <= range) {
                        widget.verticalDirection = VerticalDirection.Bottom;
                    } else {
                        widget.verticalDirection = VerticalDirection.None;
                    }
                } else {
                    if (widgetY + widgetHeight <= range) {
                        widget.verticalDirection = VerticalDirection.Top;
                    } else if (this.distanceTo(widgetCenterY, RenderSystem.getHeight() * 0.5) <= range) {
                        widget.verticalDirection = VerticalDirection.Center;
                    } else if (RenderSystem.getHeight() - (widgetY) <= range) {
                        widget.verticalDirection = VerticalDirection.Bottom;
                    } else {
                        widget.verticalDirection = VerticalDirection.None;
                    }
                }

                double lineSize = 1;

                double threshold = 4;

                if (this.distanceTo(widgetX, x) > threshold || widget.horizontalDirection == HorizontalDirection.None) {
                    widget.setX(x);
                    widget.horizontalDirection = HorizontalDirection.None;
                }

                if (this.distanceTo(widgetY, y) > threshold || widget.verticalDirection == VerticalDirection.None) {
                    widget.setY(y);
                    widget.verticalDirection = VerticalDirection.None;
                }

//                for (Widget w : WidgetsManager.getWidgets()) {
//
//                    if (!w.isEnabled())
//                        continue;
//
//                    if (w == widget)
//                        continue;
//
//                    if (distanceTo(widgetX, w.getX()) <= range) {
//                        Rect.draw(w.getX() - lineSize, 0, lineSize, RenderSystem.getHeight(), -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetX(widget, widgetX, w.getX(), x, threshold);
//                    } else if (this.distanceTo(widgetX + widgetWidth, w.getX() + w.getWidth()) <= range) {
//                        Rect.draw(w.getX() + w.getWidth(), 0, lineSize, RenderSystem.getHeight(), -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetX(widget, widgetX + widgetWidth, w.getX() + w.getWidth() - widgetWidth, x + widgetWidth, threshold);
//                    } else if (this.distanceTo(widgetX, w.getX() + w.getWidth()) <= range) {
//                        Rect.draw(w.getX() + w.getWidth(), 0, lineSize, RenderSystem.getHeight(), -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetX(widget, widgetX, w.getX() + w.getWidth(), x, threshold);
//                    } else if (this.distanceTo(widgetX + widgetWidth, w.getX()) <= range) {
//                        Rect.draw(w.getX() - lineSize, 0, lineSize, RenderSystem.getHeight(), -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetX(widget, widgetX + widgetWidth, w.getX() - widgetWidth, x + widgetWidth, threshold);
//                    }
//
//                    if (this.distanceTo(widgetY, w.getY()) <= threshold) {
//                        Rect.draw(0, w.getY() - lineSize, RenderSystem.getWidth(), lineSize, -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetY(widget, widgetY, w.getY(), y, threshold);
//                    } else if (this.distanceTo(widgetY + widgetHeight, w.getY()) <= range) {
//                        Rect.draw(0, w.getY() - lineSize, RenderSystem.getWidth(), lineSize, -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetY(widget, widgetY + widgetHeight, w.getY() - widgetHeight, y + widgetHeight, threshold);
//                    } else if (this.distanceTo(widgetY, w.getY() + w.getHeight()) <= range) {
//                        Rect.draw(0, w.getY() + w.getHeight(), RenderSystem.getWidth(), lineSize, -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetY(widget, widgetY, w.getY() + w.getHeight(), y, threshold);
//                    } else if (this.distanceTo(widgetY + widgetHeight, w.getY() + w.getHeight()) <= range) {
//                        Rect.draw(0, w.getY() + w.getHeight(), RenderSystem.getWidth(), lineSize, -1, Rect.RectType.EXPAND);
//
//                        this.moveWidgetY(widget, widgetY + widgetHeight, w.getY() + w.getHeight() - widgetHeight, y + widgetHeight, threshold);
//                    }
//
//                }

                if (widget.horizontalDirection == HorizontalDirection.Center) {
                    Rect.draw(RenderSystem.getWidth() * 0.5 - lineSize * 0.5, 0, lineSize, RenderSystem.getHeight(), -1, Rect.RectType.EXPAND);
                }

                if (widget.verticalDirection == VerticalDirection.Center) {
                    Rect.draw(0, RenderSystem.getHeight() * 0.5 - lineSize * 0.5, RenderSystem.getWidth(), lineSize, -1, Rect.RectType.EXPAND);
                }

            }
        } else if ((widget.getMoveX() != 0 || widget.getMoveY() != 0)) {
            if (positioningWidget == widget)
                positioningWidget = null;
            widget.setMoveX(0);
            widget.setMoveY(0);
        }
    }

    private void moveWidgetX(Widget widget, double nowX, double destX, double realX, double threshold) {
        if (this.distanceTo(nowX, realX) <= threshold) {
            widget.setX(destX);
        } else {
            widget.setX(realX);
        }
    }

    private void moveWidgetY(Widget widget, double nowY, double destY, double realY, double threshold) {
        if (this.distanceTo(nowY, realY) <= threshold) {
            widget.setY(destY);
        } else {
            widget.setY(realY);
        }
    }

    private double distanceTo(double a, double b) {
        return Math.abs(a - b);
    }
}
