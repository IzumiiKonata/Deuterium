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

package tech.konata.obfuscator.exceptions;

import tech.konata.obfuscator.Logger;

public class MissingClassException extends RuntimeException {
    public MissingClassException(String msg) {
        super(msg);
        Logger.stdOut("Do NOT report an issue about this exception unless you have absolutely made sure that" +
                " the class reported missing exists in the library list you provided to Obfuscator");
    }

    private MissingClassException(String className, boolean library) {
        super("Do NOT report this as an issue unless you have ensured the supposedly missing class is actually in at " +
                "least one of the provided libraries in your config.\n" + String.format(
                "Could not find \"%s\" in the %s.", className, library ? "classpath" : "input JAR classes"));
    }

    public static MissingClassException forInputClass(String className) {
        return new MissingClassException(className, false);
    }

    public static MissingClassException forLibraryClass(String className) {
        return new MissingClassException(className, true);
    }
}
