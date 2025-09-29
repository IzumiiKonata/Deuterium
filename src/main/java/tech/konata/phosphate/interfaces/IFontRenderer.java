package tech.konata.phosphate.interfaces;

/**
 * the interworking layer of the minecraft's font renderer and the client's font renderer
 *
 * @author IzumiiKonata
 * @since 6/20/2023 9:50 AM
 */
public interface IFontRenderer {

    float drawString(String text, double x, double y, int color);

    int drawStringWithShadow(String text, double x, double y, int color);

    int getHeight();

    int getStringWidth(String text);

    void drawCenteredString(String text, double x, double y, int color);

}
