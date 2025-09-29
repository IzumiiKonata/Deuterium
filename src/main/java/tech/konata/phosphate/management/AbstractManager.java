package tech.konata.phosphate.management;

import lombok.Getter;
import tech.konata.phosphate.interfaces.SharedConstants;
import tech.konata.phosphate.utils.logging.LogManager;
import tech.konata.phosphate.utils.logging.Logger;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public abstract class AbstractManager implements SharedConstants {

    @Getter
    private final String name;
    public final Logger logger;

    public AbstractManager(String name) {
        this.name = name;
        this.logger = LogManager.getLogger(name);

        this.logger.debug("<init> @ {}", name);
    }

    public abstract void init();

    public abstract void stop();

}
