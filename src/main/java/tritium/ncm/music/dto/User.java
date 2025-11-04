package tritium.ncm.music.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import tritium.ncm.api.CloudMusicApi;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户对象
 */
@Getter
public class User {
    public long id;
    public String name;
    public String signature;
    //    public final int level;
    public int vip;
    //    public final long listenSongs;
//    public final int playlistCount;
//    public final int createTime;
//    public final int createDay;
    public String avatarUrl;
    private long likePlayListId = 0;

    public User(JsonObject profile) {
        try {
            this.id = profile.get("userId").getAsLong();

            JsonElement nickname = profile.get("nickname");
            if (nickname == null || nickname.isJsonNull()) {
                this.name = "N/A";
            } else {
                this.name = nickname.getAsString();
            }

            JsonElement sign = profile.get("signature");
            if (sign == null || sign.isJsonNull()) {
                this.signature = "N/A";
            } else {
                this.signature = sign.getAsString();
            }

            this.vip = profile.get("vipType").getAsInt();
            this.avatarUrl = profile.get("avatarUrl").getAsString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

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
            playLists.add(new PlayList(playList.getAsJsonObject()));
        });

        return playLists;
    }

}
