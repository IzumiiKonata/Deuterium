package tritium.management;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.Location;
import tritium.Tritium;
import tritium.screens.ConsoleScreen;
import tritium.utils.i18n.Language;
import tritium.settings.ClientSettings;
import tritium.utils.json.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 本地化管理器
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class Localizer extends AbstractManager {

    @Getter
    private static final List<Language> languages = new ArrayList<>();

    @Getter
    private static Language LANG;

    private static final List<String> missing = new ArrayList<>();

    public Localizer() {
        super("Localizer");
    }

    public String translate(String key) {

        String trans = LANG.getTranslationsMap().getOrDefault(key, null);

        if (trans == null) {
            this.logger.warn("Missing translate \"{}\"!", key);
            missing.add(key);
            return key;
        }

        return trans;
    }

    public static void setLang(String lang) {
        for (Language language : languages) {
            if (language.getName().equals(lang)) {
//                ConsoleScreen.log("[Localizer] Lang change triggered: {} to {}", LANG.getName(), language.getName());

                LANG = language;
            }
        }
    }

    public static String format(String translateKey, Object... args) {
        return String.format(Tritium.getInstance().getLocalizer().translate(translateKey), args);
    }

    @SneakyThrows
    public static void loadLang() {

        languages.clear();

        List<String> lang = Arrays.asList("ZH_CN", "EN_US");

        IReloadableResourceManager resMng = new SimpleReloadableResourceManager(new IMetadataSerializer());

        List<IResourcePack> list = Collections.singletonList(Minecraft.getMinecraft().mcDefaultResourcePack);

        resMng.reloadResources(list);

        for (String name : lang) {
            Location loc = Location.of("tritium/translations/" + name + ".json");

            try (
                    InputStream stream = resMng.getResource(loc).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
            ) {

                Language l = JsonUtils.parse(reader, Language.class);

                for (Map.Entry<String, JsonElement> entry : l.getTranslations().entrySet()) {
                    String trans = entry.getValue().getAsString();

                    l.getTranslationsMap().put(entry.getKey(), trans);
                }

                languages.add(l);

            }

        }

        LANG = languages.getFirst();

        ClientSettings.LANG.getModes().clear();

        for (Language language : languages) {
            ClientSettings.LANG.getModes().add(language.getName());
        }

        ClientSettings.LANG.setValue(languages.getFirst().getName());

    }

    @Override
    @SneakyThrows
    public void init() {


    }

    @Override
    public void stop() {
        for (String s : Localizer.missing.stream().distinct().toList()) {
            System.out.println("\"" + s + "\": \"\", ");
        }
    }

    public static Localizer getInstance() {
        return Tritium.getInstance().getLocalizer();
    }
}
