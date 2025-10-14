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

package tech.konata.obfuscator.transformers.obfuscators.ejector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.analysis.constant.ConstantAnalyzer;
import tech.konata.obfuscator.analysis.constant.values.AbstractValue;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.transformers.obfuscators.ejector.phases.AbstractEjectPhase;
import tech.konata.obfuscator.transformers.obfuscators.ejector.phases.FieldSetEjector;
import tech.konata.obfuscator.transformers.obfuscators.ejector.phases.MethodCallEjector;

/**
 * Extracts parts of code to individual methods.
 *
 * @author vovanre
 */
public class Ejector extends Transformer {
    private boolean ejectMethodCalls = true;
    private boolean ejectFieldSet = true;
    private boolean junkArguments = false;
    private int junkArgumentStrength = 3;

    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        getClassWrappers().stream()
                .filter(classWrapper -> !excluded(classWrapper))
                .forEach(classWrapper -> processClass(classWrapper, counter));

        Logger.stdOut(String.format("Ejected %d regions.", counter.get()));
    }

    private List<AbstractEjectPhase> getPhases(EjectorContext ejectorContext) {
        List<AbstractEjectPhase> phases = new ArrayList<>();
        if (isEjectMethodCalls())
            phases.add(new MethodCallEjector(ejectorContext));
        if (isEjectFieldSet())
            phases.add(new FieldSetEjector(ejectorContext));
        return phases;
    }

    private void processClass(ClassWrapper classWrapper, AtomicInteger counter) {
        new ArrayList<>(classWrapper.getMethods()).stream()
                .filter(methodWrapper -> !excluded(methodWrapper))
                .filter(methodWrapper -> !"<init>".equals(methodWrapper.getMethodNode().name))
                .forEach(methodWrapper -> {
                    EjectorContext ejectorContext = new EjectorContext(counter, classWrapper, junkArguments, junkArgumentStrength);
                    getPhases(ejectorContext).forEach(ejectPhase -> {
                        ConstantAnalyzer constantAnalyzer = new ConstantAnalyzer();
                        try {
                            Logger.stdOut("Analyze: " + classWrapper.getOriginalName() + "::" + methodWrapper.getOriginalName() + methodWrapper.getOriginalDescriptor());
                            Frame<AbstractValue>[] frames = constantAnalyzer.analyze(classWrapper.getName(), methodWrapper.getMethodNode());

                            ejectPhase.process(methodWrapper, frames);
                        } catch (AnalyzerException e) {
                            Logger.stdErr("Can't analyze method: " + classWrapper.getOriginalName() + "::" + methodWrapper.getOriginalName() + methodWrapper.getOriginalDescriptor());
                            Logger.stdErr(e.toString());
                        }
                    });
                });
    }

    @Override
    public ExclusionType getExclusionType() {
        return ExclusionType.EJECTOR;
    }

    @Override
    public String getName() {
        return "Ejector";
    }

    private boolean isEjectMethodCalls() {
        return ejectMethodCalls;
    }

    private void setEjectMethodCalls(boolean ejectMethodCalls) {
        this.ejectMethodCalls = ejectMethodCalls;
    }


    private boolean isEjectFieldSet() {
        return ejectFieldSet;
    }

    private void setEjectFieldSet(boolean ejectFieldSet) {
        this.ejectFieldSet = ejectFieldSet;
    }

    private void setJunkArguments(boolean junkArguments) {
        this.junkArguments = junkArguments;
    }

    private void setJunkArgumentStrength(int junkArgumentStrength) {
        this.junkArgumentStrength = Math.min(junkArgumentStrength, 50);
    }
}