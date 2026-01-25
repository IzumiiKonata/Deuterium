package tritium.screens;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tritium.Tritium;
import tritium.command.Command;
import tritium.management.CommandManager;
import tritium.management.FontManager;
import tritium.rendering.Rect;
import tritium.rendering.entities.impl.TextField;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.rendering.ui.widgets.TextFieldWidget;
import tritium.utils.other.StringFormatter;
import tritium.utils.other.info.Version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    boolean titleBarDragging = false, bottomCornerDragging = false;

    final List<String> commandHistory = new ArrayList<>();
    int commandHistoryIndex = 0;

    public ConsoleScreen() {
        this.layout();
        this.registerConsoleCommands();
    }

    private void registerConsoleCommands() {

        if (Tritium.getVersion().getReleaseType() == Version.ReleaseType.Dev) {
            // reload the screen's layout
            CommandManager.registerCommand("layout", this::layout).setDescription("Reload ConsoleScreen's layout");
        }

        CommandManager.registerCommand("clear", new String[] { "cls" }, () -> {
            logsPanel.getChildren().clear();
        }).setDescription("Clear the console");

        CommandManager.registerCommand("quit", () -> {
            Minecraft.getMinecraft().shutdown();
        }).setDescription("Quit the game");

        CommandManager.registerCommand("disconnect", () -> {
            this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.playerController.setNoCheckDisconnect(true);

            log("Ok disconnected");
        }).setDescription("Disconnect from the current server silently");

        CommandManager.registerCommand("connect", GuiConnecting::connectTo, String.class, "server address").setDescription("Connect to a server");

        CommandManager.registerCommand("help", () -> {
            log("Available commands:");

            CommandManager.getCommands()
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
                    .forEach(command -> {
                        for (Command.InvokeInfo invokeInfo : command.getInvokeInfos()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(command.getName().toLowerCase());

                            for (int j = 0; j < invokeInfo.methodToInvoke.getParameters().length; j++) {
                                sb.append(" ").append("<").append(invokeInfo.annotation.paramNames().length > 0 ? (invokeInfo.annotation.paramNames()[j]) : ("arg" + (j + 1))).append(">");
                            }

                            ConsoleScreen.log("    {} {}", sb.toString(), invokeInfo.desc.isEmpty() ? "" : "- " + invokeInfo.desc);
                        }
                    });
        }).setDescription("Show help for all commands");

        CommandManager.registerCommand("help", (String commandName) -> {

            Command command = null;
            for (Command cmd : CommandManager.getCommands()) {
                if (cmd.getName().equalsIgnoreCase(commandName)) {
                    command = cmd;
                    break;
                }
            }

            if (command == null) {
                log(EnumChatFormatting.RED + "No such command: {}", commandName);
                return;
            }

            log("{}: ", command.getName());
            for (Command.InvokeInfo invokeInfo : command.getInvokeInfos()) {
                StringBuilder sb = new StringBuilder();
                sb.append(command.getName().toLowerCase());

                for (int j = 0; j < invokeInfo.methodToInvoke.getParameters().length; j++) {
                    sb.append(" ").append("<").append(invokeInfo.annotation.paramNames().length > 0 ? (invokeInfo.annotation.paramNames()[j]) : ("arg" + (j + 1))).append(">");
                }

                ConsoleScreen.log("    {} {}", sb.toString(), invokeInfo.desc.isEmpty() ? "" : "- " + invokeInfo.desc);
            }

        }, String.class, "command name").setDescription("Show help for a command");

        CommandManager.registerCommand("bind_key", (key, command) -> {

            int keyIndex = Keyboard.getKeyIndex(key.startsWith("Key ") ? key : key.toUpperCase());

            if (keyIndex == Keyboard.KEY_NONE) {
                log(EnumChatFormatting.RED + "Invalid key: {}", key);
                return;
            }

            CommandManager.getKeyToCommandMap().put(keyIndex, command);
            log("Bound \"{}\" to \"{}\"", Keyboard.getKeyName(keyIndex), command);

        }, String.class, String.class, "key", "command").setDescription("bind a command to the key");

    }

    private void addCommandHistory(String command) {
        if (commandHistory.isEmpty() || !commandHistory.getLast().equals(command))
            commandHistory.add(command);
        commandHistoryIndex = commandHistory.size();
    }

    @Override
    public void initGui() {
        this.textField.setFocused(true);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
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

                if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_GRAVE) {
                    mc.displayGuiScreen(null);

                    if (keyCode == Keyboard.KEY_GRAVE && !this.textField.getText().isEmpty()) {
                        this.textField.setText(this.textField.getText().substring(0, this.textField.getText().length() - 1));
                    }

                    return true;
                }

                if (this.textField.isFocused()) {
                    String text = this.textField.getText();
                    if ((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) && !text.isEmpty()) {
                        Tritium.getInstance().getCommandManager().execute(text);
                        this.addCommandHistory(text);
                        this.textField.setText("");
                        this.logsPanel.scrollToEnd();
                        return true;
                    }

                    if (keyCode == Keyboard.KEY_UP) {
                        if (commandHistoryIndex > 0) {
                            commandHistoryIndex--;
                            this.textField.setText(commandHistory.get(commandHistoryIndex));
                        }
                        return true;
                    }

                    if (keyCode == Keyboard.KEY_DOWN) {
                        if (commandHistoryIndex < commandHistory.size() - 1) {
                            commandHistoryIndex++;
                            this.textField.setText(commandHistory.get(commandHistoryIndex));
                        } else if (commandHistoryIndex == commandHistory.size()) {
                            this.textField.setText("");
                        }
                        return true;
                    }

                    if (keyCode == Keyboard.KEY_TAB) {
                        TextField.TextChangedCallback callback = this.textField.getTextField().callback;
                        if (callback != null)
                            callback.onTextChanged(null);
                        return true;
                    }
                }

                return false;
            });

            // drag to change window size
            {
                Panel panel = new Panel();
                this.textField.addChild(panel);

                panel.setBeforeRenderCallback(() -> {
                    panel.setBounds(this.textField.getWidth() - textFieldBgHeight + 2, -2, textFieldBgHeight, textFieldBgHeight);

                    double lineWidth = 1;
                    double length = 6;
                    float alpha = panel.isHovering() ? .8f : .4f;
                    Rect.draw(panel.getX() + panel.getWidth() - length, panel.getY() + panel.getHeight() - lineWidth, length, lineWidth, hexColor(1, 1, 1, alpha));
                    Rect.draw(panel.getX() + panel.getWidth() - lineWidth, panel.getY() + panel.getHeight() - length, lineWidth, length - lineWidth, hexColor(1, 1, 1, alpha));
                });

                panel.setOnClickCallback((x, y, i) -> {
                    if (i == 0)
                        bottomCornerDragging = true;

                    return true;
                });
            }
        }

        // logs container
        {
            RectWidget logsBg = new RectWidget();
            this.base.addChild(logsBg);

            logsBg.setBeforeRenderCallback(() -> {
                logsBg.setBounds(0, titleBarHeight, this.base.getWidth(), this.base.getHeight() - titleBarHeight - textFieldBgHeight);
                logsBg.setColor(0xFF232323);
            });

            logsPanel = new ScrollPanel() {
                @Override
                protected double getChildrenHeightSum() {
                    return logsPanel.getChildren().size() * (FontManager.pf14.getFontHeight() + logsPanel.getSpacing()) - logsPanel.getSpacing();
                }
            };
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

        // suggestions
        {
            RectWidget rw = new RectWidget();

            rw.setColor(0xFF353535);
            base.addChild(rw);

            List<Command> suggestions = new CopyOnWriteArrayList<>();

            rw.setBeforeRenderCallback(() -> {
                rw.setBounds(0, base.getHeight() + 2, base.getWidth(), suggestions.size() * (FontManager.pf14.getFontHeight() + 4));
            });

            Panel p = new Panel();

            rw.addChild(p);

            p.setBeforeRenderCallback(() -> {
                p.setMargin(0);

                double offsetX = p.getX();
                double offsetY = p.getY();

                for (Command cmd : suggestions) {
                    FontManager.pf14.drawString(cmd.getName().toLowerCase() + (cmd.getDescription() == null ? "" : " - " + cmd.getDescription()), offsetX + 2, offsetY + 2, -1);
                    offsetY += FontManager.pf14.getFontHeight() + 4;
                }
            });

            final int[] compIndex = {0};
            final boolean[] discardNextCompletion = {false};

            this.textField.setTextChangedCallback(text -> {

                // completion
                if (text == null) {

                    if (suggestions.isEmpty())
                        return;

                    if (compIndex[0] > suggestions.size() - 1) {
                        compIndex[0] = 0;
                    }

                    discardNextCompletion[0] = true;
                    this.textField.setText(suggestions.get(compIndex[0]).getName().toLowerCase() + " ");
                    compIndex[0]++;

                } else {

                    if (discardNextCompletion[0]) {
                        discardNextCompletion[0] = false;
                        return;
                    }

                    String[] s = text.split(" ");

                    suggestions.clear();
                    compIndex[0] = 0;

                    if (s.length == 0) {
                        return;
                    }

                    String name = s[0];

                    if (name.isEmpty())
                        return;

                    for (Command cmd : CommandManager.getCommands()) {
                        if (cmd.getName().toLowerCase().startsWith(name.toLowerCase())) {
                            suggestions.add(cmd);
                        }
                    }
                }

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
        lwLog.setHeight(Math.max(2, lwLog.getHeight()));

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

        if (!Mouse.isButtonDown(0)) {
            this.titleBarDragging = false;
            this.bottomCornerDragging = false;
        }

        // dragging
        {
            if (this.titleBarDragging) {
                double w = this.lastMouseX - this.base.getX();
                double h = this.lastMouseY - this.base.getY();

                this.base.setPosition(mouseX - w, mouseY - h);
//                this.topRect.setPosition(mouseX - w, mouseY - h);
            }

            if (this.bottomCornerDragging) {
                this.base.setBounds(Math.max(400, this.base.getWidth() + mouseX - this.lastMouseX), Math.max(300, this.base.getHeight() + mouseY - this.lastMouseY));
            }
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
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

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
