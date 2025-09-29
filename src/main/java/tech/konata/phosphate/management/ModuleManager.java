package tech.konata.phosphate.management;

import lombok.Getter;
import lombok.SneakyThrows;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.game.KeyPressedEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.module.impl.movement.AutoSprint;
import tech.konata.phosphate.module.impl.other.LoyisaServerPrefix;
import tech.konata.phosphate.module.impl.other.NameSpoof;
import tech.konata.phosphate.module.impl.other.NoCommand;
import tech.konata.phosphate.module.impl.other.OpenConsole;
import tech.konata.phosphate.module.impl.render.*;
import tech.konata.phosphate.module.submodule.SubModule;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.Setting;
import tech.konata.phosphate.settings.StringModeSetting;

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
    public static final BreadCrumbs breadCrumbs = new BreadCrumbs();
    public static final CameraPositions cameraPositions = new CameraPositions();
    public static final HotBar hotBar = new HotBar();
    public static final ItemPhysic itemPhysic = new ItemPhysic();
    public static final MoreParticles moreParticles = new MoreParticles();
    public static final NightVision nightVision = new NightVision();
    public static final WaveyCapes waveyCapes = new WaveyCapes();
    public static final Wings wings = new Wings();
    public static final Chat chat = new Chat();
    public static final SmallPlayerModel smallPlayerModel = new SmallPlayerModel();
    public static final HitColor hitColor = new HitColor();
    public static final WorldTime worldTime = new WorldTime();
    public static final MotionBlur motionBlur = new MotionBlur();
    public static final NoHurtCam noHurtCam = new NoHurtCam();
    public static final NoAchievements noAchievements = new NoAchievements();
    public static final Perspective perspective = new Perspective();
    public static final ColorSaturation colorSaturation = new ColorSaturation();
    public static final BloodParticles bloodParticles = new BloodParticles();
    public static final BlockOverlay blockOverlay = new BlockOverlay();
    public static final BowZoom bowZoom = new BowZoom();
    public static final Halo halo = new Halo();
    public static final PerfectAimingAngle perfectAimingAngle = new PerfectAimingAngle();
    public static final OldAnimation oldAnimation = new OldAnimation();
    public static final ShaderTrails shaderCape = new ShaderTrails();

    // MOVEMENT
    public static final AutoSprint autoSprint = new AutoSprint();

    // OTHER
    public static final NoCommand noCommand = new NoCommand();
    public static final LoyisaServerPrefix loyisServerPrefix = new LoyisaServerPrefix();
    public static final NameSpoof nameSpoof = new NameSpoof();
    public static final OpenConsole openConsole = new OpenConsole();
//    public static final HeadCrasher hc = new HeadCrasher();


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

        List<Module> collect = modules.stream().distinct().collect(Collectors.toList());

        modules.clear();
        modules.addAll(collect);

        for (Module module : modules) {

            // clear settings for reload command
            module.getSettings().removeIf(s -> !(module.getSettings().indexOf(s) == 0 && s instanceof StringModeSetting && s.getInternalName().equalsIgnoreCase("Mode")));

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

        modules.add(GlobalSettings.dummyModule);
    }

    @Override
    public void stop() {

    }

}
