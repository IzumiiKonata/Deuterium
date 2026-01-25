/*
 * Copyright (c) 2002-2008 LWJGL Project All rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions are met: * Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. * Neither the name of 'LWJGL' nor the names of
 * its contributors may be used to endorse or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjglx;

import org.lwjgl.system.Platform;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

/**
 * <p>
 * Internal library methods
 * </p>
 *
 * @author Brian Matzon <brian@matzon.dk>
 * @version $Revision: 3608 $ $Id: LWJGLUtil.java 3608 2011-08-10 16:05:46Z spasi $
 */
public class LWJGLUtil {

    /**
     * Debug flag.
     */
    public static final boolean DEBUG = getPrivilegedBoolean("org.lwjgl.util.Debug");


    /**
     * Gets a boolean property as a privileged action.
     */
    public static boolean getPrivilegedBoolean(final String property_name) {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean(property_name));
    }


    /**
     * Prints the given message to System.err if DEBUG is true.
     *
     * @param msg Message to print
     */
    public static void log(CharSequence msg) {
        if (DEBUG) {
            System.err.println("[LWJGL] " + msg);
        }
    }

    /**
     * Returns a string representation of the integer argument as an unsigned integer in base&nbsp;16. The string will
     * be uppercase and will have a leading '0x'.
     *
     * @param value the integer value
     * @return the hex string representation
     */
    public static String toHexString(final int value) {
        return "0x" + Integer.toHexString(value)
                .toUpperCase();
    }

}
