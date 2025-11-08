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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.rendering.Rect;
import tritium.rendering.StencilClipManager;
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
    // 基础属性
    @Getter
    private final int textFieldNumber;

    public float xPosition;
    public float yPosition;
    public float width;
    public float height;

    // 文本内容
    @Getter
    private String text = "";
    @Getter
    private int maxStringLength = 128;
    @Setter
    public String placeholder = "";
    public boolean isPassword;
    @Setter
    private TextChangedCallback callback = null;

    // 光标和选择
    @Getter
    private int cursorPosition;
    @Getter
    private int selectionEnd;
    private int lineScrollOffset;
    private int cursorCounter;

    // 拖拽选择相关
    public boolean dragging;
    private int dragStartChar;
    private int dragEndChar;

    // 滚动相关
    private float scrollOffset;
    private float lastScrollOffset;

    @Setter
    private boolean drawLineUnder = true;
    @Setter
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    @Setter
    private boolean visible = true;

    // 样式相关
    public int enabledColor = 14737632;
    private int disabledColor = 7368816;
    @Setter
    private float lineOffset;
    @Setter
    private float wholeAlpha = 1;
    private float opacity;
    private float hoverAlpha;
    private Color lineColor;

    // 字体
    @Setter
    private CFontRenderer fontRenderer = FontManager.pf18;

    // 其他
    private GuiPageButtonList.GuiResponder responder;
    private Predicate<String> textPredicate = Predicates.alwaysTrue();
    public MsTimer backspaceTime = new MsTimer();
    private Timer imePositionUpdateTimer = new Timer();
    private Timer cursorForceShowTimer = new Timer();

    public interface TextChangedCallback {
        void onTextChanged(String newText);
    }

    public TextField(int number, float x, float y, int width, int height) {
        super(number, Minecraft.getMinecraft().fontRendererObj, (int) x, (int) y, width, height);
        this.textFieldNumber = number;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        this.lineColor = new Color(160, 160, 160);
    }

    // ========== 文本操作 ==========

    public void setText(String text) {
        this.text = text;
        if (this.callback != null)
            this.callback.onTextChanged(text);
        setCursorPositionEnd();
    }

    public String getSelectedText() {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }

    public void writeText(String input) {
        String filtered = ChatAllowedCharacters.filterAllowedCharacters(input);
        int selStart = Math.min(cursorPosition, selectionEnd);
        int selEnd = Math.max(cursorPosition, selectionEnd);
        int availableSpace = maxStringLength - text.length() - (selEnd - selStart);

        StringBuilder newText = new StringBuilder();

        // 添加光标前的文本
        if (!text.isEmpty()) {
            newText.append(text, 0, selStart);
        }

        // 添加新输入的文本
        int charsToAdd = Math.min(availableSpace, filtered.length());
        newText.append(filtered, 0, charsToAdd);

        // 添加光标后的文本
        if (!text.isEmpty() && selEnd < text.length()) {
            newText.append(text.substring(selEnd));
        }

        String result = newText.toString();
        if (textPredicate.apply(result)) {
            text = result;
            if (this.callback != null)
                this.callback.onTextChanged(text);
            moveCursorBy(selStart - selectionEnd + charsToAdd);

            if (responder != null) {
                responder.func_175319_a(textFieldNumber, text);
            }
        }
    }

    public void deleteFromCursor(int count) {
        if (text.isEmpty()) {
            return;
        }

        if (selectionEnd != cursorPosition) {
            writeText("");
            return;
        }

        boolean deleteBefore = count < 0;
        int deleteStart = deleteBefore ? cursorPosition + count : cursorPosition;
        int deleteEnd = deleteBefore ? cursorPosition : cursorPosition + count;

        StringBuilder newText = new StringBuilder();
        if (deleteStart >= 0) {
            newText.append(text, 0, deleteStart);
        }
        if (deleteEnd < text.length()) {
            newText.append(text.substring(deleteEnd));
        }

        text = newText.toString();
        if (this.callback != null)
            this.callback.onTextChanged(text);

        if (deleteBefore) {
            moveCursorBy(count);
        }

        if (responder != null) {
            responder.func_175319_a(textFieldNumber, text);
        }
    }

    public void deleteWords(int count) {
        if (!text.isEmpty()) {
            if (selectionEnd != cursorPosition) {
                writeText("");
            } else {
                deleteFromCursor(getNthWordFromCursor(count) - cursorPosition);
            }
        }
    }

    // ========== 光标操作 ==========

    public void setCursorPosition(int position) {
        cursorPosition = MathHelper.clamp_int(position, 0, text.length());
        dragStartChar = cursorPosition;
        dragEndChar = cursorPosition;
        setSelectionPos(cursorPosition);
        cursorForceShowTimer.reset();
    }

    public void setCursorPositionZero() {
        setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        setCursorPosition(text.length());
        dragStartChar = 0;
        dragEndChar = text.length();
        cursorForceShowTimer.reset();
    }

    public void moveCursorBy(int offset) {
        setCursorPosition(selectionEnd + offset);
    }

    public void setSelectionPos(int position) {
        int clampedPos = MathHelper.clamp_int(position, 0, text.length());
        selectionEnd = clampedPos;

        // 调整滚动偏移
        if (lineScrollOffset > text.length()) {
            lineScrollOffset = text.length();
        }

        float visibleWidth = getWidth();
        String visibleText = String.join("\n", getFontRenderer().fitWidth(
                text.substring(lineScrollOffset), visibleWidth));
        int visibleEndPos = visibleText.length() + lineScrollOffset;

        if (clampedPos == lineScrollOffset) {
            lineScrollOffset -= String.join("\n", getFontRenderer().fitWidth(
                    text, (int) visibleWidth)).length();
        }

        if (clampedPos > visibleEndPos) {
            lineScrollOffset += (clampedPos - visibleEndPos);
        } else if (clampedPos <= lineScrollOffset) {
            lineScrollOffset -= (lineScrollOffset - clampedPos);
        }

        lineScrollOffset = MathHelper.clamp_int(lineScrollOffset, 0, text.length());
    }

    public void updateCursorCounter() {
        cursorCounter++;
    }

    // ========== 单词导航 ==========

    public int getNthWordFromCursor(int n) {
        return getNthWordFromPos(n, cursorPosition);
    }

    public int getNthWordFromPos(int n, int position) {
        return findWordBoundary(n, position, true);
    }

    private int findWordBoundary(int wordCount, int position, boolean skipSpaces) {
        int currentPos = position;
        boolean searchBackward = wordCount < 0;
        int absWordCount = Math.abs(wordCount);

        for (int i = 0; i < absWordCount; i++) {
            if (searchBackward) {
                // 向后搜索
                while (skipSpaces && currentPos > 0 && text.charAt(currentPos - 1) == ' ') {
                    currentPos--;
                }
                while (currentPos > 0 && text.charAt(currentPos - 1) != ' ') {
                    currentPos--;
                }
            } else {
                // 向前搜索
                int textLength = text.length();
                currentPos = text.indexOf(' ', currentPos);

                if (currentPos == -1) {
                    currentPos = textLength;
                } else {
                    while (skipSpaces && currentPos < textLength && text.charAt(currentPos) == ' ') {
                        currentPos++;
                    }
                }
            }
        }

        return currentPos;
    }

    // ========== 键盘输入处理 ==========

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!isFocused) {
            return false;
        }

        // Ctrl组合键处理
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            setCursorPositionEnd();
            setSelectionPos(0);
            return true;
        }
        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(getSelectedText());
            return true;
        }
        if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (isEnabled) {
                writeText(GuiScreen.getClipboardString());
            }
            return true;
        }
        if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(getSelectedText());
            if (isEnabled) {
                writeText("");
            }
            return true;
        }

        // 特殊键处理
        switch (keyCode) {
            case Keyboard.KEY_BACK:
                if (isEnabled) {
                    if (GuiScreen.isCtrlKeyDown()) {
                        deleteWords(-1);
                    } else {
                        deleteFromCursor(-1);
                    }
                }
                return true;

            case Keyboard.KEY_DELETE:
                if (isEnabled) {
                    if (GuiScreen.isCtrlKeyDown()) {
                        deleteWords(1);
                    } else {
                        deleteFromCursor(1);
                    }
                }
                return true;

            case Keyboard.KEY_HOME:
                if (GuiScreen.isShiftKeyDown()) {
                    setSelectionPos(0);
                } else {
                    setCursorPositionZero();
                }
                return true;

            case Keyboard.KEY_END:
                if (GuiScreen.isShiftKeyDown()) {
                    setSelectionPos(text.length());
                } else {
                    setCursorPositionEnd();
                }
                return true;

            case Keyboard.KEY_LEFT:
                handleLeftKey();
                return true;

            case Keyboard.KEY_RIGHT:
                handleRightKey();
                return true;

            default:
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    if (isEnabled) {
                        writeText(Character.toString(typedChar));
                    }
                    return true;
                }
                return false;
        }
    }

    private void handleLeftKey() {
        if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
                setSelectionPos(getNthWordFromPos(-1, selectionEnd));
            } else {
                int newPos = Math.max(0, selectionEnd - 1);
                setSelectionPos(newPos);
                if (dragStartChar > 0) {
                    cursorForceShowTimer.reset();
                    dragStartChar--;
                }
            }
        } else {
            if (GuiScreen.isCtrlKeyDown()) {
                setCursorPosition(getNthWordFromCursor(-1));
            } else {
                moveCursorBy(-1);
            }
        }
    }

    private void handleRightKey() {
        if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
                setSelectionPos(getNthWordFromPos(1, selectionEnd));
            } else {
                int newPos = Math.min(text.length(), selectionEnd + 1);
                setSelectionPos(newPos);
                if (dragStartChar < text.length()) {
                    cursorForceShowTimer.reset();
                    dragStartChar++;
                }
            }
        } else {
            if (GuiScreen.isCtrlKeyDown()) {
                setCursorPosition(getNthWordFromCursor(1));
            } else {
                moveCursorBy(1);
            }
        }
    }

    // ========== 鼠标交互 ==========

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean isHovered = RenderSystem.isHovered(mouseX, mouseY, xPosition, yPosition, width, height);

        if (canLoseFocus) {
            if (!isHovered) {
                dragging = false;
                lineScrollOffset = 0;
            }
            setFocused(isHovered);
        }

        if (isFocused && isHovered && mouseButton == 0) {
            startDragging(mouseX);
            return true;
        }

        return canLoseFocus;
    }

    private void startDragging(double mouseX) {
        float relativeX = (float) (mouseX - xPosition);

        if (!text.isEmpty() && !dragging) {
            String displayText = getDisplayText();
            float charWidth = (float) getFontRenderer().getStringWidthD(displayText) / text.length();
            cursorForceShowTimer.reset();
            dragStartChar = (int) (relativeX / charWidth);
            dragStartChar = MathHelper.clamp_int(dragStartChar, 0, text.length());
            setCursorPosition(dragStartChar);
        }

        dragging = true;
    }

    private void updateDragging(int mouseX) {
        if (!isFocused || !dragging || text.isEmpty()) {
            return;
        }

        float relativeX = mouseX - xPosition;

        float charWidth = (float) getFontRenderer().getStringWidthD(text) / text.length();
        dragEndChar = (int) (relativeX / charWidth);
        dragEndChar = MathHelper.clamp_int(dragEndChar, 0, text.length());
        setSelectionPos(dragEndChar);
    }

    // ========== 渲染 ==========

    public void onTick() {
        // 更新滚动动画
        lastScrollOffset = scrollOffset;
        if (lineScrollOffset > 0) {
            String hiddenText = text.substring(0, lineScrollOffset);
            float targetOffset = (float) getFontRenderer().getStringWidthD(hiddenText);
            scrollOffset += (targetOffset - scrollOffset) / 2f + 0.01f;
        } else {
            scrollOffset = 0;
        }

        // 处理长按退格
        if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && isFocused) {
            if (!text.isEmpty() && backspaceTime.sleep(500, false)) {
                setText(text.substring(0, text.length() - 1));
            }
        } else {
            backspaceTime.reset();
        }
    }

    public void drawTextBox(int mouseX, int mouseY) {
        drawTextBox(mouseX, mouseY, false);
    }

    public void drawTextBox(int mouseX, int mouseY, boolean useScissor) {
        if (!visible) {
            return;
        }

        // 更新透明度动画
        wholeAlpha = MathUtils.clamp(wholeAlpha, 0, 1);
        opacity = Interpolations.interpBezier(opacity, 1.0f, 0.05f) * wholeAlpha;

        // 计算文本滚动偏移
        double textScrollOffset = calculateTextScrollOffset();

        // 更新样式
        boolean isHovered = RenderSystem.isHovered(mouseX, mouseY, xPosition, yPosition, width, height);
        updateStyles(isHovered, mouseX, mouseY);

        // 绘制下划线
        if (drawLineUnder) {
            RenderSystem.drawRect(
                    xPosition, yPosition + height + lineOffset,
                    xPosition + width, yPosition + height + 0.5f + lineOffset,
                    RenderSystem.reAlpha(lineColor.getRGB(), wholeAlpha)
            );
        }

        // 处理拖拽
        if (dragging && !Mouse.isButtonDown(0)) {
            dragging = false;
        }
        if (dragging) {
            updateDragging(mouseX);
        }

        // 设置裁剪区域
        setupClipping(useScissor);

        // 绘制文本和光标
        renderTextContent(textScrollOffset, isHovered);

        // 清理裁剪
        cleanupClipping(useScissor);

        // 绘制IME
        if (isFocused && IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue()) {
            IngameIMERenderer.draw(xPosition, yPosition, false);
        }
    }

    private double calculateTextScrollOffset() {
        double offset = 0;
        double width = getFontRenderer().getStringWidthD(text);
        if (width > this.width) {
            int visibleEndPos = Math.min(text.length(), Math.max(cursorPosition, selectionEnd));
            double visibleWidth = getFontRenderer().getStringWidthD(text.substring(0, visibleEndPos));
            if (visibleWidth > this.width) {
                offset = visibleWidth - this.width;
            }
        }
        return offset;
    }

    private void updateStyles(boolean isHovered, int mouseX, int mouseY) {
        // 失焦检测
        if (isFocused && !dragging && !isHovered && Mouse.isButtonDown(0)) {
            setFocused(false);
        }

        // 线条颜色动画
        Color targetColor = (isHovered || isFocused) ?
                new Color(255, 133, 155) : new Color(180, 180, 180);
        lineColor = Interpolations.getColorAnimationState(lineColor, targetColor, 100f);

        // 悬停透明度动画
        float targetAlpha = (isHovered || isFocused) ? 0.4f : 0.3f;
        hoverAlpha = Interpolations.interpLinear(hoverAlpha, targetAlpha, 0.1f) * wholeAlpha;
    }

    private void setupClipping(boolean useScissor) {
        if (!useScissor && !Stencil.isErasing) {
            StencilClipManager.beginClip(() -> {
                RenderSystem.drawRect(xPosition - 1, yPosition,
                        xPosition + width + 1, yPosition + height - 1, -1);
            });
        } else if (useScissor) {
            RenderSystem.doScissor((int) xPosition - 1, (int) yPosition,
                    (int) width + 1, (int) height - 1);
        }
    }

    private void cleanupClipping(boolean useScissor) {
        if (!useScissor && !Stencil.isErasing) {
            StencilClipManager.endClip();
        } else if (useScissor) {
            RenderSystem.endScissor();
        }
    }

    private void renderTextContent(double scrollOffset, boolean isHovered) {
        String displayText = getDisplayText();
        float posX = xPosition;
        float posY = yPosition;
        // 绘制占位符
        if (text.isEmpty() && !placeholder.isEmpty() && !isFocused) {
            getFontRenderer().drawString(placeholder, posX, getRenderOffsetY(),
                    applyAlpha(enabledColor, hoverAlpha));
            return;
        }

        // 绘制选择高亮
        if (hasSelection()) {
            renderSelection(displayText, scrollOffset);
        }

        // 绘制文本
        if (!displayText.isEmpty()) {
            int textColor = isFocused ? enabledColor : disabledColor;
            int alpha = (textColor >> 24) & 255;
            getFontRenderer().drawString(displayText, posX - (float) scrollOffset,
                    getRenderOffsetY(),
                    RenderSystem.reAlpha(textColor, alpha * RenderSystem.DIVIDE_BY_255 * wholeAlpha));
        }

        // 绘制光标
        if (shouldDrawCursor() && !hasSelection()) {
            renderCursor(displayText, posX, getRenderOffsetY(), scrollOffset);
        }
    }

    private String getDisplayText() {
        return isPassword ? "*".repeat(text.length()) : text;
    }

    private boolean hasSelection() {
        return dragStartChar != dragEndChar;
    }

    private boolean shouldDrawCursor() {
        int visibleCursorPos = cursorPosition - lineScrollOffset;
        return isFocused && (cursorCounter / 6) % 2 == 0 &&
                visibleCursorPos >= 0 && visibleCursorPos <= text.length();
    }

    private double getRenderOffsetY() {
        return yPosition + height * .5 - getFontRenderer().getHeight() * .5;
    }

    private void renderSelection(String displayText, double scrollOffset) {
        int lowestChar = Math.min(dragStartChar, dragEndChar);
        int highestChar = Math.max(dragStartChar, dragEndChar);

        lowestChar = MathHelper.clamp_int(lowestChar, 0, text.length());
        highestChar = MathHelper.clamp_int(highestChar, 0, text.length());

        if (lowestChar < highestChar) {
            float startX = (float) (xPosition + getFontRenderer().getStringWidthD(
                                displayText.substring(0, lowestChar)) - (float) scrollOffset);
            float endX = (float) (startX + getFontRenderer().getStringWidthD(
                                displayText.substring(lowestChar, highestChar)) + 1f);

            RenderSystem.drawRect(startX, getRenderOffsetY() - 1, endX, getRenderOffsetY() + getFontRenderer().getHeight() + 1,
                    applyAlpha(new Color(196, 225, 245).getRGB(), wholeAlpha));
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    double animatedCursorX = -1;

    private void renderCursor(String displayText, double posX, double posY, double scrollOffset) {
        String textBeforeCursor = dragStartChar > 0 ?
                displayText.substring(0, dragStartChar) : "";
        float cursorX = (float) (xPosition + getFontRenderer().getStringWidthD(textBeforeCursor) - (float) scrollOffset);

        // 闪烁动画
        long l = System.currentTimeMillis() / 5 % 255;
        int alpha = (int) Math.min(255,
                (l > 127 ?
                        (255 - l) :
                        l) * 2);

        if (animatedCursorX == -1)
            animatedCursorX = cursorX;
        animatedCursorX = Interpolations.interpBezier(animatedCursorX, cursorX, .4f);

        if (alpha > 127 || !cursorForceShowTimer.isDelayed(750)) {
            RenderSystem.drawRect(animatedCursorX, posY - 2, animatedCursorX + 0.5f,
                    posY + getFontRenderer().getHeight() + 2, 0xffcdcbcd);
        }

        // 更新IME位置
        if (IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue()) {
            updateIMEPosition(cursorX, (float) posY);
        }
    }

    private void updateIMEPosition(float cursorX, float cursorY) {
        if (imePositionUpdateTimer.isDelayed(100)) {
            imePositionUpdateTimer.reset();
            PreEditRect rect = new PreEditRect();
            double scale = (ClientSettings.FIXED_SCALE.getValue() ? 1 : RenderSystem.getScaleFactor()) * 2;
            rect.setX((int) (cursorX * scale));
            rect.setY((int) ((cursorY + getFontRenderer().getHeight() - 3) * scale));
            IngameIMERenderer.InputCtx.setPreEditRect(rect);
        }
    }

    private int applyAlpha(int color, float alpha) {
        float originalAlpha = ((color >> 24) & 0xFF) / 255f;
        Color c = new Color(color, true);
        return new Color(
                c.getRed() / 255f,
                c.getGreen() / 255f,
                c.getBlue() / 255f,
                originalAlpha * alpha
        ).getRGB();
    }

    // ========== Getter/Setter ==========

    public void setMaxStringLength(int length) {
        maxStringLength = length;
        if (text.length() > length) {
            text = text.substring(0, length);
            if (this.callback != null)
                this.callback.onTextChanged(text);
        }
    }

    public void setPredicate(Predicate<String> predicate) {
        textPredicate = predicate;
    }

    public void func_175207_a(GuiPageButtonList.GuiResponder responder) {
        this.responder = responder;
    }

    public void setTextColor(int color) {
        enabledColor = color;
    }

    public void setDisabledTextColour(int color) {
        disabledColor = color;
    }

    public void setFocused(boolean focused) {
        if (focused && !isFocused) {
            cursorCounter = 0;
            Keyboard.enableRepeatEvents(true);
            if (IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue()) {
                IngameIMERenderer.setActivated(true);
            }
        }
        isFocused = focused;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean getVisible() {
        return visible;
    }

    public int getWidth() {
        return (int) (width);
    }

    public void setPosition(double x, double y) {
        xPosition = (float) x;
        yPosition = (float) y;
    }

    public void setBounds(double w, double h) {
        width = (float) w;
        height = (float) h;
    }

    private CFontRenderer getFontRenderer() {
        if (fontRenderer == null ||
                (fontRenderer.sizePx == FontManager.pf18.sizePx && fontRenderer != FontManager.pf18)) {
            fontRenderer = FontManager.pf18;
        }
        return fontRenderer;
    }

    // ========== 工具类 ==========

    public static final class MsTimer {
        private long time;
        @Setter
        private boolean active;

        public MsTimer() {
            time = System.currentTimeMillis();
            active = true;
        }

        public boolean reach(long duration) {
            return active && elapsed() >= duration;
        }

        public void reset() {
            time = System.currentTimeMillis();
        }

        public boolean sleep(long duration) {
            return sleep(duration, true);
        }

        public boolean sleep(long duration, boolean autoReset) {
            if (!active) {
                return false;
            }
            if (elapsed() >= duration) {
                if (autoReset) {
                    reset();
                }
                return true;
            }
            return false;
        }

        public long elapsed() {
            return System.currentTimeMillis() - time;
        }
    }
}