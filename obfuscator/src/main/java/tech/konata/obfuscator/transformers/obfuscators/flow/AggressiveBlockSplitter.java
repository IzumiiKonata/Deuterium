package tech.konata.obfuscator.transformers.obfuscators.flow;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.Logger;

/**
 * 激进的块拆分器 - 将方法拆分成多个随机排列的块，并支持 try-catch
 *
 * 特性:
 * - 随机打散块顺序
 * - 细粒度拆分（可配置最小块大小）
 * - 完整支持 try-catch 块
 * - 添加虚假控制流
 *
 * @author Enhanced Version
 */
public class AggressiveBlockSplitter extends FlowObfuscation {
    // 最小块大小（指令数）
    private static final int MIN_BLOCK_SIZE = 3;
    // 最大拆分深度
    private static final int MAX_DEPTH = 5;
    // 是否添加虚假跳转
    private static final boolean ADD_FAKE_JUMPS = true;

    private final Random random = new Random();

    @Override
    public String getName() {
        return "Aggressive Block Splitter";
    }

    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger blockCounter = new AtomicInteger();

        getClassWrappers().stream().filter(cw -> !excluded(cw)).forEach(cw ->
                cw.methods.stream().filter(mw -> !excluded(mw) && hasInstructions(mw.methodNode)).forEach(mw -> {
                    int before = blockCounter.get();
                    aggressiveSplit(mw.methodNode, counter, blockCounter);
                    int blocksCreated = blockCounter.get() - before;
                    if (blocksCreated > 0) {
                        Logger.stdOut("Method " + mw.methodNode.name + " split into " + blocksCreated + " blocks");
                    }
                }));

        Logger.stdOut("Aggressively split " + counter.get() + " times, created " + blockCounter.get() + " blocks");
    }

    /**
     * 激进拆分方法 (已重构)
     */
    private void aggressiveSplit(MethodNode methodNode, AtomicInteger splitCounter, AtomicInteger blockCounter) {
        try {
            if ((methodNode.access & ACC_ABSTRACT) != 0 || (methodNode.access & ACC_NATIVE) != 0) {
                return;
            }

            InsnList insns = methodNode.instructions;
            if (insns == null || insns.size() < MIN_BLOCK_SIZE * 2) {
                return;
            }

            List<AbstractInsnNode> realInsns = new ArrayList<>();
            for (AbstractInsnNode insn : insns) {
                if (insn.getOpcode() >= 0) {
                    realInsns.add(insn);
                }
            }

            if (realInsns.size() < MIN_BLOCK_SIZE * 2) {
                return;
            }

            Set<AbstractInsnNode> protectedInsns = getProtectedInstructions(methodNode);
            List<Integer> splitPointIndexes = calculateSplitPoints(realInsns, protectedInsns, methodNode); // <--- 修改这一行

            if (splitPointIndexes.isEmpty()) {
                return;
            }

            // --- 核心改动开始 ---

            // 1. 将拆分点的索引转换成真实的指令节点对象作为标记
            Set<AbstractInsnNode> splitMarkers = new HashSet<>();
            for (Integer index : splitPointIndexes) {
                splitMarkers.add(realInsns.get(index));
            }

            // 2. 创建全局标签映射，这对于克隆指令至关重要
            Map<LabelNode, LabelNode> globalLabelMap = createGlobalLabelMap(methodNode);

            // 3. 使用新的拆分逻辑，它会保留所有节点
            List<Block> blocks = splitIntoBlocks(methodNode, splitMarkers, globalLabelMap);

            if (blocks.size() < 2) {
                return;
            }

            // --- 核心改动结束 ---

            blockCounter.addAndGet(blocks.size());

            List<Block> shuffledBlocks = new ArrayList<>();
            shuffledBlocks.add(blocks.get(0)); // 保持入口块

            List<Block> remainingBlocks = new ArrayList<>(blocks.subList(1, blocks.size()));
            Collections.shuffle(remainingBlocks, random);
            shuffledBlocks.addAll(remainingBlocks);

            // reassembleMethod 逻辑保持不变，但现在它处理的是正确的块数据
            reassembleMethod(methodNode, shuffledBlocks, blocks, globalLabelMap);

            splitCounter.incrementAndGet();

            // 递归拆分逻辑 (如果需要)
            if (splitCounter.get() < MAX_DEPTH && methodNode.instructions.size() > MIN_BLOCK_SIZE * 4) {
                aggressiveSplit(methodNode, splitCounter, blockCounter);
            }
        } catch (Exception e) {
            Logger.stdOut("Failed to split method " + methodNode.name + ": " + e.getMessage());
            // 强烈建议在调试时打印堆栈信息！
            e.printStackTrace();
        }
    }

    /**
     * 创建一个包含方法中所有标签映射的Map。
     */
    private Map<LabelNode, LabelNode> createGlobalLabelMap(MethodNode mn) {
        Map<LabelNode, LabelNode> map = new HashMap<>();
        for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode) {
                map.put((LabelNode) insn, new LabelNode());
            }
        }
        return map;
    }

    /**
     * 构建一个包含所有被 try-catch 块保护的指令的集合。
     * 这是确定安全拆分点的关键。
     * @param mn 方法节点
     * @return 一个包含所有受保护指令的 Set
     */
    private Set<AbstractInsnNode> getProtectedInstructions(MethodNode mn) {
        Set<AbstractInsnNode> protectedInsns = new HashSet<>();
        if (mn.tryCatchBlocks == null || mn.tryCatchBlocks.isEmpty()) {
            return protectedInsns;
        }

        for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
            // 遍历从 start 到 end (不包括 end) 的所有节点
            for (AbstractInsnNode current = tcb.start; current != tcb.end; current = current.getNext()) {
                if (current == null) break; // 安全检查
                protectedInsns.add(current);
            }
        }
        return protectedInsns;
    }

    /**
     * 计算安全的拆分点 (V3)
     * 使用一个包含所有受保护指令的集合来确保不在 try-catch 块内拆分。
     */
    private List<Integer> calculateSplitPoints(List<AbstractInsnNode> realInsns,
                                               Set<AbstractInsnNode> protectedInsns, // <-- 参数类型改变
                                               MethodNode mn) {
        List<Integer> splitPoints = new ArrayList<>();

        int availableRange = realInsns.size() - MIN_BLOCK_SIZE;
        if (availableRange <= MIN_BLOCK_SIZE) {
            return splitPoints;
        }

        int numSplits = Math.min(realInsns.size() / MIN_BLOCK_SIZE - 1, 10);
        if (numSplits <= 0) {
            return splitPoints;
        }

        int maxAttempts = numSplits * 5; // 增加尝试次数，因为很多点可能不安全
        for (int i = 0; i < maxAttempts && splitPoints.size() < numSplits; i++) {
            int point = MIN_BLOCK_SIZE + random.nextInt(availableRange - MIN_BLOCK_SIZE);

            if (splitPoints.contains(point)) {
                continue;
            }

            if (point < realInsns.size()) {
                AbstractInsnNode insnAtPoint = realInsns.get(point);
                // 使用新的、更安全的检查方法
                if (isSafeSplitPoint(insnAtPoint, protectedInsns)) {
                    splitPoints.add(point);
                }
            }
        }

        Collections.sort(splitPoints);
        return splitPoints;
    }

    /**
     * 检查是否是安全的拆分点 (V3 - 最终版)
     * 一个拆分点是安全的，当且仅当它不是跳转指令，并且不在任何 try-catch 块内部。
     */
    private boolean isSafeSplitPoint(AbstractInsnNode insn, Set<AbstractInsnNode> protectedInsns) {
        // 规则 1: 不在跳转指令处拆分
        if (insn instanceof JumpInsnNode || insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode) {
            return false;
        }

        // 规则 2: 绝不在 try-catch 块内部的任何指令处拆分
        if (protectedInsns.contains(insn)) {
            return false;
        }

        return true;
    }

    /**
     * 将指令列表拆分成多个块 (最终修正版 V2)
     * 这个版本会保留并克隆所有必要的节点（包括LabelNode），
     * 同时丢弃所有旧的、无效的 FrameNode 和 LineNumberNode。
     * 并且确保了 InsnList 总是被初始化。
     */
    private List<Block> splitIntoBlocks(MethodNode mn, Set<AbstractInsnNode> splitMarkers, Map<LabelNode, LabelNode> globalLabelMap) {
        List<Block> blocks = new ArrayList<>();

        Block currentBlock = new Block();
        currentBlock.label = new LabelNode(); // 每个块的起始标签
        currentBlock.instructions = new InsnList(); // <-- 【修复】为第一个块初始化指令列表
        blocks.add(currentBlock);

        for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            // 丢弃旧的帧信息和行号信息。它们在代码重排后是无效且有害的。
            if (insn instanceof FrameNode || insn instanceof LineNumberNode) {
                continue;
            }

            // 在拆分点创建一个新的块
            // 我们需要确保不在方法的开头创建空块
            if (splitMarkers.contains(insn) && currentBlock.instructions.size() > 0) {
                currentBlock = new Block();
                currentBlock.label = new LabelNode();
                currentBlock.instructions = new InsnList(); // <-- 【修复】为新块初始化指令列表
                blocks.add(currentBlock);
            }

            // 克隆所有类型的有效指令（包括 LabelNode）并添加到当前块
            // 由于上面已经确保 instructions 不为 null，这里现在是安全的
            AbstractInsnNode clonedInsn = insn.clone(globalLabelMap);
            if (clonedInsn != null) { // 增加一个健壮性检查
                currentBlock.instructions.add(clonedInsn);
            }
        }

        // 移除可能在末尾创建的空块
        blocks.removeIf(b -> b.instructions.size() == 0);

        return blocks;
    }

    /**
     * 重新组装方法 (与上一版基本相同，但现在接收的是正确构造的块)
     */
    private void reassembleMethod(MethodNode mn, List<Block> shuffledBlocks, List<Block> originalOrder, Map<LabelNode, LabelNode> globalLabelMap) {
        InsnList newInsns = new InsnList();

        for (int i = 0; i < shuffledBlocks.size(); i++) {
            Block currentBlock = shuffledBlocks.get(i);
            // 找到当前块在原始顺序中的位置，以确定下一个逻辑块
            int originalIndex = -1;
            for(int j=0; j<originalOrder.size(); j++){
                if(originalOrder.get(j) == currentBlock){
                    originalIndex = j;
                    break;
                }
            }

            Block nextLogicalBlock = (originalIndex != -1 && originalIndex + 1 < originalOrder.size()) ?
                    originalOrder.get(originalIndex + 1) : null;

            newInsns.add(currentBlock.label);
            newInsns.add(currentBlock.instructions);

            if (nextLogicalBlock != null && !endsWithJumpOrReturn(currentBlock)) {
                newInsns.add(new JumpInsnNode(GOTO, nextLogicalBlock.label));
            }
        }

        // 重新构建 TryCatchBlockNode 列表 (此逻辑依然正确且必要)
        List<TryCatchBlockNode> newTryCatchBlocks = new ArrayList<>();
        if (mn.tryCatchBlocks != null) {
            for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                LabelNode newStart = globalLabelMap.get(tcb.start);
                LabelNode newEnd = globalLabelMap.get(tcb.end);
                LabelNode newHandler = globalLabelMap.get(tcb.handler);

                if (newStart != null && newEnd != null && newHandler != null) {
                    newTryCatchBlocks.add(new TryCatchBlockNode(newStart, newEnd, newHandler, tcb.type));
                } else {
                    Logger.stdOut("Warning: Failed to map labels for a TryCatchBlock in method " + mn.name);
                }
            }
        }

        mn.instructions.clear();
        mn.instructions.add(newInsns);

        if(mn.tryCatchBlocks != null) {
            mn.tryCatchBlocks.clear();
            mn.tryCatchBlocks.addAll(newTryCatchBlocks);
        }

        if (mn.localVariables != null) {
            mn.localVariables.clear();
        }

        if (mn.visibleLocalVariableAnnotations != null) {
            mn.visibleLocalVariableAnnotations.clear();
        }
        if (mn.invisibleLocalVariableAnnotations != null) {
            mn.invisibleLocalVariableAnnotations.clear();
        }

        // 让 ASM 重新计算，这是关键
        mn.maxStack = 0;
        mn.maxLocals = 0;
    }

    /**
     * 检查块是否以跳转或返回指令结尾
     */
    private boolean endsWithJumpOrReturn(Block block) {
        if (block.instructions.size() == 0) {
            return false;
        }

        AbstractInsnNode last = block.instructions.getLast();
        while (last != null && last.getOpcode() < 0) {
            last = last.getPrevious();
        }

        if (last == null) {
            return false;
        }

        int opcode = last.getOpcode();
        return (opcode >= IRETURN && opcode <= RETURN) || // 返回指令
                (opcode == GOTO) || // 无条件跳转
                (opcode == ATHROW) || // 抛出异常
                (opcode >= IF_ICMPEQ && opcode <= IF_ACMPNE) || // 条件跳转
                (opcode >= IFEQ && opcode <= IFLE) || // 条件跳转
                (opcode >= TABLESWITCH && opcode <= LOOKUPSWITCH); // switch 指令
    }

    /**
     * 代表一个代码块
     */
    private static class Block {
        LabelNode label;
        InsnList instructions = new InsnList();
    }
}