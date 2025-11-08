package tritium.management;

import lombok.Getter;
import lombok.SneakyThrows;
import tritium.event.eventapi.Handler;
import tritium.event.events.game.KeyPressedEvent;
import tritium.module.Module;
import tritium.module.impl.movement.AutoSprint;
import tritium.module.impl.other.NameSpoof;
import tritium.module.impl.other.NoCommand;
import tritium.module.impl.other.OpenConsole;
import tritium.module.impl.render.*;
import tritium.module.submodule.SubModule;
import tritium.settings.ClientSettings;
import tritium.settings.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class ModuleManager extends AbstractManager {

    @Getter
    private static final List<Module> modules = new ArrayList<>();

    // VISUAL
    public static final Interface hud = new Interface();
    public static final OpenClickGui clickGui = new OpenClickGui();
    public static final BlockAnimations blockAnimations = new BlockAnimations();
//    public static final BreadCrumbs breadCrumbs = new BreadCrumbs();
    public static final CameraPositions cameraPositions = new CameraPositions();
    public static final ItemPhysic itemPhysic = new ItemPhysic();
    public static final MoreParticles moreParticles = new MoreParticles();
    public static final NightVision nightVision = new NightVision();
    public static final WaveyCapes waveyCapes = new WaveyCapes();
    public static final Wings wings = new Wings();
    public static final Chat chat = new Chat();
    public static final HitColor hitColor = new HitColor();
    public static final WorldTime worldTime = new WorldTime();
    public static final MotionBlur motionBlur = new MotionBlur();
    public static final NoHurtCam noHurtCam = new NoHurtCam();
    public static final Perspective perspective = new Perspective();
    public static final ColorSaturation colorSaturation = new ColorSaturation();
    public static final BloodParticles bloodParticles = new BloodParticles();
    public static final BlockOverlay blockOverlay = new BlockOverlay();
    public static final BowZoom bowZoom = new BowZoom();
    public static final OldAnimation oldAnimation = new OldAnimation();
    public static final OpenNCMScreen openNCMScreen = new OpenNCMScreen();

    // MOVEMENT
    public static final AutoSprint autoSprint = new AutoSprint();

    // OTHER
    public static final NoCommand noCommand = new NoCommand();
    public static final NameSpoof nameSpoof = new NameSpoof();
    public static final OpenConsole openConsole = new OpenConsole();


    public ModuleManager() {
        super("ModuleManager");
    }

    @Handler
    public void onKeyPress(KeyPressedEvent event) {
        modules.stream().filter(m -> m.getKeyBind() == event.getKeyCode()).forEach(Module::toggle);
    }

    ;

    public Optional<Module> getModuleByName(String name) {

        for (Module m : ModuleManager.getModules()) {
            if (m.nameEquals(name))
                return Optional.of(m);
        }

        return Optional.empty();

    }

    public static List<Module> getModulesInCategory(Module.Category category) {
        return ModuleManager.getModules().stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public void init() {
        // Reflection time
        // :teapot:
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (Module.class.isAssignableFrom(field.getType())) {
                Module module = (Module) field.get(null);

                modules.add(module);
            }
        }

        // 设置自动装配
        for (Module module : modules) {

            for (Field moduleField : module.getClass().getDeclaredFields()) {
                moduleField.setAccessible(true);

                if (Setting.class.isAssignableFrom(moduleField.getType())) {
                    module.addSettings((Setting<?>) moduleField.get(module));
                }
            }

            List<SubModule<?>> subModules = module.getSubModules();
            if (!subModules.isEmpty()) {
                for (SubModule<?> subModule : subModules) {
                    for (Field declaredField : subModule.getClass().getDeclaredFields()) {
                        declaredField.setAccessible(true);

                        if (Setting.class.isAssignableFrom(declaredField.getType())) {
                            Setting<?> setting = (Setting<?>) declaredField.get(subModule);
                            module.addSettings(setting);
                        }
                    }
                }
            }
        }

        modules.add(ClientSettings.settingsModule);
    }

    @Override
    public void stop() {

    }

}
