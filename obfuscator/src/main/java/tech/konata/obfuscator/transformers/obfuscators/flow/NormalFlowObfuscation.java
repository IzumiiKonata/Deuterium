/*
 * Copyright (C) 2018 ItzSomebody
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package tech.konata.obfuscator.transformers.obfuscators.flow;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.asm.StackEmulator;
import tech.konata.obfuscator.exceptions.ObfuscatorException;
import tech.konata.obfuscator.exceptions.StackEmulationException;
import tech.konata.obfuscator.utils.BytecodeUtils;
import tech.konata.obfuscator.utils.RandomUtils;
import tech.konata.obfuscator.utils.StringUtils;
import org.objectweb.asm.Type;

/**
 * Same as{@link LightFlowObfuscation}, but also inserts fake jumps where the stack is empty.
 *
 * @author ItzSomebody
 */
public class NormalFlowObfuscation extends FlowObfuscation {
    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        this.getClassWrappers().stream().filter(classWrapper ->
                !excluded(classWrapper)).forEach(classWrapper -> {
            ClassNode classNode = classWrapper.classNode;
            FieldNode field = new FieldNode(ACC_PUBLIC + ACC_STATIC + ACC_FINAL,
                    StringUtils.randomSpacesString(RandomUtils.getRandomInt(10)), "Z", null, null);

            classNode.fields.add(field);
            classWrapper.methods.stream().filter(methodWrapper -> !excluded(methodWrapper)
                    && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper -> {
                MethodNode methodNode = methodWrapper.methodNode;
                int leeway = getSizeLeeway(methodNode);
                int varIndex = methodNode.maxLocals;
                methodNode.maxLocals++;
                AbstractInsnNode[] untouchedList = methodNode.instructions.toArray();
                LabelNode labelNode = exitLabel(methodNode);
                boolean calledSuper = false;
                StackEmulator stackEmulator = new StackEmulator(methodNode, methodNode.instructions.getLast());
                try {
                    stackEmulator.execute(false);
                } catch (StackEmulationException e) {
                    e.printStackTrace();
                    throw new ObfuscatorException(String.format("Error happened while trying to emulate the stack of %s.%s%s",
                            classNode.name, methodNode.name, methodNode.desc));
                }
                Set<AbstractInsnNode> emptyAt = stackEmulator.getEmptyAt();
                for (AbstractInsnNode insn : untouchedList) {
                    if (leeway < 10000)
                        break;

                    if ("<init>".equals(methodNode.name))
                        calledSuper = (insn instanceof MethodInsnNode && insn.getOpcode() == INVOKESPECIAL
                                && insn.getPrevious() instanceof VarInsnNode && ((VarInsnNode) insn.getPrevious()).var == 0);
                    if (insn != methodNode.instructions.getFirst() && !(insn instanceof LineNumberNode)) {
                        if ("<init>".equals(methodNode.name) && !calledSuper)
                            continue;
                        if (emptyAt.contains(insn)) { // We need to make sure stack is empty before making jumps
                            methodNode.instructions.insertBefore(insn, new VarInsnNode(ILOAD, varIndex));
                            methodNode.instructions.insertBefore(insn, new JumpInsnNode(IFNE, labelNode));
                            leeway -= 5;
                            counter.incrementAndGet();
                        }
                    }
                    if (insn.getOpcode() == GOTO) {
                        methodNode.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, classNode.name,
                                field.name, "Z"));
                        methodNode.instructions.insert(insn, new InsnNode(ATHROW));
                        methodNode.instructions.insert(insn, new InsnNode(ACONST_NULL));
                        methodNode.instructions.set(insn, new JumpInsnNode(IFEQ, ((JumpInsnNode) insn).label));
                        leeway -= 7;
                        counter.incrementAndGet();
                    }
                }
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                        new VarInsnNode(ISTORE, varIndex));
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                        new FieldInsnNode(GETSTATIC, classNode.name, field.name, "Z"));
            });
        });
    }

    @Override
    protected boolean excluded(ClassWrapper classWrapper) {

        if (obfuscator.sessionInfo.isNoAnnotations() || this.isSkipAnnotationsCheck())
            return super.excluded(classWrapper);

        boolean clazz = hasAnnotation(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.Flow");

        if (clazz) {
            String clazzType = getEnumAnnotationValue(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.Flow", "strength");

            return !clazzType.equals("Normal");
        } else {

            for (MethodWrapper methodWrapper : classWrapper.methods) {
                boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.Flow");

                if (method) {
                    String type = getEnumAnnotationValue(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.Flow", "strength");

                    if (type.equals("Normal"))
                        return false;
                }
            }

        }

        return true;
    }

    @Override
    protected boolean excluded(MethodWrapper methodWrapper) {

        if (obfuscator.sessionInfo.isNoAnnotations() || this.isSkipAnnotationsCheck())
            return super.excluded(methodWrapper);

        boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.Flow");
        boolean clazz = hasAnnotation(methodWrapper.getOwner().classNode.visibleAnnotations, "tech.konata.obfuscation.Flow");

        if (!(method || clazz))
            return true;

        String type = getEnumAnnotationValue(method ? methodWrapper.getMethodNode().visibleAnnotations : methodWrapper.getOwner().classNode.visibleAnnotations, "tech.konata.obfuscation.Flow", "strength");

        if (type == null) {
            return true;
        }

        

        return !type.equals("Normal");
    }

    @Override
    public String getName() {
        return "Normal flow obfuscation";
    }

    /**
     * Generates a generic "escape" pattern to avoid inserting multiple copies of the same bytecode instructions.
     *
     * @param methodNode the {@link MethodNode} we are inserting into.
     * @return a {@link LabelNode} which "escapes" all other flow.
     */
    public static LabelNode exitLabel(MethodNode methodNode) {
        LabelNode lb = new LabelNode();
        LabelNode escapeNode = new LabelNode();
        AbstractInsnNode target = methodNode.instructions.getFirst();
        methodNode.instructions.insertBefore(target, new JumpInsnNode(GOTO, escapeNode));
        methodNode.instructions.insertBefore(target, lb);
        Type returnType = Type.getReturnType(methodNode.desc);
        switch (returnType.getSort()) {
            case Type.VOID:
                methodNode.instructions.insertBefore(target, new InsnNode(RETURN));
                break;
            case Type.BOOLEAN:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt(2)));
                methodNode.instructions.insertBefore(target, new InsnNode(IRETURN));
                break;
            case Type.CHAR:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils
                        .getRandomInt(Character.MAX_VALUE + 1)));
                methodNode.instructions.insertBefore(target, new InsnNode(IRETURN));
                break;
            case Type.BYTE:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils
                        .getRandomInt(Byte.MAX_VALUE + 1)));
                methodNode.instructions.insertBefore(target, new InsnNode(IRETURN));
                break;
            case Type.SHORT:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils
                        .getRandomInt(Short.MAX_VALUE + 1)));
                methodNode.instructions.insertBefore(target, new InsnNode(IRETURN));
                break;
            case Type.INT:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt()));
                methodNode.instructions.insertBefore(target, new InsnNode(IRETURN));
                break;
            case Type.LONG:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils.getRandomLong()));
                methodNode.instructions.insertBefore(target, new InsnNode(LRETURN));
                break;
            case Type.FLOAT:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils.getRandomFloat()));
                methodNode.instructions.insertBefore(target, new InsnNode(FRETURN));
                break;
            case Type.DOUBLE:
                methodNode.instructions.insertBefore(target, BytecodeUtils.getNumberInsn(RandomUtils.getRandomDouble()));
                methodNode.instructions.insertBefore(target, new InsnNode(DRETURN));
                break;
            default:
                methodNode.instructions.insertBefore(target, new InsnNode(ACONST_NULL));
                methodNode.instructions.insertBefore(target, new InsnNode(ARETURN));
                break;
        }
        methodNode.instructions.insertBefore(target, escapeNode);

        return lb;
    }
}
