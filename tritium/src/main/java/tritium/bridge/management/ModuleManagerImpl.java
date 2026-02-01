package tritium.bridge.management;

import lombok.Getter;
import today.opai.api.enums.EnumModuleCategory;
import today.opai.api.interfaces.managers.ModuleManager;
import today.opai.api.interfaces.modules.PresetModule;
import tritium.module.Module;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:46
 */
public class ModuleManagerImpl implements ModuleManager {

    @Getter
    private static final ModuleManagerImpl instance = new ModuleManagerImpl();

    @Override
    public PresetModule getModule(String name) {

        for (Module module : tritium.management.ModuleManager.getModules()) {
            if (module.getInternalName().equals(name) || module.getName().get().equals(name))
                return module.getWrapper();
        }

        return null;
    }

    @Override
    public Collection<PresetModule> getModulesInCategory(EnumModuleCategory category) {
        return tritium.management.ModuleManager.getModules().stream().filter(module -> module.getCategory() == Module.Category.fromEnumCategory(category)).map(Module::getWrapper).collect(Collectors.toList());
    }

    @Override
    public Collection<PresetModule> getModules() {
        return tritium.management.ModuleManager.getModules().stream().map(Module::getWrapper).collect(Collectors.toList());
    }
}
