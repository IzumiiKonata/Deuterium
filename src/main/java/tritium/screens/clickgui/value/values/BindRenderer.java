package tritium.screens.clickgui.value.values;

import org.lwjgl.input.Keyboard;
import tritium.event.eventapi.Handler;
import tritium.event.events.game.KeyPressedEvent;
import tritium.management.EventManager;
import tritium.management.FontManager;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.settings.BindSetting;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:20
 */
public class BindRenderer extends AbstractWidget<BindRenderer> {

    private final BindSetting setting;

    final AtomicBoolean listening = new AtomicBoolean(false);

    public BindRenderer(BindSetting setting) {
        this.setting = setting;
        double height = FontManager.pf14.getHeight() + 4;
        this.setBounds(142, height);

        LabelWidget label = new LabelWidget(() -> setting.getName().get(), FontManager.pf14);
        label.setBeforeRenderCallback(() -> {
            label.setColor(ClickGui.getColor(20));
        });
        label.setPosition(0, height * .5 - FontManager.pf14.getHeight() * .5);
        this.addChild(label);

        RectWidget rw = new RectWidget();
        this.addChild(rw);

        rw
            .setShouldSetMouseCursor(true)
            .setBeforeRenderCallback(() -> {
                String keyName = this.getKeyName();

                double width = Math.max(8, FontManager.pf14.getStringWidthD(keyName) + 4);
                rw.setPosition(this.getWidth() - rw.getWidth(), 0);
                rw.setBounds(width, height);
                rw.setColor(rw.isHovering() ? ClickGui.getColor(24) : ClickGui.getColor(23));
            })
            .setOnClickCallback((mouseX, mouseY, mouseButton) -> {
                if (mouseButton == 0) {
                    if (!listening.get()) {
                        this.listening.set(true);

                        EventManager.register(new Object() {

                            boolean skipFirst = false;

                            @Handler
                            public void onKeyEvent(KeyPressedEvent event) {

                                if (event.getKeyCode() < 0 && !skipFirst) {
                                    skipFirst = true;
                                    return;
                                }

                                if (event.getKeyCode() < 0 && BindRenderer.this.listening.get()) {
                                    BindRenderer.this.listening.set(false);
                                    setting.setValue(event.getKeyCode());
                                    EventManager.unregister(this);
                                    return;
                                }

                                if (!BindRenderer.this.listening.get()) {
                                    EventManager.unregister(this);
                                }

                            }

                        });

                        return true;
                    }
                }

                return true;
            })
            .setOnKeyTypedCallback((character, keyCode) -> {
                if (this.listening.get()) {
                    listening.set(false);

                    if (keyCode == Keyboard.KEY_ESCAPE)
                        keyCode = Keyboard.KEY_NONE;

                    setting.setValue(keyCode);
                    return true;
                }

                return false;
            });

        LabelWidget lblKeyName = new LabelWidget(this::getKeyName, FontManager.pf14);
        rw.addChild(lblKeyName);

        lblKeyName.setClickable(false);

        lblKeyName.setBeforeRenderCallback(() -> {
            lblKeyName.setColor(RenderSystem.reAlpha(ClickGui.getColor(20), this.getAlpha()));
            lblKeyName.center();
        });
    }

    private String getKeyName() {
        if (this.listening.get())
            return "Listening...";

        if (setting.getValue() < 0)
            return switch (setting.getValue() + 100) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MIDDLE";
                case 3 -> "Mouse 4";
                case 4 -> "Mouse 5";
                default -> "UNK: " + setting.getValue();
            };

        return Keyboard.getKeyName(setting.getValue());
    }

    @Override
    public double getHeight() {
        this.setHidden(!setting.shouldRender());

        if (!setting.shouldRender())
            return 0;
        return super.getHeight();
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

    }
}
