package tritium.module;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.lwjglx.input.Keyboard;
import tritium.management.Localizer;
import tritium.module.submodule.SubModule;
import tritium.utils.i18n.Localizable;
import tritium.interfaces.SharedConstants;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.EventManager;
import tritium.settings.Setting;
import tritium.settings.StringModeSetting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class Module implements SharedConstants, SharedRenderingConstants {

    @Getter
    private final String internalName;
    @Getter
    @Setter
    protected Localizable name, description;

    @Getter
    private final Category category;

    @Getter
    private boolean enabled;

    @Getter
    @Setter
    private int keyBind = Keyboard.KEY_NONE;

    @Getter
    @Setter
    private String suffix = "";

    @Getter
    @Setter
    private Supplier<Boolean> shouldRender = () -> true;

    @Getter
    public final List<Setting<?>> settings = new ArrayList<>();

    @Getter
    private final List<SubModule<?>> subModules = new ArrayList<>();

    public Module(String internalName, Category category) {

        this.internalName = internalName;
        this.category = category;

        String lowerCase = internalName.toLowerCase();

        this.name = Localizable.of("module." + lowerCase + ".name");
        this.description = Localizable.of("module." + lowerCase + ".desc");

//        if (!internalName.equals("Setting") && category != Category.WIDGET)
//            ModuleManager.getModules().add(this);
    }

    public boolean nameEquals(String another) {

        if (this.getInternalName().equalsIgnoreCase(another) || this.getInternalName().replace(" ", "").equalsIgnoreCase(another))
            return true;

        if (this.getName().get().equalsIgnoreCase(another) || this.getName().get().replace(" ", "").equalsIgnoreCase(another))
            return true;

        return false;
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.isEnabled()) {

            EventManager.register(this);
            this.onEnable();

        } else {

            EventManager.unregister(this);
            this.onDisable();

        }
    }

    public void toggle() {
        this.enabled = !this.enabled;

        SubModule<?> subModule = this.getCurrentSubModule();
        if (this.isEnabled()) {

            if (subModule != null) {
                EventManager.register(subModule);
                subModule.onEnable();
            }

            EventManager.register(this);
            this.onEnable();

        } else {

            if (subModule != null) {
                EventManager.unregister(subModule);
                subModule.onDisable();
            }

            EventManager.unregister(this);
            this.onDisable();

        }

    }

    @SafeVarargs
    @SneakyThrows
    public final void addSubModules(SubModule<? extends Module>... subModules) {
        this.subModules.addAll(Arrays.asList(subModules));

        List<String> names = new ArrayList<>();

        for (SubModule subModule : subModules) {
            subModule.setModule(this);
            names.add(subModule.getInternalName());
        }

        StringModeSetting subModes = new StringModeSetting("Mode", names.get(0), names) {
            @Override
            public void onModeChanged(String before, String now) {

                SubModule<?> bef = getSubModuleByName(before);
                EventManager.unregister(bef);
                bef.onDisable();

                if (isEnabled()) {
                    SubModule<?> aft = getSubModuleByName(now);
                    EventManager.register(aft);
                    aft.onEnable();
                }
            }

            @Override
            public String getNameForRender(String modeIn) {
                return Localizer.getInstance().translate("module." + Module.this.getInternalName() + ".submodule." + modeIn + ".name");
            }
        };

        for (SubModule<?> subModule : subModules) {
            for (Field declaredField : subModule.getClass().getDeclaredFields()) {
                declaredField.setAccessible(true);

                if (Setting.class.isAssignableFrom(declaredField.getType())) {
                    Setting<?> setting = (Setting<?>) declaredField.get(subModule);
                    final Supplier<Boolean> beforeShow = setting.getShouldRender();
                    setting.setShouldRender(() -> subModes.getValue().equals(subModule.getInternalName()) && beforeShow.get());
                    this.addSettings(setting);
                }
            }
        }

        this.settings.add(0, subModes);
    }

    public SubModule<?> getSubModuleByName(String name) {
        if (subModules.isEmpty())
            return null;

        for (SubModule<?> subModule : subModules) {
            if (name.equals(subModule.getInternalName()))
                return subModule;
        }

        return null;
    }

    public SubModule<?> getCurrentSubModule() {
        if (subModules.isEmpty())
            return null;

        for (SubModule<?> subModule : subModules) {
            if (this.getSubModes().getValue().equals(subModule.getInternalName()))
                return subModule;
        }

        return null;
    }

    public StringModeSetting getSubModes() {
        return (StringModeSetting) this.settings.get(0);
    }

    public void loadConfig(JsonObject directory) {
        directory.entrySet().forEach(data -> {
            switch (data.getKey()) {
                case "Key": {
                    this.setKeyBind(data.getValue().getAsInt());
                    break;
                }
                case "Enabled": {
                    this.setEnabled(data.getValue().getAsBoolean());
                    break;

                }
            }
            Setting<?> val = this.find(data.getKey());
            if (val != null) {
                val.loadValue(data.getValue().getAsString());
            }
        });
    }

    public JsonObject saveConfig() {
        JsonObject directory = new JsonObject();
        directory.addProperty("Key", this.getKeyBind());
        directory.addProperty("Enabled", this.isEnabled());
        this.settings.forEach(val -> {
            directory.addProperty(val.getInternalName(), val.getValueForConfig());
        });

        return directory;
    }

    public Setting<?> find(final String term) {
        for (Setting<?> setting : this.settings) {
            if (setting.getInternalName().equalsIgnoreCase(term)) {
                return setting;
            }
        }
        return null;
    }

    public void addSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {

//            if (this == GlobalSettings.dummyModule) {
//                System.out.println(setting.getInternalName());
//                Thread.dumpStack();
//            }

            this.settings.add(setting);
            setting.onInit(this);
            setting.onInit();
        }
    }

    // This class used to be an enum class...
    @Getter
    public static enum Category {
        ALL("All"),
        MOVEMENT("Movement"),
        RENDER("Visual"),
        OTHER("Misc"),
        SETTING("Settings"),
        WIDGET("Widget");

//        private static final List<Category> categories = new ArrayList<>();
//
//        public static final Category ALL = new Category("All");
//        public static final Category MOVEMENT = new Category("Movement");
//        public static final Category RENDER = new Category("Visual");
//        public static final Category OTHER = new Category("Misc");
//        public static final Category SETTING = new Category("Settings");
//        public static final Category WIDGET = new Category("Widget");

        public float alpha = 0.0f, hoverAlpha = 0.0f;

        private final String internalName;

        public Localizable name;

        /*public */Category(String internalName) {
            this.internalName = internalName;

            this.name = Localizable.of("category." + this.internalName.toLowerCase() + ".name");

//            categories.add(this);
        }
    }
}
