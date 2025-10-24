package tritium.bridge.module;

import today.opai.api.interfaces.modules.PresetModule;
import today.opai.api.interfaces.modules.Value;
import tritium.module.Module;
import tritium.settings.Setting;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:24
 */
public class PresetModuleWrapper implements PresetModule {

    private final Module module;

    public PresetModuleWrapper(Module module) {
        this.module = module;
    }

    @Override
    public String getDescription() {
        return this.module.getDescription().get();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.module.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return this.module.isEnabled();
    }

    @Override
    public String getName() {
        return this.module.getInternalName();
    }

    @Override
    public int getKey() {
        return this.module.getKeyBind();
    }

    @Override
    public void setKey(int key) {
        this.module.setKeyBind(key);
    }

    @Override
    public boolean isHidden() {
        return !this.module.getShouldRender().get();
    }

    @Override
    public void setHidden(boolean hidden) {
        this.module.setShouldRender(() -> !hidden);
    }

    @Override
    public Collection<Value<?>> getValues() {
        return this.module.getSettings().stream().map(Setting::getWrapper).collect(Collectors.toList());
    }
}
