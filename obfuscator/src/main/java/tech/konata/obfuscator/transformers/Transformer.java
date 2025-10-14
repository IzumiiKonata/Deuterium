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

package tech.konata.obfuscator.transformers;

import java.util.*;

import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.Obfuscator;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.FieldWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.exceptions.ObfuscatorException;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.utils.SequencedNameGenerator;
import tech.konata.obfuscator.utils.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.CodeSizeEvaluator;

/**
 * Abstract transformer for all the transformers. \o/
 *
 * @author ItzSomebody
 */
public abstract class Transformer implements Opcodes {
    protected Obfuscator obfuscator;

    private boolean skipAnnotationsCheck = false;

    public Transformer setSkipAnnotationsCheck() {
        this.skipAnnotationsCheck = true;
        return this;
    }

    public boolean isSkipAnnotationsCheck() {
        return skipAnnotationsCheck;
    }

    /**
     * Strings which have already been generated and used.
     */
    private HashSet<String> usedStrings = new HashSet<>();

    public void init(Obfuscator obfuscator) {
        this.obfuscator = obfuscator;
    }

    protected String getOverrideByAnnotation() {
        return "";
    }

    protected boolean hasAnnotation(List<AnnotationNode> nodes, String annotationFullName) {

        if (nodes == null)
            return false;

        for (AnnotationNode node : nodes) {

            if (node.desc.equals("L" + annotationFullName.replaceAll("\\.", "/") + ";")) {
                return true;
            }
        }

        return false;
    }

    protected <T> T getAnnotationValue(AnnotationNode node, String valName) {
        List<Object> values = node.values;

        if (values != null) {
            for (int j = 0; j < values.size(); j += 2) {

                String attrName = (String) values.get(j);

                if (valName.equals(attrName)) {

                    Object val = values.get(j + 1);

                    return (T) val;
                }

            }

        }

        return null;
    }

    protected <T> T getAnnotationValue(List<AnnotationNode> nodes, String annotationFullName, String valName) {

        if (nodes == null)
            return null;

        for (AnnotationNode node : nodes) {

            if (node.desc.equals("L" + annotationFullName.replaceAll("\\.", "/") + ";")) {

                List<Object> values = node.values;

                if (values != null) {

                    for (int j = 0; j < values.size(); j += 2) {

                        String attrName = (String) values.get(j);

                        if (valName.equals(attrName)) {

                            Object val = values.get(j + 1);

                            return (T) val;
                        }

                    }

                }

            }

        }

        return null;
    }

    protected String getEnumAnnotationValue(List<AnnotationNode> nodes, String annotationFullName, String valName) {

        if (nodes == null)
            return null;

        for (AnnotationNode node : nodes) {

            if (node.desc.equals("L" + annotationFullName.replaceAll("\\.", "/") + ";")) {

                List<Object> values = node.values;

                if (values != null) {

                    for (int j = 0; j < values.size(); j += 2) {

                        String attrName = (String) values.get(j);

                        if (valName.equals(attrName)) {

                            String[] val = (String[]) values.get(j + 1);

                            return val[1];

                        }

                    }

                }

            }

        }

        return null;
    }

    protected boolean excluded(String str) {
        return this.obfuscator.sessionInfo.getExclusionManager().isExcluded(str, getExclusionType());
    }

    protected boolean excluded(ClassWrapper classWrapper) {

        if (classWrapper.getName().startsWith("tech/konata/obfuscation"))
            return true;

        if (classWrapper.hasVisibleAnnotations()) {
            boolean b = this.hasAnnotation(classWrapper.classNode.visibleAnnotations, "tech.konata.obfuscation.ExcludeThis");
//                if (b) {
//                    System.out.println("Excluded: " + classWrapper.originalName);
//                }
            return b;
        }

        return this.excluded(classWrapper.originalName);
    }

    protected boolean excluded(MethodWrapper methodWrapper) {
        return this.excluded(methodWrapper.owner.originalName + '.' + methodWrapper.originalName
                + methodWrapper.originalDescription);
    }

    protected boolean excluded(FieldWrapper fieldWrapper) {
        return this.excluded(fieldWrapper.owner.originalName + '.' + fieldWrapper.originalName + '.'
                + fieldWrapper.originalDescription);
    }

    /**
     * Returns the remaining leeway of a method's allowed size.
     *
     * @param methodNode the {@link MethodNode} to check.
     * @return the remaining leeway of a method's allowed size.
     */
    protected int getSizeLeeway(MethodNode methodNode) {
        CodeSizeEvaluator cse = new CodeSizeEvaluator(null);
        methodNode.accept(cse);
        // Max allowed method size is 65534
        // https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-4.html#jvms-4.7.3
        return (65534 - cse.getMaxSize());
    }

    protected boolean hasInstructions(MethodNode methodNode) {
        return methodNode.instructions != null && methodNode.instructions.size() > 0;
    }

    protected long tookThisLong(long from) {
        return System.currentTimeMillis() - from;
    }

    protected String randomString(int length) {
        String str;
        do {
            str = getRandomString(length);
        } while (usedStrings.contains(str));

        usedStrings.add(str);
        return str;
    }

    protected String randomSigmaStyleString(int length) {
        String str;
        do {
            str = StringUtils.randomAlphaString(length);
        } while (usedStrings.contains(str));

        usedStrings.add(str);
        return str;
    }

    final String SIGMA = "강남스타챔피언알랑몰라왜화끈야하건끔리있잖사람으로씀드자용패똘끼멋쟁너듣픈난데불꺼진내주위는온통눈속에비친그만힐끗대고말해줘요다시숨기싶어널믿은가정보여줄게겨왔던또꿈길을걷뜨면나솔직히지금이편상큼짜릿한걸까매일서연습했죠포할봐원림없더않아밤번못들척회를";

    private String getRandomSigmaString(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(SIGMA.charAt(Math.abs(new Random().nextInt() % SIGMA.length())));
        }

        return sb.toString();
    }

    private String getRandomString(int length) {
        switch (obfuscator.sessionInfo.getDictionaryType()) {
            case SPACES:
                return StringUtils.randomSpacesString(length);
            case UNRECOGNIZED:
                return StringUtils.randomUnrecognizedString(length);
            case ALPHABETICAL:
                return StringUtils.randomAlphaString(length);
            case ALPHANUMERIC:
                return StringUtils.randomAlphaNumericString(length);
            default: {
                throw new ObfuscatorException("Illegal dictionary type: " + obfuscator.sessionInfo.getDictionaryType());
            }
        }
    }

    SequencedNameGenerator sng = new SequencedNameGenerator();

    protected void clearSequencedString() {
        sng.clear();
    }

    protected String nextSequencedString() {
        return sng.nextSequencedString();
    }

    protected Map<String, ClassWrapper> getClasses() {
        return this.obfuscator.classes;
    }

    protected Collection<ClassWrapper> getClassWrappers() {
        return this.obfuscator.classes.values();
    }

    protected List<ClassWrapper> getClassWrappersAsList() {
        return new ArrayList<>(this.obfuscator.classes.values());
    }

    protected Map<String, ClassWrapper> getClassPath() {
        return this.obfuscator.classPath;
    }

    protected Map<String, byte[]> getResources() {
        return this.obfuscator.resources;
    }

    public abstract void transform();

    public abstract String getName();

    protected abstract ExclusionType getExclusionType();
}
