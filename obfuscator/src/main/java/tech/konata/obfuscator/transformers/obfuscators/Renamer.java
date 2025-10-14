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

package tech.konata.obfuscator.transformers.obfuscators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.asm.ClassTree;
import tech.konata.obfuscator.asm.FieldWrapper;
import tech.konata.obfuscator.asm.MemberRemapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.utils.IOUtils;
import tech.konata.obfuscator.utils.SequencedNameGenerator;


/**
 * Transformer which renames classes and their members.
 *
 * @author ItzSomebody
 */
public class Renamer extends Transformer {
    private List<String> adaptTheseResources = new ArrayList<>();
    private boolean dumpMappings;
    private String repackageName;
    private Map<String, String> mappings;

    private static boolean methodCanBeRenamed(MethodWrapper wrapper) {
        return !wrapper.getAccess().isNative() && !"main".equals(wrapper.getOriginalName())
                && !"premain".equals(wrapper.getOriginalName()) && !wrapper.getOriginalName().startsWith("<");
    }

    /**
     * 检查方法是否在排除列表中
     */
    protected boolean excluded(MethodWrapper wrapper) {
        // 构建方法的标识符，格式为：类名.方法名.方法描述符
        String methodId = wrapper.getOwner().getOriginalName() + "." + 
                         wrapper.getOriginalName() + wrapper.getOriginalDescriptor();
        
        // 检查方法是否在排除列表中
        return excluded(methodId);
    }

    @Override
    public void transform() {
        obfuscator.buildInheritance();
        mappings = new HashMap<>();
        Map<String, String> packageMappings = new HashMap<>();

        SequencedNameGenerator classDictionary = new SequencedNameGenerator();
        SequencedNameGenerator fieldDictionary = new SequencedNameGenerator();
        SequencedNameGenerator methodDictionary = new SequencedNameGenerator();

        Logger.stdOut("Generating mappings.");
        long current = System.currentTimeMillis();

        getClassWrappers().forEach(classWrapper -> {
            classWrapper.getMethods().stream().filter(Renamer::methodCanBeRenamed).forEach(methodWrapper -> {
                HashSet<String> visited = new HashSet<>();

                if (!cannotRenameMethod(obfuscator.getTree(classWrapper.getOriginalName()), methodWrapper, visited))
                    genMethodMappings(methodWrapper, methodWrapper.getOwner().getOriginalName(), methodDictionary.nextSequencedString());
            });

            classWrapper.getFields().forEach(fieldWrapper -> {
                HashSet<String> visited = new HashSet<>();

                if (!cannotRenameField(obfuscator.getTree(classWrapper.getOriginalName()), fieldWrapper, visited))
                    genFieldMappings(fieldWrapper, fieldWrapper.getOwner().getOriginalName(), fieldDictionary.nextSequencedString());
            });

            if (!excluded(classWrapper)) {
                String newName;

                if (getRepackageName() == null) {
                    throw new IllegalArgumentException("No repackage name!");
//                    String mappedPackageName = randomString();

//                    packageMappings.putIfAbsent(classWrapper.getPackageName(), mappedPackageName);
//                    newName = packageMappings.get(classWrapper.getPackageName());
                } else
                    newName = getRepackageName();

                if (!newName.isEmpty())
                    newName += '/' + classDictionary.nextSequencedString();
                else
                    newName = classDictionary.nextSequencedString();

                mappings.put(classWrapper.getOriginalName(), newName);
            }
        });

        Logger.stdOut(String.format("Finished generated mappings. [%dms]", tookThisLong(current)));
        Logger.stdOut("Applying mappings.");
        current = System.currentTimeMillis();

        // Apply mappings
        Remapper simpleRemapper = new MemberRemapper(mappings);
        new ArrayList<>(getClassWrappers()).forEach(classWrapper -> {
            ClassNode classNode = classWrapper.getClassNode();

            ClassNode copy = new ClassNode();
            classNode.accept(new ClassRemapper(copy, simpleRemapper));

            // In order to preserve the original names to prevent exclusions from breaking,
            // we update the MethodNode/FieldNode/ClassNode each wrapper wraps instead.
            IntStream.range(0, copy.methods.size())
                    .forEach(i -> classWrapper.getMethods().get(i).setMethodNode(copy.methods.get(i)));
            IntStream.range(0, copy.fields.size())
                    .forEach(i -> classWrapper.getFields().get(i).setFieldNode(copy.fields.get(i)));

            classWrapper.setClassNode(copy);

            getClasses().remove(classWrapper.getOriginalName());
            getClasses().put(classWrapper.getName(), classWrapper);
            getClassPath().put(classWrapper.getName(), classWrapper);
        });

        Logger.stdOut(String.format("Mapped %d members. [%dms]", mappings.size(), tookThisLong(current)));
        current = System.currentTimeMillis();

        // Now we gotta fix those resources because we probably screwed up random files.
        Logger.stdOut("Attempting to map class names in resources");
        AtomicInteger fixed = new AtomicInteger();
        getResources().forEach((name, byteArray) -> getAdaptTheseResources().forEach(s -> {
            Pattern pattern = Pattern.compile(s);

            if (pattern.matcher(name).matches()) {
                String stringVer = new String(byteArray, StandardCharsets.UTF_8);

                for (String mapping : mappings.keySet()) {
                    String original = mapping.replace("/", ".");
                    if (stringVer.contains(original)) {
                        // Regex that ensures that class names that match words in the manifest don't break the
                        // manifest.
                        // Example: name == Main
                        if ("META-INF/MANIFEST.MF".equals(name) // Manifest
                                || "plugin.yml".equals(name) // Spigot plugin
                                || "bungee.yml".equals(name)) // Bungeecord plugin
                            stringVer = stringVer.replaceAll("(?<=[: ])" + original,
                                    mappings.get(mapping).replace("/", "."));
                        else
                            stringVer = stringVer.replace(original, mappings.get(mapping).replace("/", "."));
                    }
                }

                getResources().put(name, stringVer.getBytes(StandardCharsets.UTF_8));
                fixed.incrementAndGet();
            }
        }));

        Logger.stdOut(String.format("Mapped %d names in resources. [%dms]", fixed.get(), tookThisLong(current)));

        if (isDumpMappings())
            dumpMappings();
    }

    private void genMethodMappings(MethodWrapper methodWrapper, String owner, String newName) {
        String key = owner + '.' + methodWrapper.getOriginalName() + methodWrapper.getOriginalDescriptor();

        // This (supposedly) will always stop the recursion because the tree was already renamed
        if (mappings.containsKey(key))
            return;

        ClassTree tree = obfuscator.getTree(owner);

        mappings.put(key, newName);

        if (!methodWrapper.getAccess().isStatic()) { // Static methods can't be overridden
            tree.getParentClasses().forEach(parentClass -> genMethodMappings(methodWrapper, parentClass, newName));
            tree.getSubClasses().forEach(subClass -> genMethodMappings(methodWrapper, subClass, newName));
        }
    }

    private boolean cannotRenameMethod(ClassTree tree, MethodWrapper wrapper, Set<String> visited) {
        String check = tree.getClassWrapper().getOriginalName() + '.' + wrapper.getOriginalName() + wrapper.getOriginalDescriptor();

        // Don't check these
        if (visited.contains(check))
            return false;

        visited.add(check);

        // If excluded, we don't want to rename.
        // If we already mapped the tree, we don't want to waste time doing it again.
        if (excluded(check) || mappings.containsKey(check))
            return true;

        // Methods which are static don't need to be checked for inheritance
        if (!wrapper.getAccess().isStatic()) {
            // We can't rename members which inherit methods from external libraries
            if (tree.getClassWrapper() != wrapper.getOwner() && tree.getClassWrapper().isLibraryNode()
                    && tree.getClassWrapper().getMethods().stream().anyMatch(mw -> mw.getOriginalName().equals(wrapper.getOriginalName())
                    && mw.getOriginalDescriptor().equals(wrapper.getOriginalDescriptor())))
                return true;

            // Check if any parent class or interface is a library node with this method
            boolean libraryInheritance = tree.getParentClasses().stream()
                .map(parent -> obfuscator.getTree(parent))
                .filter(Objects::nonNull)
                .anyMatch(parentTree -> 
                    parentTree.getClassWrapper().isLibraryNode() && 
                    parentTree.getClassWrapper().getMethods().stream().anyMatch(mw -> 
                        mw.getOriginalName().equals(wrapper.getOriginalName()) && 
                        mw.getOriginalDescriptor().equals(wrapper.getOriginalDescriptor())
                    )
                );
            
            if (libraryInheritance)
                return true;

            return tree.getParentClasses().stream().anyMatch(parent -> cannotRenameMethod(obfuscator.getTree(parent), wrapper, visited))
                    || (tree.getSubClasses().stream().anyMatch(sub -> cannotRenameMethod(obfuscator.getTree(sub), wrapper, visited)));
        } else {
            return tree.getClassWrapper().getAccess().isEnum()
                    && ("valueOf".equals(wrapper.getOriginalName()) || "values".equals(wrapper.getOriginalName()));
        }
    }

    private void genFieldMappings(FieldWrapper fieldWrapper, String owner, String newName) {
        // This (supposedly) will always stop the recursion because the tree was already renamed
        if (mappings.containsKey(owner + '.' + fieldWrapper.getOriginalName() + '.' + fieldWrapper.getOriginalType()))
            return;

        ClassTree tree = obfuscator.getTree(owner);

        mappings.put(owner + '.' + fieldWrapper.getOriginalName() + '.' + fieldWrapper.getOriginalType(), newName);

        if (!fieldWrapper.getAccess().isStatic()) { // Static fields can't be inherited
            tree.getParentClasses().forEach(parentClass -> genFieldMappings(fieldWrapper, parentClass, newName));
            tree.getSubClasses().forEach(subClass -> genFieldMappings(fieldWrapper, subClass, newName));
        }
    }

    private boolean cannotRenameField(ClassTree tree, FieldWrapper wrapper, Set<String> visited) {
        String check = tree.getClassWrapper().getOriginalName() + '.' + wrapper.getOriginalName() + '.' + wrapper.getOriginalType();

        // Don't check these
        if (visited.contains(check))
            return false;

        visited.add(check);

        // If excluded, we don't want to rename.
        // If we already mapped the tree, we don't want to waste time doing it again.
        if (excluded(check) || mappings.containsKey(check))
            return true;

        // Fields which are static don't need to be checked for inheritance
        if (!wrapper.getAccess().isStatic()) {
            // We can't rename members which inherit fields from external libraries
            if (tree.getClassWrapper() != wrapper.getOwner() && tree.getClassWrapper().isLibraryNode()
                    && tree.getClassWrapper().getFields().stream().anyMatch(fw -> fw.getOriginalName().equals(wrapper.getOriginalName())
                    && fw.getOriginalType().equals(wrapper.getOriginalType())))
                return true;

            // Check if any parent class or interface is a library node with this field
            boolean libraryInheritance = tree.getParentClasses().stream()
                .map(parent -> obfuscator.getTree(parent))
                .filter(Objects::nonNull)
                .anyMatch(parentTree -> 
                    parentTree.getClassWrapper().isLibraryNode() && 
                    parentTree.getClassWrapper().getFields().stream().anyMatch(fw -> 
                        fw.getOriginalName().equals(wrapper.getOriginalName()) && 
                        fw.getOriginalType().equals(wrapper.getOriginalType())
                    )
                );
            
            if (libraryInheritance)
                return true;

            return tree.getParentClasses().stream().anyMatch(parent -> cannotRenameField(obfuscator.getTree(parent), wrapper, visited))
                    || (tree.getSubClasses().stream().anyMatch(sub -> cannotRenameField(obfuscator.getTree(sub), wrapper, visited)));
        }

        return false;
    }

    private void dumpMappings() {
        long current = System.currentTimeMillis();
        Logger.stdOut("Dumping mappings.");
        File file = new File("mappings.txt");
        if (file.exists())
            IOUtils.renameExistingFile(file);

        try {
            file.createNewFile(); // TODO: handle this properly
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            mappings.forEach((oldName, newName) -> {
                try {
                    bw.append(oldName).append(" -> ").append(newName).append('\n');
                } catch (IOException ioe) {
                    Logger.stdErr(String.format("Ran into an error trying to append \"%s -> %s\"", oldName, newName));
                    ioe.printStackTrace();
                }
            });

            bw.close();
            Logger.stdOut(String.format("Finished dumping mappings at %s. [%dms]", file.getAbsolutePath(),
                    tookThisLong(current)));
        } catch (Throwable t) {
            Logger.stdErr("Ran into an error trying to create the mappings file.");
            t.printStackTrace();
        }
    }

    @Override
    public ExclusionType getExclusionType() {
        return ExclusionType.RENAMER;
    }

    @Override
    public String getName() {
        return "Renamer";
    }

    public List<String> getAdaptTheseResources() {
        return adaptTheseResources;
    }

    public void setAdaptTheseResources(List<String> adaptTheseResources) {
        this.adaptTheseResources = adaptTheseResources;
    }

    private boolean isDumpMappings() {
        return dumpMappings;
    }

    private void setDumpMappings(boolean dumpMappings) {
        this.dumpMappings = dumpMappings;
    }

    private String getRepackageName() {
        return repackageName;
    }

    public void setRepackageName(String repackageName) {
        this.repackageName = repackageName;
    }
}