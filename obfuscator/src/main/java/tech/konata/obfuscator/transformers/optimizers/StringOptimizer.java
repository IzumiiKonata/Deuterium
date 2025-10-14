package tech.konata.obfuscator.transformers.optimizers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 黑魔法之将String.equals()替换为str1.hashCode() == str2.hashCode()和static call optimizer
 * @author IzumiiKonata
 * @since 2024/10/18 18:15
 */
public class StringOptimizer extends Optimizer {

    @Override
    public void transform() {
        AtomicInteger stringComparisons = new AtomicInteger();
        AtomicInteger staticCall = new AtomicInteger();
        long current = System.currentTimeMillis();

        getClassWrappers().stream().filter(classWrapper -> !excluded(classWrapper)).forEach(classWrapper -> {
            classWrapper.methods.stream().filter(methodWrapper -> !excluded(methodWrapper)
                    && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper -> {
                MethodNode methodNode = methodWrapper.methodNode;

//                this.replaceStringEqualsComparisons(methodNode, true, false, stringComparisons);

                this.optimizeStringStaticCall(methodNode, staticCall);

            });
        });

        Logger.stdOut(String.format("Replaced %d String.equals() comparisons with hashCode comparisons. [%dms]", stringComparisons.get(),
                tookThisLong(current)));
    }

    public boolean matchMethodNode(MethodInsnNode methodInsnNode, String s) {
        return s.equals(methodInsnNode.owner + "." + methodInsnNode.name + ":" + methodInsnNode.desc);
    }

    private void replaceStringEqualsComparisons(MethodNode method, boolean replaceEquals, boolean replaceEqualsIgnoreCase, AtomicInteger stringComparisons) {
        for (AbstractInsnNode insnNode : method.instructions.toArray()) {
            if (insnNode instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;

                if (replaceEquals && this.matchMethodNode(methodInsnNode, "java/lang/String.equals:(Ljava/lang/Object;)Z")) {
                    InsnList replacement = new InsnList();

                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));

                    replacement.add(new InsnNode(Opcodes.SWAP));

                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));

                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "equals", "(Ljava/lang/Object;)Z", false));

                    method.instructions.insert(insnNode, replacement);
                    method.instructions.remove(insnNode);
                    stringComparisons.incrementAndGet();
                }
                if (replaceEqualsIgnoreCase && this.matchMethodNode(methodInsnNode, "java/lang/String.equalsIgnoreCase:(Ljava/lang/String;)Z")) {
                    InsnList replacement = new InsnList();

                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toUpperCase", "()Ljava/lang/String;", false));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));

                    replacement.add(new InsnNode(Opcodes.SWAP));

                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toUpperCase", "()Ljava/lang/String;", false));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));

                    replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "equals", "(Ljava/lang/Object;)Z", false));

                    method.instructions.insert(insnNode, replacement);
                    method.instructions.remove(insnNode);
                    stringComparisons.incrementAndGet();
                }
            }
        }

    }

    public AbstractInsnNode getPrevious(AbstractInsnNode node, int amount) {
        for (int i = 0; i < amount; i++) {
            node = this.getPrevious(node);
        }
        return node;
    }

    public AbstractInsnNode getPrevious(AbstractInsnNode node) {
        AbstractInsnNode prev = node.getPrevious();
        while (this.isNotInstruction(prev)) {
            prev = prev.getPrevious();
        }
        return prev;
    }

    private boolean isNotInstruction(AbstractInsnNode node) {
        return node instanceof LineNumberNode || node instanceof FrameNode || node instanceof LabelNode;
    }

    public AbstractInsnNode generateIntPush(int i) {
        if (i <= 5 && i >= -1) {
            return new InsnNode(i + 3); //iconst_i
        }
        if (i >= -128 && i <= 127) {
            return new IntInsnNode(BIPUSH, i);
        }

        if (i >= -32768 && i <= 32767) {
            return new IntInsnNode(SIPUSH, i);
        }
        return new LdcInsnNode(i);
    }

    private void optimizeStringStaticCall(MethodNode method, AtomicInteger staticCall) {
        boolean found;

        do {
            found = false;

            for (AbstractInsnNode insnNode : method.instructions.toArray()) {
                if (insnNode instanceof MethodInsnNode) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    AbstractInsnNode prev = this.getPrevious(methodInsnNode, 1);

                    if (prev instanceof LdcInsnNode && ((LdcInsnNode) prev).cst instanceof String && (this.matchMethodNode(methodInsnNode, "java/lang/Object.hashCode:()I") || this.matchMethodNode(methodInsnNode, "java/lang/String.hashCode:()I"))) {
                        method.instructions.insert(insnNode, this.generateIntPush(((LdcInsnNode) prev).cst.hashCode()));
                        method.instructions.remove(insnNode);
                        method.instructions.remove(prev);
                        found = true;
                    }
                    if (prev instanceof LdcInsnNode && ((LdcInsnNode) prev).cst instanceof String && (this.matchMethodNode(methodInsnNode, "java/lang/String.toUpperCase:()Ljava/lang/String;"))) {
                        method.instructions.insert(insnNode, new LdcInsnNode(((String) ((LdcInsnNode) prev).cst).toUpperCase()));
                        method.instructions.remove(insnNode);
                        method.instructions.remove(prev);
                        found = true;
                    }
                    if (prev instanceof LdcInsnNode && ((LdcInsnNode) prev).cst instanceof String && (this.matchMethodNode(methodInsnNode, "java/lang/String.toLowerCase:()Ljava/lang/String;"))) {
                        method.instructions.insert(insnNode, new LdcInsnNode(((String) ((LdcInsnNode) prev).cst).toLowerCase()));
                        method.instructions.remove(insnNode);
                        method.instructions.remove(prev);
                        found = true;
                    }

                    if (found)
                        staticCall.incrementAndGet();
                }
            }
        } while (found);
    }

    @Override
    public String getName() {
        return "String Comparison Optimizer";
    }

}
