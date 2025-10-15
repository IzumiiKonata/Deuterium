package tech.konata.obfuscator.transformers.obfuscators;

import org.objectweb.asm.tree.MethodNode;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;

/**
 * @author IzumiiKonata
 * Date: 2025/10/15 21:48
 */
public class RecomputeASM extends Transformer {

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.GLOBAL;
    }

    @Override
    public void transform() {
        for (ClassWrapper cw : this.getClasses().values()) {
            for (MethodWrapper method : cw.methods) {
                MethodNode methodNode = method.methodNode;
                methodNode.maxStack = methodNode.maxLocals = 0;
            }
        }
    }

    @Override
    public String getName() {
        return "";
    }
}
