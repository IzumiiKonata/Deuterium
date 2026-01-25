package tritium.utils.i18n;

import lombok.Getter;
import tritium.Tritium;
import tritium.management.Localizer;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class Localizable {

    String translated = null;

    @Getter
    final String localizeKey;

    boolean translatable = true;

    Language LANG = null;

    private Localizable(String key) {
        this.localizeKey = key;
    }

    public static Localizable of(String key) {
        return new Localizable(key);
    }

    public static Localizable ofUntranslatable(String text) {
        Localizable localizable = new Localizable(text);
        localizable.translatable = false;
        return localizable;
    }

    public String get() {

        if (!translatable)
            return localizeKey;

        boolean needReset = LANG != Localizer.getLANG();

        if (translated == null || needReset) {
            this.translated = Tritium.getInstance().getLocalizer().translate(this.localizeKey);

            if (needReset) {
                LANG = Localizer.getLANG();
            }
        }

        return translated;
    }

}
