package tritium.module.impl.render;


import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import org.lwjgl.input.Mouse;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.Render2DEvent;
import tritium.interfaces.IFontRenderer;
import tritium.management.FontManager;
import tritium.management.ModuleManager;
import tritium.module.Module;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.settings.ClientSettings;
import tritium.settings.NumberSetting;

public class Chat extends Module {

    public BooleanSetting fastChat = new BooleanSetting("Fast Chat", false);
    public BooleanSetting animation = new BooleanSetting("Chat Animation", false);
    public BooleanSetting noChatClear = new BooleanSetting("No Chat Clear", false);
    public BooleanSetting noLengthLimit = new BooleanSetting("No Length Limit", false);
    public BooleanSetting onlyVisibleWhileTyping = new BooleanSetting("Only Visible While Typing", false);
    public BooleanSetting clientChat = new BooleanSetting("Use client font renderer", false);

    public NumberSetting<Integer> yOffset = new NumberSetting<>("Y Offset", 0, -100, 100, 1);

    public Chat() {
        super("Chat", Category.RENDER);
    }
    
    @Handler
    public void onRender2D(Render2DEvent event) {
        this.drawChat(mc.ingameGUI.getUpdateCounter());
    }

    public void drawChat(int updateCounter) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN && clientChat.getValue()) {
            if (this.onlyVisibleWhileTyping.getValue() && !(mc.currentScreen instanceof GuiChat)) {
                return;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, (float) (RenderSystem.getHeight() - 48 * RenderSystem.getScaleFactor()), 0.0F);

            int lineCount = mc.ingameGUI.getChatGUI().getLineCount();
            boolean chatOpen = false;
            int j = 0;
            int chatSize = mc.ingameGUI.getChatGUI().drawnChatLines.size();
            float chatOpacity = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
            IFontRenderer fontRenderer = FontManager.pf28;

            if (chatSize > 0) {
                if (mc.ingameGUI.getChatGUI().getChatOpen()) {
                    chatOpen = true;
                }

                float chatScale = mc.ingameGUI.getChatGUI().getChatScale();
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F * RenderSystem.getScaleFactor(), 8.0F * RenderSystem.getScaleFactor(), 0.0F);

                GlStateManager.scale(chatScale, chatScale, 1.0F);

                for (int chatLineIndex = 0; chatLineIndex + mc.ingameGUI.getChatGUI().scrollPos < mc.ingameGUI.getChatGUI().drawnChatLines.size() && chatLineIndex < lineCount; ++chatLineIndex) {
                    ChatLine chatline = mc.ingameGUI.getChatGUI().drawnChatLines.get(chatLineIndex + mc.ingameGUI.getChatGUI().scrollPos);

                    if (chatline != null) {
                        int chatLineLength = MathHelper.ceiling_float_int((float) ((float) mc.ingameGUI.getChatGUI().getChatWidth() * RenderSystem.getScaleFactor() / chatScale));

                        if (this.noLengthLimit.getValue()) {

                            chatLineLength = (int) Math.min(chatLineLength, fontRenderer.getStringWidth(chatline.getChatComponent().getFormattedText()) / 1.5f - 3);

                            chatLineLength = Math.max(MathHelper.ceiling_float_int((float) ((float) GuiNewChat.calculateChatboxWidth(this.mc.gameSettings.chatWidth) * RenderSystem.getScaleFactor() / chatScale)), chatLineLength);

                        }

                        int updateTicksLeft = updateCounter - chatline.getUpdatedCounter();

                        if (updateTicksLeft < 200 || chatOpen) {
                            double leftPercent = (double) updateTicksLeft / 200.0D;
                            leftPercent = 1.0D - leftPercent;
                            leftPercent = leftPercent * 10.0D;
                            leftPercent = MathHelper.clamp_double(leftPercent, 0.0D, 1.0D);
                            leftPercent = leftPercent * leftPercent;
                            int alpha = (int) (255.0D * leftPercent);

                            if (chatOpen) {
                                alpha = 255;
                            }

                            alpha = (int) ((float) alpha * chatOpacity);
                            ++j;


                            if (alpha > 3) {
                                int xOffset = 0;

                                int chatLineHeight = fontRenderer.getHeight() / 2 + 8;

                                int chatLineY = -chatLineIndex * chatLineHeight + this.yOffset.getValue();

                                double chatLineYTop = this.animation.getValue() && updateTicksLeft < 200 ? chatline.rectY - chatLineHeight : chatLineY - chatLineHeight;
                                double chatLineYBottom = this.animation.getValue() && updateTicksLeft < 200 ? chatline.rectY : chatLineY;

                                if (!this.fastChat.getValue()) {
                                    GlStateManager.resetColor();
                                    RenderSystem.drawRect(xOffset, chatLineYTop, xOffset + chatLineLength + 4, chatLineYBottom, alpha / 2 << 24);
                                }


                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();

                                GlStateManager.pushMatrix();

                                double v = ((this.animation.getValue() && updateTicksLeft < 200) ? chatline.textY : (chatLineY - chatLineHeight * 0.5 - fontRenderer.getHeight() * 0.5));
                                GlStateManager.translate((float) xOffset, v - .5, 0);

                                String font = s.replaceAll("，", ",").replaceAll("：", ":").replaceAll("；", ";").replaceAll("？", "?");

                                int color = 16777215 + (alpha << 24);
                                fontRenderer.drawString(StringUtils.stripControlCodes(font), 1, 1, hexColor(0, 0, 0, alpha));
                                fontRenderer.drawString(font, 0, 0, color);

                                GlStateManager.popMatrix();

                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                                chatline.rectY = Interpolations.interpBezier(chatline.rectY, chatLineY, 0.4f);
                                chatline.textY = Interpolations.interpBezier(chatline.textY, chatLineY - chatLineHeight * 0.5 - fontRenderer.getHeight() * 0.5, 0.4f);
                            }
                        }
                    }
                }

                if (chatOpen) {
                    int k2 = fontRenderer.getHeight();
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = chatSize * k2 + chatSize;
                    int i3 = j * k2 + j;
                    int j3 = mc.ingameGUI.getChatGUI().scrollPos * i3 / chatSize;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = mc.ingameGUI.getChatGUI().isScrolled ? 13382451 : 3355562;
                        Gui.drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        Gui.drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
        }
    }

    public IChatComponent getChatComponent() {

        final ScaledResolution scaledresolution = ScaledResolution.get();
        int scaledWidth = scaledresolution.getScaledWidth();
        int scaledHeight = scaledresolution.getScaledHeight();
        double k1 = Mouse.getX() * scaledWidth / this.mc.displayWidth;
        double l1 = Mouse.getY() * scaledHeight / this.mc.displayHeight - 40;

        double mouseX = ClientSettings.FIXED_SCALE.getValue() ? k1 * RenderSystem.getScaleFactor() : k1;
        double mouseY = ClientSettings.FIXED_SCALE.getValue() ? l1 * RenderSystem.getScaleFactor() : l1;

        float f = mc.ingameGUI.getChatGUI().getChatScale();

        if (ModuleManager.chat.isEnabled())
            mouseY += ModuleManager.chat.yOffset.getValue();

        mouseX = MathHelper.floor_float((float) mouseX / f);
        mouseY = MathHelper.floor_float((float) mouseY / f);

        if (mouseX >= 0 && mouseY >= 0) {
            int l = Math.min(mc.ingameGUI.getChatGUI().getLineCount(), mc.ingameGUI.getChatGUI().drawnChatLines.size());

            int chatLineLength = MathHelper.floor_float((float) mc.ingameGUI.getChatGUI().getChatWidth() / mc.ingameGUI.getChatGUI().getChatScale());

            if (ModuleManager.chat.isEnabled() && ModuleManager.chat.noLengthLimit.getValue()) {
                chatLineLength = Integer.MAX_VALUE;
            }

            if (mouseX <= chatLineLength && mouseY < FontManager.pf28.getHeight() * l + l) {
                int i1 = (int) (mouseY / FontManager.pf28.getHeight() + mc.ingameGUI.getChatGUI().scrollPos);

                if (i1 >= 0 && i1 < mc.ingameGUI.getChatGUI().drawnChatLines.size()) {
                    ChatLine chatline = mc.ingameGUI.getChatGUI().drawnChatLines.get(i1);
                    double j1 = 0;

                    for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
                        if (ichatcomponent instanceof ChatComponentText) {
                            j1 += FontManager.pf28.getWidthDouble(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) ichatcomponent).getChatComponentText_TextValue(), false));

                            if (j1 > mouseX) {
                                return ichatcomponent;
                            }
                        }
                    }
                }

                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
