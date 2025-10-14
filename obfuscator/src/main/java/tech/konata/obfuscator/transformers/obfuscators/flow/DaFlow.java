package tech.konata.obfuscator.transformers.obfuscators.flow;

import lombok.var;
import net.minecraft.util.Tuple;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.analysis.frames.FrameAnalyzer;
import tech.konata.obfuscator.analysis.misc.Local;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.utils.ASMUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/14 20:21
 */
public class DaFlow extends Transformer {

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.DAFLOW;
    }

    Random random = new Random();

    @Override
    public void transform() {
        getClassWrappers().stream().filter(cw -> !excluded(cw)).forEach(cw -> {
            cw.methods.stream().filter(mw -> !excluded(mw) && mw.methodNode.instructions.size() > 0).forEach(mw -> {
                MethodNode method = mw.methodNode;

                if (mw.isAbstract() || mw.isNative())
                    return;

                var frames = ASMUtils.analyzeMethod(cw.classNode, method);
                if(frames == null)
                    return;

                var local = Local.alloc(method, Type.INT_TYPE);
                int number = random.nextInt(); //we're gonna integer encrypt it anyway

                method.instructions.insert(local.store());
                method.instructions.insert(ASMUtils.pushInt(number));

                var targets = new HashMap<String, List<LabelNode>>();
                var empty = new HashMap<String, List<LabelNode>>();

                for(var insn : method.instructions) {
                    var frame = frames.get(insn);
                    if(frame == null)
                        continue;

                    switch (frame.getStackSize()) {
                        //for gotos
                        case 0: {
                            var lbl = new LabelNode();
                            if(insn instanceof LabelNode) {
                                LabelNode target = (LabelNode) insn;
                                lbl = target;
                            } else {
                                method.instructions.insertBefore(insn, lbl);
                            }

                            empty.computeIfAbsent(FrameAnalyzer.generateMap(frame), str -> new ArrayList<>()).add(lbl);
                            break;
                        }
                        //for actual flow
                        case 1: {
                            if(frame.getStack(0).getType().getSort() == Type.OBJECT)
                                continue;

                            var lbl = new LabelNode();
                            method.instructions.insertBefore(insn, lbl);

                            targets.computeIfAbsent(FrameAnalyzer.generateMap(frame), str -> new ArrayList<>()).add(lbl);
                            break;
                        }
                    }
                }

                //iterate second time with all jumps ready
                for(var insn : method.instructions) {
                    var frame = frames.get(insn);
                    if(frame == null)
                        continue;

                    switch (frame.getStackSize()) {
                        case 0: {
                            if(!(insn instanceof JumpInsnNode && insn.getOpcode() == GOTO))
                                continue;
                            JumpInsnNode jmp = (JumpInsnNode) insn;

                            var frameTargets = empty.get(FrameAnalyzer.generateMap(frame));
                            if(frameTargets == null)
                                continue;

                            var available = frameTargets.stream()
                                    .filter(e -> method.instructions.indexOf(e) > method.instructions.indexOf(insn))//again, only forward jumps
                                    .collect(Collectors.toList());
                            if(available.isEmpty())
                                continue;

                            if(!willHitReturn(insn))
                                continue;

                            if(isAtEndOfTcb(method, insn))
                                continue;

                            var last = available.get(available.size() - 1);
                            if(last == jmp.label)
                                continue;

                            var list = new InsnList();
                            list.add(local.load());
                            list.add(new JumpInsnNode(IFEQ, last)); //never jumps
                            list.add(local.load());
                            list.add(new JumpInsnNode(IFNE, jmp.label)); //real deal

                            method.instructions.insertBefore(insn, list);
                            method.instructions.remove(insn);
                            frameTargets.remove(last);
                            break;
                        }
                        case 1: {
                            if(frame.getStack(0).getType().getSort() == Type.OBJECT)
                                continue;

                            var frameTargets = targets.get(FrameAnalyzer.generateMap(frame));
                            if(frameTargets == null)
                                continue;

                            var availableLabels = frameTargets.stream()
                                    .filter(e -> method.instructions.indexOf(e) > method.instructions.indexOf(insn)) //forward jumps only
                                    .filter(e -> Math.abs(method.instructions.indexOf(e) - method.instructions.indexOf(insn)) > 7) //keep empty labels away!!
                                    .collect(Collectors.toList());
                            if(availableLabels.isEmpty())
                                continue;

                            var lastLbl = availableLabels.get(0);
                            var list = new InsnList();

                            list.add(local.load());
                            list.add(new JumpInsnNode(IFEQ, lastLbl)); //if local is 0, jump to label (should realistically never happen)

                            method.instructions.insertBefore(insn, list);
                            frameTargets.remove(lastLbl);
                            break;
                        }
                    }
                }
            });
        });
    }

    private boolean willHitReturn(AbstractInsnNode insn) {
        var current = insn;

        while (current != null) {
            if(ASMUtils.isReturn(current))
                return true;

            current = current.getNext(); //iterate forwards till hit return
        }

        return false;
    }

    //we only really need to check this, everything else is valid lol
    private boolean isAtEndOfTcb(MethodNode method, AbstractInsnNode insn) {
        if(method.tryCatchBlocks == null || method.tryCatchBlocks.isEmpty())
            return false;

        var ranges = new ArrayList<Tuple<Integer, Integer>>();
        for(var tcb : method.tryCatchBlocks) {
            ranges.add(new Tuple<>(method.instructions.indexOf(tcb.end), method.instructions.indexOf(tcb.handler)));
        }

        var idx = method.instructions.indexOf(insn);
        for(var range : ranges) {
            //start
            if(range.getFirst() <= idx && range.getSecond() >= idx)
                return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "Da Flow";
    }
}
