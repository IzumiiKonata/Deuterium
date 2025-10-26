package tritium.management;

import tritium.ncm.music.CloudMusic;
import tritium.utils.other.multithreading.MultiThreadingUtil;

/**
 * @author IzumiiKonata
 * Date 2025/10/3 10:44
 */
public class NCMManager extends AbstractManager {

    public NCMManager() {
        super("NCMManager");
    }

    @Override
    public void init() {
        MultiThreadingUtil.runAsync(CloudMusic::initNCM);
    }

    @Override
    public void stop() {
        CloudMusic.onStop();
    }
}
