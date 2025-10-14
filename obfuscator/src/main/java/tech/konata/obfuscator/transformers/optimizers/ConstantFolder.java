package tech.konata.obfuscator.transformers.optimizers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tech.konata.obfuscator.Logger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class ConstantFolder extends Optimizer {
    @Override
    public void transform() {
        AtomicInteger count = new AtomicInteger();
        long current = System.currentTimeMillis();

        getClassWrappers().stream().filter(classWrapper -> !excluded(classWrapper)).forEach(classWrapper -> {
            classWrapper.methods.stream().filter(methodWrapper -> !excluded(methodWrapper)
                    && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper -> {
                MethodNode methodNode = methodWrapper.methodNode;
                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode insn = iterator.next();
                    if (insn.getOpcode() == LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        Object cst = ldcInsn.cst;
                        AbstractInsnNode nextInsn = iterator.hasNext() ? iterator.next() : null;
                        if (nextInsn != null && isConstantPushInstruction(nextInsn)) {
                            Object nextCst = nextInsn.getOpcode() == LDC ? ((LdcInsnNode) nextInsn).cst :
                                    nextInsn.getOpcode() == BIPUSH ? Integer.valueOf(((IntInsnNode) nextInsn).operand) :
                                            nextInsn.getOpcode() == SIPUSH ? ((IntInsnNode) nextInsn).operand : null;
                            if (nextCst != null) {
                                Object result = null;
                                int opcode = nextInsn.getOpcode();
                                if ((opcode == IADD || opcode == ISUB) && cst instanceof Integer && nextCst instanceof Integer) {
                                    result = (Integer) cst + (Integer) nextCst;
                                } else if ((opcode == FADD || opcode == FSUB) && cst instanceof Float && nextCst instanceof Float) {
                                    result = (Float) cst + (Float) nextCst;
                                } else if ((opcode == DADD || opcode == DSUB) && cst instanceof Double && nextCst instanceof Double) {
                                    result = (Double) cst + (Double) nextCst;
                                }
                                // Add more cases for other numeric operations and types if needed

                                if (result != null) {
                                    AbstractInsnNode newInsn = new LdcInsnNode(result);
                                    methodNode.instructions.insertBefore(ldcInsn, newInsn);
                                    methodNode.instructions.remove(ldcInsn);
                                    methodNode.instructions.remove(nextInsn);
                                    count.incrementAndGet();
                                    iterator = methodNode.instructions.iterator(); // Reset iterator after modification
                                }
                            }
                        }
                    }
                }
            });
        });

        Logger.stdOut(String.format("Performed constant folding %d times. [%dms]", count.get(), tookThisLong(current)));
    }

    private boolean isConstantPushInstruction(AbstractInsnNode insn) {
        return insn.getOpcode() == LDC || insn.getOpcode() == BIPUSH || insn.getOpcode() == SIPUSH;
    }

    @Override
    public String getName() {
        return "Constant Folder";
    }
}