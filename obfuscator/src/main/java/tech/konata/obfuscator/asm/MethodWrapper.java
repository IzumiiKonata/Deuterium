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

import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.MethodNode;
import tech.konata.obfuscator.asm.accesses.Access;
import tech.konata.obfuscator.asm.accesses.MethodAccess;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for MethodNodes.
 *
 * @author ItzSomebody
 */
public class MethodWrapper {
    /**
     * Attached MethodNode.
     */
    public MethodNode methodNode;

    /**
     * Owner of the method this MethodWrapper represents.
     */
    public final ClassWrapper owner;

    /**
     * Original method name;
     */
    public final String originalName;

    /**
     * Original method description.
     */
    public final String originalDescription;

    private final Access access;

    /**
     * Creates a MethodWrapper object.
     *
     * @param methodNode          the {@link MethodNode} this wrapper represents.
     * @param owner               the owner of this represented method.
     * @param originalName        the original method name.
     * @param originalDescription the original method description.
     */
    public MethodWrapper(MethodNode methodNode, ClassWrapper owner, String originalName, String originalDescription) {
        this.methodNode = methodNode;
        this.owner = owner;
        this.originalName = originalName;
        this.originalDescription = originalDescription;
        this.access = new MethodAccess(this);
    }

    public MethodWrapper(MethodNode methodNode, ClassWrapper owner) {
        this(methodNode, owner, methodNode.name, methodNode.desc);
    }


    // -----------------
    // Getters / Setters
    // -----------------

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void setMethodNode(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getOriginalDescriptor() {
        return originalDescription;
    }

    public ClassWrapper getOwner() {
        return owner;
    }

    // ------------
    // Access stuff
    // ------------

    public void addAccessFlags(int flags) {
        methodNode.access |= flags;
    }

    public void removeAccessFlags(int flags) {
        methodNode.access &= ~flags;
    }

    public boolean isPublic() {
        return (ACC_PUBLIC & methodNode.access) != 0;
    }

    public boolean isPrivate() {
        return (ACC_PRIVATE & methodNode.access) != 0;
    }

    public boolean isProtected() {
        return (ACC_PROTECTED & methodNode.access) != 0;
    }

    public boolean isStatic() {
        return (ACC_STATIC & methodNode.access) != 0;
    }

    public boolean isFinal() {
        return (ACC_FINAL & methodNode.access) != 0;
    }

    public boolean isSynchronized() {
        return (ACC_SYNCHRONIZED & methodNode.access) != 0;
    }

    public boolean isBridge() {
        return (ACC_BRIDGE & methodNode.access) != 0;
    }

    public boolean isVarargs() {
        return (ACC_VARARGS & methodNode.access) != 0;
    }

    public boolean isNative() {
        return (ACC_NATIVE & methodNode.access) != 0;
    }

    public boolean isAbstract() {
        return (ACC_ABSTRACT & methodNode.access) != 0;
    }

    public boolean isStrict() {
        return (ACC_STRICT & methodNode.access) != 0;
    }

    public boolean isSynthetic() {
        return (ACC_SYNTHETIC & methodNode.access) != 0;
    }

    public boolean isMandated() {
        return (ACC_MANDATED & methodNode.access) != 0;
    }

    public boolean isDeprecated() {
        return (ACC_DEPRECATED & methodNode.access) != 0;
    }

    // -----
    // Misc.
    // -----

    public boolean hasInstructions() {
        return methodNode.instructions.size() > 0;
    }

    public boolean hasVisibleAnnotations() {
        return methodNode.visibleAnnotations != null && methodNode.visibleAnnotations.size() > 0;
    }

    public int getCodeSize() {
        CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        methodNode.accept(evaluator);
        return evaluator.getMaxSize();
    }

    public int getLeewaySize() {
        return /*MAX_CODE_SIZE*/ 65536 - getCodeSize();
    }

    /**
     * @return {@link MethodAccess} wrapper of represented {@link MethodNode}'s access flags.
     */
    public Access getAccess() {
        return access;
    }

    /**
     * @return raw access flags of wrapped {@link MethodNode}.
     */
    public int getAccessFlags() {
        return methodNode.access;
    }

    /**
     * @param access access flags to set.
     */
    public void setAccessFlags(int access) {
        methodNode.access = access;
    }

}
