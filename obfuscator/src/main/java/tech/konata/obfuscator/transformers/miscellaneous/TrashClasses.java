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

package tech.konata.obfuscator.transformers.miscellaneous;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import lombok.SneakyThrows;
import obfuscated.by.IzumiKonata.KonataShieldTrashClasses;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.utils.IOUtils;
import tech.konata.obfuscator.utils.RandomUtils;
import tech.konata.obfuscator.utils.StringUtils;

/**
 * Not really a transformer. This "transformer" generates unused classes full
 * of random bytecode.
 *
 * @author ItzSomebody
 */
public class TrashClasses extends Transformer {
    private static ArrayList<String> DESCRIPTORS = new ArrayList<>();

    static {
        DESCRIPTORS.add("Z");
        DESCRIPTORS.add("C");
        DESCRIPTORS.add("B");
        DESCRIPTORS.add("S");
        DESCRIPTORS.add("I");
        DESCRIPTORS.add("F");
        DESCRIPTORS.add("J");
        DESCRIPTORS.add("D");
        DESCRIPTORS.add("V");
    }

    final String byteArrayName = randomSigmaStyleString(3);

    @Override
    public void transform() {
        ArrayList<String> classNames = getClassPath().keySet().stream().filter(cp -> cp.startsWith("catch_me_if_u_can")).collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < classNames.size() % 20; i++) {
            String s = classNames.get(RandomUtils.getRandomIntNoOrigin(classNames.size()));
            DESCRIPTORS.add("L" + s + ";");
        }

        ClassNode loader = getLoaderClass();
        ClassWriter w = new ClassWriter(0);
//        w.newUTF8(Main.TRASH);
        loader.accept(w);

        this.getResources().put(loader.name + ".class", w.toByteArray());

        Random random = new Random();

        this.getClassWrappers().stream()
                .filter(wrapper -> !this.excluded(wrapper))
                .filter(wrapper -> !wrapper.isInterface() && !wrapper.isAnnotation() && !wrapper.isEnum() && !wrapper.isLibraryNode() && !wrapper.isSynthetic() && !wrapper.isAbstract())
                .filter(wrapper -> random.nextBoolean())
                .forEach(wrapper -> {
                    ClassNode classNode = wrapper.classNode;

                    this.clearSequencedString();

                    for (int i = 0; i < RandomUtils.getRandomInt(4); i++) {
                        MethodNode methodNode = methodGen(classNode, loader, true);
                        classNode.methods.add(methodNode);
                    }

//                    classNode.methods.add(new MethodNode(ASM4, ACC_PUBLIC | ACC_STATIC | ACC_NATIVE, "$kntaLoader", "()V", null, null));

                    MethodNode staticBlock = null;
                    for (MethodNode method : classNode.methods) {
                        if (method.name.equals("<clinit>")) {
                            staticBlock = method;
                            break;
                        }
                    }

                    InsnList insn = new InsnList();

                    insn.add(new MethodInsnNode(INVOKESTATIC, loader.name, "init", "()V"));
//                    insn.add(new MethodInsnNode(INVOKESTATIC, classNode.name, "$kntaLoader", "()V"));
                    insn.add(new InsnNode(RETURN));

                    if (staticBlock == null) {
                        MethodNode mn = new MethodNode(ASM4, ACC_STATIC, "<clinit>", "()V", null, null);
                        mn.instructions = insn;
                        classNode.methods.add(mn);
                    } else {
                        staticBlock.instructions.insert(insn);
                        classNode.methods.remove(staticBlock);
                        classNode.methods.add(staticBlock); // Re-add to ensure it is at the end
                    }

                });

        for (int i = 0; i < this.obfuscator.sessionInfo.getTrashClasses(); i++) {
            ClassNode classNode = generateClass(loader);
            ClassWriter cw = new ClassWriter(0);
//            cw.newUTF8(Main.TRASH);
            classNode.accept(cw);

            this.getResources().put(classNode.name + ".class", cw.toByteArray());
        }

        this.getResources().put("obfuscated/by/IzumiKonata/KNTASHIELD64.dll", compress(IOUtils.toByteArray(TrashClasses.class.getResourceAsStream("/克清.png"))));

        Logger.stdOut(String.format("Generated %d trash classes.", this.obfuscator.sessionInfo.getTrashClasses()));
    }

    @SneakyThrows
    public static byte[] compress(byte[] byArray) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream def = new DeflaterOutputStream(byteArrayOutputStream, new Deflater(Deflater.BEST_COMPRESSION));
        def.write(byArray);
        def.flush();
        def.close();
        return byteArrayOutputStream.toByteArray();
    }

    private ClassNode generateClass(ClassNode node) {
        ClassNode classNode = createClass(node, StringUtils.randomClassNameFromBase(this.getClasses().keySet()));
        int methodsToGenerate = RandomUtils.getRandomInt(12) + 2;
        int fieldsToGenerate = RandomUtils.getRandomInt(8) + 2;

        classNode.fields = new ArrayList<>();

        this.clearSequencedString();

        for (int i = 0; i < fieldsToGenerate; i++) {
            FieldNode fieldNode = fieldGen(node);
            classNode.fields.add(fieldNode);
        }

        this.clearSequencedString();

        for (int i = 0; i < methodsToGenerate; i++) {
            MethodNode methodNode = methodGen(classNode, node, false);
            classNode.methods.add(methodNode);
        }

        classNode.methods.add(new MethodNode(ASM4, ACC_PUBLIC | ACC_STATIC | ACC_NATIVE, "$kntaLoader", "()V", null, null));

        MethodNode mn = new MethodNode(ASM4, ACC_STATIC, "<clinit>", "()V", null, null);
        InsnList insn = new InsnList();

        insn.add(new MethodInsnNode(INVOKESTATIC, node.name, "init", "()V"));
        insn.add(new MethodInsnNode(INVOKESTATIC, classNode.name, "$kntaLoader", "()V"));
        insn.add(new InsnNode(RETURN));

        mn.instructions = insn;
        classNode.methods.add(mn);

        return classNode;
    }

    private ClassNode getLoaderClass() {

//        ClassNode classNode = createClass("obfuscated/by/IzumiKonata/NativeLoader");
//
//        classNode.fields = new ArrayList<>();
//
//        classNode.fields.add(new FieldNode(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, byteArrayName, "[B", null, null));
//
//        classNode.methods.add(this.genFakeExtractMethod(classNode));
//        classNode.methods.add(this.genClinit(classNode));
//        classNode.methods.add(this.genDecompress(classNode));
//        classNode.methods.add(this.genNativeMethod("defineClass", "Ljava/lang/String;", "name"));
//        classNode.methods.add(this.genNativeMethod("defineClass0", "Ljava/lang/String;[B", "name", "rawClassData"));

        return this.getClassNode(KonataShieldTrashClasses.class);
    }

    @SneakyThrows
    public byte[] getClassBytes(Class<?> clazz) {
        String className = clazz.getName().replace('.', '/') + ".class";

        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(className);
        if (inputStream == null) {
            throw new IllegalArgumentException("Class not found: " + clazz.getName());
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    public ClassNode getClassNode(Class<?> clazz) {
        byte[] classBytes = getClassBytes(clazz);
        ClassReader classReader = new ClassReader(classBytes);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    private ClassNode createClass(ClassNode fakeJNINode, String className) {
        ClassNode classNode = new ClassNode();
        classNode.visit(V1_8, ACC_SUPER + ACC_PUBLIC, className, null, "java/lang/Object", null);

        MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        classNode.visitEnd();

        return classNode;
    }

    private int randomModifier() {
        return new Random().nextBoolean() ? ACC_PRIVATE : ACC_PUBLIC;
    }

    List<String> validClassName = null;

    private String getRandomDesc(boolean noVoid) {
        String s = DESCRIPTORS.get(RandomUtils.getRandomIntNoOrigin(DESCRIPTORS.size()));

        while (noVoid && s.equals("V"))
            s = DESCRIPTORS.get(RandomUtils.getRandomIntNoOrigin(DESCRIPTORS.size()));

        return s;
    }

    private FieldNode fieldGen(ClassNode node) {
        FieldNode field = new FieldNode(this.randomModifier(), nextSequencedString(), this.getRandomDesc(true), null, null);

        if (new Random().nextBoolean()) {
            field.access += ACC_STATIC;
        }

        if (new Random().nextBoolean()) {
            field.access += ACC_FINAL;
        }

        return field;
    }

    private MethodNode methodGen(ClassNode node, ClassNode junkJNI, boolean nativeOnly) {

        Random random = new Random();
        String returnType = !random.nextBoolean() ? "V" : this.getRandomDesc(true);

        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < RandomUtils.getRandomInt(5); i++) {
            parameters.append(this.getRandomDesc(true));
        }

        MethodNode method = new MethodNode(ASM4,ACC_PUBLIC + ACC_STATIC, nextSequencedString(), "(" + parameters + ")" + returnType, null, null);

        boolean isNative = false;

        if (random.nextBoolean() || nativeOnly) {
            method.access += ACC_NATIVE;
            isNative = true;
        }

        if (random.nextBoolean()) {
            method.access -= ACC_STATIC;
        }

        method.access += ACC_SYNTHETIC;

        if (!isNative) {
            InsnList insns = new InsnList();

            int randomInt = RandomUtils.getRandomInt(0, 7);

            if (randomInt <= 1) {
                insns.add(new LdcInsnNode(Base64.getEncoder().encodeToString(getRandomDesc(false).getBytes(StandardCharsets.UTF_8))));
                insns.add(new MethodInsnNode(INVOKESTATIC, junkJNI.name, "a", "(Ljava/lang/String;)V"));
            }

            if (randomInt == 2) {
                insns.add(new LdcInsnNode("Exception: native [" + method.name + "]"));
                insns.add(new InsnNode(ATHROW));
            }

            if (randomInt >= 3) {
                List<FieldNode> staticField = new ArrayList<>();
                List<FieldNode> nonStaticField = new ArrayList<>();
                List<FieldNode> finalField = new ArrayList<>();
                List<FieldNode> nonFinalField = new ArrayList<>();
                for (FieldNode field : node.fields) {
                    if ((field.access & ACC_FINAL) == 0) {
                        nonFinalField.add(field);
                    } else {
                        finalField.add(field);
                    }

                    if ((field.access & ACC_STATIC) == 0) {
                        nonStaticField.add(field);
                    } else {
                         staticField.add(field);
                    }
                }

                if ((method.access & ACC_STATIC) == 0) {
                    for (FieldNode field : nonStaticField) {
                        if ((field.access & ACC_FINAL) != 0) {
                            continue;
                        }

                        List<FieldNode> choices = new ArrayList<>(node.fields);
                        choices.remove(field);

                        insns.add(new VarInsnNode(ALOAD, 0));
                        FieldNode choose = choices.get(RandomUtils.getRandomIntNoOrigin(choices.size()));

                        if ((choose.access & ACC_STATIC) == 0) {
                            insns.add(new VarInsnNode(ALOAD, 0));
                            insns.add(new FieldInsnNode(GETFIELD, node.name, choose.name, choose.desc));
                            insns.add(new TypeInsnNode(CHECKCAST, field.desc.substring(1).replace(";", "")));
                            insns.add(new FieldInsnNode(PUTFIELD, node.name, field.name, field.desc));
                        } else {
                            insns.add(new FieldInsnNode(GETSTATIC, node.name, choose.name, choose.desc));
                            insns.add(new TypeInsnNode(CHECKCAST, field.desc.substring(1).replace(";", "")));
                            insns.add(new FieldInsnNode(PUTFIELD, node.name, field.name, field.desc));
                        }
                    }

                    for (FieldNode field : staticField) {
                        if ((field.access & ACC_FINAL) != 0) {
                            continue;
                        }

                        List<FieldNode> choices = new ArrayList<>(node.fields);
                        choices.remove(field);

                        if (choices.isEmpty())
                            break;

//                        insns.add(new VarInsnNode(ALOAD, 0));
                        FieldNode choose = choices.get(RandomUtils.getRandomIntNoOrigin(choices.size()));

                        if ((choose.access & ACC_STATIC) == 0) {
                            insns.add(new VarInsnNode(ALOAD, 0));
                            insns.add(new FieldInsnNode(GETFIELD, node.name, choose.name, choose.desc));
                            insns.add(new TypeInsnNode(CHECKCAST, field.desc.substring(1).replace(";", "")));
                            insns.add(new FieldInsnNode(PUTSTATIC, node.name, field.name, field.desc));
                        } else {
                            insns.add(new FieldInsnNode(GETSTATIC, node.name, choose.name, choose.desc));
                            insns.add(new TypeInsnNode(CHECKCAST, field.desc.substring(1).replace(";", "")));
                            insns.add(new FieldInsnNode(PUTSTATIC, node.name, field.name, field.desc));
                        }
                    }
                } else {
                    for (FieldNode field : staticField) {
                        if ((field.access & ACC_FINAL) != 0) {
                            continue;
                        }

                        List<FieldNode> choices = new ArrayList<>(staticField);
                        choices.remove(field);

                        if (choices.isEmpty())
                            break;

//                        insns.add(new VarInsnNode(ALOAD, 0));
                        FieldNode choose = choices.get(RandomUtils.getRandomIntNoOrigin(choices.size()));

                        insns.add(new FieldInsnNode(GETSTATIC, node.name, choose.name, choose.desc));
                        insns.add(new TypeInsnNode(CHECKCAST, field.desc.substring(1).replace(";", "")));
                        insns.add(new FieldInsnNode(PUTSTATIC, node.name, field.name, field.desc));
                    }
                }
            }

            insns.add(new InsnNode(RETURN));

            method.instructions = insns;
        }

        return method;
    }


    @Override
    protected ExclusionType getExclusionType() {
        return null;
    }

    @Override
    public String getName() {
        return "Trash classes";
    }
}
