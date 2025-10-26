/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
package tritium.screens.alt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.utils.alt.Alt;
import tritium.utils.alt.AltLoginThread;
import tritium.utils.alt.AltManager;
import tritium.rendering.rendersystem.RenderSystem;

import java.io.IOException;
import java.util.Random;

public class GuiAltManager
        extends GuiScreen {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final GuiScreen parentScreen;
    public Alt selectedAlt = null;
    private GuiButton login;
    private GuiButton remove;
    private AltLoginThread loginThread;
    private int offset;
    private String status = "§eWaiting...";

    public GuiAltManager(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if (this.loginThread == null) {
                    mc.displayGuiScreen(this.parentScreen);
                    break;
                }
                if (!this.loginThread.isFinished()) {
                    mc.displayGuiScreen(this.parentScreen);
                    break;
                }
                this.loginThread.setStatus("Do not hit back! Logging in...");
                break;
            case 1:
                this.loginThread = new AltLoginThread(this.selectedAlt);
                this.loginThread.start();
                break;
            case 2:
                if (this.loginThread != null) {
                    this.loginThread = null;
                }

                AltManager.getAlts().remove(this.selectedAlt);
                this.status = "§cRemoved.";
                this.selectedAlt = null;
                break;
            case 3:
                mc.displayGuiScreen(new GuiAddAlt(this));
                break;
            case 4:
                mc.displayGuiScreen(new GuiAltLogin(this));
                break;
            case 5:
                if (AltManager.alts.size() == 0) {
                    this.status = "\247cYou must have 1 alt!";
                    break;
                }
                Alt randomAlt = AltManager.alts.get(new Random().nextInt(AltManager.alts.size()));
                this.loginThread = new AltLoginThread(randomAlt);
                this.loginThread.start();
                break;
            case 7:
                Alt lastAlt = AltManager.lastAlt;
                if (lastAlt == null) {
                    if (this.loginThread == null) {
                        this.status = "?cThere is no last used alt!";
                        break;
                    }
                    this.loginThread.setStatus("?cThere is no last used alt!");
                    break;
                }

                this.loginThread = new AltLoginThread(lastAlt);
                this.loginThread.start();
                break;
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        int wheel = Mouse.getDWheel();
        if (wheel < 0) {
            this.offset += 26;
            if (this.offset < 0) {
                this.offset = 0;
            }
        } else if (wheel > 0) {
            this.offset -= 26;
            if (this.offset < 0) {
                this.offset = 0;
            }
        }
        this.drawDefaultBackground();
        mc.fontRendererObj.drawStringWithShadow(GuiAltManager.mc.getSession().getUsername(), 10, 10, -7829368);

        mc.fontRendererObj.drawCenteredString("Account Manager - " + AltManager.getAlts().size() + " alts", this.width / 2.0f, 10, -1);
        mc.fontRendererObj.drawCenteredString(this.loginThread == null ? this.status : this.loginThread.getStatus(), this.width / 2.0f, 20, -1);
        GL11.glPushMatrix();
        this.prepareScissorBox(0, 33, this.width, this.height - 50);
        GL11.glEnable(3089);
        int y = 38;

        for (Alt alt : AltManager.getAlts()) {
            String pass;
            if (!this.isAltInArea(y)) continue;
            String name = alt.getUsername();
            pass = alt.isMicrosoft() ? "Microsoft Account" + (alt.isExpired() ? " (Expired)" : " (Expiring in " + alt.getLeftExpiringTime() + " seconds.)") : "§cCracked";
            if (alt == this.selectedAlt) {
                if (this.isMouseOverAlt(par1, par2, y - this.offset) && Mouse.isButtonDown(0)) {
                    RenderSystem.drawBorderedRect(52, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1, -16777216, -2142943931);
                } else if (this.isMouseOverAlt(par1, par2, y - this.offset)) {
                    RenderSystem.drawBorderedRect(52, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1, -16777216, -2142088622);
                } else {
                    RenderSystem.drawBorderedRect(52, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1, -16777216, -2144259791);
                }
            } else if (this.isMouseOverAlt(par1, par2, y - this.offset) && Mouse.isButtonDown(0)) {
                RenderSystem.drawBorderedRect(52, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1, -16777216, -2146101995);
            } else if (this.isMouseOverAlt(par1, par2, y - this.offset)) {
                RenderSystem.drawBorderedRect(52, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1, -16777216, -2145180893);
            }
            mc.fontRendererObj.drawCenteredString(name, this.width / 2.0f, y - this.offset, -1);
            mc.fontRendererObj.drawCenteredString(pass, this.width / 2.0f, y - this.offset + 10, 0x555555);
            y += 26;
        }
        GL11.glDisable(3089);
        GL11.glPopMatrix();
        super.drawScreen(par1, par2, par3);
        if (this.selectedAlt == null) {
            this.login.enabled = false;
            this.remove.enabled = false;
        } else {
            this.login.enabled = true;
            this.remove.enabled = true;
        }
        if (Keyboard.isKeyDown(200)) {
            this.offset -= 26;
            if (this.offset < 0) {
                this.offset = 0;
            }
        } else if (Keyboard.isKeyDown(208)) {
            this.offset += 26;
            if (this.offset < 0) {
                this.offset = 0;
            }
        }
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 76, this.height - 24, 75, 20, "Cancel"));
        this.login = new GuiButton(1, this.width / 2 - 154, this.height - 48, 70, 20, "Login");
        this.buttonList.add(this.login);
        this.remove = new GuiButton(2, this.width / 2 - 74, this.height - 24, 70, 20, "Remove");
        this.buttonList.add(this.remove);
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 76, this.height - 48, 75, 20, "Add"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 74, this.height - 48, 70, 20, "Direct Login"));
        this.buttonList.add(new GuiButton(5, this.width / 2 + 4, this.height - 48, 70, 20, "Random"));
        this.login.enabled = false;
        this.remove.enabled = false;
    }

    private boolean isAltInArea(int y) {
        return y - this.offset <= this.height - 50;
    }

    private boolean isMouseOverAlt(int x, int y, int y1) {
        return x >= 52 && y >= y1 - 4 && x <= this.width - 52 && y <= y1 + 20 && y >= 33 && x <= this.width && y <= this.height - 50;
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        if (this.offset < 0) {
            this.offset = 0;
        }
        int y = 38 - this.offset;

        for (Alt alt : AltManager.getAlts()) {
            if (this.isMouseOverAlt(par1, par2, y)) {
                if (alt == this.selectedAlt) {
                    this.actionPerformed(this.buttonList.get(1));
                    return;
                }
                this.selectedAlt = alt;
            }
            y += 26;
        }
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareScissorBox(float x, float y, float x2, float y2) {
        int factor = ScaledResolution.get().getScaleFactor();
        GL11.glScissor((int) (x * (float) factor), (int) (((float) ScaledResolution.get().getScaledHeight() - y2) * (float) factor), (int) ((x2 - x) * (float) factor), (int) ((y2 - y) * (float) factor));
    }
}

