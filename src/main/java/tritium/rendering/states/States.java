/*
 * This file is part of https://github.com/Lyzev/Skija.
 *
 * Copyright (c) 2024-2025. Lyzev
 *
 * Skija is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * (at your option) any later version.
 *
 * Skija is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skija. If not, see <https://www.gnu.org/licenses/>.
 */

package tritium.rendering.states;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Stack;

/**
 * Stores and restores OpenGL states.
 */
public class States {
    /**
     * The current OpenGL version.
     */
    private static int glVersion;
    
    /**
     * The stack of OpenGL states.
     */
    private static final Stack<State> states = new Stack<>();
    
    /**
     * Gets the current OpenGL version.
     *
     * This code was inspired by <a href="https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L250-L254">imgui-java</a>
     * and modified to fit the project's codebase.
     */
    static {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            GL11.glGetIntegerv(GL30.GL_MAJOR_VERSION, major);
            GL11.glGetIntegerv(GL30.GL_MINOR_VERSION, minor);
            glVersion = major.get(0) * 100 + minor.get(0) * 10;
        }
    }
    
    /**
     * Pushes the current OpenGL state onto the stack.
     */
    public static void push() {
        states.push(new State(glVersion).push());
    }
    
    /**
     * Pops the last OpenGL state from the stack and restores it.
     */
    public static void pop() {
        if (states.isEmpty()) {
            throw new IllegalStateException("No state to restore.");
        }
        states.pop().pop();
    }
}