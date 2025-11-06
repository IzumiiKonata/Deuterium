package tritium.command.impl;

import tritium.Tritium;
import tritium.command.Command;
import tritium.management.EventManager;
import tritium.management.Localizer;
import tritium.management.ModuleManager;
import tritium.management.WidgetsManager;
import tritium.module.Module;
import tritium.ncm.music.CloudMusic;
import tritium.rendering.shader.Shaders;
import tritium.rendering.shader.impl.*;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.widget.Widget;
import tritium.rendering.shader.impl.*;

/**
 * @author IzumiiKonata
 * @since 2024/9/17 19:26
 */
public class Reload extends Command {

    public Reload() {
        super("Reload", "reloads translations.", "reload", "r");
    }

    @Override
    public void execute(String[] args) {
        Localizer.loadLang();
        this.print("Reloaded translations!");

//        PlayerAdapter.getInstances().clear();
//        this.print("Reloaded Cloud!");

        Tritium.getInstance().getConfigManager().stop();

//        this.reloadModules();
//        this.print("Reloaded Modules!");

//        Phosphate.getInstance().getConfigManager().loadConfig();

        Shaders.POST_BLOOM_SHADER = new BloomShader();
        Shaders.UI_BLOOM_SHADER = Shaders.POST_BLOOM_SHADER;
        Shaders.UI_POST_BLOOM_SHADER = Shaders.POST_BLOOM_SHADER;
        Shaders.GAUSSIAN_BLUR_SHADER = new GaussianBlurShader();
        Shaders.UI_GAUSSIAN_BLUR_SHADER = Shaders.GAUSSIAN_BLUR_SHADER;
        Shaders.BLEND = new BlendShader();
        Shaders.MOTION = new MotionShader();
        Shaders.COLOR = new ColorShader();

        Shaders.ROQ_SHADER = new ROQShader();
        Shaders.ROGQ_SHADER = new ROGQShader();
        Shaders.RQ_SHADER = new RQShader();
        Shaders.RQT_SHADER = new RQTShader();
        Shaders.RQG_SHADER = new RQGShader();

        this.print("Reloaded shaders!");

        MultiThreadingUtil.runAsync(() -> {
            CloudMusic.initNCM();
            NCMScreen.getInstance().layout();
            this.print("Reloaded NCM!");
        });
    }

    private void reloadModules() {

        for (Module module : ModuleManager.getModules()) {
            EventManager.unregister(module);
        }

        ModuleManager.getModules().clear();
        Tritium.getInstance().getModuleManager().init();

        for (Widget widget : WidgetsManager.getWidgets()) {
            EventManager.unregister(widget);
        }

        WidgetsManager.getWidgets().clear();
        Tritium.getInstance().getWidgetsManager().init();

    }
}
