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
import tech.konata.obfuscator.utils.StringUtils;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * This transformer uses a two-layer approach at attempting to obscure numbers. First, it encodes the constant in
 * a special way, then it applies a bitwise obfuscation on top of that. Be careful with this as it can make some
 * programs really slow.
 *
 * @author ItzSomebody
 */
public class HeavyNumberObfuscation extends NumberObfuscation {
    @Override
    public void transform() {
        MemberNames memberNames = new MemberNames();
        AtomicInteger counter = new AtomicInteger();
        this.getClassWrappers().stream().filter(classWrapper ->
                !excluded(classWrapper)).forEach(classWrapper ->
                classWrapper.methods.stream().filter(methodWrapper -> !excluded(methodWrapper)
                        && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper -> {
                    MethodNode methodNode = methodWrapper.methodNode;
                    int leeway = getSizeLeeway(methodNode);

                    for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
                        if (leeway < 10000)
                            break;
                        if (BytecodeUtils.isIntInsn(insn)) {
                            int originalNum = BytecodeUtils.getIntegerFromInsn(insn);
                            int encodedInt = encodeInt(originalNum, methodNode.name.hashCode());

                            int value1 = RandomUtils.getRandomInt();
                            int value2 = encodedInt ^ value1;

                            InsnList insnList = new InsnList();
                            insnList.add(BytecodeUtils.getNumberInsn(value1));
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt()));
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new InsnNode(Opcodes.DUP_X1));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(BytecodeUtils.getNumberInsn(value2));
                            insnList.add(new InsnNode(Opcodes.IXOR));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
                            insnList.add(new InsnNode(Opcodes.ICONST_0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeConstantMethodName, "(Ljava/lang/Object;I)Ljava/lang/Object;", false));
                            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));

                            methodNode.instructions.insertBefore(insn, insnList);
                            methodNode.instructions.remove(insn);
                            leeway -= 20;
                            counter.incrementAndGet();
                        } else if (BytecodeUtils.isLongInsn(insn)) {
                            long originalNum = BytecodeUtils.getLongFromInsn(insn);
                            long encodedLong = encodeLong(originalNum, methodNode.name.hashCode());

                            long value1 = RandomUtils.getRandomLong();
                            long value2 = encodedLong ^ value1;

                            InsnList insnList = new InsnList();
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomLong()));
                            insnList.add(BytecodeUtils.getNumberInsn(value1));
                            insnList.add(new InsnNode(Opcodes.DUP2_X2));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(BytecodeUtils.getNumberInsn(value2));
                            insnList.add(new InsnNode(Opcodes.LXOR));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
                            insnList.add(new InsnNode(Opcodes.ICONST_0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeConstantMethodName, "(Ljava/lang/Object;I)Ljava/lang/Object;", false));
                            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));

                            methodNode.instructions.insertBefore(insn, insnList);
                            methodNode.instructions.remove(insn);
                            leeway -= 25;
                            counter.incrementAndGet();
                        } else if (BytecodeUtils.isFloatInsn(insn)) {
                            float originalNum = BytecodeUtils.getFloatFromInsn(insn);
                            int encodedFloat = encodeFloat(originalNum, methodNode.name.hashCode());

                            int value1 = RandomUtils.getRandomInt();
                            int value2 = encodedFloat ^ value1;

                            InsnList insnList = new InsnList();
                            insnList.add(BytecodeUtils.getNumberInsn(value1));
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt()));
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new InsnNode(Opcodes.DUP_X1));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(BytecodeUtils.getNumberInsn(value2));
                            insnList.add(new InsnNode(Opcodes.IXOR));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt()));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeConstantMethodName, "(Ljava/lang/Object;I)Ljava/lang/Object;", false));
                            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));

                            methodNode.instructions.insertBefore(insn, insnList);
                            methodNode.instructions.remove(insn);

                            leeway -= 20;
                        } else if (BytecodeUtils.isDoubleInsn(insn)) {
                            double originalNum = BytecodeUtils.getDoubleFromInsn(insn);
                            long encodedLong = encodeDouble(originalNum, methodNode.name.hashCode());

                            long value1 = RandomUtils.getRandomLong();
                            long value2 = encodedLong ^ value1;

                            InsnList insnList = new InsnList();
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomLong()));
                            insnList.add(BytecodeUtils.getNumberInsn(value1));
                            insnList.add(new InsnNode(Opcodes.DUP2_X2));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(new InsnNode(Opcodes.POP2));
                            insnList.add(BytecodeUtils.getNumberInsn(value2));
                            insnList.add(new InsnNode(Opcodes.LXOR));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
                            insnList.add(BytecodeUtils.getNumberInsn(RandomUtils.getRandomInt()));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeConstantMethodName, "(Ljava/lang/Object;I)Ljava/lang/Object;", false));
                            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));

                            methodNode.instructions.insertBefore(insn, insnList);
                            methodNode.instructions.remove(insn);
                            leeway -= 25;
                        }
                    }
                })
        );
        ClassNode decoder = createConstantDecoder(memberNames);
        getClasses().put(decoder.name, new ClassWrapper(decoder, false));
        Logger.stdOut(String.format("Obfuscated %d numbers.", counter.get()));
    }

    private static int encodeInt(int n, int hashCode) {
        int xorVal = n ^ hashCode;
        int[] arr = new int[4];
        for (int i = 0; i < 4; i++) {
            arr[i] = (xorVal >>> (i * 8)) & 0xFF;
        }
        int value = 0;
        for (int i = 0; i < arr.length; i++) {
            value |= arr[i] << (i * 8);
        }
        return value;
    }

    private static int encodeFloat(float f, int hashCode) {
        return encodeInt(Float.floatToIntBits(f), hashCode);
    }

    private static long encodeLong(long n, int hashCode) {
        long xorVal = n ^ hashCode;
        long[] arr = new long[8];
        for (int i = 0; i < 8; i++) {
            arr[i] = (xorVal >>> (i * 8)) & 0xFF;
        }
        long value = 0;
        for (int i = 0; i < arr.length; i++) {
            value |= arr[i] << (i * 8);
        }

        return value;
    }

    private static long encodeDouble(double d, int hashCode) {
        return encodeLong(Double.doubleToLongBits(d), hashCode);
    }

    @Override
    public String getName() {
        return "Heavy number obfuscation";
    }

    private static ClassNode createConstantDecoder(MemberNames memberNames) {
        ClassNode cw = new ClassNode();
        MethodVisitor mv;
        FieldVisitor fv;

        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, memberNames.className, null, "java/lang/Thread", null);

        {
            fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_VOLATILE, memberNames.constantFieldName, "Ljava/lang/Object;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_VOLATILE, memberNames.indicatorFieldName, "Ljava/lang/Object;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, memberNames.elementFieldName, "Ljava/lang/StackTraceElement;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, memberNames.indexFieldName, "I", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Thread", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, memberNames.threadStarterMethodName, "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitTypeInsn(Opcodes.NEW, memberNames.className);
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, memberNames.className, "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memberNames.className, "start", "()V", false);
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memberNames.className, "join", "()V", false);
            mv.visitLabel(l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
            mv.visitLabel(l0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Integer");
            Label l3 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l3);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indicatorFieldName, "Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            Label l5 = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, l5);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeWordMethodName, "()I", false);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.elementFieldName, "Ljava/lang/StackTraceElement;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
            mv.visitInsn(Opcodes.IXOR);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            Label l7 = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, l7);
            mv.visitLabel(l5);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeWordMethodName, "()I", false);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.elementFieldName, "Ljava/lang/StackTraceElement;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
            mv.visitInsn(Opcodes.IXOR);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(I)F", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitLabel(l7);
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "()V", false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(l3);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Long");
            mv.visitJumpInsn(Opcodes.IFEQ, l1);
            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indicatorFieldName, "Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            Label l9 = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, l9);
            Label l10 = new Label();
            mv.visitLabel(l10);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeDwordMethodName, "()J", false);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.elementFieldName, "Ljava/lang/StackTraceElement;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
            mv.visitInsn(Opcodes.I2L);
            mv.visitInsn(Opcodes.LXOR);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            Label l11 = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, l11);
            mv.visitLabel(l9);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, memberNames.className, memberNames.decodeDwordMethodName, "()J", false);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.elementFieldName, "Ljava/lang/StackTraceElement;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
            mv.visitInsn(Opcodes.I2L);
            mv.visitInsn(Opcodes.LXOR);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "longBitsToDouble", "(J)D", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitLabel(l11);
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "()V", false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(l1);
            Label l12 = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, l12);
            mv.visitLabel(l2);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            Label l13 = new Label();
            mv.visitLabel(l13);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(l12);
            mv.visitInsn(Opcodes.RETURN);
            Label l14 = new Label();
            mv.visitLabel(l14);
            mv.visitMaxs(4, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, memberNames.decodeWordMethodName, "()I", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.ISHL);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 2);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitInsn(Opcodes.IMUL);
            Label l4 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, l4);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitIntInsn(Opcodes.BIPUSH, 8);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.ISHR);
            mv.visitIntInsn(Opcodes.SIPUSH, 255);
            mv.visitInsn(Opcodes.IAND);
            mv.visitInsn(Opcodes.IASTORE);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitIincInsn(2, 1);
            mv.visitJumpInsn(Opcodes.GOTO, l3);
            mv.visitLabel(l4);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 2);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 3);
            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            Label l9 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, l9);
            Label l10 = new Label();
            mv.visitLabel(l10);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitInsn(Opcodes.IALOAD);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitInsn(Opcodes.ISHL);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.ISHL);
            mv.visitInsn(Opcodes.IOR);
            mv.visitVarInsn(Opcodes.ISTORE, 2);
            Label l11 = new Label();
            mv.visitLabel(l11);
            mv.visitIincInsn(3, 1);
            mv.visitJumpInsn(Opcodes.GOTO, l8);
            mv.visitLabel(l9);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitInsn(Opcodes.IRETURN);
            Label l12 = new Label();
            mv.visitLabel(l12);
            mv.visitMaxs(5, 4);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, memberNames.decodeDwordMethodName, "()J", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            mv.visitVarInsn(Opcodes.LSTORE, 0);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitInsn(Opcodes.ISHL);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
            mv.visitVarInsn(Opcodes.ASTORE, 2);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 3);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.ICONST_4);
            mv.visitInsn(Opcodes.IMUL);
            Label l4 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, l4);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitVarInsn(Opcodes.LLOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitIntInsn(Opcodes.BIPUSH, 8);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.LSHR);
            mv.visitLdcInsn(255L);
            mv.visitInsn(Opcodes.LAND);
            mv.visitInsn(Opcodes.LASTORE);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitIincInsn(3, 1);
            mv.visitJumpInsn(Opcodes.GOTO, l3);
            mv.visitLabel(l4);
            mv.visitInsn(Opcodes.LCONST_0);
            mv.visitVarInsn(Opcodes.LSTORE, 3);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 5);
            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitVarInsn(Opcodes.ILOAD, 5);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            Label l9 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, l9);
            Label l10 = new Label();
            mv.visitLabel(l10);
            mv.visitVarInsn(Opcodes.LLOAD, 3);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, 5);
            mv.visitInsn(Opcodes.LALOAD);
            mv.visitVarInsn(Opcodes.ILOAD, 5);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitInsn(Opcodes.ISHL);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.LSHL);
            mv.visitInsn(Opcodes.LOR);
            mv.visitVarInsn(Opcodes.LSTORE, 3);
            Label l11 = new Label();
            mv.visitLabel(l11);
            mv.visitIincInsn(5, 1);
            mv.visitJumpInsn(Opcodes.GOTO, l8);
            mv.visitLabel(l9);
            mv.visitVarInsn(Opcodes.LLOAD, 3);
            mv.visitInsn(Opcodes.LRETURN);
            Label l12 = new Label();
            mv.visitLabel(l12);
            mv.visitMaxs(7, 6);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, memberNames.decodeConstantMethodName, "(Ljava/lang/Object;I)Ljava/lang/Object;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.elementFieldName, "Ljava/lang/StackTraceElement;");
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.indicatorFieldName, "Ljava/lang/Object;");
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, memberNames.className, memberNames.threadStarterMethodName, "()V", false);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitFieldInsn(Opcodes.GETSTATIC, memberNames.className, memberNames.constantFieldName, "Ljava/lang/Object;");
            mv.visitInsn(Opcodes.ARETURN);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/concurrent/ThreadLocalRandom", "current", "()Ljava/util/concurrent/ThreadLocalRandom;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/concurrent/ThreadLocalRandom", "nextInt", "()I", false);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitInsn(Opcodes.ICONST_4);
            mv.visitInsn(Opcodes.IUSHR);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.SIPUSH, 255);
            mv.visitInsn(Opcodes.IAND);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitInsn(Opcodes.ICONST_3);
            mv.visitInsn(Opcodes.IREM);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitLdcInsn("000010");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;I)Ljava/lang/Integer;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitInsn(Opcodes.IOR);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitLdcInsn("000010");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;I)Ljava/lang/Integer;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitInsn(Opcodes.IAND);
            mv.visitVarInsn(Opcodes.ISTORE, 0);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, memberNames.className, memberNames.indexFieldName, "I");
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw;
    }

    private class MemberNames {
        private String className;
        private String constantFieldName;
        private String indicatorFieldName;
        private String elementFieldName;
        private String indexFieldName;
        private String threadStarterMethodName;
        private String decodeWordMethodName;
        private String decodeDwordMethodName;
        private String decodeConstantMethodName;

        private MemberNames() {
            this.className = StringUtils.randomClassName(getClasses());
            this.constantFieldName = randomString(4);
            this.indicatorFieldName = randomString(4);
            this.elementFieldName = randomString(4);
            this.indexFieldName = randomString(4);
            this.threadStarterMethodName = randomString(4);
            this.decodeWordMethodName = randomString(4);
            this.decodeDwordMethodName = randomString(4);
            this.decodeConstantMethodName = randomString(4);
        }
    }

    @Override
    protected boolean excluded(ClassWrapper classWrapper) {

        if (obfuscator.sessionInfo.isNoAnnotations() || this.isSkipAnnotationsCheck())
            return super.excluded(classWrapper);

        boolean clazz = hasAnnotation(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation");

        if (clazz) {
            String clazzType = getEnumAnnotationValue(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation", "strength");

            return !clazzType.equals("Heavy");
        } else {

            for (MethodWrapper methodWrapper : classWrapper.methods) {
                boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation");

                if (method) {
                    String type = getEnumAnnotationValue(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.NumberObfuscation", "strength");

                    if (type.equals("Heavy"))
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

        

        return !type.equals("Heavy");
    }
}
