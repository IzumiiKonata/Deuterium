package net.minecraft.launchwrapper.injector;

import net.minecraft.launchwrapper.IClassTransformer;

public class VanillaTweakInjector implements IClassTransformer {
    public VanillaTweakInjector() {
    }

    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        return bytes;
    }


}
