package tech.konata.phosphate.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;

/**
 * @author IzumiiKonata
 * @since 2024/11/10 16:59
 */
@UtilityClass
public class JsonUtils {

    private final Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();

    public JsonObject jsonObjectFromString(String json) {
        return gson.fromJson(json, JsonObject.class);
    }

}
