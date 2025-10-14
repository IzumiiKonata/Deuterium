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

package tech.konata.obfuscator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;

import lombok.SneakyThrows;
import lombok.var;
import org.objectweb.asm.tree.AnnotationNode;
import tech.konata.obfuscator.asm.ClassTree;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.exceptions.MissingClassException;
import tech.konata.obfuscator.exceptions.ObfuscatorException;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.miscellaneous.TrashClasses;
import tech.konata.obfuscator.transformers.obfuscators.miscellaneous.CRCFucker;
import tech.konata.obfuscator.transformers.obfuscators.miscellaneous.ClassFolder;
import tech.konata.obfuscator.transformers.obfuscators.miscellaneous.TimeManipulator;
import tech.konata.obfuscator.utils.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This class is how Obfuscator processes the provided {@link SessionInfo} to produce an obfuscated jar.
 *
 * @author ItzSomebody
 */
public class Obfuscator {
    public SessionInfo sessionInfo;
    private Map<String, ClassTree> hierarchy = new ConcurrentHashMap<>();
    public Map<String, ClassWrapper> classes = new ConcurrentHashMap<>();
    public Map<String, ClassWrapper> classPath = new ConcurrentHashMap<>();
    public Map<String, byte[]> resources = new ConcurrentHashMap<>();

    public Obfuscator(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    /**
     * Execution order. Feel free to modify.
     */
    public void run() {
        loadClassPath();
        loadInput();
//        buildInheritance();

        if (this.sessionInfo.getTrashClasses() > 0)
            this.sessionInfo.getTransformers().add(new TrashClasses());
        if (this.sessionInfo.getTransformers().isEmpty())
            throw new ObfuscatorException("No transformers are enabled.");
        Logger.stdOut("------------------------------------------------");
        this.sessionInfo.getTransformers().stream().filter(Objects::nonNull).forEach(transformer -> {
            long current = System.currentTimeMillis();
            Logger.stdOut(String.format("Running %s transformer.", transformer.getName()));
            transformer.init(this);
            transformer.transform();
            Logger.stdOut(String.format("Finished running %s transformer. [%dms]", transformer.getName(), (System.currentTimeMillis() - current)));
            Logger.stdOut("------------------------------------------------");
        });

        this.classes.values().forEach(cw -> {

            this.cleanUpAnnotations(cw.classNode.visibleAnnotations);

            cw.methods.forEach(mn -> {
                this.cleanUpAnnotations(mn.methodNode.visibleAnnotations);
            });

        });

        writeOutput();
    }

    protected void cleanUpAnnotations(List<AnnotationNode> nodes) {
        if (nodes != null)
            nodes.removeIf(annotationNode -> annotationNode.desc.startsWith("Ltech/konata/obfuscation"));
    }

    @SneakyThrows
    private void fuckCRCHash(ZipOutputStream zos) {
        Field crc = ZipOutputStream.class.getDeclaredField("crc");
        crc.setAccessible(true);
        CRC32 crc32 = (CRC32) crc.get(zos);

//        System.out.println("Val: " + crc32.getValue());
        crc32.update(ThreadLocalRandom.current().nextInt(0xFFFF));
//        System.out.println("Modified: " + crc32.getValue());
    }

    private void manipulateEntryTime(ZipEntry entry) {

        ZonedDateTime zdt = ZonedDateTime.of(1989, 6, 5, 0, 0, 0, 0, ZoneId.systemDefault());

        entry.setTime(Instant.from(zdt).toEpochMilli());
        entry.setCreationTime(FileTime.from(Instant.from(zdt)));
        entry.setLastAccessTime(FileTime.from(Instant.from(zdt)));
        entry.setLastModifiedTime(FileTime.from(Instant.from(zdt)));

    }

    private void writeOutput() {
        File output = this.sessionInfo.getOutput();
        Logger.stdOut(String.format("Writing output to \"%s\".", output.getAbsolutePath()));
        if (output.exists())
            Logger.stdOut(String.format("Output file already exists, renamed to %s.", IOUtils.renameExistingFile(output)));

        boolean bClassFolder = this.sessionInfo.getTransformers().stream().anyMatch(t -> t instanceof ClassFolder);
        boolean bCRCFucker = this.sessionInfo.getTransformers().stream().anyMatch(t -> t instanceof CRCFucker);
        boolean bTimeManipulator = this.sessionInfo.getTransformers().stream().anyMatch(t -> t instanceof TimeManipulator);
        AtomicInteger cfCount = new AtomicInteger();

        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));

            if (bClassFolder) {
                // lmao
                {
                    ZipEntry entry = new ZipEntry("catch_me_if_u_can/0000诶我去，怎么全是文件夹啊？！.class/");

                    if (bTimeManipulator)
                        this.manipulateEntryTime(entry);

                    zos.putNextEntry(entry);
                    if (bCRCFucker)
                        this.fuckCRCHash(zos);
                    zos.closeEntry();
                }
            }

            this.classes.values().forEach(classWrapper -> {

                if (classWrapper.classNode.name.startsWith("tech/konata/obfuscation/")) {
                    return;
                }

                String name = classWrapper.classNode.name + ".class";

                // turn classes into folders
                if (bClassFolder && !this.sessionInfo.getExclusionManager().isExcluded(classWrapper.originalName, ExclusionType.CLASS_FOLDER)) {

                    name += "/";

                    cfCount.getAndIncrement();
                }

                try {

                    ZipEntry entry = new ZipEntry(name);
                    entry.setCompressedSize(-1);

                    if (bCRCFucker) {
                        entry.setCrc(ThreadLocalRandom.current().nextLong(0xFFFFFFFFL));
                    }

                    if (bTimeManipulator)
                        this.manipulateEntryTime(entry);

                    ClassWriter cw = new CustomClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
//                    cw.newUTF8(Main.TRASH);
                    try {
                        classWrapper.classNode.accept(cw);
                    } catch (Throwable t) {
                        Logger.stdErr(String.format("Error writing class %s. Skipping frames.", name));
                        t.printStackTrace();
                        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//                        cw.newUTF8(Main.TRASH);
                        classWrapper.classNode.accept(cw);
                    }

                    zos.putNextEntry(entry);
                    zos.write(cw.toByteArray());

                    if (bCRCFucker)
                        this.fuckCRCHash(zos);

                    zos.closeEntry();
                } catch (Throwable t) {
                    Logger.stdErr(String.format("Error writing class %s. Skipping.", name));
                    t.printStackTrace();
                }
            });

            this.resources.forEach((name, bytes) -> {
                try {
                    ZipEntry entry = new ZipEntry(name);
                    entry.setCompressedSize(-1);

                    if (bTimeManipulator)
                        this.manipulateEntryTime(entry);

                    zos.putNextEntry(entry);
                    zos.write(bytes);

                    if (bCRCFucker)
                        this.fuckCRCHash(zos);

                    zos.closeEntry();
                } catch (IOException ioe) {
                    Logger.stdErr(String.format("Error writing resource %s. Skipping.", name));
                    ioe.printStackTrace();
                }
            });

            zos.setComment(Main.PROPAGANDA_GARBAGE);
            zos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ObfuscatorException();
        }

        if (bClassFolder) {
            Logger.stdOut(String.format("Turned %s classes into folder.", cfCount.get()));
        }

    }

    private void loadClassPath() {
        for (File file : this.sessionInfo.getLibraries()) {
            if (file.exists()) {
                Logger.stdOut(String.format("Loading library \"%s\".", file.getAbsolutePath()));
                try {
                    ZipFile zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            try {
                                ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                                ClassNode classNode = new ClassNode();
                                cr.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                                ClassWrapper classWrapper = new ClassWrapper(classNode, true);

                                this.classPath.put(classWrapper.originalName, classWrapper);
                            } catch (Throwable t) {
                                // Don't care.
                            }
                        }
                    }
                } catch (ZipException e) {
                    Logger.stdErr(String.format("Library \"%s\" could not be opened as a zip file.", file.getAbsolutePath()));
                    e.printStackTrace();
                } catch (IOException e) {
                    Logger.stdErr(String.format("IOException happened while trying to load classes from \"%s\".", file.getAbsolutePath()));
                    e.printStackTrace();
                }
            } else {
                Logger.stdWarn(String.format("Library \"%s\" could not be found and will be ignored.", file.getAbsolutePath()));
            }
        }
    }

    private void loadInput() {
        File input = this.sessionInfo.getInput();
        if (input.exists()) {
            Logger.stdOut(String.format("Loading input \"%s\".", input.getAbsolutePath()));
            try {
                ZipFile zipFile = new ZipFile(input);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        if (entry.getName().endsWith(".class")) {
                            try {
                                ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                                ClassNode classNode = new ClassNode();
                                cr.accept(classNode, ClassReader.EXPAND_FRAMES);
                                if (classNode.version <= Opcodes.V1_5) {
                                    for (int i = 0; i < classNode.methods.size(); i++) {
                                        MethodNode methodNode = classNode.methods.get(i);
                                        JSRInlinerAdapter adapter = new JSRInlinerAdapter(methodNode, methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, methodNode.exceptions.toArray(new String[0]));
                                        methodNode.accept(adapter);
                                        classNode.methods.set(i, adapter);
                                    }
                                }
                                ClassWrapper classWrapper = new ClassWrapper(classNode, false);

                                this.classPath.put(classWrapper.originalName, classWrapper);
                                this.classes.put(classWrapper.originalName, classWrapper);
                            } catch (Throwable t) {
                                Logger.stdWarn(String.format("Could not load %s as a class.", entry.getName()));
                                this.resources.put(entry.getName(), IOUtils.toByteArray(zipFile.getInputStream(entry)));
                            }
                        } else {
                            this.resources.put(entry.getName(), IOUtils.toByteArray(zipFile.getInputStream(entry)));
                        }
                    }
                }
            } catch (ZipException e) {
                Logger.stdErr(String.format("Input file \"%s\" could not be opened as a zip file.", input.getAbsolutePath()));
                e.printStackTrace();
                throw new ObfuscatorException(e);
            } catch (IOException e) {
                Logger.stdErr(String.format("IOException happened while trying to load classes from \"%s\".", input.getAbsolutePath()));
                e.printStackTrace();
                throw new ObfuscatorException(e);
            }
        } else {
            Logger.stdErr(String.format("Unable to find \"%s\".", input.getAbsolutePath()));
            throw new ObfuscatorException();
        }
    }

    public ClassTree getTree(String ref) {
        if (!hierarchy.containsKey(ref)) {
            ClassWrapper wrapper = classPath.get(ref);
            buildHierarchy(wrapper, null);
        }

        return hierarchy.get(ref);
    }

    /**
     * Finds {@link ClassWrapper} with given name.
     *
     * @return {@link ClassWrapper}.
     */
    public ClassWrapper getClassWrapper(String ref) {
        if (!classPath.containsKey(ref))
            throw new RuntimeException("Could not find " + ref);

        return classPath.get(ref);
    }

    private void buildHierarchy(ClassWrapper wrapper, ClassWrapper sub) {
        if (hierarchy.get(wrapper.getName()) == null) {
            ClassTree tree = new ClassTree(wrapper);

            if (wrapper.getSuperName() != null) {
                tree.getParentClasses().add(wrapper.getSuperName());

                buildHierarchy(getClassWrapper(wrapper.getSuperName()), wrapper);
            }
            if (wrapper.getInterfaces() != null)
                wrapper.getInterfaces().forEach(s -> {
                    tree.getParentClasses().add(s);

                    buildHierarchy(getClassWrapper(s), wrapper);
                });

            hierarchy.put(wrapper.getName(), tree);
        }

        if (sub != null)
            hierarchy.get(wrapper.getName()).getSubClasses().add(sub.getName());
    }

    public void buildInheritance() {
        classes.values().forEach(classWrapper -> buildHierarchy(classWrapper, null));
    }

    public ClassWrapper getClasspathWrapper(String name) {
        var wrapper = classPath.get(name);

        if (wrapper == null) {
            throw MissingClassException.forLibraryClass(name);
        }

        return wrapper;
    }

    private void buildHierarchy(ClassWrapper wrapper, ClassWrapper sub, Set<String> visited) {
        if (visited.add(wrapper.getName())) {
            if (wrapper.getSuperName() != null) {
                var superParent = getClasspathWrapper(wrapper.getSuperName());
                wrapper.getParents().add(superParent);
                buildHierarchy(superParent, wrapper, visited);
            }
            if (wrapper.getInterfaceNames() != null) {
                wrapper.getInterfaceNames().forEach(interfaceName -> {
                    var interfaceParent = getClasspathWrapper(interfaceName);
                    wrapper.getParents().add(interfaceParent);
                    buildHierarchy(interfaceParent, wrapper, visited);

                });
            }
        }
        if (sub != null) {
            wrapper.getChildren().add(sub);
        }

        if (wrapper.getSuperName() != null) {
            var superParent = getClasspathWrapper(wrapper.getSuperName());

            for (ClassWrapper child : wrapper.getChildren()) {
                if (!superParent.getChildren().contains(child)) {
                    superParent.getChildren().add(child);
                }
            }
        }

        if (wrapper.getInterfaceNames() != null) {
            wrapper.getInterfaceNames().forEach(interfaceName -> {
                var interfaceParent = getClasspathWrapper(interfaceName);

                for (ClassWrapper child : wrapper.getChildren()) {
                    if (!interfaceParent.getChildren().contains(child)) {
                        interfaceParent.getChildren().add(child);
                    }
                }
            });
        }

//        if (wrapper.getOriginalName().endsWith("/SharedRenderingConstants")) {
//            System.out.println("Parents: ");
//
//            for (ClassWrapper parent : wrapper.getParents()) {
//                System.out.println("    " + parent.getName());
//            }
//
//            System.out.println("Children: ");
//
//            for (ClassWrapper child : wrapper.getChildren()) {
//                System.out.println("    " + child.getName());
//            }
//        }
    }

    public void buildHierarchyGraph() {
        HashSet<String> visited = new HashSet<>();
        classes.values().forEach(wrapper -> buildHierarchy(wrapper, null, visited));
    }

    class CustomClassWriter extends ClassWriter {
        private CustomClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(final String type1, final String type2) {
            if ("java/lang/Object".equals(type1) || "java/lang/Object".equals(type2))
                return "java/lang/Object";

            String first = deriveCommonSuperName(type1, type2);
            String second = deriveCommonSuperName(type2, type1);
            if (!"java/lang/Object".equals(first)) {
                return first;
            }
            if (!"java/lang/Object".equals(second)) {
                return second;
            }

            return getCommonSuperClass(getClasspathWrapper(type1).getSuperName(), getClasspathWrapper(type2).getSuperName());
        }

        private String deriveCommonSuperName(final String type1, final String type2) {
            ClassWrapper first = getClasspathWrapper(type1);
            ClassWrapper second = getClasspathWrapper(type2);
            if (isAssignableFrom(type1, type2)) {
                return type1;
            } else if (isAssignableFrom(type2, type1)) {
                return type2;
            } else if (first.isInterface() || second.isInterface()) {
                return "java/lang/Object";
            } else {
                String temp;

                do {
                    temp = first.getSuperName();
                    first = getClasspathWrapper(temp);
                } while (!isAssignableFrom(temp, type2));
                return temp;
            }
        }

        private boolean isAssignableFrom(String type1, String type2) {
            if ("java/lang/Object".equals(type1)) {
                return true;
            }
            if (type1.equals(type2)) {
                return true;
            }

            ClassWrapper first = getClasspathWrapper(type1);
            getClasspathWrapper(type2); // Ensure type2 was loaded at some point

            var allChildren = new HashSet<String>();
            var toProcess = new ArrayDeque<String>();
            first.getChildren().forEach(child -> {
                toProcess.add(child.getName());
            });

            while (!toProcess.isEmpty()) {
                String next = toProcess.poll();

                if (allChildren.add(next)) {
                    ClassWrapper temp = getClasspathWrapper(next);
                    temp.getChildren().forEach(child -> {
                        toProcess.add(child.getName());
                    });
                }
            }
            return allChildren.contains(type2);
        }
    }
}
