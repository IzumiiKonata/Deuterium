package tech.konata.obfuscation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author IzumiiKonata
 * Date: 2025/1/19 18:57
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Indy {

    ObfuscationStrength strength();

}
