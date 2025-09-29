/*
 * Copyright LWJGL. All rights reserved. License terms: http://lwjgl.org/license.php
 */
package org.lwjglx.openal;

import org.lwjgl.openal.ALC;

/**
 * Utility class for the OpenAL extension AL_EXT_EFX. Provides functions to check for the extension and support of
 * various effects and filters.
 * <p/>
 * Currently supports AL_EXT_EFX version 1.0 effects and filters.
 *
 * @author Ciardhubh <ciardhubh[at]ciardhubh.de>
 */
public final class EFXUtil {

    /**
     * Constant for testSupportGeneric to check an effect.
     */
    private static final int EFFECT = 1111;
    /**
     * Constant for testSupportGeneric to check a filter.
     */
    private static final int FILTER = 2222;

    /**
     * Utility class, hidden contructor.
     */
    private EFXUtil() {
    }

    /**
     * Checks if OpenAL implementation is loaded and supports AL_EXT_EFX.
     *
     * @return True if AL_EXT_EFX is supported, false if not.
     * @throws OpenALException If OpenAL has not been created yet.
     */
    public static boolean isEfxSupported() {
        return ALC.getCapabilities().ALC_EXT_EFX;
    }

}
