package tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries;

import net.minecraft.util.Location;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.SettingEntry;
import tech.konata.phosphate.screens.clickgui.settingrenderer.ColorRenderer;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author IzumiiKonata
 * @since 2024/10/26 20:46
 */
public class VideoSettingsEntry extends SettingEntry {

    final List<Setting<?>> settings = Arrays.asList(
            GlobalSettings.VIDEO_PRESET,
            GlobalSettings.FRAME_PREDICT,
            GlobalSettings.FIXED_SCALE,
            GlobalSettings.RENDER_GLOW,
            GlobalSettings.RENDER_BLUR,
            GlobalSettings.RENDER_SELF_NAME_TAG,
            GlobalSettings.CENTER_INVENTORY,
            null, // split
            GlobalSettings.FULL_SCREEN_RESOLUTION,
            GlobalSettings.FULL_SCREEN_REFRESH_RATE,
            GlobalSettings.WINDOW_THEME_STYLE,
            GlobalSettings.WINDOW_BACKDROP_STYLE,
            GlobalSettings.WINDOW_CORNER_STYLE,
            GlobalSettings.HIDE_BORDER,
            GlobalSettings.CUSTOM_BORDER,
            GlobalSettings.BORDER_COLOR,
            GlobalSettings.BORDER_COLOR.rainbow,
            GlobalSettings.DEBUG_MODE/*,
            GlobalSettings.BORDERLESS_WINDOW*/
    );

    final List<SettingRenderer<?>> renderers = settings.stream().flatMap(s -> Stream.of(SettingRenderer.of(s))).collect(Collectors.toList());

    public VideoSettingsEntry() {
        super("Video Settings");
        super.imgLocation = Location.of(Phosphate.NAME + "/textures/settings/settings.svg");
    }

    @Override
    public void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        Stencil.write();
        Rect.draw(posX, posY, width + 8, height, -1, Rect.RectType.EXPAND);
        Stencil.erase();

        double offsetX = posX;
        double offsetY = posY;
        double spacing = 8;
        double settingWidth = width * 0.5 - spacing * 0.5;

        for (SettingRenderer<?> renderer : renderers) {

            if (renderer == null) {
                offsetX += settingWidth + spacing;
                offsetY = posY;
                continue;
            }

            if (!renderer.setting.shouldRender())
                continue;

            renderer.x = offsetX;
            renderer.y = offsetY;

            renderer.width = settingWidth;

            offsetY += renderer.render(mouseX, mouseY, dWheel) + spacing;
        }

        Stencil.dispose();

        if (ColorRenderer.floatingPane != null) {
            ColorRenderer.floatingPane.renderPane(mouseX, mouseY);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (ColorRenderer.floatingPane == null) {
            this.renderers.forEach(r -> { if (r != null) r.mouseClicked(mouseX, mouseY, mouseButton); } );
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (ColorRenderer.floatingPane == null) {
            this.renderers.forEach(r -> { if (r != null) r.mouseReleased(mouseX, mouseY, mouseButton); } );
        }
    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        if (ColorRenderer.floatingPane == null) {
            this.renderers.forEach(r -> { if (r != null) r.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick); } );
        }
    }

}
