package tritium.screens;

import lombok.Getter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.Tritium;
import tritium.management.CommandManager;
import tritium.management.FontManager;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.rendering.ui.widgets.TextFieldWidget;
import tritium.utils.other.StringFormatter;

/**
 * @author IzumiiKonata
 * Date: 2025/11/11 21:27
 */
public class ConsoleScreen extends BaseScreen {

    @Getter
    private static final ConsoleScreen instance = new ConsoleScreen();

    RectWidget base = new RectWidget();
    TextFieldWidget textField;
    ScrollPanel logsPanel;

    boolean titleBarDragging = false;

    public ConsoleScreen() {
        this.layout();
        this.registerConsoleCommands();
    }

    private void registerConsoleCommands() {
        CommandManager.registerSimpleCommand("clear", new String[] { "cls" }, args -> {
            logsPanel.getChildren().clear();
        });
    }

    @Override
    public void initGui() {
        this.textField.setFocused(true);
    }

    private void layout() {
        this.base = new RectWidget();

        this.base.setBounds(100, 100, 400, 300);

        double titleBarHeight = 12;
        // title bar
        RectWidget titleBar = new RectWidget();
        {
            this.base.addChild(titleBar);

            titleBar.setBeforeRenderCallback(() -> {
                titleBar.setBounds(0, 0, this.base.getWidth(), titleBarHeight);
                titleBar.setColor(0xFF353535);
            });

            titleBar.setOnClickCallback((x, y, i) -> {
                if (i == 0)
                    this.titleBarDragging = true;
                return true;
            });

            LabelWidget lwConsole = new LabelWidget("C O N S O L E", FontManager.pf14bold);
            titleBar.addChild(lwConsole);
            lwConsole.setClickable(false);

            lwConsole.setBeforeRenderCallback(() -> {
                lwConsole.setColor(-1);
                lwConsole.centerVertically();
                lwConsole.setPosition(lwConsole.getRelativeY(), lwConsole.getRelativeY());
            });
        }

        double textFieldBgHeight = 12;
        // text field
        {
            this.textField = new TextFieldWidget(FontManager.pf14);

            RectWidget textFieldBg = new RectWidget();
            this.base.addChild(textFieldBg);

            textFieldBg.setBeforeRenderCallback(() -> {
                textFieldBg.setBounds(0, textFieldBg.getParentHeight() - textFieldBgHeight, this.base.getWidth(), textFieldBgHeight);
                textFieldBg.setColor(0xFF000000);
            });

            textFieldBg.addChild(this.textField);

            this.textField.setColor(-1);
            // gray
            this.textField.setDisabledTextColor(0xFF808080);
            this.textField.drawUnderline(false);

            this.textField.setBeforeRenderCallback(() -> {
                this.textField.setMargin(2);
            });

            this.textField.setOnKeyTypedCallback((typedChar, keyCode) -> {
                if (this.textField.isFocused()) {
                    if (keyCode == Keyboard.KEY_RETURN && !this.textField.getText().isEmpty()) {
                        Tritium.getInstance().getCommandManager().execute(this.textField.getText());
                        this.textField.setText("");
                    }

                    if (keyCode == Keyboard.KEY_ESCAPE) {
                        mc.displayGuiScreen(null);
                    }

                    return true;
                }

                return false;
            });
        }

        // logs container
        {
            RectWidget logsBg = new RectWidget();
            this.base.addChild(logsBg);

            logsBg.setBeforeRenderCallback(() -> {
                logsBg.setBounds(0, titleBarHeight, this.base.getWidth(), this.base.getHeight() - titleBarHeight - textFieldBgHeight);
                logsBg.setColor(0xFF232323);
            });

            logsPanel = new ScrollPanel();
            logsBg.addChild(logsPanel);
            logsPanel.setSpacing(3);

            logsPanel.setBeforeRenderCallback(() -> {
                logsPanel.setMargin(3);
            });
        }

        // close button
        {
            Panel closeBg = new Panel();
            titleBar.addChild(closeBg);
            closeBg.setBeforeRenderCallback(() -> {
                closeBg.setBounds(this.base.getWidth() - titleBarHeight, 0, titleBarHeight, titleBarHeight);
            });

            LabelWidget lblClose = new LabelWidget("x", FontManager.pf14bold);
            closeBg.addChild(lblClose);
            lblClose.setClickable(false);
            lblClose.setBeforeRenderCallback(() -> {
                lblClose.setColor(closeBg.isHovering() ? 0xFFFFFFFF : 0xFF808080);
                lblClose.center();
            });

            closeBg.setOnClickCallback((x, y, i) -> {
                if (i == 0)
                    mc.displayGuiScreen(null);
                return true;
            });
        }

//        for (int i = 0; i <= 100; i++)
//            addLog("Hello, World! [" + i + "]");

//        this.logsPanel.scrollToEnd();
    }

    public void push(String log) {
        boolean scrolledToEnd = logsPanel.isScrolledToEnd();

        LabelWidget lwLog = new LabelWidget(log, FontManager.pf14);
        this.logsPanel.addChild(lwLog);
        lwLog.setColor(-1);
        // lol
        lwLog.setHeight(2);

        if (scrolledToEnd)
            logsPanel.scrollToEnd();
    }

    public static void log(String log) {
        ConsoleScreen.getInstance().push(log);
    }

    public static void log(String format, Object... args) {
        ConsoleScreen.getInstance().push(StringFormatter.format(format, args));
    }

    // dragging
    private double lastMouseX, lastMouseY;

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        this.base.renderWidget(mouseX, mouseY, Mouse.getDWheel());

        if (!Mouse.isButtonDown(0))
            this.titleBarDragging = false;

        // dragging
        {
            if (this.titleBarDragging) {
                double w = this.lastMouseX - this.base.getX();
                double h = this.lastMouseY - this.base.getY();

                this.base.setPosition(mouseX - w, mouseY - h);
//                this.topRect.setPosition(mouseX - w, mouseY - h);
            }

            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.base.onKeyTypedReceived(typedChar, keyCode))
            return;

        if (keyCode == Keyboard.KEY_GRAVE || keyCode == Keyboard.KEY_ESCAPE)
            this.mc.displayGuiScreen(null);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.base.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }
}
