package tech.konata.obfuscator.transformers.obfuscators.flow;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import tech.konata.obfuscator.Logger;

/**
 * Replaces GOTO instructions with an expression which is always true. This does nothing more than adding
 * a one more edge to a control flow graph for every GOTO instruction present.
 *
 * @author ItzSomebody
 */
public class GotoReplacer extends FlowObfuscation {
    private static final int PRED_ACCESS = ACC_PUBLIC | ACC_STATIC | ACC_FINAL;

    @Override
    public String getName() {
        return "Goto Replacer";
    }

    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        getClassWrappers().stream().filter(cw -> !excluded(cw)).forEach(cw -> {
            AtomicBoolean shouldAdd = new AtomicBoolean();
            FieldNode predicate = new FieldNode(PRED_ACCESS, randomString(4)/*PhosphateObfuscator.randomString()*/, "Z", null, null);

            cw.methods.stream().filter(mw -> !excluded(mw) && mw.methodNode.instructions.size() > 0).forEach(mw -> {
                InsnList insns = mw.methodNode.instructions;

                int leeway = getSizeLeeway(mw.methodNode);
                int varIndex = mw.methodNode.maxLocals;
                mw.methodNode.maxLocals++; // Prevents breaking of other transformers which rely on this field.

                for (AbstractInsnNode insn : insns.toArray()) {
                    if (leeway < 10000)
                        break;

                    if (insn.getOpcode() == GOTO) {
                        insns.insertBefore(insn, new VarInsnNode(ILOAD, varIndex));
                        insns.insertBefore(insn, new JumpInsnNode(IFEQ, ((JumpInsnNode) insn).label));
                        insns.insert(insn, new InsnNode(ATHROW));
                        insns.insert(insn, new InsnNode(ACONST_NULL));
                        insns.remove(insn);

                        leeway -= 10;

                        counter.incrementAndGet();
                        shouldAdd.set(true);
                    }
                }

                if (shouldAdd.get()) {
                    insns.insert(new VarInsnNode(ISTORE, varIndex));
                    insns.insert(new FieldInsnNode(GETSTATIC, cw.originalName, predicate.name, "Z"));
                }
            });

            if (shouldAdd.get())
                cw.classNode.fields.add(predicate);
        });

        Logger.stdOut("Swapped " + counter.get() + " GOTO instructions");
    }
}