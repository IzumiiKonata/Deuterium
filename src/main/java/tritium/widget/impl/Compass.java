package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import net.minecraft.util.MathHelper;
import tritium.Tritium;
import tritium.management.FontManager;
import tritium.rendering.Stencil;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.settings.NumberSetting;
import tritium.widget.Widget;

/**
 * @author IzumiiKonata
 * @since 2024/11/3 11:51
 */
public class Compass extends Widget {

    public Compass() {
        super("Compass");
    }

    public final NumberSetting<Integer> width = new NumberSetting<>("Width", 250, 50, 450, 1);

    @Override
    public void onRender(boolean editing) {
        int width = this.width.getValue();
        double height = 40;

//        RenderUtils.renderMarker((this.getX() + this.getX() + width) / 2, this.getY() + 2.5F, ThemeManager.get(ThemeManager.ThemeColor.Text));

//        StencilUtils.initStencilToWrite();
//        Rect.draw(this.getX(), this.getY(), width, 28, -1);
//        StencilUtils.readStencilBuffer(1);

        NORMAL.add(() -> {

            GlStateManager.pushMatrix();
            this.doScale();

            this.renderStyledBackground(this.getX(), this.getY(), width, height, 8);

            double angle = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
            double angle2 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
            double angle3 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
            double angle4 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
            double angle5 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
            double angle6 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;

            double lineHeight = 9;
            double longLineHeight = 14;
            double axisYOffset = 24;
            double axis2YOffset = 9;
            double degreeYOffset = 20;

            Image.draw(Location.of(Tritium.NAME + "/textures/triangle.png"), this.getX() + width * 0.5 - 5, this.getY() + height - 7, 8, 4.5, Image.Type.Normal);

            Stencil.write();
            double shrink = 2;
            Rect.draw(this.getX() + shrink, this.getY() + shrink, width - shrink * 2, height - shrink * 2, -1, Rect.RectType.EXPAND);
            Stencil.erase();

            for(int i = 0; i<=1; i++) {
                for(double d = 0.0D; d<= 1.5D; d+=0.5D) {

                    String s = "W";

                    if(d == 0.0D) {
                        s = "S";
                    }

                    if(d == 1.0D) {
                        s = "N";
                    }

                    if(d == 1.5D) {
                        s = "E";
                    }

                    Rect.draw(this.getX() + (width * 0.5) + angle - 2F, this.getY() + 8, 1, longLineHeight, -1, Rect.RectType.EXPAND);

                    Rect.draw(this.getX() + (width * 0.5) + angle + 12F, this.getY() + 8, 1, lineHeight, -1, Rect.RectType.EXPAND);
                    Rect.draw(this.getX() + (width * 0.5) + angle + 26F, this.getY() + 8, 1, lineHeight, -1, Rect.RectType.EXPAND);

                    Rect.draw(this.getX() + (width * 0.5) + angle - 16F, this.getY() + 8, 1, lineHeight, -1, Rect.RectType.EXPAND);
                    Rect.draw(this.getX() + (width * 0.5) + angle - 30F, this.getY() + 8, 1, lineHeight, -1, Rect.RectType.EXPAND);

                    FontManager.pf18bold.drawCenteredString(s, this.getX() + (width * 0.5) + angle - 1.5F, this.getY() + axisYOffset, -1);

                    angle += 90;
                }

                for(double d = 0.0D; d<= 1.5D; d+=0.5D) {

                    String s = "NW";

                    if(d == 0.0D) {
                        s = "SW";
                    }

                    if(d == 1.0D) {
                        s = "NE";
                    }

                    if(d == 1.5D) {
                        s = "SE";
                    }


                    FontManager.pf16.drawCenteredString(s, this.getX() + (width * 0.5) + angle2 + 43F, this.getY() + axis2YOffset, -1);

                    angle2+= 90;
                }

                for(double d = 0.0D; d<= 1.5D; d+=0.5D) {

                    String s = "105";

                    if(d == 0.0D) {
                        s = "15";
                    }

                    if(d == 1.0D) {
                        s = "195";
                    }

                    if(d == 1.5D) {
                        s = "285";
                    }

                    FontManager.pf12.drawCenteredString(s, this.getX() + (width * 0.5) + angle3 + 13F, this.getY() + degreeYOffset, -1);

                    angle3+= 90;
                }

                for(double d = 0.0D; d<= 1.5D; d+=0.5D) {

                    String s = "120";

                    if(d == 0.0D) {
                        s = "30";
                    }

                    if(d == 1.0D) {
                        s = "210";
                    }

                    if(d == 1.5D) {
                        s = "300";
                    }

                    FontManager.pf12.drawCenteredString(s, this.getX() + (width * 0.5) + angle4 + 27F, this.getY() + degreeYOffset, -1);


                    angle4+= 90;
                }

                for(double d = 0.0D; d<= 1.5D; d+=0.5D) {

                    String s = "150";

                    if(d == 0.0D) {
                        s = "60";
                    }

                    if(d == 1.0D) {
                        s = "240";
                    }

                    if(d == 1.5D) {
                        s = "300";
                    }

                    FontManager.pf12.drawCenteredString(s, this.getX() + (width * 0.5) + angle5 + 60.5F, this.getY() + degreeYOffset, -1);


                    angle5 += 90;
                }

                for(double d = 0.0D; d<= 1.5D; d+=0.5D) {

                    String s = "165";

                    if(d == 0.0D) {
                        s = "70";
                    }

                    if(d == 1.0D) {
                        s = "255";
                    }

                    if(d == 1.5D) {
                        s = "345";
                    }

                    FontManager.pf12.drawCenteredString(s, this.getX() + (width * 0.5) + angle6 + 74.5F, this.getY() + degreeYOffset, -1);

                    angle6 += 90;
                }
            }

            Stencil.dispose();

            GlStateManager.popMatrix();
        });

        this.setWidth(width);
        this.setHeight(height);
    }
}
