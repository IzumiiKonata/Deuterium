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

package tech.konata.obfuscator.transformers.obfuscators.numbers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.utils.BytecodeUtils;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.utils.RandomUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Obfuscates integer and long constants using xor operations.
 *
 * @author ItzSomebody
 */
public class LightNumberObfuscation extends NumberObfuscation {
    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();
        getClassWrappers().stream().filter(classWrapper -> !excluded(classWrapper)).forEach(classWrapper ->
                classWrapper.methods.stream().filter(methodWrapper -> !excluded(methodWrapper)
                        && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper -> {
                    MethodNode methodNode = methodWrapper.methodNode;
                    int leeway = getSizeLeeway(methodNode);

                    for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
                        if (leeway < 10000)
                            break;
                        if (BytecodeUtils.isIntInsn(insn)) {
                            int originalNum = BytecodeUtils.getIntegerFromInsn(insn);
                            int value1 = RandomUtils.getRandomInt();
                            int value2 = originalNum ^ value1;

                            InsnList insnList = new InsnList();
                            insnList.add(BytecodeUtils.getNumberInsn(value1));
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt()));
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new InsnNode(Opcodes.DUP_X1));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(BytecodeUtils.getNumberInsn(value2));
                            insnList.add(new InsnNode(Opcodes.IXOR));

                            methodNode.instructions.insertBefore(insn, insnList);
                            methodNode.instructions.remove(insn);
                            leeway -= 10;
                            counter.incrementAndGet();
                        } else if (BytecodeUtils.isLongInsn(insn)) {
                            long originalNum = BytecodeUtils.getLongFromInsn(insn);
                            long value1 = RandomUtils.getRandomLong();
                            long value2 = originalNum ^ value1;

                            InsnList insnList = new InsnList();
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomLong()));
                            insnList.add(BytecodeUtils.getNumberInsn(value1));
                            insnList.add(new InsnNode(Opcodes.DUP2_X2));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(BytecodeUtils.getNumberInsn(value2));
                            insnList.add(new InsnNode(Opcodes.LXOR));

                            methodNode.instructions.insertBefore(insn, insnList);
                            methodNode.instructions.remove(insn);
                            leeway -= 15;
                            counter.incrementAndGet();
                        }
                    }
                })
        );
        Logger.stdOut(String.format("Split %d numbers into bitwise xor instructions.", counter.get()));
    }

    @Override
    public String getName() {
        return "Light number obfuscation";
    }

    @Override
    protected boolean excluded(ClassWrapper classWrapper) {

        if (obfuscator.sessionInfo.isNoAnnotations() || this.isSkipAnnotationsCheck())
            return super.excluded(classWrapper);

        boolean clazz = hasAnnotation(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation");

        if (clazz) {
            String clazzType = getEnumAnnotationValue(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation", "strength");

            return !clazzType.equals("Light");
        } else {

            for (MethodWrapper methodWrapper : classWrapper.methods) {
                boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation");

                if (method) {
                    String type = getEnumAnnotationValue(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation", "strength");

                    if (type.equals("Light"))
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

        boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation");
        boolean clazz = hasAnnotation(methodWrapper.getOwner().classNode.visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation");

        if (!(method || clazz))
            return true;

        String type = getEnumAnnotationValue(method ? methodWrapper.getMethodNode().visibleAnnotations : methodWrapper.getOwner().classNode.visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation", "strength");

        if (type == null) {
            return true;
        }

        return !type.equals("Light");
    }
}
