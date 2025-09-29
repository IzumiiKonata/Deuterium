package tech.konata.phosphate.utils.music.dto;

import com.google.gson.JsonObject;
import lombok.Getter;
import tech.konata.ncm.api.CloudMusicApi;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.network.HttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户对象
 */
@Getter
public class User {
    public final long id;
    public final String name;
    public final String signature;
    //    public final int level;
    public final int vip;
    //    public final long listenSongs;
//    public final int playlistCount;
//    public final int createTime;
//    public final int createDay;
    public final String avatarUrl;
    private final long likePlayListId = 0;

    public User(JsonObject profile) {
//        JsonObject profile = data.getAsJsonObject("profile");

        this.id = profile.get("userId").getAsLong();

        if (profile.get("nickname").isJsonNull()) {
            this.name = "N/A";
        } else {
            this.name = profile.get("nickname").getAsString();
        }

        if (profile.get("signature").isJsonNull()) {
            this.signature = "N/A";
        } else {
            this.signature = profile.get("nickname").getAsString();
        }

        this.vip = profile.get("vipType").getAsInt();
//        this.playlistCount  = profile.get("playlistCount").getAsInt();
        this.avatarUrl = profile.get("avatarUrl").getAsString();

//        this.listenSongs    = data.get("listenSongs").getAsInt();
//        this.level          = data.get("level").getAsInt();
//        this.createTime     = data.get("createTime").getAsInt();
//        this.createDay      = data.get("createDays").getAsInt();
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
