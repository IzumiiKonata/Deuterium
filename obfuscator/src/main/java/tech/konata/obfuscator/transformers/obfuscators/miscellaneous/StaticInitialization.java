/*
 * Radon - An open-source Java obfuscator
 * Copyright (C) 2019 ItzSomebody
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

package tech.konata.obfuscator.transformers.obfuscators.miscellaneous;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.Main;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.utils.BytecodeUtils;

/**
 * Moves initialization of all static fields into {@code <clinit>} of the class
 *
 * @author ItzSomebody
 * @author superblaubeere27
 */
public class StaticInitialization extends Transformer {
    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        getClassWrappers().stream().filter(classWrapper -> !excluded(classWrapper)).forEach(classWrapper -> {
            MethodNode clinit = classWrapper.getOrCreateClinit();

            classWrapper.getFields().stream().filter(fieldWrapper -> !excluded(fieldWrapper)
                    && Modifier.isStatic(fieldWrapper.getFieldNode().access)
                    && fieldWrapper.getFieldNode().value != null).forEach(fieldWrapper -> {
                FieldNode fieldNode = fieldWrapper.getFieldNode();
                Object val = fieldNode.value;

                exit:
                {
                    InsnList toAdd = new InsnList();

                    if (val instanceof String)
                        toAdd.insert(new LdcInsnNode(val));
                    else if (val instanceof Integer)
                        toAdd.insert(BytecodeUtils.getNumberInsn((Integer) val));
                    else if (val instanceof Long)
                        toAdd.insert(BytecodeUtils.getNumberInsn((Long) val));
                    else if (val instanceof Float)
                        toAdd.insert(BytecodeUtils.getNumberInsn((Float) val));
                    else if (val instanceof Double)
                        toAdd.insert(BytecodeUtils.getNumberInsn((Double) val));
                    else
                        break exit;

                    toAdd.add(new FieldInsnNode(PUTSTATIC, classWrapper.getName(), fieldNode.name, fieldNode.desc));
                    clinit.instructions.insert(toAdd);
                    fieldNode.value = null;

                    counter.incrementAndGet();
                }
            });
        });

        Logger.stdOut("Moved " + counter.get() + " field values into static block.");
    }

    @Override
    public String getName() {
        return "Static Initialization";
    }

    @Override
    public ExclusionType getExclusionType() {
        return ExclusionType.GLOBAL;
    }

}