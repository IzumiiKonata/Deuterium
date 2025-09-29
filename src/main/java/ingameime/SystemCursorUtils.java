package ingameime;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.*;
import tech.konata.obfuscation.ExcludeThis;

import java.util.*;

/**
 * @author IzumiiKonata
 * @since 2024/10/7 22:35
 */
@ExcludeThis
public class SystemCursorUtils {

    private static final User32 user32 = User32.INSTANCE;

    public static String getCurrentCursorPointer() {
        final CURSORINFO cursorinfo = new CURSORINFO();
        final int success = user32.GetCursorInfo(cursorinfo);
        if(success != 1) {
//            throw new Error("Could not retrieve cursor info: " + Native.getLastError());
            return null;
        }

        if (cursorinfo.hCursor == null) {
            return null;
        }

        return cursorinfo.hCursor.toString();
    }

    @ExcludeThis
    public static class CURSORINFO extends Structure {

        public int cbSize;
        public int flags;
        public WinDef.HCURSOR hCursor;
        public WinDef.POINT ptScreenPos;

        public CURSORINFO() {
            this.cbSize = Native.getNativeSize(CURSORINFO.class, null);
        }
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("cbSize", "flags", "hCursor", "ptScreenPos");
        }
    }

    @ExcludeThis
    public interface User32 extends com.sun.jna.Library {
        User32 INSTANCE = Native.load("User32.dll", User32.class);

        int GetCursorInfo(CURSORINFO cursorinfo);

    }

}
