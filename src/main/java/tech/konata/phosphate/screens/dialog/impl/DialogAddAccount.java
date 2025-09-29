package tech.konata.phosphate.screens.dialog.impl;

import net.minecraft.util.EnumChatFormatting;
import tech.konata.phosphate.utils.alt.Alt;
import tech.konata.phosphate.utils.alt.AltManager;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.entities.impl.TextField;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.altmanager.AltScreen;
import tech.konata.phosphate.screens.dialog.Dialog;
import tech.konata.phosphate.screens.dialog.DialogButton;
import tech.konata.phosphate.utils.oauth.OAuth;

public class DialogAddAccount extends Dialog {

    public DialogAddAccount() {
        super.addEntity(add);
        super.addEntity(oauth);
        super.addEntity(cancel);

//        crackedName.setDrawLineUnder(false);
        crackedName.setPlaceholder("Cracked Name");
        crackedName.setDisabledTextColour(RenderSystem.hexColor(96, 96, 96));
        crackedName.enabledColor = RenderSystem.hexColor(0, 0, 0);

    }

    DialogButton add = new DialogButton(this, "dialog.addaccount.add.name", () -> {
        if (!this.crackedName.getText().isEmpty()) {
            Alt alt = new Alt(this.crackedName.getText());
            synchronized (AltManager.getAlts()) {
                AltManager.getAlts().add(alt);
            }
            this.close();
        }
    });

    DialogButton oauth = new DialogButton(this, "dialog.addaccount.microsoftlogin.name", () -> {
        OAuth oAuth = new OAuth();
        DialogMicrosoftLoginProgress dialog = new DialogMicrosoftLoginProgress();
        AltScreen.getInstance().setDialog(dialog);
        oAuth.logIn(new OAuth.LoginCallback() {
            @Override
            public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                Alt alt = new Alt(userName, refreshToken, token, uuid);
                alt.setLastRefreshedTime(System.currentTimeMillis() / 1000L);
                synchronized (AltManager.getAlts()) {
                    AltManager.getAlts().add(alt);

                }
//                Minecraft.getMinecraft().addScheduledTask(() -> {
//                    RenderSystem.playerSkinTextureCache.getSkinTexture(userName, l -> {
//                        alt.skinLoaded = true;
//                        alt.skinLocation = l;
//
//                        AltManager.getAlts().set(AltManager.getAlts().indexOf(alt), alt);
//                    });
//                });
                dialog.close();
            }

            Localizable lFailed = Localizable.of("altscreen.failed");

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                dialog.setLabel(Localizable.ofUntranslatable(lFailed.get() + "\n" + e.getMessage()));
                AltScreen.getInstance().status = EnumChatFormatting.RED + lFailed.get();
            }

            @Override
            public void setStatus(String status) {
                dialog.setLabel(Localizable.of(status));
            }
        });
    });

    DialogButton cancel = new DialogButton(this, "dialog.addaccount.cancel.name", this::close);

    private final TextField crackedName = new TextField(-1, 0, 0, 0, 0);

    @Override
    public void render(double mouseX, double mouseY) {

        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        double spacing = 4;

        crackedName.xPosition = (float) (x + spacing);
        crackedName.yPosition = (float) (y + spacing * 4);
        if (crackedName.isFocused())
            crackedName.onTick();
        crackedName.width = (float) (120);
        crackedName.height = 14;
        crackedName.setTextColor(ThemeManager.get(ThemeManager.ThemeColor.Text));
        crackedName.drawTextBox((int) mouseX, (int) mouseY);

        double offsetY = 40;
        add.setPosition(spacing, offsetY + spacing);
        oauth.setPosition(spacing, offsetY + spacing * 2 + add.getHeight());
        cancel.setPosition(spacing, offsetY + spacing * 3 + add.getHeight() + oauth.getHeight());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        if (this.crackedName.isFocused()) {
            this.crackedName.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.crackedName.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.crackedName.mouseReleased(mouseX, mouseY, mouseButton);
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
