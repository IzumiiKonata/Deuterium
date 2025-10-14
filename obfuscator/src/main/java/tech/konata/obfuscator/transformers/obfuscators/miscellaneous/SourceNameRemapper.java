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

import lombok.var;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;
import tech.konata.obfuscator.Logger;
import tech.konata.obfuscator.utils.IOUtils;

/**
 * Obfuscate the source name attribute by either randomizing the data, or removing it altogether.
 *
 * @author ItzSomebody
 */
public class SourceNameRemapper extends Transformer {

    @Override
    public void transform() {
        AtomicInteger counter = new AtomicInteger();

        getClassWrappers().stream().filter(classWrapper -> !excluded(classWrapper)).forEach(classWrapper -> {

            String sourceFile = classWrapper.classNode.sourceFile;

            while (mappings.containsKey(sourceFile)) {
                sourceFile = sourceFile + " (1)";
            }

            String newName = randomString(10) + ".java";

            classWrapper.classNode.sourceFile = newName;
            counter.incrementAndGet();
            mappings.put(sourceFile, newName);

        });

        Logger.stdOut(String.format("Obfuscated %d source name attributes.",
                counter.get()));

        this.dumpMappings();
    }

    private final Map<String, String> mappings = new HashMap<>();

    private void dumpMappings() {
        var file = new File("E:\\obf\\SourceNames.txt");
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
        return ExclusionType.SOURCE_NAME;
    }

    @Override
    public String getName() {
        return "Source name";
    }
}
