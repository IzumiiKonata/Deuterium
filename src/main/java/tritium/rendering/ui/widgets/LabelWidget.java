package tritium.rendering.ui.widgets;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.LazyLoadBase;
import tritium.management.FontManager;
import tritium.rendering.entities.impl.ScrollText;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.ui.AbstractWidget;

import java.awt.*;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date: 2025/7/8 20:59
 */
public class LabelWidget extends AbstractWidget<LabelWidget> {

    Supplier<String> label = () -> "点击输入文字";
    @Getter
    CFontRenderer font = FontManager.pf18;

    @Getter
    @Setter
    double maxWidth = -1;

    LazyLoadBase<ScrollText> scrollText = new LazyLoadBase<ScrollText>() {
        @Override
        protected ScrollText load() {
            return new ScrollText();
        }
    };

    public LabelWidget(String label, CFontRenderer font) {
        this.setLabel(label);
        this.setFont(font);
    }

    public LabelWidget(Supplier<String> label, CFontRenderer font) {
        this.setLabel(label);
        this.setFont(font);
    }

    public LabelWidget(String label) {
        this.setLabel(label);
    }

    public LabelWidget(Supplier<String> label) {
        this.setLabel(label);
    }

    public LabelWidget() {

    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        boolean widthNotLimited = this.getMaxWidth() == -1;

        String lbl = this.getLabel();

        if (widthNotLimited)
            font.drawString(lbl, this.getX(), this.getY(), this.getHexColor());
        else
            this.scrollText.getValue().render(font, lbl, this.getX(), this.getY(), this.getMaxWidth(), this.getHexColor());

        double width = widthNotLimited ? font.getWidthDouble(lbl) : this.getMaxWidth();
        this.setBounds(width, font.getStringHeight(lbl));
    }

    @Override
    public void addChild(AbstractWidget<?>... child) {
        throw new UnsupportedOperationException("LabelWidget 不应该拥有子组件。");
    }

    public LabelWidget setFont(CFontRenderer font) {
        this.font = font;
        return this;
    }

    public String getLabel() {
        return label.get();
    }

    public LabelWidget setLabel(String label) {
        this.setLabel(() -> label);
        return this;
    }

    public LabelWidget setLabel(Supplier<String> label) {
        this.label = label;
        return this;
    }
}
