package tritium.rendering.entities.impl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import ingameime.IngameIMEJNI;
import ingameime.PreEditRect;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;
import tritium.rendering.ime.IngameIMERenderer;
import tritium.management.FontManager;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.Stencil;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.settings.ClientSettings;
import tritium.utils.other.math.MathUtils;
import tritium.utils.timing.Timer;


import java.awt.*;

public class TextField extends GuiTextField {
    @Getter
    private final int textFieldNumber;
    private final float yOffset = -1.5f;
    public float xPosition;
    public float yPosition;
    /**
     * The width of this text field.
     */
    public float width;
    public float height;
    public boolean dragging;
    public int startChar;
    public int endChar;
    public MsTimer backspaceTime = new MsTimer();
    @Setter
    public String placeholder = "";
    public boolean isPassword;
    /**
     * Has the current text being edited on the textbox.
     * -- GETTER --
     * Returns the contents of the textbox
     */
    @Getter
    private String text = "";
    /**
     * -- GETTER --
     * returns the maximum number of character that can be contained in this textbox
     */
    @Getter
    private int maxStringLength = 128;
    private int cursorCounter;
    /**
     * -- SETTER --
     * enable drawing background and outline
     */
    @Setter
    private boolean enableBackgroundDrawing = true;
    @Setter
    private boolean drawLineUnder = true;
    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     * -- SETTER --
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    @Setter
    private boolean canLoseFocus = true;
    /**
     * If this value is true along with isEnabled, keyTyped will process the keys.
     */
    private boolean isFocused;
    /**
     * If this value is true along with isFocused, keyTyped will process the keys.
     */
    private boolean isEnabled = true;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    private int lineScrollOffset;
    private float offset;
    private float lastOffset;
    /**
     * -- GETTER --
     * returns the current position of the cursor
     */
    @Getter
    private int cursorPosition;

    /**
     * other selection position, maybe the same as the cursor
     * -- GETTER --
     * the side of the selection that is not the cursor, may be the same as the cursor
     */
    @Getter
    private int selectionEnd;
    public int enabledColor = 14737632;
    private int disabledColor = 7368816;
    /**
     * True if this textbox is visible
     * -- SETTER --
     * Sets whether or not this textbox is visible
     */
    @Setter
    private boolean visible = true;
    private GuiPageButtonList.GuiResponder responder;
    private Predicate<String> textPredicate = Predicates.alwaysTrue();
    @Setter
    private float lineOffset;

    private float howerAlpha;

    private Color lineColor;

    @Setter
    private float wholeAlpha = 1;
    private float opacity;

    public TextField(int number, float x, float y, int width, int height) {
        super(number, Minecraft.getMinecraft().fontRendererObj, (int) x, (int) y, width, height);
        this.textFieldNumber = number;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        this.lineColor = new Color(160, 160, 160);
    }

    public void func_175207_a(GuiPageButtonList.GuiResponder p_175207_1_) {
        this.responder = p_175207_1_;
    }

    /**
     * Increments the cursor counter
     */
    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    /**
     * Sets the text of the textbox
     */
    public void setText(String p_146180_1_) {
        this.text = p_146180_1_;
        this.setCursorPositionEnd();
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText() {
        int var1 = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int var2 = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(var1, var2);
    }

    public void setPredicate(Predicate<String> p_175205_1_) {
        this.textPredicate = p_175205_1_;
    }

    /**
     * replaces selected text, or inserts text at the position on the cursor
     */
    public void writeText(String p_146191_1_) {
        String text = "";
        String filter = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
        int start = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int end = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int var6 = this.maxStringLength - this.text.length() - (start - end);

        if (!this.text.isEmpty()) {
            text = text + this.text.substring(0, start);
        }

        int var8;

        if (var6 < filter.length()) {
            text = text + filter.substring(0, var6);
            var8 = var6;
        } else {
            text = text + filter;
            var8 = filter.length();
        }

        if (!this.text.isEmpty() && end < this.text.length()) {
            text = text + this.text.substring(end);
        }

        if (this.textPredicate.apply(text)) {
            this.text = text;
            this.moveCursorBy(start - this.selectionEnd + var8/* - 1*/);

            if (this.responder != null) {
                this.responder.func_175319_a(this.textFieldNumber, this.text);
            }
        }
    }

    /**
     * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
     * the cursor.
     */
    public void deleteWords(int p_146177_1_) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }
    }

    /**
     * delete the selected text, otherwsie deletes characters from either side of the cursor. params: delete num
     */
    public void deleteFromCursor(int p_146175_1_) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean var2 = p_146175_1_ < 0;
                int var3 = var2 ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
                int var4 = var2 ? this.cursorPosition : this.cursorPosition + p_146175_1_;
                String var5 = "";

                if (var3 >= 0) {
                    var5 = this.text.substring(0, var3);
                }

                if (var4 < this.text.length()) {
                    var5 = var5 + this.text.substring(var4);
                }

                this.text = var5;

                if (var2) {
                    this.moveCursorBy(p_146175_1_);
                }

                if (this.responder != null) {
                    this.responder.func_175319_a(this.textFieldNumber, this.text);
                }
            }
        }
    }

    /**
     * see @getNthNextWordFromPos() params: N, position
     */
    public int getNthWordFromCursor(int p_146187_1_) {
        return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
    }

    /**
     * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
     */
    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return this.func_146197_a(p_146183_1_, p_146183_2_, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int var4 = p_146197_2_;
        boolean var5 = p_146197_1_ < 0;
        int var6 = Math.abs(p_146197_1_);

        for (int var7 = 0; var7 < var6; ++var7) {
            if (var5) {
                while (p_146197_3_ && var4 > 0 && this.text.charAt(var4 - 1) == 32) {
                    --var4;
                }

                while (var4 > 0 && this.text.charAt(var4 - 1) != 32) {
                    --var4;
                }
            } else {
                int var8 = this.text.length();
                var4 = this.text.indexOf(32, var4);

                if (var4 == -1) {
                    var4 = var8;
                } else {
                    while (p_146197_3_ && var4 < var8 && this.text.charAt(var4) == 32) {
                        ++var4;
                    }
                }
            }
        }

        return var4;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int by) {
        this.setCursorPosition(this.selectionEnd + by);
    }

    /**
     * sets the cursors position to the beginning
     */
    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    /**
     * sets the cursors position to after the text
     */
    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
        startChar = 0;
        endChar = text.length();
    }

    /**
     * Call this method from your GuiScreen to process the keys into the textbox
     */
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case Keyboard.KEY_BACK:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;

                case Keyboard.KEY_HOME:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;

                case Keyboard.KEY_LEFT:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
//                            System.out.println("this.getSelectionEnd() = " + this.getSelectionEnd());
                            this.setSelectionPos(this.getSelectionEnd() - 1);
//                            if (this.startChar == this.endChar) {
//
//                            }
                            if (this.startChar > 0)
                                this.startChar -= 1;
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;

                case Keyboard.KEY_RIGHT:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);

                            if (this.startChar > 0)
                                this.startChar += 1;
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;

                case Keyboard.KEY_END:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;

                case Keyboard.KEY_DELETE:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;

                default: {

                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    public void onTick() {
        lastOffset = offset;
        String missing = this.text.substring(0, this.lineScrollOffset);
        offset += (float) ((((getFontRenderer().getStringWidth(missing)) - offset) / (2)) + 0.01);

        if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && isFocused()) {
            if (!this.getText().isEmpty() && backspaceTime.sleep(500, false))
                this.setText(this.getText().substring(0, this.getText().length() - 1));
        } else {
            backspaceTime.reset();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        dragging = false;
    }

    /**
     * Args: x, y, buttonClicked
     */
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean isHovered = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

        if (this.canLoseFocus) {
            if (!isHovered) {
                dragging = false;
                lineScrollOffset = 0;
            }
            this.setFocused(isHovered);
        }

        if (this.isFocused && isHovered && mouseButton == 0) {
            float var5 = (float) (mouseX - this.xPosition);

            if (this.enableBackgroundDrawing) {
                var5 += isPassword ? 1 : 4;
            }
            String var6 = text;
            if (!dragging) {
                if (!var6.isEmpty()) {
                    if (isPassword) {
                        StringBuilder tmp = new StringBuilder();
                        for (int i = 0; i < var6.length(); i++) {
                            tmp.append("*");
                        }

                        var6 = tmp.toString();
                    }
                    this.startChar = (int) (var5 / ((float) getFontRenderer().getStringWidth(var6) / this.text.length()));
                    this.setCursorPosition(startChar);
                }
                dragging = true;
            }
        }
    }

    public int applyAlpha(int color, float alpha) {
        float f = (color >> 24 & 0xFF) * 0.003921568627451F;
        Color c = new Color(color);
        Color c2 = new Color(c.getRed() * 0.003921568627451F, c.getGreen() * 0.003921568627451F, c.getBlue() * 0.003921568627451F, (f) * alpha);
        return c2.getRGB();
    }

    public void drawTextBox(int mouseX, int mouseY) {
        this.drawTextBox(mouseX, mouseY, false);
    }

    /**
     * Draws the textbox
     */
    public void drawTextBox(int mouseX, int mouseY, boolean useScissor) {
        if (this.getVisible()) {

            this.wholeAlpha = MathUtils.clamp(wholeAlpha, 0, 1);
            opacity = Interpolations.interpBezier(opacity, 1.0f, 0.05f) * wholeAlpha;

            double offse = 0;

            if (getFontRenderer().getStringWidth(text) > width) {
                int sW = getFontRenderer().getStringWidth(text.substring(0, Math.min(text.length() - 1, Math.max(cursorPosition, selectionEnd) + 1)));
                if (sW > width) {
                    offse = sW - width;
                }
            }

            float realOpacity = wholeAlpha;

            lineColor = Interpolations.getColorAnimationState(lineColor, (RenderSystem.isHovered(mouseX, mouseY, this.xPosition, this.yPosition, this.width, this.height) || isFocused) ? new Color(255, 133, 155) : new Color(180, 180, 180), 100f);
            howerAlpha = Interpolations.interpLinear(howerAlpha, (RenderSystem.isHovered(mouseX, mouseY, this.xPosition, this.yPosition, this.width, this.height) || isFocused) ? 0.4f : 0.3f, 0.1f) * wholeAlpha;

            if (this.getEnableBackgroundDrawing()) {
                GlStateManager.disableBlend();

                GlStateManager.enableBlend();
                if (drawLineUnder)
                    RenderSystem.drawRect(this.xPosition - 1, this.yPosition + this.height + lineOffset, this.xPosition + this.width + 1, this.yPosition + this.height + 0.5 + lineOffset, RenderSystem.reAlpha(lineColor.getRGB(), wholeAlpha));
            }

            if (this.isFocused && dragging) {
                float mouseXRelative = (mouseX - this.xPosition);

                if (this.enableBackgroundDrawing) {
                    mouseXRelative += isPassword ? 1 : 4;
                }

                if (!this.text.isEmpty()) {
                    endChar = (int) (mouseXRelative / ((float) getFontRenderer().getStringWidth(this.text) / this.text.length()));
                    if (endChar > this.text.length()) endChar = this.text.length();
                    if (endChar < 0) endChar = 0;
                    this.setSelectionPos(endChar);
                }
            }

            int positionDiff = this.cursorPosition - this.lineScrollOffset;
            int positionEndDiff = this.selectionEnd - this.lineScrollOffset;

            String text = this.text;
            if (isPassword) {
                StringBuilder tmp = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    tmp.append("*");
                }
                text = tmp.toString();
            }

            boolean var5 = positionDiff >= 0 && positionDiff <= text.length();
            boolean var6 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && var5;
            float var7 = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            float var8 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 4) / 2 : this.yPosition;

            if (positionEndDiff > text.length()) {
                positionEndDiff = text.length();
            }

            float offsetY = 2f;

            boolean erase = Stencil.isErasing;


            if (!useScissor) {

                if (!erase) {
                    Stencil.write();
                    RenderSystem.drawRect(xPosition - 1, yPosition, xPosition + this.width + 1, yPosition + this.height - 1, applyAlpha(-1, wholeAlpha));
                    Stencil.erase();
                }
            } else {
                RenderSystem.doScissor((int) (xPosition - 1), (int) yPosition, (int) (this.width + 1), (int) (this.height - 1));
            }

            if (this.getText().isEmpty() && !placeholder.isEmpty() && !this.isFocused) {
                getFontRenderer().drawString(placeholder, var7 - 3.5f, var8 - offsetY + yOffset, applyAlpha(enabledColor, howerAlpha));
            }

            boolean var13 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();

            boolean highlighting = false;
            if (positionEndDiff != positionDiff) {
                GlStateManager.color(1, 1, 1, 1);

                int lowestChar = Math.min(startChar, endChar);
                int highestChar = Math.max(startChar, endChar);

                if (startChar != endChar) {
                    highlighting = true;
                }

                if (lowestChar > text.length()) lowestChar = text.length();
                if (lowestChar < 0) lowestChar = 0;
                if (highestChar > text.length()) highestChar = text.length();
                if (highestChar < 0) highestChar = 0;


                RenderSystem.drawRect(4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse - 4f, yPosition - 1, 4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse + getFontRenderer().getStringWidth(text.substring(lowestChar, highestChar)) - 3f, yPosition + height - 1.5f, applyAlpha(new Color(196, 225, 245).getRGB(), realOpacity));
                GlStateManager.color(1, 1, 1, 1);
            }

            //DRAW OVERLAY STRING
            if (!text.isEmpty()) {
                getFontRenderer().drawString(text, var7 - offse - 3.5f, var8 - offsetY + yOffset, this.isFocused() ? enabledColor : disabledColor);
            }
            if (var6) {
                if (var13) {

                    String sub = "";
                    int alpha = (int) Math.min(255, ((System.currentTimeMillis() / 3 % 255) > 255 / 2 ? (Math.abs(Math.abs(System.currentTimeMillis() / 3) % 255 - 255)) : System.currentTimeMillis() / 3 % 255) * 2);

                    if (startChar > 0)
                        sub = text.substring(0, (startChar));
                    if (!highlighting) {

                        if (startChar > endChar)
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 3.5f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + 0.5f + 3.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);
                        else
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 4.0f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + 4.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);

                    }
                } else {
                    GlStateManager.color(1, 1, 1, 1);
                    float alpha = (float) (MathUtils.clamp(80 + (Math.sin(System.nanoTime() * 0.000000009f) * 0.5f + 0.5f) * (255 - 80), 0, 255) * 0.003921568627451F) * wholeAlpha;
                    if (!highlighting) {
                        RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(text) + 4.5f - offse - 3f, var8 - 7, xPosition + getFontRenderer().getStringWidth(text) + 0.5f + 4.5f - offse - 4f, var8 + getFontRenderer().getHeight() - 3, RenderSystem.hexColor(80, 80, 80, (int) (alpha * 255)));
                    }
                    if (IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue()) {
                        if (updInputCTXPositionTimer.isDelayed(100)) {
                            updInputCTXPositionTimer.reset();
                            PreEditRect rect = new PreEditRect();
                            double v = (ClientSettings.FIXED_SCALE.getValue() ? 1 : RenderSystem.getScaleFactor()) * 2;
                            rect.setX((int) ((xPosition + getFontRenderer().getStringWidth(text) + 4.5f - offse - 3f) * v));
                            rect.setY((int) ((var8 + getFontRenderer().getHeight() - 3) * v));
                            IngameIMERenderer.InputCtx.setPreEditRect(rect);
                        }
                    }
                    GlStateManager.color(1, 1, 1, 1);
                }
            }

            if (!useScissor) {
                if (!erase) {
                    Stencil.dispose();
                }
            } else {
                RenderSystem.endScissor();
            }
            if (this.isFocused() && IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue())
                IngameIMERenderer.draw(xPosition, yPosition, false);
        }
    }

    public void drawPasswordBox(int mouseX, int mouseY) {
        if (this.getVisible()) {
            float offse = offset * Minecraft.getMinecraft().timer.elapsedPartialTicks + (lastOffset * (1.0f - Minecraft.getMinecraft().timer.elapsedPartialTicks));

            if (this.getEnableBackgroundDrawing()) {
                GlStateManager.disableBlend();


                GlStateManager.enableBlend();
                RenderSystem.drawRect(this.xPosition - 1, this.yPosition + this.height, this.xPosition + this.width + 1, this.yPosition + this.height + 1, isFocused ? 0xffcac9ca : 0xffe5e4e5);
            }

            if (dragging && mouseX >= xPosition + width) {
                this.setSelectionPos(this.getSelectionEnd() + 1);
            }

            if (this.isFocused && dragging) {
                int xDiff = (int) (mouseX - this.xPosition);

                if (this.enableBackgroundDrawing) {
                    xDiff += 1;
                }

                if (!this.text.isEmpty()) {
                    endChar = xDiff / (getFontRenderer().getStringWidth(this.text) / this.text.length());
                    if (endChar > this.text.length()) endChar = this.text.length();
                    if (endChar < 0) endChar = 0;
                    this.setSelectionPos(endChar);
                }
            }

            int var1 = this.isEnabled ? Minecraft.getMinecraft().currentScreen instanceof GuiChat ? 0xffffffff : isFocused ? 0xff000000 : 0xff828182 : Minecraft.getMinecraft().currentScreen instanceof GuiChat ? 0xfffffffe : 0xff000001;
            int selectionStart = this.cursorPosition - this.lineScrollOffset;
            int selectionEnd = this.selectionEnd - this.lineScrollOffset;


            //this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getStringWidth(), false, true);
            boolean isSelectionValid = selectionStart >= 0 && selectionStart <= text.length();
            boolean var6 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && isSelectionValid;
            float var7 = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            float var8 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;

            if (selectionEnd > text.length()) {
                selectionEnd = text.length();
            }


            Stencil.write();
            RenderSystem.drawRect(xPosition - 1, yPosition, xPosition + this.width + 1, yPosition + this.height, -1);
            Stencil.erase();

            if (this.getText().isEmpty() && !placeholder.isEmpty() && !this.isFocused) {
                FontManager.pf25.drawString(placeholder, var7 - 3.5f, var8 - 2.5f + yOffset, 0xff8d8b8d);
            }

            String s = isSelectionValid ? text.substring(0, selectionStart) : text;
            if (!text.isEmpty()) {
                getFontRenderer().drawString(s, var7 - 2.5f, var8, -1);
            }

            boolean var13 = this.cursorPosition < text.length() || text.length() >= this.getMaxStringLength();

            boolean highlighting = false;
            if (selectionEnd != selectionStart) {
                GlStateManager.color(1, 1, 1, 1);

                int lowestChar = Math.min(startChar, endChar);
                int highestChar = Math.max(startChar, endChar);

                if ((4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse - 3.5f) - (4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse + getFontRenderer().getStringWidth(text.substring(lowestChar, highestChar)) - 3.5f) != 0) {
                    highlighting = true;
                }

                RenderSystem.drawRect(4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse - 3.5f, yPosition + 2 - 5 + 4, 4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse + getFontRenderer().getStringWidth(text.substring(lowestChar, highestChar)) - 3.5f, yPosition + height - 1.5f - 2, 0xffadcffe);
                GlStateManager.color(1, 1, 1, 1);
            }


            //DRAW OVERLAY STRING
            if (!text.isEmpty()) {
                getFontRenderer().drawString(text, var7 - offse - 3.5f, var8 - 1.5f + 6 - 19 / 2f, var1);
            }
            if (var6) {
                if (var13) {
                    String sub = "";
                    int alpha = (int) Math.min(255, ((System.currentTimeMillis() / 3 % 255) > 255 / 2 ? (Math.abs(Math.abs(System.currentTimeMillis() / 3) % 255 - 255)) : System.currentTimeMillis() / 3 % 255) * 2);

                    if (startChar > 0)
                        sub = text.substring(0, (startChar));
                    if (!highlighting) {

                        if (startChar > endChar)
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 3.5f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + 0.5f + 3.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);
                        else
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 4.0f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + 4.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);

                    }
                } else {
                    GlStateManager.color(1, 1, 1, 1);
                    //if(!dragging)
                    int alpha = (int) ((System.currentTimeMillis() / 3 % 255) > 255 / 2 ? (255) : 255 / 2f);
                    if (!highlighting) {
                        RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(text) + 4.5f - offse - 4f, var8 - 5, xPosition + getFontRenderer().getStringWidth(text) + 0.5f + 4.5f - offse - 4f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);
                    }
                    GlStateManager.color(1, 1, 1, 1);
                }
            }
            Stencil.dispose();

        }
    }

    /**
     * draws the vertical line cursor in the textbox
     */
    private void drawCursorVertical(float f, float p_146188_2_, float g, float p_146188_4_) {
        float var5;

        if (f < g) {
            var5 = f;
            f = g;
            g = var5;
        }

        if (p_146188_2_ < p_146188_4_) {
            var5 = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = var5;
        }

        if (g > this.xPosition + this.width) {
            g = this.xPosition + this.width;
        }

        if (f > this.xPosition + this.width) {
            f = this.xPosition + this.width;
        }

        RenderSystem.drawRect(f + 1.5f, p_146188_2_ + 0.5f, g + 0.5f, p_146188_4_ - 1, 0xffadcffe);

       /* Tessellator var7 = Tessellator.getInstance();
        WorldRenderer var6 = var7.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        var6.startDrawingQuads();
        var6.addVertex((double)p_146188_1_, (double)p_146188_4_, 0.0D);
        var6.addVertex((double)p_146188_3_, (double)p_146188_4_, 0.0D);
        var6.addVertex((double)p_146188_3_, (double)p_146188_2_, 0.0D);
        var6.addVertex((double)p_146188_1_, (double)p_146188_2_, 0.0D);
        var7.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();*/
    }

    public void setMaxStringLength(int p_146203_1_) {
        this.maxStringLength = p_146203_1_;

        if (this.text.length() > p_146203_1_) {
            this.text = this.text.substring(0, p_146203_1_);
        }
    }

    /**
     * sets the position of the cursor to the provided index
     */
    public void setCursorPosition(int p_146190_1_) {
        this.cursorPosition = p_146190_1_;
        startChar = p_146190_1_;
        endChar = p_146190_1_;
        int var2 = this.text.length();
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, var2);
        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * get enable drawing background and outline
     */
    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    /**
     * Sets the text colour for this textbox (disabled text will not use this colour)
     */
    public void setTextColor(int textColor) {
        this.enabledColor = textColor;
    }

    public void setDisabledTextColour(int disabledColor) {
        this.disabledColor = disabledColor;
    }

    /**
     * Getter for the focused field
     */
    public boolean isFocused() {
        return this.isFocused;
    }

    /**
     * Sets focus to this gui element
     */
    public void setFocused(boolean focused) {
        if (focused && !this.isFocused) {
            this.cursorCounter = 0;
            Keyboard.enableRepeatEvents(true);

            if (IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue())
                IngameIMERenderer.setActivated(true);
        }

        this.isFocused = focused;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    public int getWidth() {
        return (int) (this.getEnableBackgroundDrawing() ? this.width - 8 : this.width);
    }

    /**
     * Sets the position of the selection anchor (i.e. position the selection was started at)
     */
    public void setSelectionPos(int pos) {
        int length = this.text.length();

        if (pos > length) {
            pos = length;
        }

        if (pos < 0) {
            pos = 0;
        }

        this.selectionEnd = pos;

        if (this.lineScrollOffset > length) {
            this.lineScrollOffset = length;
        }

        float textWidth = this.getWidth();
        String split = String.join("\n", getFontRenderer().fitWidth(this.text.substring(this.lineScrollOffset), textWidth));

        float wWidth = split.length() + this.lineScrollOffset;

        if (pos == this.lineScrollOffset) {
            this.lineScrollOffset -= String.join("\n", getFontRenderer().fitWidth(this.text, (int) textWidth)).length();
        }

        if (pos > wWidth) {
            this.lineScrollOffset += (int) (pos - wWidth);
        } else if (pos <= this.lineScrollOffset) {
            this.lineScrollOffset -= this.lineScrollOffset - pos;
        }

        this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, length);
    }

    /**
     * returns true if this textbox is visible
     */
    public boolean getVisible() {
        return this.visible;
    }

    @Setter
    private CFontRenderer fontRenderer = FontManager.pf18;

    private CFontRenderer getFontRenderer() {

        if (fontRenderer == null || (fontRenderer.sizePx == FontManager.pf18.sizePx && fontRenderer != FontManager.pf18))
            fontRenderer = FontManager.pf18;

        return fontRenderer;
    }

    public void setPosition(double v, double v1) {
        xPosition = (float) v;
        yPosition = (float) v1;
    }

    public void setBounds(double i, double i1) {
        width = (float) i;
        height = (float) i1;
    }

    Timer updInputCTXPositionTimer = new Timer();

    public static final class MsTimer {
        private long time;
        @Setter
        private boolean active;

        public MsTimer() {
            time = System.currentTimeMillis();
            active = true;
        }

        public boolean reach(final long time) {
            if (!active)
                return false;
            return time() >= time;
        }

        public void reset() {
            time = System.currentTimeMillis();
        }

        public boolean sleep(final long time) {
            if (!active)
                return false;
            if (time() >= time) {
                reset();
                return true;
            }
            return false;
        }

        public boolean sleep(final long time, boolean reset) {
            if (!active)
                return false;
            if (time() >= time) {
                if (reset) {
                    reset();
                }
                return true;
            }
            return false;
        }

        public long time() {
            return System.currentTimeMillis() - time;
        }

    }
}