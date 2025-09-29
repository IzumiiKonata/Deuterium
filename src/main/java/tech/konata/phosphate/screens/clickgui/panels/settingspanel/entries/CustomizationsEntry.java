package tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Location;
import org.kc7bfi.jflac.apps.ExtensionFileFilter;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.font.CFontRenderer;

import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.SettingEntry;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.Setting;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author IzumiiKonata
 * @since 2024/10/26 21:15
 */
public class CustomizationsEntry extends SettingEntry {

    public CustomizationsEntry() {
        super("Customizations");
        super.imgLocation = Location.of(Phosphate.NAME + "/textures/settings/customize.svg");

        MultiThreadingUtil.runAsync(() -> {
            jFileChooser = new JFileChooser(new File("."), FileSystemView.getFileSystemView());
        });
    }

    JFileChooser jFileChooser;

    Localizable lCustomizeFont = Localizable.of("panel.settings.customizations.customize_font");

    boolean lmbPressed = false;

    @Override
    public void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {
        this.renderCustomizeFont(mouseX, mouseY, posX, posY, width, height, dWheel);

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;
    }

    Localizable lChooseRegular = Localizable.of("panel.settings.customizations.choose_regular");
    Localizable lChooseBold = Localizable.of("panel.settings.customizations.choose_bold");
    Localizable lSelect = Localizable.of("panel.settings.customizations.select");
    Localizable lReloadFontManager = Localizable.of("panel.settings.customizations.reload_font_manager");
    Localizable lReload = Localizable.of("panel.settings.customizations.reload");
    Localizable lResetToDefault = Localizable.of("panel.settings.customizations.reset_to_default");
    Localizable lReset = Localizable.of("panel.settings.customizations.reset");

    final java.util.List<Setting<?>> settings = Arrays.asList(
            GlobalSettings.FONT_OFFSET_X,
            GlobalSettings.FONT_OFFSET_Y
    );

    final List<SettingRenderer<?>> renderers = settings.stream().flatMap(s -> Stream.of(SettingRenderer.of(s))).collect(Collectors.toList());

    double panelHeight = 198;

    private void renderCustomizeFont(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        roundedRect(posX, posY, width, panelHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        FontManager.pf40.drawString(lCustomizeFont.get(), posX + 8, posY + 8, ThemeManager.get(ThemeManager.ThemeColor.Text));

        String regular = GlobalSettings.REGULAR_FONT_RENDERER_PATH.getValue();
        String regularName = " " + (regular.isEmpty() ? "" : regular.substring(regular.lastIndexOf(File.separatorChar) + 1));

        this.renderButton(mouseX, mouseY, lChooseRegular.get() + regularName, lSelect.get(), posX + 8, posY + 12 + FontManager.pf40.getHeight(), width, () -> {
            MultiThreadingUtil.runAsync(() -> {
                File file = this.chooseFile();

                if (file == null) {
                    return;
                }

                GlobalSettings.REGULAR_FONT_RENDERER_PATH.setValue(file.getAbsolutePath());
            });
        });

        String bold = GlobalSettings.BOLD_FONT_RENDERER_PATH.getValue();
        String boldName = " " + (bold.isEmpty() ? "" : bold.substring(bold.lastIndexOf(File.separatorChar) + 1));

        this.renderButton(mouseX, mouseY, lChooseBold.get() + boldName, lSelect.get(), posX + 8, posY + 38 + FontManager.pf40.getHeight(), width, () -> {
            MultiThreadingUtil.runAsync(() -> {
                File file = this.chooseFile();

                if (file == null) {
                    return;
                }

                GlobalSettings.BOLD_FONT_RENDERER_PATH.setValue(file.getAbsolutePath());
            });
        });

        this.renderButton(mouseX, mouseY, lReloadFontManager.get(), lReload.get(), posX + 8, posY + 64 + FontManager.pf40.getHeight(), width, () -> {
            FontManager.loadFonts();
//            MultiThreadingUtil.runAsync(() -> {
//                try {
//                    Thread.sleep(2000L);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
//                FontManager.checkDuplications();
//            });
        });

        this.renderButton(mouseX, mouseY, lResetToDefault.get(), lReset.get(), posX + 8, posY + 90 + FontManager.pf40.getHeight(), width, () -> {
            GlobalSettings.BOLD_FONT_RENDERER_PATH.reset();
            GlobalSettings.REGULAR_FONT_RENDERER_PATH.reset();
            GlobalSettings.FONT_OFFSET_X.reset();
            GlobalSettings.FONT_OFFSET_Y.reset();
            FontManager.loadFonts();
        });

        double offsetYSettings = posY + 118 + FontManager.pf40.getHeight();
        double spacing = 8;

        for (SettingRenderer<?> renderer : renderers) {

            renderer.x = posX + 8;
            renderer.y = offsetYSettings;

            renderer.width = width - 16;

            offsetYSettings += renderer.render(mouseX, mouseY, dWheel) + spacing;
        }

        panelHeight = offsetYSettings - posY + 4;
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.renderers.forEach(r -> r.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.renderers.forEach(r -> r.mouseReleased(mouseX, mouseY, mouseButton));
    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        this.renderers.forEach(r -> r.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick));
    }

    private void renderButton(double mouseX, double mouseY, String label, String buttonText, double posX, double posY, double width, Runnable onClick) {

        CFontRenderer btnFr = FontManager.pf20;
        double btnTextWidth = btnFr.getStringWidth(buttonText);

        double btnHeight = 20;

        FontManager.pf25bold.drawString(label, posX, posY + btnHeight * 0.5 - FontManager.pf25bold.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        this.roundedRectAccentColor(posX + width - 24 - btnTextWidth, posY, 8 + btnTextWidth, btnHeight, 6);
        btnFr.drawString(buttonText, posX + width - 24 - btnTextWidth + 4, posY + btnHeight * 0.5 - btnFr.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        if (Mouse.isButtonDown(0) && isHovered(mouseX, mouseY, posX + width - 24 - btnTextWidth, posY, 8 + btnTextWidth, btnHeight) && !lmbPressed) {
            lmbPressed = true;
            onClick.run();
        }
    }

    private File chooseFile() {

        if (jFileChooser.isShowing())
            return null;

        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setDragEnabled(true);
        ExtensionFileFilter filter = new ExtensionFileFilter();

        filter.addExtension("ttf");
        filter.addExtension("otf");

        filter.setDescription("TrueType 字体文件 (*.ttf, *.otf)");

        jFileChooser.resetChoosableFileFilters();
        jFileChooser.setFileFilter(filter);

        JFrame component = (JFrame) Minecraft.genFrame();

        int flag = jFileChooser.showOpenDialog(component);

        if (flag == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        }

        component.dispose();

        return null;
    }

}
