package tech.konata.phosphate.rendering.shader.special;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ShaderRenderer {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final int programID;

    @SneakyThrows
    public ShaderRenderer(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = GL20.glCreateProgram();
        InputStream r = ShaderRenderer.class.getResourceAsStream("/assets/minecraft/" + fragmentShaderLoc);
        if (r == null)
            System.out.println("/assets/minecraft/" + fragmentShaderLoc);
        int fragmentShaderID = createShader(r, GL20.GL_FRAGMENT_SHADER);

        GL20.glAttachShader(program, fragmentShaderID);
        InputStream v = ShaderRenderer.class.getResourceAsStream("/assets/minecraft/" + vertexShaderLoc);
        if (v == null)
            System.out.println("/assets/minecraft/" + vertexShaderLoc);

        int vertexShaderID = createShader(v, GL20.GL_VERTEX_SHADER);
        GL20.glAttachShader(program, vertexShaderID);

        GL20.glLinkProgram(program);
        int status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);

        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }
        this.programID = program;
    }

    public ShaderRenderer(String fragmentShaderLoc) {
        this(fragmentShaderLoc, Phosphate.NAME + "/shaders/vertex.vsh");
    }

    public static void drawQuads(double x, double y, double width, double height) {
        if (mc.gameSettings.ofFastRender) return;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
    }

    public static void drawQuads() {
        if (mc.gameSettings.ofFastRender) return;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(0, 0);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(0, RenderSystem.getHeight());
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(RenderSystem.getWidth(), RenderSystem.getHeight());
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(RenderSystem.getWidth(), 0);
        GL11.glEnd();
    }

    public static void drawBind(double x, double y, double width, double height) {
        GL11.glBegin(7);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void drawQuads(float alpha) {
        if (mc.gameSettings.ofFastRender) return;
        GlStateManager.color(1, 1, 1, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(0, 0);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(0, RenderSystem.getHeight());
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(RenderSystem.getWidth(), RenderSystem.getHeight());
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(RenderSystem.getWidth(), 0);
        GL11.glEnd();
    }

    private static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public void init() {
        GL20.glUseProgram(programID);
    }

    public void unload() {
        GL20.glUseProgram(0);
    }

    public int getUniform(String name) {
        return GL20.glGetUniformLocation(programID, name);
    }

    public void setUniformf(String name, float... args) {
        int loc = GL20.glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1:
                GL20.glUniform1f(loc, args[0]);
                break;
            case 2:
                GL20.glUniform2f(loc, args[0], args[1]);
                break;
            case 3:
                GL20.glUniform3f(loc, args[0], args[1], args[2]);
                break;
            case 4:
                GL20.glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }

    public void setUniformi(String name, int... args) {
        int loc = GL20.glGetUniformLocation(programID, name);
        if (args.length > 1) GL20.glUniform2i(loc, args[0], args[1]);
        else GL20.glUniform1i(loc, args[0]);
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, readInputStream(inputStream));
        GL20.glCompileShader(shader);


        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            System.out.println(GL20.glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }

        return shader;
    }
}
