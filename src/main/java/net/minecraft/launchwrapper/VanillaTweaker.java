package net.minecraft.launchwrapper;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VanillaTweaker implements ITweaker {
    private List<String> args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.args = new ArrayList<>(args);
        this.args.add("--version");
        this.args.add(profile);
    }

    static final IMixinConfigSource MIXIN_CONFIG_SOURCE = new IMixinConfigSource() {
        @Override
        public String getDescription() {
            return "Tritium";
        }

        @Override
        public String getId() {
            return "Tritium";
        }
    };

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.init();

        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();

        env.setOption(MixinEnvironment.Option.DISABLE_REFMAP, true);
        env.setSide(MixinEnvironment.Side.CLIENT);

        File extensionsDir = Paths.get(Launch.minecraftHome.getAbsolutePath(), "Tritium", "extensions").toFile();
        if (extensionsDir.exists() && extensionsDir.isDirectory() && extensionsDir.listFiles() != null) {
            for (File extension : extensionsDir.listFiles()) {
                if (!(extension.isFile() && extension.getName().toLowerCase().endsWith(".jar")))
                    continue;

                ZipFile zipFile;

                try {
                    zipFile = new ZipFile( extension);
                } catch (IOException e) {
                    System.err.println("Failed to open extension: " + extension.getAbsolutePath());
                    e.printStackTrace();
                    continue;
                }

                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    if (entry.getName().startsWith("mixins.") && entry.getName().endsWith(".json")) {
                        Mixins.addConfiguration(entry.getName(), MIXIN_CONFIG_SOURCE);
                    }
                }

                try {
                    zipFile.close();
                } catch (IOException e) {
                    System.err.println("Resource leak on extension: " + extension.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }

        env.setSide(MixinEnvironment.Side.CLIENT);
        env.setOption(MixinEnvironment.Option.DISABLE_REFMAP, true);

        env.setOption(MixinEnvironment.Option.DEBUG_VERBOSE, true);

        MixinBootstrap.getPlatform().inject();
    }

    @Override
    public String getLaunchTarget() {
        return "tritium.launch.MinecraftBootstrap";
    }

    @Override
    public String[] getLaunchArguments() {
        return args.toArray(new String[0]);
    }
}
