package tritium.bridge.rendering;

import lombok.Getter;
import today.opai.api.interfaces.render.ShaderUtil;

import java.awt.*;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 17:11
 */
public class ShaderUtilImpl implements ShaderUtil {

    @Getter
    private static final ShaderUtilImpl instance = new ShaderUtilImpl();

    @Override
    public void drawWithBloom(Runnable runnable) {

    }

    @Override
    public void pushBlur() {

    }

    @Override
    public void popBlur(int radius) {

    }

    @Override
    public void pushBloom() {

    }

    @Override
    public void popBloom(int radius, Color color) {

    }

    @Override
    public void pushGradient(Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {

    }

    @Override
    public void popGradient() {

    }

    @Override
    public void drawWithBlur(Runnable runnable) {

    }
}
