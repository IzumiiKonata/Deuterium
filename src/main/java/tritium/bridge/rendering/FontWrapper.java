package tritium.bridge.rendering;

import today.opai.api.interfaces.render.Font;
import tritium.rendering.font.CFontRenderer;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 17:12
 */
public class FontWrapper implements Font {

    private final CFontRenderer font;

    public FontWrapper(CFontRenderer font) {
        this.font = font;
    }

    @Override
    public void close() {
        font.close();
    }

    @Override
    public float drawString(String text, double x, double y, int color) {
        return font.drawString(text, x, y, color);
    }

    @Override
    public float drawStringWithShadow(String text, double x, double y, int color) {
        font.drawStringWithShadow(text, x, y, color);
        return (float) font.getStringWidthD(text);
    }

    @Override
    public float drawCenteredString(String text, double x, double y, int color) {
        font.drawCenteredString(text, x, y, color);
        return (float) font.getStringWidthD(text);
    }

    @Override
    public void drawCenteredStringWithShadow(String text, double x, double y, int color) {
        font.drawCenteredStringWithShadow(text, x, y, color);
    }

    @Override
    public int getWidth(String text) {
        return font.getWidth(text);
    }

    @Override
    public int getHeight() {
        return font.getHeight();
    }
}
