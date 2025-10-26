package tritium.bridge;

import today.opai.api.features.ExtensionWidget;
import tritium.utils.i18n.Localizable;
import tritium.widget.Widget;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 19:08
 */
public class ExtensionWidgetWrapper extends Widget {

    private final ExtensionWidget widget;

    public ExtensionWidgetWrapper(ExtensionWidget widget) {
        super(widget.getName());
        this.setName(Localizable.ofUntranslatable(widget.getName()));
        this.widget = widget;
    }

    @Override
    public void onRender(boolean editing) {
        NORMAL.add(widget::render);
    }

    @Override
    public double getX() {
        return widget.getX();
    }

    @Override
    public void setX(double x) {
        widget.setX((float) x);
    }

    @Override
    public double getY() {
        return widget.getY();
    }

    @Override
    public void setY(double y) {
        widget.setY((float) y);
    }

    @Override
    public double getWidth() {
        return widget.getWidth();
    }

    @Override
    public void setWidth(double width) {
        widget.setWidth((float) width);
    }

    @Override
    public double getHeight() {
        return widget.getHeight();
    }

    @Override
    public void setHeight(double height) {
        widget.setHeight((float) height);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
