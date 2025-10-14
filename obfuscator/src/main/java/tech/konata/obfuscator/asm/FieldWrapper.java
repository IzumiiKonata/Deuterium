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

package tech.konata.obfuscator.asm;

import lombok.SneakyThrows;
import org.objectweb.asm.tree.FieldNode;
import tech.konata.obfuscator.asm.accesses.Access;
import tech.konata.obfuscator.asm.accesses.FieldAccess;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for FieldNodes.
 *
 * @author ItzSomebody.
 */
public class FieldWrapper implements Cloneable {
    /**
     * Attached FieldNode.
     */
    public FieldNode fieldNode;

    /**
     * Owner of this represented field.
     */
    public ClassWrapper owner;

    /**
     * Original field name.
     */
    public final String originalName;

    /**
     * Original field description.
     */
    public final String originalDescription;

    public Access access;

    /**
     * Creates a FieldWrapper object.
     *
     * @param fieldNode           the {@link FieldNode} attached to this FieldWrapper.
     * @param owner               the owner of this represented field.
     * @param originalName        the original name of the field represented.
     * @param originalDescription the original description of the field represented.
     */
    public FieldWrapper(FieldNode fieldNode, ClassWrapper owner, String originalName, String originalDescription) {
        this.fieldNode = fieldNode;
        this.owner = owner;
        this.originalName = originalName;
        this.originalDescription = originalDescription;
        this.access = new FieldAccess(this);
    }


    // -----------------
    // Getters / Setters
    // -----------------

    public FieldNode getFieldNode() {
        return fieldNode;
    }

    public void setFieldNode(FieldNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getOriginalType() {
        return originalDescription;
    }

    public ClassWrapper getOwner() {
        return owner;
    }

    // ------------
    // Access stuff
    // ------------

    public void addAccessFlags(int flags) {
        fieldNode.access |= flags;
    }

    public void removeAccessFlags(int flags) {
        fieldNode.access &= ~flags;
    }

    public boolean isPublic() {
        return (ACC_PUBLIC & fieldNode.access) != 0;
    }

    public boolean isPrivate() {
        return (ACC_PRIVATE & fieldNode.access) != 0;
    }

    public boolean isProtected() {
        return (ACC_PROTECTED & fieldNode.access) != 0;
    }

    public boolean isStatic() {
        return (ACC_STATIC & fieldNode.access) != 0;
    }

    public boolean isFinal() {
        return (ACC_FINAL & fieldNode.access) != 0;
    }

    public boolean isVolatile() {
        return (ACC_PUBLIC & fieldNode.access) != 0;
    }

    public boolean isTransient() {
        return (ACC_PUBLIC & fieldNode.access) != 0;
    }

    public boolean isSynthetic() {
        return (ACC_SYNTHETIC & fieldNode.access) != 0;
    }

    public boolean isDeprecated() {
        return (ACC_DEPRECATED & fieldNode.access) != 0;
    }

    /**
     * @return {@link FieldAccess} wrapper of represented {@link FieldNode}'s access flags.
     */
    public Access getAccess() {
        return access;
    }

    /**
     * @return raw access flags of wrapped {@link FieldNode}.
     */
    public int getAccessFlags() {
        return fieldNode.access;
    }

    /**
     * @param access access flags to set.
     */
    public void setAccessFlags(int access) {
        fieldNode.access = access;
    }

    @Override
    @SneakyThrows
    public Object clone() {
        return super.clone();
    }
}
