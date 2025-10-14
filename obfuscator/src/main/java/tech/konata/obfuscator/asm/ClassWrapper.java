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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import tech.konata.obfuscator.asm.accesses.Access;
import tech.konata.obfuscator.asm.accesses.ClassAccess;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for ClassNodes.
 *
 * @author ItzSomebody
 */
@Getter
public class ClassWrapper {
    /**
     * Attached class node.
     */
    public ClassNode classNode;

    /**
     * Original name of ClassNode. Really useful when class got renamed.
     */
    public final String originalName;

    /**
     * Quick way of figuring out if this is represents library class or not.
     */
    public final boolean libraryNode;

    /**
     * Methods.
     */
    public final List<MethodWrapper> methods = new CopyOnWriteArrayList<>();

    /**
     * Fields.
     */
    public final List<FieldWrapper> fields = new CopyOnWriteArrayList<>();

    private final List<ClassWrapper> parents = new CopyOnWriteArrayList<>();
    private final List<ClassWrapper> children = new CopyOnWriteArrayList<>();

    private final Access access;

    /**
     * Creates a ClassWrapper object.
     *
     * @param classNode   the attached {@link ClassNode}.
     * @param libraryNode is this a library class?
     */
    public ClassWrapper(ClassNode classNode, boolean libraryNode) {
        this.classNode = classNode;
        this.originalName = classNode.name;
        this.libraryNode = libraryNode;

        ClassWrapper instance = this;
        classNode.methods.forEach(methodNode -> methods.add(new MethodWrapper(methodNode, instance, methodNode.name,
                methodNode.desc)));
        if (classNode.fields != null)
            classNode.fields.forEach(fieldNode -> fields.add(new FieldWrapper(fieldNode, instance, fieldNode.name,
                    fieldNode.desc)));

        this.access = new ClassAccess(this);
    }


    public ClassNode getClassNode() {
        return classNode;
    }

    public void setClassNode(ClassNode classNode) {
        this.classNode = classNode;
    }

    public String getName() {
        return classNode.name;
    }

    public String getSuperName() {
        return classNode.superName;
    }

    public List<String> getInterfaceNames() {
        return classNode.interfaces;
    }

    public String getOriginalName() {
        return originalName;
    }

    public boolean isLibraryNode() {
        return libraryNode;
    }

    public List<MethodWrapper> getMethods() {
        return methods;
    }

    public Stream<MethodWrapper> methodStream() {
        return getMethods().stream();
    }

    public List<FieldWrapper> getFields() {
        return fields;
    }

    public Stream<FieldWrapper> fieldStream() {
        return getFields().stream();
    }

    /**
     * @return current interfaces of wrapped {@link ClassNode}.
     */
    public List<String> getInterfaces() {
        return classNode.interfaces;
    }

    /**
     * @return current package name of wrapped {@link ClassNode}.
     */
    public String getPackageName() {
        return classNode.name.substring(0, classNode.name.lastIndexOf('/') + 1);
    }

    // ------------
    // Access stuff
    // ------------

    public void addAccessFlags(int flags) {
        classNode.access |= flags;
    }

    public void removeAccessFlags(int flags) {
        classNode.access &= ~flags;
    }

    public boolean isPublic() {
        return (classNode.access & ACC_PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (classNode.access & ACC_PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (classNode.access & ACC_PROTECTED) != 0;
    }

    public boolean isFinal() {
        return (classNode.access & ACC_FINAL) != 0;
    }

    public boolean isSuper() {
        return (classNode.access & ACC_SUPER) != 0;
    }

    public boolean isInterface() {
        return (classNode.access & ACC_INTERFACE) != 0;
    }

    public boolean isAbstract() {
        return (classNode.access & ACC_ABSTRACT) != 0;
    }

    public boolean isSynthetic() {
        return (classNode.access & ACC_SYNTHETIC) != 0;
    }

    public boolean isAnnotation() {
        return (classNode.access & ACC_ANNOTATION) != 0;
    }

    public boolean isEnum() {
        return (classNode.access & ACC_ENUM) != 0;
    }

    public boolean isModule() {
        return (classNode.access & ACC_MODULE) != 0;
    }

    public boolean isRecord() {
        return (classNode.access & ACC_RECORD) != 0;
    }

    public boolean isDeprecated() {
        return (classNode.access & ACC_DEPRECATED) != 0;
    }

    public boolean containsMethodNode(String name, String desc) {
        return classNode.methods.stream().anyMatch(methodNode -> name.equals(methodNode.name) && desc.equals(methodNode.desc));
    }

    public boolean containsFieldNode(String name, String desc) {
        return classNode.fields.stream().anyMatch(fieldNode -> name.equals(fieldNode.name) && desc.equals(fieldNode.desc));
    }

    /**
     * Returns true if the class allows dynamic constants (Java 11 and above).
     */
    public boolean allowsConstDy() {
        return (classNode.version >= V11) && (classNode.version != V1_1);
    }

    /**
     * Returns true if this class allows invokedynamic instructions (Java 7 and above).
     */
    public boolean allowsIndy() {
        return (classNode.version >= V1_7) && (classNode.version != V1_1);
    }

    /**
     * Returns true if this class allows JSR and RET instructions (Java 5 and below).
     */
    public boolean allowsJsr() {
        return (classNode.version <= V1_5) || (classNode.version == V1_1);
    }

    public boolean hasVisibleAnnotations() {
        return classNode.visibleAnnotations != null && classNode.visibleAnnotations.size() > 0;
    }

    public MethodNode getMethod(String name, String desc) {
        return getClassNode().methods.stream().filter(methodNode -> name.equals(methodNode.name)
                && desc.equals(methodNode.desc)).findAny().orElse(null);
    }

    public void addMethod(MethodNode methodNode) {
        classNode.methods.add(methodNode);
        methods.add(new MethodWrapper(methodNode, this));
    }

    public MethodNode getOrCreateClinit() {
        MethodNode clinit = getMethod("<clinit>", "()V");

        if (clinit == null) {
            clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(new InsnNode(Opcodes.RETURN));
            addMethod(clinit);
        }

        return clinit;
    }

    /**
     * @return {@link ClassAccess} wrapper of represented {@link ClassNode}'s access flags.
     */
    public Access getAccess() {
        return access;
    }

    /**
     * @return raw access flags of wrapped {@link ClassNode}.
     */
    public int getAccessFlags() {
        return classNode.access;
    }

    /**
     * @param access access flags to set.
     */
    public void setAccessFlags(int access) {
        classNode.access = access;
    }

}
