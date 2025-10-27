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

package tech.konata.obfuscator.transformers.obfuscators.miscellaneous;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.var;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.utils.IOUtils;
import tech.konata.obfuscator.utils.RandomUtils;
import org.objectweb.asm.tree.LineNumberNode;

/**
 * Obfuscates lines by changing their values, or removing them entirely.
 *
 * @author ItzSomebody.
 */
public class LineNumbers extends Transformer {

    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        this.getClassWrappers().parallelStream().filter(classWrapper -> !excluded(classWrapper)).forEach(classWrapper ->
                classWrapper.classNode.methods.parallelStream().filter(this::hasInstructions).forEach(methodNode -> {
                    Stream.of(methodNode.instructions.toArray()).filter(insn -> insn instanceof LineNumberNode).forEach(insn -> {
                        ((LineNumberNode) insn).line = 0;
                        counter.incrementAndGet();
                    });
                }));

        Logger.stdOut(String.format("Obfuscated %d line numbers.", counter.get()));

//        this.dumpMappings();
    }

    private final Map<String, String> mappings = new HashMap<>();

    private void dumpMappings() {
        var file = new File("E:\\obf\\LineNumbers.txt");
        if (file.exists()) {
            IOUtils.renameExistingFile(file);
        }

        try {
            file.createNewFile();
            var bw = new BufferedWriter(new FileWriter(file));
            mappings.forEach((oldName, newName) -> {
                try {
                    bw.append(oldName).append(" -> ").append(newName).append("\n");
                } catch (IOException ioe) {
                    Logger.stdWarn(String.format("Caught IOException while attempting to write line \"%s -> %s\"", oldName, newName));
                    ioe.printStackTrace(System.out);

                }
            });
        } catch (Throwable t) {
            Logger.stdWarn("Captured throwable upon attempting to generate mappings file: " + t.getMessage());
            t.printStackTrace(System.out);

        }
    }

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.LINE_NUMBERS;
    }

    @Override
    public String getName() {
        return "Line numbers";
    }
}
