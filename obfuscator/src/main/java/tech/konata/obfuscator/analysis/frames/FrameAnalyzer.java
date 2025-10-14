package tech.konata.obfuscator.analysis.frames;

import lombok.var;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.analysis.Frame;

public class FrameAnalyzer {
    public static String generateMap(Frame<AbstractValue> frame) {
        var sb = new StringBuilder("stack: { ");

        if(frame.getStackSize() > 0) {
            for(int i = 0; i < frame.getStackSize(); i++) {
                var type = frame.getStack(i).getType();

                if(type != null) {
                    sb.append("'").append(type.getDescriptor()).append("'").append(", ");
                } else {
                    sb.append("'uninitialized', ");
                }
            }
            sb.delete(sb.length() - 2, sb.length());
        }

        sb.append(" }, locals: { ");
        if(frame.getLocals() > 0) {
            for(int i = 0; i < frame.getLocals(); i++) {
                var type = frame.getLocal(i).getType();

                if(type != null) {
                    sb.append("'").append(type.getDescriptor()).append("'").append(", ");
                } else {
                    sb.append("'uninitialized', ");
                }
            }
            sb.delete(sb.length() - 2, sb.length());
        }

        sb.append(" }");
        return sb.toString();
    }

    public static boolean equals(Frame<AbstractValue> frame, Frame<AbstractValue> frame2) {
        return generateMap(frame).equals(generateMap(frame2));
    }
}