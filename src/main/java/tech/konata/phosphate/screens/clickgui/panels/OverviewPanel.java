package tech.konata.phosphate.screens.clickgui.panels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.Location;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.clickgui.Panel;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OverviewPanel extends Panel {

    public OverviewPanel() {
        super("Overview");
    }

    @Override
    public void init() {
        this.onSwitchedTo();
    }

    @Override
    public void onSwitchedTo() {
        randomTitle = this.chooseRandom();
    }

    Minecraft mc = Minecraft.getMinecraft();

    String randomTitle = this.chooseRandom();

    private String chooseRandom() {

        List<String> randomStrings = Arrays.asList(
                Phosphate.NAME + " Client",
                "磷酸盐客户端",
                "我不会设计ui啊 别魔怔我"
//                "你的代码里引用了GL11 我就不能引用任何GL11了 你让我把抄的代码删除 我是不是要把 GlStateManager也删了",
//                "我开这个网页 因为 我喜欢你在背后骂我的样子",
//                "我的modernhalo跟你有什么关系吗",
//                "哦 我看见了 用了GlStateManager 你的专利 不被允许的",
//                "比你晚就是抄你的?",
//                "你给我屏幕买摄像头了? 看见我抄了 笑死我了 我只要写个像的 就是抄了 那你自己去翻我的代码",
//                "Stars只是 在我发布了代码之后 写出了和我极其相似的代码而已!"
        );

        return randomStrings.get(Math.abs(new Random().nextInt() % randomStrings.size()));
    }

    ScrollText stTitle = new ScrollText();

    double scroll = 0, ySmooth = 0;

    @Override
    public void draw(double mouseX, double mouseY, int dWheel) {

        FontManager.pf25bold.drawString(this.getName().get(), posX + 4, posY + 2, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double x = posX + 4, y = posY + 8 + FontManager.pf25bold.getHeight();

        this.roundedRect(x, y, 200, 84, 8, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        stTitle.render(FontManager.pf25bold, randomTitle, x + 8, y + 8, 184, ThemeManager.get(ThemeManager.ThemeColor.Text));

//        FontManager.pf25bold.drawString(randomTitle, x + 8, y + 8, ThemeManager.get(ThemeManager.ThemeColor.Text));

        Render<Entity> entityRender = mc.getRenderManager().getEntityRenderObject(mc.thePlayer);
        Location skinLoc = entityRender.getTexture(mc.thePlayer);

        mc.getTextureManager().bindTexture(skinLoc);

        double wtf = 1 / 64.0;
        roundedRectTextured(x + 8, y + 14 + FontManager.pf25bold.getHeight(), 50, 50, 8 * wtf, 8 * wtf, 16 * wtf, 16 * wtf, 8);
        if (mc.thePlayer.isWearing(EnumPlayerModelParts.HAT)) {
            roundedRectTextured(x + 8, y + 14 + FontManager.pf25bold.getHeight(), 50, 50, 40 * wtf, 8 * wtf, 48 * wtf, 16 * wtf, 8);
        }

//        roundedRectTextured(x + 8, y + 14 + FontManager.pf25bold.getHeight(), 48, 48, 8);

        FontManager.pf25bold.drawString(Minecraft.getMinecraft().getSession().getUsername(), x + 64, y + 30, ThemeManager.get(ThemeManager.ThemeColor.Text));

        this.roundedRect(x, y + 92, 200, height - 30 - FontManager.pf25bold.getHeight() - 76, 8, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        double yAdd = 5;
        if (dWheel > 0)
            ySmooth -= yAdd;
        else if (dWheel < 0)
            ySmooth += yAdd;

        ySmooth = Interpolations.interpBezier(ySmooth, 0, 0.1f);
        scroll = Interpolations.interpBezier(scroll, scroll + ySmooth, 0.6f);

        if (scroll < 0)
            scroll = Interpolations.interpBezier(scroll, 0, 0.2f);

        FontManager.pf25bold.drawString(lChangeLog.get() + ": " + Phosphate.BUILD_DATE, x + 8, y + 100, ThemeManager.get(ThemeManager.ThemeColor.Text));

        RenderSystem.doScissor(x, y + 116, 200, height - 54 - FontManager.pf25bold.getHeight() - 76);

        double offsetX = x + 8;
        double offsetY = y + 108 + FontManager.pf25bold.getHeight() - scroll;

        double cWidth = 46;
        double cHeight = 18;

        CFontRenderer fr = FontManager.pf20;

        for (ChangeLog c : this.changeLogs) {

            roundedRect(offsetX, offsetY, cWidth, cHeight, 5, c.type.getColor());

            FontManager.pf20bold.drawCenteredString(c.getType().translateKey.get(), offsetX + cWidth * 0.5, offsetY + cHeight * 0.5 - FontManager.pf20bold.getHeight() * 0.5, -1);

//            fr.drawString(c.content, offsetX + cWidth + 6, offsetY + cHeight * 0.5 - FontManager.segoe20.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

            c.st.render(fr, c.content, offsetX + cWidth + 6, offsetY + cHeight * 0.5 - fr.getHeight() * 0.5, cWidth + 86, ThemeManager.get(ThemeManager.ThemeColor.Text));

            //cWidth + 90

            offsetY += cHeight + 8;

        }

        RenderSystem.endScissor();
    }

    List<ChangeLog> changeLogs = Arrays.asList(
            new ChangeLog(ChangeLog.Type.Added, "复活"),
            new ChangeLog(ChangeLog.Type.Misc, "如果你还在使用这个客户端的话请添加 617399255"),
            new ChangeLog(ChangeLog.Type.Fixed, "修复网易云音乐")
//            new ChangeLog(ChangeLog.Type.Improved, "减少播放音乐时的内存占用"),
//            new ChangeLog(ChangeLog.Type.Improved, "增加文字可读性")

//        new ChangeLog(ChangeLog.Type.Fixed, "修复 .bind"),
//        new ChangeLog(ChangeLog.Type.Fixed, "修复 插件的 Render3DEvent 调用")
    );

    @Getter
    @RequiredArgsConstructor
    private static class ChangeLog {

        private final Type type;
        private final String content;

        ScrollText st = new ScrollText();

        @Getter
        public enum Type {
            Added(new Color(68, 200, 129)),
            Fixed(new Color(220, 220, 68)),
            Removed(new Color(194, 37, 37)),
            Improved(new Color(23, 124, 239)),
            Misc(new Color(97, 90, 90, 255));

            private final Color color;
            private final Localizable translateKey;

            Type(Color color) {
                this.color = color;
                this.translateKey = Localizable.of("panel.overview." + this.name().toLowerCase() + ".name");
            }
        }

    }


    Localizable lChangeLog = Localizable.of("panel.overview.changelog");

}
