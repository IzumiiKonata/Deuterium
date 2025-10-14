package tech.konata.obfuscator.transformers.obfuscators.miscellaneous;

import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;

/**
 * @author IzumiiKonata
 * @since 2024/10/8 21:12
 */
public class TimeManipulator extends Transformer {

    @Override
    public void transform() {

    }

    @Override
    public String getName() {
        return "Time Manipulator";
    }

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.GLOBAL;
    }

}
