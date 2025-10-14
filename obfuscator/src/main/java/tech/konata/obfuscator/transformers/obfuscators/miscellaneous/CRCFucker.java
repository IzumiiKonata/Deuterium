package tech.konata.obfuscator.transformers.obfuscators.miscellaneous;

import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;

/**
 * @author IzumiiKonata
 * @since 2024/10/8 21:12
 */
public class CRCFucker extends Transformer {

    @Override
    public void transform() {

    }

    @Override
    public String getName() {
        return "CRC folder";
    }

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.CLASS_FOLDER;
    }

}
