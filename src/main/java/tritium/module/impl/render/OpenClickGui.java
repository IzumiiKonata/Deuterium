package tritium.module.impl.render;

import org.lwjglx.input.Keyboard;
import tritium.module.Module;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
public class OpenClickGui extends Module {

    public OpenClickGui() {
        super("ClickGui", Category.RENDER);
        super.setKeyBind(Keyboard.KEY_RSHIFT);
        super.setShouldRender(() -> false);
    }

    @Override
    public void onEnable() {

        this.toggle();

    }
}
