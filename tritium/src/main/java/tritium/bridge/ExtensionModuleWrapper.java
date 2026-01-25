package tritium.bridge;

import today.opai.api.features.ExtensionModule;
import tritium.bridge.management.ValueManagerImpl;
import tritium.module.Module;
import tritium.settings.Setting;
import tritium.utils.i18n.Localizable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 18:58
 */
public class ExtensionModuleWrapper extends Module {

    private final ExtensionModule module;

    public ExtensionModuleWrapper(ExtensionModule module) {
        super(module.getName(), Category.fromEnumCategory(module.getCategory()));
        this.setName(Localizable.ofUntranslatable(module.getName()));
        this.setDescription(Localizable.ofUntranslatable(module.getDescription()));
        this.module = module;
    }

    @Override
    protected void createWrapper() {
        this.wrapper = module;
    }

    @Override
    public List<Setting<?>> getSettings() {
        return this.module.getValues().stream().map(v -> ValueManagerImpl.getWrapperToSettingMap().get(v)).collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return module.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        module.setEnabled(enabled);
    }

    @Override
    public void onEnable() {
        BridgeEventHandler.register(module.getEventHandler());
    }

    @Override
    public void onDisable() {
        BridgeEventHandler.unregister(module.getEventHandler());
    }

    @Override
    public void setKeyBind(int keyBind) {
        module.setKey(keyBind);
    }

    @Override
    public int getKeyBind() {
        return module.getKey();
    }

    @Override
    public Supplier<Boolean> getShouldRender() {
        return () -> !module.isHidden();
    }

    @Override
    public String getSuffix() {
        return module.getSuffix();
    }

    @Override
    public void setSuffix(String suffix) {
        module.setSuffix(suffix);
    }

}
