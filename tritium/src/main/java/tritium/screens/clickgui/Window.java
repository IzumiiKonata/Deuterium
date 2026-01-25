package tritium.screens.clickgui;

import lombok.Getter;
import lombok.Setter;
import tritium.screens.ClickGui;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 12:25
 */
public abstract class Window {

    @Getter
    @Setter
    private double x, y, width, height;

    public void init() {

    }

    public void render(double mouseX, double mouseY) {

    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {

    }

    public void setAlpha(float alpha) {

    }

    protected int getDWheel() {
        return ClickGui.getInstance().getDWheel();
    }

    protected int getColor(int type) {
        return ClickGui.getColor(type);
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

}
