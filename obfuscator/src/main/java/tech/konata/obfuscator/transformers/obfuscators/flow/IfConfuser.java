package tech.konata.obfuscator.transformers.obfuscators.flow;

import lombok.var;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.analysis.misc.Local;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.utils.BytecodeUtils;

import java.util.Random;

/**
 * @author IzumiiKonata
 * Date: 2025/10/14 20:16
 */
public class IfConfuser extends Transformer {

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.IF_CONFUSER;
    }

    @Override
    public void transform() {
        getClassWrappers().stream().filter(cw -> !excluded(cw)).forEach(cw -> {
            cw.methods.stream().filter(mw -> !excluded(mw) && mw.methodNode.instructions.size() > 0).forEach(mw -> {
                MethodNode methodNode = mw.methodNode;

                for (AbstractInsnNode insn : methodNode.instructions) {
                    if(!(insn instanceof JumpInsnNode))
                        continue;

                    JumpInsnNode jmp = (JumpInsnNode) insn;

                    handleSimpleIf(methodNode, jmp);
                    handleNormalCompare(methodNode, jmp);
                }
            });
        });
    }
    
    static Random random = new Random();

    //obfuscate compare ifs, (v1, v2, IF_ICMPEQ -> v1 + num, v2 + num, IF_ICMPEQ)
    private static void handleNormalCompare(MethodNode method, JumpInsnNode jmp) {
        if(!isNormalIf(jmp.getOpcode()))
            return;

        var list = new InsnList();
        int type = random.nextInt(3);
        int norm = random.nextInt();

        switch (type) {
            case 0: {
                var v2 = Local.alloc(method, Type.INT_TYPE);

                list.add(v2.store());               // v1
                list.add(BytecodeUtils.getNumberInsn(norm));   // v1, num
                list.add(new InsnNode(DUP_X1));     // num, v1, num
                list.add(new InsnNode(IADD));       // num, (v1 + num)
                list.add(new InsnNode(SWAP));       // (v1 + num), num
                list.add(v2.load());                // (v1 + num), num, v2
                list.add(new InsnNode(SWAP));       // (v1 + num), v2, num
                list.add(new InsnNode(IADD));       // (v1 + num), (v2 + num)
                break;
            }
            case 1: {
                var v1 = Local.alloc(method, Type.INT_TYPE);
                var v2 = Local.alloc(method, Type.INT_TYPE);

                list.add(v2.store());
                list.add(v1.store());

                list.add(BytecodeUtils.getNumberInsn(norm));
                list.add(new InsnNode(DUP));

                list.add(v1.load());
                list.add(new InsnNode(IADD));
                list.add(new InsnNode(SWAP));

                list.add(v2.load());
                list.add(new InsnNode(IADD));
                break;
            }
            case 2: {
                list.add(BytecodeUtils.getNumberInsn(norm)); // v1, v2, num
                list.add(new InsnNode(DUP_X2)); // num, v1, v2, num
                list.add(new InsnNode(IADD)); // num, v1, (v2 + num)
                list.add(new InsnNode(DUP_X2)); // (v2 + num), num, v1, (v2 + num)
                list.add(new InsnNode(POP)); // (v2 + num), num, v1

                list.add(new InsnNode(SWAP)); // (v2 + num), v1, num
                list.add(new InsnNode(IADD)); // (v2 + num), (v1 + num)
                list.add(new InsnNode(SWAP)); // (v1 + num), (v2 + num)
                break;
            }
        }

        method.instructions.insertBefore(jmp, list);
    }

    //obfuscate simple ifs (IFEQ -> IF_ICMPEQ with some more instruction on top)
    private static void handleSimpleIf(MethodNode method, JumpInsnNode jmp) {
        if(!isSimpleIf(jmp.getOpcode()))
            return;

        var list = new InsnList();
        int type = random.nextInt(2);
        int norm = random.nextInt();

        switch (type) {
            case 0://use local, same action
                var local = Local.alloc(method, Type.INT_TYPE);

                list.add(local.store());            // --
                list.add(BytecodeUtils.getNumberInsn(norm));   // num
                list.add(new InsnNode(DUP));        // num, num
                list.add(local.load());             // num, num, cond
                list.add(new InsnNode(SWAP));       // num, cond, num
                list.add(new InsnNode(IADD));       // num, (cond + num)
                list.add(new InsnNode(SWAP));       // (cond + num), num
                break;
            case 1://simpler
                list.add(BytecodeUtils.getNumberInsn(norm));   // cond (1, 0), num
                list.add(new InsnNode(DUP_X1));     // num, cond (1, 0), num
                list.add(new InsnNode(IADD));       // num, (cond + num)
                list.add(new InsnNode(SWAP));       // (cond + num), num
                break;
        }

        list.add(new JumpInsnNode(getCmpOpcode(jmp.getOpcode()), jmp.label)); // (cond + num) == num?
        method.instructions.insertBefore(jmp, list);
        method.instructions.remove(jmp);
    }

    private static boolean isSimpleIf(int opcode) {
        return opcode >= IFEQ && opcode <= IFLE;
    }

    private static int getCmpOpcode(int op) {
        return op + 6;
    }

    private static boolean isNormalIf(int opcode) {
        return opcode >= IF_ICMPEQ && opcode <= IF_ICMPLE;
    }

    @Override
    public String getName() {
        return "IfConfuser";
    }
}
