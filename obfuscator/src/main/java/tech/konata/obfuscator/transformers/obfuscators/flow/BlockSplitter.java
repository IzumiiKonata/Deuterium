package tech.konata.obfuscator.transformers.obfuscators.flow;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import tech.konata.obfuscator.Logger;

/**
 * This splits a method's block of code into two blocks: P1 and P2 and then inserting P2 behind P1.
 * <p>
 * P1->P2 becomes GOTO_P1->P2->P1->GOTO_P2
 * </p>
 * This is similar in functionality to http://www.sable.mcgill.ca/JBCO/examples.html#GIA but is done
 * recursively on the method to ensure maximum effectiveness.
 *
 * @author ItzSomebody
 */
public class BlockSplitter extends FlowObfuscation {
    // used to limit number of recursive calls on doSplit()
    private static final int LIMIT_SIZE = 32;

    @Override
    public String getName() {
        return "Block Splitter";
    }

    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        getClassWrappers().stream().filter(cw -> !excluded(cw)).forEach(cw ->
                cw.methods.stream().filter(mw -> !excluded(mw)).forEach(mw -> {
                    doSplit(mw.methodNode, counter, 0);
                }));

        Logger.stdOut("Split " + counter.get() + " blocks");
    }

    private static void doSplit(MethodNode methodNode, AtomicInteger counter, int callStackSize) {
        InsnList insns = methodNode.instructions;

        if (insns.size() > 10 && callStackSize < LIMIT_SIZE) {
            LabelNode p1 = new LabelNode();
            LabelNode p2 = new LabelNode();

            AbstractInsnNode p2Start = insns.get((insns.size() - 1) / 2);
            AbstractInsnNode p2End = insns.getLast();

            AbstractInsnNode p1Start = insns.getFirst();

            // We can't have trap ranges mutilated by block splitting
            if (methodNode.tryCatchBlocks.stream().anyMatch(tcbn ->
                    insns.indexOf(tcbn.end) >= insns.indexOf(p2Start)
                            && insns.indexOf(tcbn.start) <= insns.indexOf(p2Start)))
                return;

            ArrayList<AbstractInsnNode> insnNodes = new ArrayList<>();
            AbstractInsnNode currentInsn = p1Start;

            InsnList p1Block = new InsnList();

            while (currentInsn != p2Start) {
                insnNodes.add(currentInsn);

                currentInsn = currentInsn.getNext();
            }

            insnNodes.forEach(insn -> {
                insns.remove(insn);
                p1Block.add(insn);
            });

            p1Block.insert(p1);
            p1Block.add(new JumpInsnNode(GOTO, p2));

            insns.insert(p2End, p1Block);
            insns.insertBefore(p2Start, new JumpInsnNode(GOTO, p1));
            insns.insertBefore(p2Start, p2);

            counter.incrementAndGet();

            // We might have messed up variable ranges when rearranging the block order.
            if (methodNode.localVariables != null)
                new ArrayList<>(methodNode.localVariables).stream().filter(lvn ->
                        insns.indexOf(lvn.end) < insns.indexOf(lvn.start)
                ).forEach(methodNode.localVariables::remove);

            doSplit(methodNode, counter, callStackSize + 1);
        }
    }
}