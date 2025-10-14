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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tech.konata.obfuscator.exclusions.ExclusionManager;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.transformers.obfuscators.flow.HeavyFlowObfuscation;
import tech.konata.obfuscator.transformers.obfuscators.flow.LightFlowObfuscation;
import tech.konata.obfuscator.transformers.obfuscators.flow.NormalFlowObfuscation;
import tech.konata.obfuscator.transformers.obfuscators.invokedynamic.HeavyInvokeDynamic;
import tech.konata.obfuscator.transformers.obfuscators.invokedynamic.LightInvokeDynamic;
import tech.konata.obfuscator.transformers.obfuscators.invokedynamic.NormalInvokeDynamic;
import tech.konata.obfuscator.transformers.obfuscators.numbers.HeavyNumberObfuscation;
import tech.konata.obfuscator.transformers.obfuscators.numbers.LightNumberObfuscation;
import tech.konata.obfuscator.transformers.obfuscators.numbers.NormalNumberObfuscation;
import tech.konata.obfuscator.transformers.obfuscators.strings.HeavyStringEncryption;
import tech.konata.obfuscator.transformers.obfuscators.strings.LightStringEncryption;
import tech.konata.obfuscator.transformers.obfuscators.strings.NormalStringEncryption;

public class SessionInfo {
    private File input;
    private File output;
    private List<File> libraries;
    private List<Transformer> transformers = new ArrayList<>(Arrays.asList(
            new LightFlowObfuscation(),
            new NormalFlowObfuscation(),
            new HeavyFlowObfuscation(),

            new LightInvokeDynamic(),
            new NormalInvokeDynamic(),
            new HeavyInvokeDynamic(),

            new LightNumberObfuscation(),
            new NormalNumberObfuscation(),
            new HeavyNumberObfuscation(),

            new LightStringEncryption(),
            new NormalStringEncryption(),
            new HeavyStringEncryption()
    ));
    private ExclusionManager exclusions;
    private int trashClasses;
    private Dictionaries dictionaryType;

    private boolean noAnnotations = false;

    public void setNoAnnotations(boolean noAnnotations) {
        this.noAnnotations = noAnnotations;
    }

    public boolean isNoAnnotations() {
        return noAnnotations;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public File getInput() {
        return this.input;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public File getOutput() {
        return this.output;
    }

    public void setLibraries(List<File> libraries) {
        this.libraries = libraries;
    }

    public List<File> getLibraries() {
        return libraries;
    }

    public void setTransformers(List<Transformer> transformers) {

        if (isNoAnnotations())
            this.transformers.clear();

//        ArrayList<Transformer> curTransformers = new ArrayList<>(this.transformers);
//
//        this.transformers.clear();
//
//        transformers.addAll(curTransformers);
//
//        this.transformers = transformers;
        this.transformers.addAll(transformers);
    }

    public void setTransformersForcibly(List<Transformer> transformers) {
        this.transformers.clear();
        this.transformers.addAll(transformers);
    }

    public List<Transformer> getTransformers() {
        return this.transformers;
    }

    public void setExclusions(ExclusionManager exclusions) {
        this.exclusions = exclusions;
    }

    public ExclusionManager getExclusionManager() {
        return this.exclusions;
    }

    public void setTrashClasses(int trashClasses) {
        this.trashClasses = trashClasses;
    }

    public int getTrashClasses() {
        return this.trashClasses;
    }

    public void setDictionaryType(Dictionaries dictionaryType) {
        this.dictionaryType = dictionaryType;
    }

    public Dictionaries getDictionaryType() {
        return this.dictionaryType;
    }
}
