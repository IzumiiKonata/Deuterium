package tritium.utils.i18n;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author IzumiiKonata
 * @since 4/30/2023 10:00 PM
 */
@NoArgsConstructor
@Data
public class Language {


    @Getter
    private final Map<String, String> translationsMap = new HashMap<>();

    @SerializedName("Name")
    private String name;
    @SerializedName("Version")
    private String version;
    @SerializedName("Author")
    private String author;
    @SerializedName("Unicode")
    private boolean unicode;
    @SerializedName("Translations")
    private JsonObject translations;

}
