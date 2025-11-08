/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package tritium.screens.alt;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;
import tritium.utils.oauth.OAuth;

import java.io.IOException;

public class GuiAltLogin
        extends GuiScreen {
    private final GuiScreen previousScreen;
    private GuiTextField username;
    private OAuth oAuth;
    private String status = "§eWaiting...";

    public GuiAltLogin(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                this.mc.displayGuiScreen(this.previousScreen);
                break;
            case 0:
                Session session = new Session(this.username.getText(), "", "", "mojang");
                mc.setSession(session);
                // TODO
                break;
            case 2:
                oAuth = new OAuth();
                oAuth.logIn(
                        new OAuth.LoginCallback() {
                            @Override
                            public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                                var session = new Session(userName, uuid, token, "mojang");
                                mc.setSession(session);
                                status = "§aLogged in. (" + userName + ")";
                            }

                            @Override
                            public void onFailed(Exception e) {

                            }

                            @Override
                            public void setStatus(String status) {
                                GuiAltLogin.this.status = status;
                            }
                        }
                );
                break;
        }
    }

    @Override
    public void drawScreen(int x, int y, float z) {
        this.drawDefaultBackground();
        this.username.drawTextBox();
        mc.fontRendererObj.drawCenteredString("Alt Login", this.width / 2, 20, -1);
        mc.fontRendererObj.drawCenteredString(status, this.width / 2, 29, -1);
        if (this.username.getText().isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow("Username / E-Mail", this.width / 2 - 96, 66.0f, -7829368);
        }

        super.drawScreen(x, y, z);
    }

    @Override
    public void initGui() {
        int var3 = this.height / 4 + 24;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, var3 + 72 + 12, "Login"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, var3 + 72 + 12 + 24, "Back"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 140 + 16, "Microsoft Login"));
        this.username = new GuiTextField(1, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.username.setFocused(true);
        this.username.setMaxStringLength(200);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void keyTyped(char character, int key) {
        try {
            super.keyTyped(character, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (character == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
        this.username.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        try {
            super.mouseClicked(x, y, button);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username.mouseClicked(x, y, button);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
    }
}

