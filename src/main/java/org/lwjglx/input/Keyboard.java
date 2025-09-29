package org.lwjglx.input;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.LWJGLException;
import org.lwjglx.Sys;
import org.lwjglx.opengl.Display;
import tech.konata.phosphate.Phosphate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    /**
     * The special character meaning that no character was translated for the event.
     */
    public static final int CHAR_NONE = '\0';

    /**
     * The special keycode meaning that only the translated character is valid.
     */
    public static final int KEY_NONE = 0x00;

    public static final int KEY_ESCAPE = 0x01;
    public static final int KEY_1 = 0x02;
    public static final int KEY_2 = 0x03;
    public static final int KEY_3 = 0x04;
    public static final int KEY_4 = 0x05;
    public static final int KEY_5 = 0x06;
    public static final int KEY_6 = 0x07;
    public static final int KEY_7 = 0x08;
    public static final int KEY_8 = 0x09;
    public static final int KEY_9 = 0x0A;
    public static final int KEY_0 = 0x0B;
    public static final int KEY_MINUS = 0x0C; /* - on main keyboard */
    public static final int KEY_EQUALS = 0x0D;
    public static final int KEY_BACK = 0x0E; /* backspace */
    public static final int KEY_TAB = 0x0F;
    public static final int KEY_Q = 0x10;
    public static final int KEY_W = 0x11;
    public static final int KEY_E = 0x12;
    public static final int KEY_R = 0x13;
    public static final int KEY_T = 0x14;
    public static final int KEY_Y = 0x15;
    public static final int KEY_U = 0x16;
    public static final int KEY_I = 0x17;
    public static final int KEY_O = 0x18;
    public static final int KEY_P = 0x19;
    public static final int KEY_LBRACKET = 0x1A;
    public static final int KEY_RBRACKET = 0x1B;
    public static final int KEY_RETURN = 0x1C; /* Enter on main keyboard */
    public static final int KEY_LCONTROL = 0x1D;
    public static final int KEY_A = 0x1E;
    public static final int KEY_S = 0x1F;
    public static final int KEY_D = 0x20;
    public static final int KEY_F = 0x21;
    public static final int KEY_G = 0x22;
    public static final int KEY_H = 0x23;
    public static final int KEY_J = 0x24;
    public static final int KEY_K = 0x25;
    public static final int KEY_L = 0x26;
    public static final int KEY_SEMICOLON = 0x27;
    public static final int KEY_APOSTROPHE = 0x28;
    public static final int KEY_GRAVE = 0x29; /* accent grave */
    public static final int KEY_LSHIFT = 0x2A;
    public static final int KEY_BACKSLASH = 0x2B;
    public static final int KEY_Z = 0x2C;
    public static final int KEY_X = 0x2D;
    public static final int KEY_C = 0x2E;
    public static final int KEY_V = 0x2F;
    public static final int KEY_B = 0x30;
    public static final int KEY_N = 0x31;
    public static final int KEY_M = 0x32;
    public static final int KEY_COMMA = 0x33;
    public static final int KEY_PERIOD = 0x34; /* . on main keyboard */
    public static final int KEY_SLASH = 0x35; /* / on main keyboard */
    public static final int KEY_RSHIFT = 0x36;
    public static final int KEY_MULTIPLY = 0x37; /* * on numeric keypad */
    public static final int KEY_LMENU = 0x38; /* left Alt */
    public static final int KEY_SPACE = 0x39;
    public static final int KEY_CAPITAL = 0x3A;
    public static final int KEY_F1 = 0x3B;
    public static final int KEY_F2 = 0x3C;
    public static final int KEY_F3 = 0x3D;
    public static final int KEY_F4 = 0x3E;
    public static final int KEY_F5 = 0x3F;
    public static final int KEY_F6 = 0x40;
    public static final int KEY_F7 = 0x41;
    public static final int KEY_F8 = 0x42;
    public static final int KEY_F9 = 0x43;
    public static final int KEY_F10 = 0x44;
    public static final int KEY_NUMLOCK = 0x45;
    public static final int KEY_SCROLL = 0x46; /* Scroll Lock */
    public static final int KEY_NUMPAD7 = 0x47;
    public static final int KEY_NUMPAD8 = 0x48;
    public static final int KEY_NUMPAD9 = 0x49;
    public static final int KEY_SUBTRACT = 0x4A; /* - on numeric keypad */
    public static final int KEY_NUMPAD4 = 0x4B;
    public static final int KEY_NUMPAD5 = 0x4C;
    public static final int KEY_NUMPAD6 = 0x4D;
    public static final int KEY_ADD = 0x4E; /* + on numeric keypad */
    public static final int KEY_NUMPAD1 = 0x4F;
    public static final int KEY_NUMPAD2 = 0x50;
    public static final int KEY_NUMPAD3 = 0x51;
    public static final int KEY_NUMPAD0 = 0x52;
    public static final int KEY_DECIMAL = 0x53; /* . on numeric keypad */
    public static final int KEY_F11 = 0x57;
    public static final int KEY_F12 = 0x58;
    public static final int KEY_F13 = 0x64; /* (NEC PC98) */
    public static final int KEY_F14 = 0x65; /* (NEC PC98) */
    public static final int KEY_F15 = 0x66; /* (NEC PC98) */
    public static final int KEY_F16 = 0x67; /* Extended Function keys - (Mac) */
    public static final int KEY_F17 = 0x68;
    public static final int KEY_F18 = 0x69;
    public static final int KEY_KANA = 0x70; /* (Japanese keyboard) */
    public static final int KEY_F19 = 0x71; /* Extended Function keys - (Mac) */
    public static final int KEY_CONVERT = 0x79; /* (Japanese keyboard) */
    public static final int KEY_NOCONVERT = 0x7B; /* (Japanese keyboard) */
    public static final int KEY_YEN = 0x7D; /* (Japanese keyboard) */
    public static final int KEY_NUMPADEQUALS = 0x8D; /* = on numeric keypad (NEC PC98) */
    public static final int KEY_CIRCUMFLEX = 0x90; /* (Japanese keyboard) */
    public static final int KEY_AT = 0x91; /* (NEC PC98) */
    public static final int KEY_COLON = 0x92; /* (NEC PC98) */
    public static final int KEY_UNDERLINE = 0x93; /* (NEC PC98) */
    public static final int KEY_KANJI = 0x94; /* (Japanese keyboard) */
    public static final int KEY_STOP = 0x95; /* (NEC PC98) */
    public static final int KEY_AX = 0x96; /* (Japan AX) */
    public static final int KEY_UNLABELED = 0x97; /* (J3100) */
    public static final int KEY_NUMPADENTER = 0x9C; /* Enter on numeric keypad */
    public static final int KEY_RCONTROL = 0x9D;
    public static final int KEY_SECTION = 0xA7; /* Section symbol (Mac) */
    public static final int KEY_NUMPADCOMMA = 0xB3; /* , on numeric keypad (NEC PC98) */
    public static final int KEY_DIVIDE = 0xB5; /* / on numeric keypad */
    public static final int KEY_SYSRQ = 0xB7;
    public static final int KEY_RMENU = 0xB8; /* right Alt */
    public static final int KEY_FUNCTION = 0xC4; /* Function (Mac) */
    public static final int KEY_PAUSE = 0xC5; /* Pause */
    public static final int KEY_HOME = 0xC7; /* Home on arrow keypad */
    public static final int KEY_UP = 0xC8; /* UpArrow on arrow keypad */
    public static final int KEY_PRIOR = 0xC9; /* PgUp on arrow keypad */
    public static final int KEY_LEFT = 0xCB; /* LeftArrow on arrow keypad */
    public static final int KEY_RIGHT = 0xCD; /* RightArrow on arrow keypad */
    public static final int KEY_END = 0xCF; /* End on arrow keypad */
    public static final int KEY_DOWN = 0xD0; /* DownArrow on arrow keypad */
    public static final int KEY_NEXT = 0xD1; /* PgDn on arrow keypad */
    public static final int KEY_INSERT = 0xD2; /* Insert on arrow keypad */
    public static final int KEY_DELETE = 0xD3; /* Delete on arrow keypad */
    public static final int KEY_CLEAR = 0xDA; /* Clear key (Mac) */
    public static final int KEY_LMETA = 0xDB; /* Left Windows/Option key */
    public static final int KEY_LWIN = KEY_LMETA; /* Left Windows key */
    public static final int KEY_RMETA = 0xDC; /* Right Windows/Option key */
    public static final int KEY_RWIN = KEY_RMETA; /* Right Windows key */
    public static final int KEY_APPS = 0xDD; /* AppMenu key */
    public static final int KEY_POWER = 0xDE;
    public static final int KEY_SLEEP = 0xDF;

    public static final int keyCount;

    private static final Map<String, Integer> reverseKeyMap = new ConcurrentHashMap<>();


    public enum KeyState {

        PRESS(true),
        RELEASE(false),
        REPEAT(true);

        public final boolean isPressed;

        KeyState(boolean isPressed) {
            this.isPressed = isPressed;
        }
    }

    private static boolean doRepeatEvents = true;

    public static final int KEYBOARD_SIZE = Short.MAX_VALUE;
    public static Queue<KeyEvent> eventQueue = new ArrayBlockingQueue<>(256);
    private static final String[] unlocalizedKeyNameMiniLut = new String[Short.MAX_VALUE];

    static {

        if (Phosphate.getInstance().isObfuscated()) {
            reverseKeyMap.put("NONE", 0);
            reverseKeyMap.put("ESCAPE", 1);
            reverseKeyMap.put("1", 2);
            reverseKeyMap.put("2", 3);
            reverseKeyMap.put("3", 4);
            reverseKeyMap.put("4", 5);
            reverseKeyMap.put("5", 6);
            reverseKeyMap.put("6", 7);
            reverseKeyMap.put("7", 8);
            reverseKeyMap.put("8", 9);
            reverseKeyMap.put("9", 10);
            reverseKeyMap.put("0", 11);
            reverseKeyMap.put("MINUS", 12);
            reverseKeyMap.put("EQUALS", 13);
            reverseKeyMap.put("BACK", 14);
            reverseKeyMap.put("TAB", 15);
            reverseKeyMap.put("Q", 16);
            reverseKeyMap.put("W", 17);
            reverseKeyMap.put("E", 18);
            reverseKeyMap.put("R", 19);
            reverseKeyMap.put("T", 20);
            reverseKeyMap.put("Y", 21);
            reverseKeyMap.put("U", 22);
            reverseKeyMap.put("I", 23);
            reverseKeyMap.put("O", 24);
            reverseKeyMap.put("P", 25);
            reverseKeyMap.put("LBRACKET", 26);
            reverseKeyMap.put("RBRACKET", 27);
            reverseKeyMap.put("RETURN", 28);
            reverseKeyMap.put("LCONTROL", 29);
            reverseKeyMap.put("A", 30);
            reverseKeyMap.put("S", 31);
            reverseKeyMap.put("D", 32);
            reverseKeyMap.put("F", 33);
            reverseKeyMap.put("G", 34);
            reverseKeyMap.put("H", 35);
            reverseKeyMap.put("J", 36);
            reverseKeyMap.put("K", 37);
            reverseKeyMap.put("L", 38);
            reverseKeyMap.put("SEMICOLON", 39);
            reverseKeyMap.put("APOSTROPHE", 40);
            reverseKeyMap.put("GRAVE", 41);
            reverseKeyMap.put("LSHIFT", 42);
            reverseKeyMap.put("BACKSLASH", 43);
            reverseKeyMap.put("Z", 44);
            reverseKeyMap.put("X", 45);
            reverseKeyMap.put("C", 46);
            reverseKeyMap.put("V", 47);
            reverseKeyMap.put("B", 48);
            reverseKeyMap.put("N", 49);
            reverseKeyMap.put("M", 50);
            reverseKeyMap.put("COMMA", 51);
            reverseKeyMap.put("PERIOD", 52);
            reverseKeyMap.put("SLASH", 53);
            reverseKeyMap.put("RSHIFT", 54);
            reverseKeyMap.put("MULTIPLY", 55);
            reverseKeyMap.put("LMENU", 56);
            reverseKeyMap.put("SPACE", 57);
            reverseKeyMap.put("CAPITAL", 58);
            reverseKeyMap.put("F1", 59);
            reverseKeyMap.put("F2", 60);
            reverseKeyMap.put("F3", 61);
            reverseKeyMap.put("F4", 62);
            reverseKeyMap.put("F5", 63);
            reverseKeyMap.put("F6", 64);
            reverseKeyMap.put("F7", 65);
            reverseKeyMap.put("F8", 66);
            reverseKeyMap.put("F9", 67);
            reverseKeyMap.put("F10", 68);
            reverseKeyMap.put("NUMLOCK", 69);
            reverseKeyMap.put("SCROLL", 70);
            reverseKeyMap.put("NUMPAD7", 71);
            reverseKeyMap.put("NUMPAD8", 72);
            reverseKeyMap.put("NUMPAD9", 73);
            reverseKeyMap.put("SUBTRACT", 74);
            reverseKeyMap.put("NUMPAD4", 75);
            reverseKeyMap.put("NUMPAD5", 76);
            reverseKeyMap.put("NUMPAD6", 77);
            reverseKeyMap.put("ADD", 78);
            reverseKeyMap.put("NUMPAD1", 79);
            reverseKeyMap.put("NUMPAD2", 80);
            reverseKeyMap.put("NUMPAD3", 81);
            reverseKeyMap.put("NUMPAD0", 82);
            reverseKeyMap.put("DECIMAL", 83);
            reverseKeyMap.put("F11", 87);
            reverseKeyMap.put("F12", 88);
            reverseKeyMap.put("F13", 100);
            reverseKeyMap.put("F14", 101);
            reverseKeyMap.put("F15", 102);
            reverseKeyMap.put("F16", 103);
            reverseKeyMap.put("F17", 104);
            reverseKeyMap.put("F18", 105);
            reverseKeyMap.put("KANA", 112);
            reverseKeyMap.put("F19", 113);
            reverseKeyMap.put("CONVERT", 121);
            reverseKeyMap.put("NOCONVERT", 123);
            reverseKeyMap.put("YEN", 125);
            reverseKeyMap.put("NUMPADEQUALS", 141);
            reverseKeyMap.put("CIRCUMFLEX", 144);
            reverseKeyMap.put("AT", 145);
            reverseKeyMap.put("COLON", 146);
            reverseKeyMap.put("UNDERLINE", 147);
            reverseKeyMap.put("KANJI", 148);
            reverseKeyMap.put("STOP", 149);
            reverseKeyMap.put("AX", 150);
            reverseKeyMap.put("UNLABELED", 151);
            reverseKeyMap.put("NUMPADENTER", 156);
            reverseKeyMap.put("RCONTROL", 157);
            reverseKeyMap.put("SECTION", 167);
            reverseKeyMap.put("NUMPADCOMMA", 179);
            reverseKeyMap.put("DIVIDE", 181);
            reverseKeyMap.put("SYSRQ", 183);
            reverseKeyMap.put("RMENU", 184);
            reverseKeyMap.put("FUNCTION", 196);
            reverseKeyMap.put("PAUSE", 197);
            reverseKeyMap.put("HOME", 199);
            reverseKeyMap.put("UP", 200);
            reverseKeyMap.put("PRIOR", 201);
            reverseKeyMap.put("LEFT", 203);
            reverseKeyMap.put("RIGHT", 205);
            reverseKeyMap.put("END", 207);
            reverseKeyMap.put("DOWN", 208);
            reverseKeyMap.put("NEXT", 209);
            reverseKeyMap.put("INSERT", 210);
            reverseKeyMap.put("DELETE", 211);
            reverseKeyMap.put("CLEAR", 218);
            reverseKeyMap.put("LMETA", 219);
            reverseKeyMap.put("RMETA", 220);
            reverseKeyMap.put("APPS", 221);
            reverseKeyMap.put("POWER", 222);
            reverseKeyMap.put("SLEEP", 223);
        }

        // Use reflection to find out key names
        Field[] fields = Keyboard.class.getFields();
        int keyCounter = 0;
        try {
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && field.getType()
                        .equals(int.class)
                        && field.getName()
                        .startsWith("KEY_")
                        && !field.getName()
                        .endsWith("WIN")) {
                    /* Don't use deprecated names */
                    int key = field.getInt(null);
                    String name = field.getName()
                            .substring(4);
                    unlocalizedKeyNameMiniLut[key] = name;
                    keyCounter++;

                    reverseKeyMap.put(name, key);
                }
            }
        } catch (Exception e) {
        }
        keyCount = keyCounter;
        for (int i = 0; i < unlocalizedKeyNameMiniLut.length; i++) {
            if (unlocalizedKeyNameMiniLut[i] == null) {
                unlocalizedKeyNameMiniLut[i] = "Key " + i;
            }
        }
        eventQueue.add(new KeyEvent(0, '\0', KeyState.RELEASE, Sys.getNanoTime()));
    }

    public static void addRawKeyEvent(KeyEvent event) {
        if (event == null || (event.state == KeyState.REPEAT && !doRepeatEvents)) {
            return;
        }

        try {
            eventQueue.add(event);
        } catch (IllegalStateException ignored) {
        }
    }

    private static KeyState getKeyState(int action) {

        if (action == GLFW_PRESS)
            return KeyState.PRESS;

        if (action == GLFW_RELEASE)
            return KeyState.RELEASE;

        if (action == GLFW_REPEAT)
            return KeyState.REPEAT;

        return KeyState.RELEASE;

    }

    public static void addGlfwKeyEvent(long window, int key, int scancode, int action, int mods, char c) {
        final KeyState state = getKeyState(action);
        addRawKeyEvent(new KeyEvent(KeyCodes.glfwToLwjgl(key), c, state, Sys.getNanoTime()));
    }

    public static void addCharEvent(int key, char c) {
        try {
            eventQueue.add(new KeyEvent(KEY_NONE, c, KeyState.PRESS, Sys.getNanoTime()));
        } catch (IllegalStateException ignored) {
        }
    }

    public static void create() throws LWJGLException {
    }

    public static boolean isKeyDown(int key) {
        if (key == KEY_NONE) {
            return false;
        }
        final int keyCode = KeyCodes.lwjglToGlfw(key);
        return keyCode != GLFW_KEY_UNKNOWN && GLFW.glfwGetKey(Display.getWindow(), keyCode) == GLFW.GLFW_PRESS;
    }

    public static void poll() {
        // no-op
    }

    public static void enableRepeatEvents(boolean enable) {
        doRepeatEvents = enable;
    }

    public static boolean areRepeatEventsEnabled() {
        return doRepeatEvents;
    }

    public static int getKeyCount() {
        return keyCount;
    }

    public static int getNumKeyboardEvents() {
        return eventQueue.size();
    }

    public static boolean isRepeatEvent() {
        return eventQueue.peek().state == KeyState.REPEAT;
    }

    public static boolean next() {
        boolean next = eventQueue.size() > 1;
        if (next) {
            eventQueue.remove();
        }
        return next;
    }

    public static int getEventKey() {
        return eventQueue.peek().key;
    }

    public static char getEventCharacter() {
        return eventQueue.peek().aChar;

    }

    public static boolean getEventKeyState() {
        return eventQueue.peek().state.isPressed;
    }

    public static long getEventNanoseconds() {
        return eventQueue.peek().nano;
    }


    public static String getKeyName(int key) {
        if (key == KEY_NONE) {
            return "NONE";
        }

        // GLFW caches this internally, and knows when keyboard layouts switch.
        String glfwName = GLFW.glfwGetKeyName(KeyCodes.lwjglToGlfw(key), 0);

        if (glfwName != null)
            glfwName = glfwName.toUpperCase(Locale.ROOT);

        final String name;
        if (glfwName == null) {
            if (key >= 0 && key < unlocalizedKeyNameMiniLut.length) {
                name = unlocalizedKeyNameMiniLut[key];
            } else {
                name = "Key " + key;
            }
        } else {
            name = glfwName;
        }

        return name;
    }

    public static int getKeyIndex(String keyName) {
        if (keyName.equals("NONE")) {
            return KEY_NONE;
        }
        Integer ret = reverseKeyMap.get(keyName);
        if (ret == null) {
            if (keyName.matches("Key -?[0-9]+]")) {
                return Integer.parseInt(StringUtils.removeStart(keyName, "Key "));
            }
            return KEY_NONE;
        } else return ret;
    }

    public static boolean isCreated() {
        return Display.isCreated();
    }

    public static void destroy() {
    }

    public static final class KeyEvent {

        public int key;
        public char aChar;
        public KeyState state;
        public long nano;
        public boolean queueOutOfOrderRelease = false;

        public KeyEvent(int key, char aChar, KeyState state, long nano) {
            this.key = key;
            this.aChar = aChar;
            this.state = state;
            this.nano = nano;
        }

        public KeyEvent copy() {
            final KeyEvent ev = new KeyEvent(key, aChar, state, nano);
            ev.queueOutOfOrderRelease = this.queueOutOfOrderRelease;
            return ev;
        }
    }
}
