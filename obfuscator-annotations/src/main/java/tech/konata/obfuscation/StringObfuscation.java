package tech.konata.obfuscation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author IzumiiKonata
 * Date: 2025/1/19 18:41
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StringObfuscation {

    ObfuscationStrength strength();

}
