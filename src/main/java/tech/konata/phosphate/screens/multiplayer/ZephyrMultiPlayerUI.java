package tech.konata.phosphate.screens.multiplayer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.Localizer;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.CheckRenderer;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.TransitionAnimation;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.rendering.shader.StencilShader;
import tech.konata.phosphate.screens.BaseScreen;
import tech.konata.phosphate.screens.MainMenu;
import tech.konata.phosphate.screens.dialog.Dialog;
import tech.konata.phosphate.screens.multiplayer.dialog.dialogs.ActionsDialog;
import tech.konata.phosphate.screens.multiplayer.dialog.dialogs.ServerInfoDialog;
import tech.konata.phosphate.utils.timing.Timer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ZephyrMultiPlayerUI extends BaseScreen {

    private final GuiScreen parentScreen;
    public final List<ServerBean> serverBeans = new ArrayList<>();
    public final ThreadPoolExecutor pingers = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    public final OldServerPinger oldServerPinger = new ModifiedServerPinger();
    public ServerList serverList;

    public final List<TexturedButton> buttons = new ArrayList<>();

    public boolean deleteMode = false;

    public Dialog dialog = null;

    public ZephyrMultiPlayerUI(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    private double scrollOffset = 0, scrollSmooth = 0;

    public void addServers() {
        serverList = new ServerList(mc);
        serverList.loadServerList();

        this.serverBeans.clear();

        for (ServerData data : serverList.getServers()) {
            this.serverBeans.add(new ServerBean(data));
        }
    }

    @Override
    public void initGui() {
        this.buttons.clear();

        this.addServers();

        List<Tuple<Location, Runnable>> b = new ArrayList<>();

        b.add(new Tuple<>(Location.of(Phosphate.NAME + "/textures/multiplayer/add.png"), () -> {
            this.dialog = new ServerInfoDialog();
        }));

        b.add(new Tuple<>(Location.of(Phosphate.NAME + "/textures/multiplayer/remove.png"), () -> {
            if (!deleteMode) {
                deleteMode = true;

                for (ServerBean serverBean : this.serverBeans) {
                    serverBean.selected = false;
                }
            } else {

                boolean isAtLeastOneServerSelected = this.serverBeans.stream().anyMatch(s -> s.selected);

                if (isAtLeastOneServerSelected) {

                    Tuple<String, Runnable> yes = ActionsDialog.buildAction(Localizer.getInstance().translate("dialog.confirmlogout.confirm.name"), () -> {
                            deleteMode = false;

                            for (ServerBean serverBean : this.serverBeans.stream().filter(s -> s.selected).collect(Collectors.toList())) {
                                int index = this.serverList.getServers().indexOf(serverBean.getServer());

                                this.serverList.removeServerData(index);
                                this.serverList.saveServerList();
                                this.addServers();

                            }
                    });

                    Tuple<String, Runnable> no = ActionsDialog.buildAction(Localizer.getInstance().translate("dialog.confirmlogout.cancel.name"), () -> {
                        deleteMode = false;

                        for (ServerBean serverBean : this.serverBeans) {
                            serverBean.selected = false;
                        }

                    });



                    this.dialog = new ActionsDialog(Localizer.getInstance().translate("dialog.deleteservers.title"), Localizer.getInstance().translate("dialog.deleteservers.content"), Arrays.asList(yes, no));
                } else {
                    deleteMode = false;
                }

            }
        }));

        b.add(new Tuple<>(Location.of(Phosphate.NAME + "/textures/multiplayer/refresh.png"), this::addServers));
        b.add(new Tuple<>(Location.of(Phosphate.NAME + "/textures/multiplayer/back.png"), () -> {
            deleteMode = false;
        }));


        double imgSize = 60, spacing = -15;
        double startX = RenderSystem.getWidth() * 0.5 - imgSize * 1.5 - spacing;
        long delay = 0;

        for (Tuple<Location, Runnable> tuple : b) {

            if (tuple.getFirst().getResourcePath().substring(tuple.getFirst().getResourcePath().lastIndexOf("/") + 1, tuple.getFirst().getResourcePath().lastIndexOf(".")).equals("back")) {
                this.buttons.add(new TexturedButton(tuple.getFirst(), RenderSystem.getWidth() * 0.5 - imgSize * 1.5 - spacing, 0, imgSize, imgSize, 60, tuple.getSecond()));
                continue;
            }

            this.buttons.add(new TexturedButton(tuple.getFirst(), startX, 0, imgSize, imgSize, delay, tuple.getSecond()));

            startX += imgSize + spacing;
            delay += 60;
        }

        scrollOffset = scrollSmooth = 0;

        this.dWheelTimer.reset();
    }

    List<Runnable> bloom = new ArrayList<>();

    Timer dWheelTimer = new Timer();

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        MainMenu.getInstance().renderBackground();

        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(ShaderRenderType.OVERLAY, Collections.singletonList(() -> {
            Rect.draw(0, 0, this.getWidth(), this.getHeight(), -1, Rect.RectType.EXPAND);
        }));

        float phosphate = FontManager.pf40.drawString(Phosphate.NAME + "", 16, 9, RenderSystem.hexColor(255, 255, 255));
        FontManager.pf18.drawString("Server List", 16 + phosphate + 4, 20, RenderSystem.hexColor(233, 233, 233));

        int dWheel = Mouse.getDWheel2();

        double yAdd = 8;

        if (dWheelTimer.isDelayed(100)) {

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                yAdd *= 2;

            if (dWheel > 0)
                scrollSmooth -= yAdd;
            else if (dWheel != 0)
                scrollSmooth += yAdd;
        }

        scrollSmooth = Interpolations.interpBezier(scrollSmooth, 0, 0.1f);
        scrollOffset = Interpolations.interpBezier(scrollOffset, scrollOffset + scrollSmooth, 0.6f);

        if (scrollOffset < 0)
            scrollOffset = Interpolations.interpBezier(scrollOffset, 0, 0.3f);

        int maskX = 10, maskY = 38;

        Stencil.write();
        Rect.draw(maskX, maskY, RenderSystem.getWidth(), RenderSystem.getHeight(), -1, Rect.RectType.EXPAND);
        Stencil.erase();

        double offsetX = 15, offsetY = 42 - scrollOffset;
        double width = 223, height = 80;
        double xSpace = 12, ySpace = 15;

        int count = 0;
        int horizontalLength = (int) ((RenderSystem.getWidth()) / (width + xSpace)) - 1;

        int lines = 0;

        bloom.clear();

        for (int i = 0; i < this.serverBeans.size(); i++) {
            ServerBean serverBean = this.serverBeans.get(i);
            serverBean.draw(offsetX, offsetY, width, height, mouseX, mouseY, this);

            offsetX += width + xSpace;


            if (count == horizontalLength && i != this.serverBeans.size() - 1) {
                count = 0;
                offsetX = 15;
                offsetY += ySpace + height;
                lines ++;
            } else {
                ++count;
            }
        }

        Shaders.POST_BLOOM_SHADER.runNoCaching(ShaderRenderType.OVERLAY, bloom);

        double max = (lines) * (height + ySpace);

        if (scrollOffset > max)
            scrollOffset = Interpolations.interpBezier(scrollOffset, max, 0.3f);

        Stencil.dispose();

        for (TexturedButton button : this.buttons) {
            button.draw(mouseX, mouseY, this);
        }

        if (this.dialog != null) {
            this.dialog.render(mouseX, mouseY);

            if (this.dialog.canClose()) {
                this.dialog = null;
            }
        }

    }


    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {

            if (this.dialog != null) {
                return;
            }

            TransitionAnimation.task(() -> mc.displayGuiScreen(this.parentScreen));


        }

        if (this.dialog != null)
            this.dialog.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.dialog != null)
            this.dialog.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (this.dialog != null)
            this.dialog.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
