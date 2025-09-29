package tech.konata.phosphate.screens.multiplayer.dialog.dialogs;

import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.screens.dialog.Dialog;
import tech.konata.phosphate.screens.multiplayer.ZephyrMultiPlayerUI;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerInfoDialog extends Dialog implements SharedRenderingConstants {

    private double openCloseScale = 1.1;

    private final tech.konata.phosphate.rendering.entities.impl.TextField name = new tech.konata.phosphate.rendering.entities.impl.TextField(-1, 0, 0, 0, 0);
    private final tech.konata.phosphate.rendering.entities.impl.TextField address = new tech.konata.phosphate.rendering.entities.impl.TextField(-2, 0, 0, 0,  0);

    private int resourcePackMode = 2;

    private final int index;

    public ServerInfoDialog() {
        name.setDrawLineUnder(false);
        name.setPlaceholder("Name");
        name.width = 100;
        name.setText("Minecraft Server");
        name.setFontRenderer(FontManager.pf20);
        name.setDisabledTextColour(RenderSystem.hexColor(96, 96, 96));
        name.enabledColor = RenderSystem.hexColor(0, 0, 0);
        address.setDrawLineUnder(false);
        address.width = 100;
        address.setPlaceholder("Address");
        address.setFontRenderer(FontManager.pf20);
        address.setDisabledTextColour(RenderSystem.hexColor(96, 96, 96));
        address.enabledColor = RenderSystem.hexColor(0, 0, 0);

        this.index = -1;
    }

    public ServerInfoDialog(ServerData data, int index) {
        name.setDrawLineUnder(false);
        name.setPlaceholder("Name");
        name.setFontRenderer(FontManager.pf20);
        name.setDisabledTextColour(RenderSystem.hexColor(96, 96, 96));
        name.enabledColor = RenderSystem.hexColor(0, 0, 0);
        name.width = 100;
        name.setText(data.serverName);
        address.setDrawLineUnder(false);
        address.setPlaceholder("Address");
        address.setFontRenderer(FontManager.pf20);
        address.setDisabledTextColour(RenderSystem.hexColor(96, 96, 96));
        address.enabledColor = RenderSystem.hexColor(0, 0, 0);
        address.width = 100;
        address.setText(data.serverIP);

        this.index = index;
        this.resourcePackMode = data.getResourceMode().ordinal();
    }

    Localizable lServerName = Localizable.of("serverinfodialog.servername");
    Localizable lServerAddr = Localizable.of("serverinfodialog.serveraddress");
    Localizable lServerResPack = Localizable.of("serverinfodialog.serverresourcepack");

    Localizable lPrompt = Localizable.of("serverinfodialog.serverresourcepack.prompt");
    Localizable lEnabled = Localizable.of("serverinfodialog.serverresourcepack.enabled");
    Localizable lDisabled = Localizable.of("serverinfodialog.serverresourcepack.disabled");

    Localizable lDone = Localizable.of("serverinfodialog.done");
    Localizable lCancel = Localizable.of("serverinfodialog.cancel");

    @Override
    public void render(double mouseX, double mouseY) {
        super.drawBackgroundMask();

        this.openCloseScale = Interpolations.interpBezier(this.openCloseScale, this.isClosing() ? 1.1 : 1, 0.3);
        CFontRenderer titleRenderer = FontManager.pf25;

        int intAlpha = (int) (this.alpha * 255);

        double width = 400;
        double height = 226;
        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(ShaderRenderType.OVERLAY, Collections.singletonList(() -> {
            roundedRect(x, y, width, height, 16, new Color(255, 255, 255, intAlpha));
        }));

        this.doGlPreTransforms(this.openCloseScale);

        roundedRect(x, y, width, height, 16, new Color(0, 0, 0, (int) (intAlpha * 0.3)));

        titleRenderer.drawString(lServerName.get(), x + 32, y + 25, RenderSystem.hexColor(233, 233, 233, intAlpha));

        roundedRect(x + 30, y + 25 + 7 + titleRenderer.getHeight(), width - 60, 24, 8, new Color(133, 133, 133, (int) (intAlpha * 0.4)));

        name.xPosition = (float) (x + 40);
        name.yPosition = (float) (y + 37 + titleRenderer.getHeight());
        if (name.isFocused())
            name.onTick();
        name.width = (float) (width - 80);
        name.height = 14;
        name.setDisabledTextColour(RenderSystem.hexColor(188, 188, 188, intAlpha));
        name.setTextColor(RenderSystem.hexColor(228, 228, 228, intAlpha));
        name.drawTextBox((int) mouseX, (int) mouseY);

        titleRenderer.drawString(lServerAddr.get(), x + 32, y + 32 + 24 + 15 + titleRenderer.getHeight(), RenderSystem.hexColor(233, 233, 233, intAlpha));

        roundedRect(x + 30, y + 32 + 24 + 15 + 7 + titleRenderer.getHeight() * 2, width - 60, 24, 8, new Color(133, 133, 133, (int) (intAlpha * 0.4)));

        address.xPosition = (float) (x + 40);
        address.yPosition = (float) (y + 32 + 24 + 15 + 7 + 5 + titleRenderer.getHeight() * 2);
        if (address.isFocused())
            address.onTick();
        address.width = (float) (width - 80);
        address.height = 14;
        address.setDisabledTextColour(RenderSystem.hexColor(188, 188, 188, intAlpha));
        address.setTextColor(RenderSystem.hexColor(228, 228, 228, intAlpha));
        address.drawTextBox((int) mouseX, (int) mouseY);

        titleRenderer.drawString(lServerResPack.get(), x + 32, y + 32 + 24 + 15 + 46 + 14 + titleRenderer.getHeight(), RenderSystem.hexColor(233, 233, 233, intAlpha));

        List<ResourceMode> options = Arrays.asList(new ResourceMode(lPrompt.get(), 2), new ResourceMode(lEnabled.get(), 0), new ResourceMode(lDisabled.get(), 1));

        CFontRenderer optionsRenderer = FontManager.pf16;

        double selectorWidth = 8;

        for (ResourceMode option : options) {
            selectorWidth += 9 + optionsRenderer.getStringWidth(option.name);
        }

        double selectorX = x + width - 30 - selectorWidth;
        double selectorY = y + 32 + 24 + 15 + 46 + 14 + titleRenderer.getHeight();
        double selectorHeight = 18;

        roundedRect(selectorX - 2, selectorY, selectorWidth + 1, selectorHeight, 5, new Color(100, 100, 100, (int) (intAlpha * 0.6)));

        double offsetX = selectorX + 4;
        for (ResourceMode option : options) {

            int indexOf = option.ordinary;

            if (indexOf == resourcePackMode) {
                roundedRect(offsetX - 3, selectorY + 2, 5 + optionsRenderer.getStringWidth(option.name), 14, 3, new Color(188, 188, 188, (int) (intAlpha * 0.4)));
            } else {
                if (RenderSystem.isHovered(mouseX, mouseY, offsetX - 3, selectorY + 1, 4 + optionsRenderer.getStringWidth(option.name), 14) && Mouse.isButtonDown(0) && !previousMouse) {
                    resourcePackMode = option.ordinary;
                    previousMouse = true;
                }
            }

            if (indexOf == 0 || indexOf == 2) {
                optionsRenderer.drawString("|", offsetX + 5 + optionsRenderer.getStringWidth(option.name), selectorY + selectorHeight * 0.5 - optionsRenderer.getHeight() * 0.5, RenderSystem.hexColor(177, 177, 177, intAlpha));
            }

            optionsRenderer.drawString(option.name, offsetX - 0.5, selectorY + selectorHeight * 0.5 - optionsRenderer.getHeight() * 0.5, RenderSystem.hexColor(233, 233, 233, intAlpha));

            offsetX += 12 + optionsRenderer.getStringWidth(option.name);
        }

        //Done button

        roundedRect(x + 30, y + height - 46, (width - 68) * 0.5, 20, 3, (!name.getText().isEmpty() && !address.getText().isEmpty()) ? new Color(94, 169, 255, (int) (intAlpha * 0.6)) : new Color(190, 190, 190, (int) (intAlpha * 0.6)));

        FontManager.pf20.drawCenteredString(lDone.get(), x + 30 + (width - 68) * 0.25, y + height - 36.5 - FontManager.pf20.getHeight() * 0.5, RenderSystem.hexColor(255, 255, 255, intAlpha));

        if (RenderSystem.isHovered(mouseX, mouseY, x + 30, y + height - 46, (width - 68) * 0.5, 20) && Mouse.isButtonDown(0) && !previousMouse) {
            previousMouse = true;

            if (!name.getText().isEmpty() && !address.getText().isEmpty()) {
                ServerData data = new ServerData(name.getText(), address.getText(), false);
                data.setResourceMode(ServerData.ServerResourceMode.values()[resourcePackMode]);

                if (index == -1)
                    ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).serverList.addServerData(data);
                else
                    ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).serverList.setServer(this.index, data);
                ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).serverList.saveServerList();
                ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).addServers();

                this.close();
            }

        }

        //Cancel button

        roundedRect(x + (width - 68) * 0.5 + 38, y + height - 46, (width - 68) * 0.5, 20, 3, new Color(133, 133, 133, (int) (intAlpha * 0.6)));

        FontManager.pf20.drawCenteredString(lCancel.get(), x + 34 + (width - 60) * 0.75, y + height - 36.5 - FontManager.pf20.getHeight() * 0.5, RenderSystem.hexColor(233, 233, 233, intAlpha));

        if (RenderSystem.isHovered(mouseX, mouseY, x + (width - 68) * 0.5 + 38, y + height - 46, (width - 68) * 0.5, 20) && Mouse.isButtonDown(0) && !previousMouse) {
            previousMouse = true;
            this.close();
        }

        this.disposeTransforms();

        if (!Mouse.isButtonDown(0) && previousMouse) {
            previousMouse = false;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
            this.close();
    }

    @AllArgsConstructor
    private static class ResourceMode {
        String name;
        int ordinary;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.name.textboxKeyTyped(typedChar, keyCode);
        this.address.textboxKeyTyped(typedChar, keyCode);

        if (address.isFocused() && (keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) && (!name.getText().isEmpty() && !address.getText().isEmpty())) {
            ServerData data = new ServerData(name.getText(), address.getText(), false);
            data.setResourceMode(ServerData.ServerResourceMode.values()[resourcePackMode]);

            if (index == -1)
                ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).serverList.addServerData(data);
            else
                ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).serverList.setServer(this.index, data);
            ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).serverList.saveServerList();
            ((ZephyrMultiPlayerUI) Minecraft.getMinecraft().currentScreen).addServers();

            this.close();
        }

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.name.mouseClicked(mouseX, mouseY, mouseButton);
        this.address.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int state) {
        this.name.mouseReleased(mouseX, mouseY, state);
        this.address.mouseReleased(mouseX, mouseY, state);
    }
}
