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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.utils.RandomUtils;
import tech.konata.obfuscator.utils.StringUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This replaces all gotos with the equivalent: if (false) throw null;.
 *
 * @author ItzSomebody
 */
public class LightFlowObfuscation extends FlowObfuscation {
    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        this.getClassWrappers().stream().filter(classWrapper ->
                !excluded(classWrapper)).forEach(classWrapper -> {
            ClassNode classNode = classWrapper.classNode;
            String fieldName = StringUtils.randomSpacesString(RandomUtils.getRandomInt(10));

            classWrapper.methods.stream().filter(methodWrapper -> !excluded(methodWrapper)
                    && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper -> {
                MethodNode methodNode = methodWrapper.methodNode;
                int leeway = getSizeLeeway(methodNode);

                for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
                    if (leeway < 10000)
                        break;

                    if (insn.getOpcode() == GOTO) {
                        methodNode.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, classNode.name,
                                fieldName, "Z"));
                        methodNode.instructions.insert(insn, new InsnNode(ATHROW));
                        methodNode.instructions.insert(insn, new InsnNode(ACONST_NULL));
                        methodNode.instructions.set(insn, new JumpInsnNode(IFEQ, ((JumpInsnNode) insn).label));
                        leeway -= 7;
                        counter.incrementAndGet();
                    }
                }
            });

            classNode.fields.add(new FieldNode(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, fieldName, "Z", null, null));
        });

        Logger.stdOut(String.format("Added %d fake throw-null sequences", counter.get()));
    }

    @Override
    protected boolean excluded(ClassWrapper classWrapper) {

        if (obfuscator.sessionInfo.isNoAnnotations() || this.isSkipAnnotationsCheck())
            return super.excluded(classWrapper);

        boolean clazz = hasAnnotation(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.Flow");

        if (clazz) {
            String clazzType = getEnumAnnotationValue(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.Flow", "strength");

            return !clazzType.equals("Light");
        } else {

            for (MethodWrapper methodWrapper : classWrapper.methods) {
                boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.Flow");

                if (method) {
                    String type = getEnumAnnotationValue(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.Flow", "strength");

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

        boolean method = hasAnnotation(methodWrapper.getMethodNode().visibleAnnotations, "tech.konata.obfuscation.Flow");
        boolean clazz = hasAnnotation(methodWrapper.getOwner().classNode.visibleAnnotations, "tech.konata.obfuscation.Flow");

        if (!(method || clazz))
            return true;

        String type = getEnumAnnotationValue(method ? methodWrapper.getMethodNode().visibleAnnotations : methodWrapper.getOwner().classNode.visibleAnnotations, "tech.konata.obfuscation.Flow", "strength");

        if (type == null) {
            return true;
        }

        return !type.equals("Light");
    }

    @Override
    public String getName() {
        return "Light flow obfuscation";
    }
}
