package tritium.management;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.util.EnumChatFormatting;
import today.opai.api.Extension;
import today.opai.api.annotations.ExtensionInfo;
import tritium.bridge.OpenAPIImpl;
import tritium.screens.ConsoleScreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 19:14
 */
public class ExtensionManager extends AbstractManager {

    public ExtensionManager() {
        super("Extension Manager");
    }

    @Getter
    private final File extensionsDir = new File(ConfigManager.configDir, "extensions");

    @Getter
    private final Map<ExtensionInfo, Extension> loadedExtensions = new HashMap<>();

    @Override
    public void init() {
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs();
            return;
        }

        ConsoleScreen.log("[ExtensionManager] Checking extensions in folder {}...", extensionsDir.getAbsolutePath());

        File[] files = extensionsDir.listFiles();

        if (files == null || files.length == 0) {
            ConsoleScreen.log("[ExtensionManager] No extensions found.");
            return;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                tryLoadJar(file);
            }
        }
    }

    @SneakyThrows
    private void tryLoadJar(File jar) {

        ConsoleScreen.log("[ExtensionManager] Loading file {}...", jar.getName());

        // don't care about closing
        URLClassLoader cl = new URLClassLoader( new URL[]{ jar.toURI().toURL() }, Thread.currentThread().getContextClassLoader()) {

        };

        try {
            iterateClasses(jar, (className, classLoader) -> {
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    if (clazz.isAnnotationPresent(ExtensionInfo.class) && Extension.class.isAssignableFrom(clazz)) {
                        if (!this.initializeExtension(clazz)) {
                            cl.close();
                        }

                        // stop scanning
                        return false;
                    }
                } catch (Throwable t) {
                    System.err.println("Cannot load extension: " + jar.getName() + ", iterating class " + className);
                    t.printStackTrace();
                    ConsoleScreen.log(EnumChatFormatting.RED + "[ExtensionManager] Cannot load extension: {}, iterating class {}", jar.getName(), className);
                }
                return true;
            }, cl);
        } catch (Exception e) {
            cl.close();
            System.err.println("Cannot load extension: " + jar.getName());
            e.printStackTrace();
            ConsoleScreen.log(EnumChatFormatting.RED + "[ExtensionManager] Cannot load extension: {}", jar.getName());
        }

    }

    private boolean initializeExtension(Class<?> extensionClass) {
        ExtensionInfo annotation = extensionClass.getAnnotation(ExtensionInfo.class);

        try {
            Extension extension = (Extension) extensionClass.newInstance();

            System.out.println("Loading extension " + annotation.name() + " by " + annotation.author() + ", version " + annotation.version());
            extension.initialize(OpenAPIImpl.getInstance());

            loadedExtensions.put(annotation, extension);
            ConsoleScreen.log(EnumChatFormatting.GREEN + "[ExtensionManager] Loaded extension {} by {}, version {}", annotation.name(), annotation.author(), annotation.version());

            return true;
        } catch (Exception e) {
            System.err.println("Cannot load extension: " + annotation.name() + " by " + annotation.author());
            e.printStackTrace();
            ConsoleScreen.log(EnumChatFormatting.RED + "[ExtensionManager] Cannot load extension: {} by {}, version {}", annotation.name(), annotation.author(), annotation.version());
            return false;
        }
    }

    private void iterateClasses(File jarFile, ClassScannerCallback callback, URLClassLoader loader) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    if (!callback.accept(className, loader)) return;
                }
            }
        }
    }

    public interface ClassScannerCallback {
        boolean accept(String className, ClassLoader loader);
    }

    @Override
    public void stop() {
        for (Map.Entry<ExtensionInfo, Extension> entry : loadedExtensions.entrySet()) {

            ExtensionInfo info = entry.getKey();
            Extension extension = entry.getValue();

            System.out.println("Unloading extension " + info.name() + " by " + info.author());

            try {
                extension.onUnload();
            } catch (Exception e) {
                System.err.println("Cannot unload extension: " + info.name() + " by " + info.author());
                e.printStackTrace();
            }

        }
    }
}
