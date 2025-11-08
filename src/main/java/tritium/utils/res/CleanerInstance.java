package tritium.utils.res;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.lang.ref.Cleaner;

/**
 * @author IzumiiKonata
 * Date: 2025/11/8 11:15
 */
@UtilityClass
public class CleanerInstance {

    @Getter
    private static final Cleaner instance = Cleaner.create();

}
