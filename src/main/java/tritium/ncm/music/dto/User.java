package tritium.ncm.music.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import tritium.ncm.api.CloudMusicApi;
import tritium.utils.json.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户对象
 */
@Data
public class User {

    @SerializedName("userId")
    private final long id;
    @SerializedName("nickname")
    private final String name;
    @SerializedName("signature")
    private final String signature;
    @SerializedName("vipType")
    private final int vip;
    @SerializedName("avatarUrl")
    private final String avatarUrl;

    /**
     * 用户歌单
     *
     * @return 歌单列表
     */
    public List<PlayList> playLists(int page, int limit) {
        if (limit == 0) {
            limit = 30;
        }

        JsonObject data = CloudMusicApi.userPlaylist(this.id, limit, limit * page).toJsonObject();

        List<PlayList> playLists = new ArrayList<>();
        data.get("playlist").getAsJsonArray().forEach(playList -> {
            PlayList parse = JsonUtils.parse(playList.getAsJsonObject(), PlayList.class);
            playLists.add(parse);
        });

        return playLists;
    }

}
