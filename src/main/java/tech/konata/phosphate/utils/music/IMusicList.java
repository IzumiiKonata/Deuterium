package tech.konata.phosphate.utils.music;

import tech.konata.phosphate.utils.music.dto.Music;

import java.util.List;

public interface IMusicList {

    /**
     * 获取对象歌曲列表
     *
     * @return 歌曲列表
     */
    List<Music> getMusics();
}
