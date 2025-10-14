package tech.konata.obfuscator.utils;

import lombok.var;
import me.coley.analysis.SimAnalyzer;
import me.coley.analysis.SimInterpreter;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ASMUtils implements Opcodes {
    public static Map<AbstractInsnNode, Frame<AbstractValue>> analyzeMethod(ClassNode owner, MethodNode method) {
        var analyzer = new SimAnalyzer(new SimInterpreter());
        analyzer.setSkipDeadCodeBlocks(true);

        try {
            var frames = new HashMap<AbstractInsnNode, Frame<AbstractValue>>();
            var arr = analyzer.analyzeAndComputeMaxs(owner.name, method);

            for(int i = 0; i < method.instructions.size(); i++) {
                var insn = method.instructions.get(i);
                frames.put(insn, arr[i]);
            }

            return frames;
        } catch (AnalyzerException _) {}

        return null;
    }

    public static int getInt(AbstractInsnNode insn) {
        if(insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Integer) {
            Integer num = (Integer) ((LdcInsnNode) insn).cst;
            LdcInsnNode ldc = (LdcInsnNode) insn;
            return num;
        }

        if(insn instanceof IntInsnNode && ((IntInsnNode) insn).getOpcode() != NEWARRAY) {
            IntInsnNode node = (IntInsnNode) insn;
            return node.operand;
        }

        return insn.getOpcode() - ICONST_0;
    }

    public static AbstractInsnNode pushInt(int num) {
        if(num <= 5 && num >= -1) {
            return new InsnNode(ICONST_0 + num);
        }

        if(num <= Byte.MAX_VALUE && num >= Byte.MIN_VALUE) {
            return new IntInsnNode(BIPUSH, num);
        }

        if(num <= Short.MAX_VALUE && num >= Short.MIN_VALUE) {
            return new IntInsnNode(SIPUSH, num);
        }

        return new LdcInsnNode(num);
    }

    public static boolean isReturn(AbstractInsnNode insn) {
        return insn.getOpcode() >= IRETURN && insn.getOpcode() <= RETURN;
    }

    public static int getJumpConsumer(int opcode) {
        switch (opcode) {
            case IFEQ:
            case IFNE:
            case IFLE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFNULL:
            case IFNONNULL:
                return 1;
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
                return 2;
            default:
                return 0;
        }
    }

//    public static void sortSwitch(LookupSwitchInsnNode sw) {
//        var entries = IntStream.range(0, sw.keys.size())
//                .mapToObj(i -> Map.Entry(sw.keys.get(i), sw.labels.get(i)))
//                .sorted(Map.Entry.comparingByKey())
//                .toList();
//
//        sw.keys.clear();
//        sw.labels.clear();
//
//        for (var entry : entries) {
//            sw.keys.add(entry.getKey());
//            sw.labels.add(entry.getValue());
//        }
//    }

    public static void translateConcatenation(MethodNode methodNode) {
        final char STACK_ARG_CONSTANT = '\u0001';
        final char BSM_ARG_CONSTANT = '\u0002';

        for (AbstractInsnNode ain : methodNode.instructions.toArray()) {
            if (ain.getOpcode() == INVOKEDYNAMIC) {
                final InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) ain;

                if (indy.bsm.getOwner().equals("java/lang/invoke/StringConcatFactory")
                        && indy.bsm.getName().equals("makeConcatWithConstants")) {
                    final String pattern = (String) indy.bsmArgs[0];

                    final Type[] stackArgs = Type.getArgumentTypes(indy.desc);
                    final Object[] bsmArgs = Arrays.copyOfRange(indy.bsmArgs, 1, indy.bsmArgs.length);

                    int stackArgsCount = 0;
                    for (char c : pattern.toCharArray()) {
                        if (c == STACK_ARG_CONSTANT)
                            stackArgsCount++;
                    }

                    int bsmArgsCount = 0;
                    for (char c : pattern.toCharArray()) {
                        if (c == BSM_ARG_CONSTANT)
                            bsmArgsCount++;
                    }

                    if (stackArgs.length != stackArgsCount)
                        continue;

                    if (bsmArgs.length != bsmArgsCount)
                        continue;

                    int freeVarIndex = methodNode.maxLocals++;
                    final int[] stackIndices = new int[stackArgsCount];

                    for (int i = 0; i < stackArgs.length; i++) {
                        stackIndices[i] = freeVarIndex;
                        freeVarIndex += stackArgs[i].getSize();
                    }

                    for (int i = stackIndices.length - 1; i >= 0; i--) {
                        methodNode.instructions.insertBefore(indy, new VarInsnNode(stackArgs[i].getOpcode(ISTORE), stackIndices[i]));
                    }

                    final InsnList list = new InsnList();
                    final char[] arr = pattern.toCharArray();

                    int stackArgsIndex = 0;
                    int bsmArgsIndex = 0;

                    StringBuilder builder = new StringBuilder();
                    list.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
                    list.add(new InsnNode(DUP));
                    list.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));

                    for (char c : arr) {
                        if (c == STACK_ARG_CONSTANT) {
                            if (builder.length() > 0) {
                                list.add(new LdcInsnNode(builder.toString()));
                                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
                                builder = new StringBuilder();
                            }

                            final Type stackArg = stackArgs[stackArgsIndex++];
                            final int stackIndex = stackIndices[stackArgsIndex - 1];

                            if (stackArg.getSort() == Type.OBJECT) {
                                list.add(new VarInsnNode(ALOAD, stackIndex));
                                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;"));
                            } else if (stackArg.getSort() == Type.ARRAY) {
                                list.add(new VarInsnNode(ALOAD, stackIndex));
                                list.add(new MethodInsnNode(INVOKESTATIC, "java/util/Arrays", "toString", "([Ljava/lang/Object;)Ljava/lang/String;"));
                                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
                            } else {
                                list.add(new VarInsnNode(stackArg.getOpcode(ILOAD), stackIndex));
                                String adaptedDescriptor = stackArg.getDescriptor();
                                if (adaptedDescriptor.equals("B")
                                        || adaptedDescriptor.equals("S")) {
                                    adaptedDescriptor = "I";
                                }
                                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + adaptedDescriptor + ")Ljava/lang/StringBuilder;"));
                            }
                        } else if (c == BSM_ARG_CONSTANT) {
                            list.add(new LdcInsnNode(bsmArgs[bsmArgsIndex++]));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;"));
                        } else {
                            builder.append(c);
                        }
                    }

                    if (builder.length() > 0) {
                        list.add(new LdcInsnNode(builder.toString()));
                        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
                    }
                    
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));

                    methodNode.instructions.insertBefore(indy, list);
                    methodNode.instructions.remove(indy);
                }
            }
        }
    }

    public static void box(InsnList list, Type type) {
        switch (type.getSort()) {
            case Type.CHAR:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;"));
                break;
            case Type.INT:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
                break;
            case Type.SHORT:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false));
                break;
            case Type.FLOAT:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false));
                break;
            case Type.DOUBLE:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false));
                break;
            case Type.LONG:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
                break;
            case Type.BOOLEAN:
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
                break;
            case Type.ARRAY:
                list.add(new TypeInsnNode(CHECKCAST, type.getDescriptor()));
                break;
            case Type.OBJECT:
                list.add(new TypeInsnNode(CHECKCAST, type.getInternalName()));
                break;
        }
    }

    public static void unbox(InsnList list, Type type) {
        switch (type.getSort()) {
            case Type.CHAR:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Character"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C"));
                break;
            case Type.INT:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I"));
                break;
            case Type.SHORT:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Short"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S"));
                break;
            case Type.FLOAT:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Float"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F"));
                break;
            case Type.DOUBLE:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Double"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D"));
                break;
            case Type.LONG:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Long"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J"));
                break;
            case Type.BOOLEAN:
                list.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z"));
                break;
        }
    }

    public static boolean isValidIntPush(AbstractInsnNode insn) {
        return (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Integer) || insn.getOpcode() == SIPUSH || insn.getOpcode() == BIPUSH;
    }

    public static boolean isValidLongPush(AbstractInsnNode insn) {
        return insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Long;
    }

    public static long getLong(AbstractInsnNode insn) {
        if(insn instanceof LdcInsnNode) {
            LdcInsnNode ldc = (LdcInsnNode) insn;
            return (long) ldc.cst;
        }

        throw new RuntimeException("Ass nigga");
    }

    public static AbstractInsnNode pushLong(long l) {
        if(l == 1L || l == 0L)
            return new InsnNode(LCONST_0 + (int) l);

        return new LdcInsnNode(l);
    }
}