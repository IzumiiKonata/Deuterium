package tritium.rendering.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import tritium.Tritium;
import tritium.interfaces.SharedConstants;
import tritium.rendering.rendersystem.RenderSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.lwjgl.opengl.GL11.*;

public class ShaderUtil implements SharedConstants {

    private static final IResourceManager RESOURCE_MANAGER = Minecraft.getMinecraft().getResourceManager();

    public static int createShader(final String fragmentResource, final String vertexResource) {
        final String fragmentSource = getShaderResource(fragmentResource);
        final String vertexSource = getShaderResource(vertexResource);

        if (fragmentResource == null || vertexResource == null) {
            System.out.println("An error occurred whilst creating shader");
            return -1;
        }

        final int fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        final int vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

        GL20.glShaderSource(fragmentId, fragmentSource);
        GL20.glShaderSource(vertexId, vertexSource);
        GL20.glCompileShader(fragmentId);
        GL20.glCompileShader(vertexId);

        if (!compileShader(fragmentId)) return -1;
        if (!compileShader(vertexId)) return -1;

        final int programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, fragmentId);
        GL20.glAttachShader(programId, vertexId);
        GL20.glValidateProgram(programId);
        GL20.glLinkProgram(programId);
        GL20.glDeleteShader(fragmentId);
        GL20.glDeleteShader(vertexId);

        return programId;
    }

    private static boolean compileShader(final int shaderId) {
        final boolean compiled = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE;
        if (compiled) return true;

        final String shaderLog = GL20.glGetShaderInfoLog(shaderId, 8192);
        System.out.println("\nError while compiling shader: ");
        System.out.println("-------------------------------");
        System.out.println(shaderLog);
        return false;
    }

    public static String getShaderResource(final String resource) {
        try {
            final InputStream inputStream = RESOURCE_MANAGER.getResource(Location.of("tritium/shaders/" + resource)).getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String source = "";

            try {
                for (String s; (s = bufferedReader.readLine()) != null; source += s + System.lineSeparator()) ;
            } catch (final IOException ignored) {
            }

            return source;
        } catch (final IOException | NullPointerException e) {
            System.out.println("An error occurred while getting a shader resource");
            e.printStackTrace();
            return null;
        }
    }

    public static void drawQuads() {
        if (Minecraft.getMinecraft().gameSettings.ofFastRender) return;
        final float width = (float) RenderSystem.getWidth();
        final float height = (float) RenderSystem.getHeight();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    public static void drawQuads(float width, float height) {
        if (Minecraft.getMinecraft().gameSettings.ofFastRender) return;
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }
}