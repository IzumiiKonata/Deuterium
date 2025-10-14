package tech.konata.obfuscator.utils;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsnListSerializer {
    
    private static final int MAGIC = 0xCAFEBABE;
    private static final byte VERSION = 1; // 添加版本号以便future-proofing

    /**
     * 将InsnList序列化到DataOutputStream中
     */
    public static void serialize(InsnList insnList, List<TryCatchBlockNode> tryCatchBlocks, DataOutputStream dos) throws IOException {
        // Write header
        dos.writeInt(MAGIC);
        dos.writeByte(VERSION);

        // Create label map
        Map<LabelNode, Integer> labelMap = new HashMap<>();
        int labelCounter = 0;
        for (AbstractInsnNode insn : insnList) {
            if (insn instanceof LabelNode) {
                labelMap.put((LabelNode)insn, labelCounter++);
            }
        }

        // Write try-catch blocks
        dos.writeInt(tryCatchBlocks.size());
        for (TryCatchBlockNode tcb : tryCatchBlocks) {
            dos.writeInt(labelMap.get(tcb.start));
            dos.writeInt(labelMap.get(tcb.end));
            dos.writeInt(labelMap.get(tcb.handler));
            writeString(dos, tcb.type);
        }

        // Write instructions
        dos.writeInt(insnList.size());
        for (AbstractInsnNode insn : insnList) {
            serializeInstruction(insn, dos, labelMap);
        }
    }

    /**
     * 序列化单个指令
     */
    private static void serializeInstruction(AbstractInsnNode insn, DataOutputStream dos, Map<LabelNode, Integer> labelMap) throws IOException {
        dos.writeByte(insn.getType());
        dos.writeShort(insn.getOpcode());

        switch (insn.getType()) {
            case AbstractInsnNode.INSN:
                break; // No extra data needed

            case AbstractInsnNode.INT_INSN:
                dos.writeInt(((IntInsnNode)insn).operand);
                break;
                
            case AbstractInsnNode.VAR_INSN:
                dos.writeInt(((VarInsnNode)insn).var);
                break;
                
            case AbstractInsnNode.TYPE_INSN:
                writeString(dos, ((TypeInsnNode)insn).desc);
                break;
                
            case AbstractInsnNode.FIELD_INSN:
                FieldInsnNode fieldInsn = (FieldInsnNode)insn;
                writeString(dos, fieldInsn.owner);
                writeString(dos, fieldInsn.name);
                writeString(dos, fieldInsn.desc);
                break;
                
            case AbstractInsnNode.METHOD_INSN:
                MethodInsnNode methodInsn = (MethodInsnNode)insn;
                writeString(dos, methodInsn.owner);
                writeString(dos, methodInsn.name);
                writeString(dos, methodInsn.desc);
                dos.writeBoolean(methodInsn.itf);
                break;
                
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                InvokeDynamicInsnNode invokeInsn = (InvokeDynamicInsnNode)insn;
                writeString(dos, invokeInsn.name);
                writeString(dos, invokeInsn.desc);
                writeHandle(dos, invokeInsn.bsm);
                dos.writeInt(invokeInsn.bsmArgs.length);
                for (Object arg : invokeInsn.bsmArgs) {
                    writeConstant(dos, arg);
                }
                break;
                
            case AbstractInsnNode.JUMP_INSN:
                dos.writeInt(labelMap.get(((JumpInsnNode)insn).label));
                break;
                
            case AbstractInsnNode.LABEL:
                dos.writeInt(labelMap.get((LabelNode)insn));
                break;

            case AbstractInsnNode.LDC_INSN:
                writeConstant(dos, ((LdcInsnNode)insn).cst);
                break;

            case AbstractInsnNode.IINC_INSN:
                IincInsnNode iincInsn = (IincInsnNode)insn;
                dos.writeInt(iincInsn.var);
                dos.writeInt(iincInsn.incr);
                break;

            case AbstractInsnNode.TABLESWITCH_INSN:
                TableSwitchInsnNode tableSwitchInsn = (TableSwitchInsnNode)insn;
                dos.writeInt(tableSwitchInsn.min);
                dos.writeInt(tableSwitchInsn.max);
                dos.writeInt(labelMap.get(tableSwitchInsn.dflt));
                dos.writeInt(tableSwitchInsn.labels.size());
                for (LabelNode label : tableSwitchInsn.labels) {
                    dos.writeInt(labelMap.get(label));
                }
                break;
                
            case AbstractInsnNode.LOOKUPSWITCH_INSN:
                LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode)insn;
                dos.writeInt(labelMap.get(lookupSwitchInsn.dflt));
                dos.writeInt(lookupSwitchInsn.labels.size());
                for (int i = 0; i < lookupSwitchInsn.labels.size(); i++) {
                    dos.writeInt(lookupSwitchInsn.keys.get(i));
                    dos.writeInt(labelMap.get(lookupSwitchInsn.labels.get(i)));
                }
                break;
                
            case AbstractInsnNode.MULTIANEWARRAY_INSN:
                MultiANewArrayInsnNode multiArrayInsn = (MultiANewArrayInsnNode)insn;
                writeString(dos, multiArrayInsn.desc);
                dos.writeInt(multiArrayInsn.dims);
                break;
                
            case AbstractInsnNode.FRAME:
                FrameNode frameNode = (FrameNode)insn;
                dos.writeInt(frameNode.type);
                writeFrameTypes(dos, frameNode.local, labelMap);
                writeFrameTypes(dos, frameNode.stack, labelMap);
                break;
                
            case AbstractInsnNode.LINE:
                LineNumberNode lineNode = (LineNumberNode)insn;
                dos.writeInt(lineNode.line);
                dos.writeInt(labelMap.get(lineNode.start));
                break;
        }
    }

    private static void writeString(DataOutputStream dos, String str) throws IOException {
        dos.writeBoolean(str != null);
        if (str != null) {
            dos.writeUTF(str);
        }
    }

    private static void writeHandle(DataOutputStream dos, Handle handle) throws IOException {
        dos.writeInt(handle.getTag());
        writeString(dos, handle.getOwner());
        writeString(dos, handle.getName());
        writeString(dos, handle.getDesc());
        dos.writeBoolean(handle.isInterface());
    }

    private static void writeConstant(DataOutputStream dos, Object cst) throws IOException {
        if (cst == null) {
            dos.writeByte(0);
        } else if (cst instanceof Integer) {
            dos.writeByte(1);
            dos.writeInt((Integer)cst);
        } else if (cst instanceof Float) {
            dos.writeByte(2);
            dos.writeFloat((Float)cst);
        } else if (cst instanceof Long) {
            dos.writeByte(3);
            dos.writeLong((Long)cst);
        } else if (cst instanceof Double) {
            dos.writeByte(4);
            dos.writeDouble((Double)cst);
        } else if (cst instanceof String) {
            dos.writeByte(5);
            dos.writeUTF((String)cst);
        } else if (cst instanceof Type) {
            dos.writeByte(6);
            dos.writeUTF(((Type)cst).getDescriptor());
        } else if (cst instanceof Handle) {
            dos.writeByte(7);
            writeHandle(dos, (Handle)cst);
        } else {
            throw new IOException("Unsupported constant type: " + cst.getClass().getName());
        }
    }

    private static void writeFrameTypes(DataOutputStream dos, List<Object> types, Map<LabelNode, Integer> labelMap) throws IOException {
        dos.writeInt(types != null ? types.size() : 0);
        if (types != null) {
            for (Object type : types) {
                writeFrameType(dos, type, labelMap);
            }
        }
    }

    private static void writeFrameType(DataOutputStream dos, Object type, Map<LabelNode, Integer> labelMap) throws IOException {
        if (type == null) {
            dos.writeByte(0);
        } else if (type instanceof Integer) {
            dos.writeByte(1);
            dos.writeInt((Integer)type);
        } else if (type instanceof String) {
            dos.writeByte(2);
            dos.writeUTF((String)type);
        } else if (type instanceof LabelNode) {
            dos.writeByte(3);
            dos.writeInt(labelMap.get(type));
        } else {
            throw new IOException("Unsupported frame type: " + type.getClass().getName());
        }
    }
}
