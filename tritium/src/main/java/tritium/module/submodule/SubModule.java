package tritium.module.submodule;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import tritium.utils.i18n.Localizable;
import tritium.module.Module;

public class SubModule<T extends Module> {

    @Getter
    private final String internalName;

    @Getter
    protected Localizable name;

    public Minecraft mc = Minecraft.getMinecraft();
    @Getter
    @Setter
    private T module;

    public SubModule(String internalName) {
        this.internalName = internalName;

        String lowerCase = internalName.toLowerCase();

        this.name = Localizable.of("submodule." + lowerCase + ".name");
    }

    public void onEnable() {

    }

    public void onDisable() {

    }


}
