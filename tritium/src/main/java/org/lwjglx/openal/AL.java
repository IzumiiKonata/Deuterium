package org.lwjglx.openal;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjglx.BufferUtils;
import org.lwjglx.LWJGLException;

import java.nio.IntBuffer;

public class AL {

    static ALCdevice alcDevice;
    static ALCcontext alcContext;

    private static boolean created = false;

    static {
//        Sys.initialize(); // init using dummy sys method
    }

    public static void create() {
        create(null, 44100, 60, false);
    }

    public static void create(String deviceArguments, int contextFrequency, int contextRefresh,
                              boolean contextSynchronized) {
        create(deviceArguments, contextFrequency, contextRefresh, contextSynchronized, true);
    }

    public static void create(String deviceArguments, int contextFrequency, int contextRefresh,
                              boolean contextSynchronized, boolean openDevice) {
        IntBuffer attribs = BufferUtils.createIntBuffer(16);

        attribs.put(ALC10.ALC_FREQUENCY);
        attribs.put(contextFrequency);

        attribs.put(ALC10.ALC_REFRESH);
        attribs.put(contextRefresh);

        attribs.put(ALC10.ALC_SYNC);
        attribs.put(contextSynchronized ? ALC10.ALC_TRUE : ALC10.ALC_FALSE);

        attribs.put(0);
        attribs.flip();

        String defaultDevice = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);

        long deviceHandle = ALC10.alcOpenDevice(defaultDevice);

        alcDevice = new ALCdevice(deviceHandle);

        final ALCCapabilities deviceCaps = org.lwjgl.openal.ALC.createCapabilities(deviceHandle);

        long contextHandle = ALC10.alcCreateContext(AL.getDevice().device, attribs);
        alcContext = new ALCcontext(contextHandle);
        ALC10.alcMakeContextCurrent(contextHandle);
        org.lwjgl.openal.AL.createCapabilities(deviceCaps);

        created = true;
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        ALC10.alcDestroyContext(alcContext.context);
        ALC10.alcCloseDevice(alcDevice.device);
        alcContext = null;
        alcDevice = null;
        created = false;
    }

    public static ALCcontext getContext() {
        return alcContext;
    }

    public static ALCdevice getDevice() {
        return alcDevice;
    }
}
