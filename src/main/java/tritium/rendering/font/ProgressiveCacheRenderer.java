package tritium.rendering.font;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.*;

public class ProgressiveCacheRenderer {
    
    private final CFontRenderer renderer;

    private FloatBuffer posBuffer;
    private FloatBuffer texCoordBuffer;
    private FloatBuffer colorBuffer;
    
    private static final int INITIAL_BUFFER_SIZE = 4096;
    private static final int MAX_BUFFER_SIZE = 65536;
    
    private final Map<String, CachedString> vboCache = new LinkedHashMap<String, CachedString>(1024, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedString> eldest) {
            if (size() > MAX_CACHE_SIZE) {
                eldest.getValue().cleanup();
                return true;
            }
            return false;
        }
    };
    
    private final Set<String> pendingCache = new HashSet<>();
    
    private static final int MAX_CACHE_SIZE = 768;
    private long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL = 5000;
    
    private static class CachedString {
        final Map<Integer, VBOData> vboByTexture;
        long lastUsed;
        final String text;
        
        CachedString(String text, Map<Integer, VBOData> vboByTexture) {
            this.text = text;
            this.vboByTexture = vboByTexture;
            this.lastUsed = System.currentTimeMillis();
        }
        
        void cleanup() {
            for (VBOData vbo : vboByTexture.values()) {
                vbo.cleanup();
            }
        }
    }
    
    private static class VBOData {
        int posVBO, texVBO, colorVBO;
        int vertexCount;
        
        VBOData(int posVBO, int texVBO, int colorVBO, int vertexCount) {
            this.posVBO = posVBO;
            this.texVBO = texVBO;
            this.colorVBO = colorVBO;
            this.vertexCount = vertexCount;
        }
        
        void cleanup() {
            if (posVBO != 0) GL15.glDeleteBuffers(posVBO);
            if (texVBO != 0) GL15.glDeleteBuffers(texVBO);
            if (colorVBO != 0) GL15.glDeleteBuffers(colorVBO);
        }
    }
    
    public ProgressiveCacheRenderer(CFontRenderer renderer) {
        this.renderer = renderer;
        this.posBuffer = GLAllocation.createDirectFloatBuffer(INITIAL_BUFFER_SIZE * 2);
        this.texCoordBuffer = GLAllocation.createDirectFloatBuffer(INITIAL_BUFFER_SIZE * 2);
        this.colorBuffer = GLAllocation.createDirectFloatBuffer(INITIAL_BUFFER_SIZE * 4);
    }
    
    public boolean drawStringOptimized(String s, float x, float y, float r, float g, float b, float a) {
        periodicCleanup();
        
        String cacheKey = s + "_" + Float.floatToIntBits(r) + "_" + Float.floatToIntBits(g) + 
                         "_" + Float.floatToIntBits(b) + "_" + Float.floatToIntBits(a);
        
        
        CachedString cached = vboCache.get(cacheKey);
        
        if (cached != null) {
            
            cached.lastUsed = System.currentTimeMillis();
            return renderCached(cached, x, y);
        }
        
        
        boolean allGlyphsLoaded = areAllGlyphsLoaded(s);
        
        if (allGlyphsLoaded) {
            
            if (pendingCache.contains(cacheKey) || shouldCache(s)) {
                cached = createCachedString(s, r, g, b, a);
                if (cached != null) {
                    vboCache.put(cacheKey, cached);
                    pendingCache.remove(cacheKey);
                    cached.lastUsed = System.currentTimeMillis();
                    return renderCached(cached, x, y);
                }
            }
        } else {
            
            pendingCache.add(cacheKey);
        }
        
        
        return /*renderDirect(s, x, y, r, g, b, a)*/ true;
    }
    
    private boolean shouldCache(String s) {
        if (s.length() >= 3) return true;
        
        
        for (char c : s.toCharArray()) {
            if (c > 127) return true; 
        }
        
        return false;
    }
    
    private boolean areAllGlyphsLoaded(String s) {
        char[] chars = s.toCharArray();
        boolean inSel = false;
        
        for (char c : chars) {
            
            if (inSel) {
                inSel = false;
                continue;
            }
            if (c == '§') {
                inSel = true;
                continue;
            }
            
            if (c == '\n' || c == ' ') continue;
            
            if (c == '（') c = '(';
            if (c == '）') c = ')';
            
            Glyph glyph = renderer.allGlyphs[c];
            
            
            if (glyph == null || glyph.textureId == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean renderCached(CachedString cached, float x, float y) {
        GlStateManager.pushMatrix();
        
        y -= 2.0f;
        GlStateManager.translate(Math.round(x * 10.0) / 10.0, Math.round(y * 10.0) / 10.0, 0);
        GlStateManager.scale(0.5f, 0.5f, 1f);
        
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        
        for (Map.Entry<Integer, VBOData> entry : cached.vboByTexture.entrySet()) {
            GlStateManager.bindTexture(entry.getKey());
            VBOData vbo = entry.getValue();
            
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo.posVBO);
            GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
            
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo.texVBO);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
            
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo.colorVBO);
            GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);
            
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vbo.vertexCount);
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        
        GlStateManager.popMatrix();
        return true;
    }
    
    private CachedString createCachedString(String s, float r, float g, float b, float a) {
        Map<Integer, List<GlyphRenderData>> textureGroups = groupGlyphsByTexture(s, r, g, b, a);
        Map<Integer, VBOData> vboByTexture = new HashMap<>();
        
        for (Map.Entry<Integer, List<GlyphRenderData>> entry : textureGroups.entrySet()) {
            int textureId = entry.getKey();
            List<GlyphRenderData> glyphs = entry.getValue();
            
            if (textureId == -1 || glyphs.isEmpty()) continue;
            
            VBOData vbo = createVBO(glyphs);
            if (vbo != null) {
                vboByTexture.put(textureId, vbo);
            }
        }
        
        if (vboByTexture.isEmpty()) return null;
        
        return new CachedString(s, vboByTexture);
    }
    
    private VBOData createVBO(List<GlyphRenderData> glyphs) {
        int actualGlyphs = 0;
        for (GlyphRenderData data : glyphs) {
            if (data.glyph.value != ' ') actualGlyphs++;
        }
        
        if (actualGlyphs == 0) return null;
        
        int vertexCount = actualGlyphs * 6;
        
        ensureCapacity(vertexCount);
        
        posBuffer.clear();
        texCoordBuffer.clear();
        colorBuffer.clear();
        
        for (GlyphRenderData data : glyphs) {
            if (data.glyph.value == ' ') continue;
            
            float x = data.x;
            float y = data.y;
            float w = data.glyph.width;
            float h = data.glyph.height;
            
            addVertexData(x, y + h, 0, 1, data.r, data.g, data.b, data.a);
            addVertexData(x + w, y + h, 1, 1, data.r, data.g, data.b, data.a);
            addVertexData(x + w, y, 1, 0, data.r, data.g, data.b, data.a);
            
            addVertexData(x + w, y, 1, 0, data.r, data.g, data.b, data.a);
            addVertexData(x, y, 0, 0, data.r, data.g, data.b, data.a);
            addVertexData(x, y + h, 0, 1, data.r, data.g, data.b, data.a);
        }
        
        posBuffer.flip();
        texCoordBuffer.flip();
        colorBuffer.flip();
        
        
        int posVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
        
        int texVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STATIC_DRAW);
        
        int colorVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        return new VBOData(posVBO, texVBO, colorVBO, vertexCount);
    }
    
    private boolean renderDirect(String s, float x, float y, float r, float g, float b, float a) {
        GlStateManager.pushMatrix();
        
        y -= 2.0f;
        GlStateManager.translate(Math.round(x * 10.0) / 10.0, Math.round(y * 10.0) / 10.0, 0);
        GlStateManager.scale(0.5f, 0.5f, 1f);
        
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        
        Map<Integer, List<GlyphRenderData>> textureGroups = groupGlyphsByTexture(s, r, g, b, a);
        
        boolean allLoaded = true;
        for (Map.Entry<Integer, List<GlyphRenderData>> entry : textureGroups.entrySet()) {
            int textureId = entry.getKey();
            List<GlyphRenderData> glyphs = entry.getValue();
            
            if (textureId == -1) {
                allLoaded = false;
                continue;
            }
            
            GlStateManager.bindTexture(textureId);
            allLoaded = renderGlyphBatchDirect(glyphs) && allLoaded;
        }
        
        GlStateManager.popMatrix();
        return allLoaded;
    }
    
    private boolean renderGlyphBatchDirect(List<GlyphRenderData> glyphs) {
        if (glyphs.isEmpty()) return true;
        
        int actualGlyphs = 0;
        for (GlyphRenderData data : glyphs) {
            if (data.glyph.value != ' ') actualGlyphs++;
        }
        
        if (actualGlyphs == 0) return true;
        
        int vertexCount = actualGlyphs * 6;
        ensureCapacity(vertexCount);
        
        posBuffer.clear();
        texCoordBuffer.clear();
        colorBuffer.clear();
        
        for (GlyphRenderData data : glyphs) {
            if (data.glyph.value == ' ') continue;
            
            float x = data.x;
            float y = data.y;
            float w = data.glyph.width;
            float h = data.glyph.height;
            
            addVertexData(x, y + h, 0, 1, data.r, data.g, data.b, data.a);
            addVertexData(x + w, y + h, 1, 1, data.r, data.g, data.b, data.a);
            addVertexData(x + w, y, 1, 0, data.r, data.g, data.b, data.a);
            
            addVertexData(x + w, y, 1, 0, data.r, data.g, data.b, data.a);
            addVertexData(x, y, 0, 0, data.r, data.g, data.b, data.a);
            addVertexData(x, y + h, 0, 1, data.r, data.g, data.b, data.a);
        }
        
        posBuffer.flip();
        texCoordBuffer.flip();
        colorBuffer.flip();
        
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, posBuffer);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texCoordBuffer);
        GL11.glColorPointer(4, GL11.GL_FLOAT, 0, colorBuffer);
        
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
        
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        
        return true;
    }
    
    private static class GlyphRenderData {
        Glyph glyph;
        float x, y;
        float r, g, b, a;
        
        GlyphRenderData(Glyph glyph, float x, float y, float r, float g, float b, float a) {
            this.glyph = glyph;
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }
    
    private Map<Integer, List<GlyphRenderData>> groupGlyphsByTexture(String s, float r, float g, float b, float a) {
        Map<Integer, List<GlyphRenderData>> groups = new HashMap<>();
        
        float r2 = r, g2 = g, b2 = b;
        char[] chars = s.toCharArray();
        float xOffset = 0;
        float yOffset = 0;
        boolean inSel = false;
        
        for (char aChar : chars) {
            char c = aChar;
            
            if (inSel) {
                inSel = false;
                char c1 = Character.toUpperCase(c);
                if (c1 == 'R') {
                    r2 = r;
                    g2 = g;
                    b2 = b;
                } else {
                    int colorCode = getColorCode(c1);
                    if (colorCode != Integer.MIN_VALUE) {
                        int[] col = CFontRenderer.RGBIntToRGB(colorCode);
                        r2 = col[0] / 255f;
                        g2 = col[1] / 255f;
                        b2 = col[2] / 255f;
                    }
                }
                continue;
            }
            
            if (c == '§') {
                inSel = true;
                continue;
            } else if (c == '\n') {
                yOffset += renderer.getHeight() * 2 + 4;
                xOffset = 0;
                continue;
            }
            
            if (c == '（') c = '(';
            if (c == '）') c = ')';
            
            Glyph glyph = renderer.allGlyphs[c];
            if (glyph != null && glyph.textureId != -1) {
                groups.computeIfAbsent(glyph.textureId, k -> new ArrayList<>())
                      .add(new GlyphRenderData(glyph, xOffset, yOffset, r2, g2, b2, a));
                xOffset += glyph.width;
            }
        }
        
        return groups;
    }
    
    private void addVertexData(float x, float y, float u, float v, float r, float g, float b, float a) {
        posBuffer.put(x).put(y);
        texCoordBuffer.put(u).put(v);
        colorBuffer.put(r).put(g).put(b).put(a);
    }
    
    private void ensureCapacity(int vertexCount) {
        int posRequired = vertexCount * 2;
        int texRequired = vertexCount * 2;
        int colorRequired = vertexCount * 4;
        
        if (posBuffer.capacity() < posRequired) {
            int newCapacity = Math.min(MAX_BUFFER_SIZE, Math.max(posRequired, posBuffer.capacity() * 2));
            posBuffer = GLAllocation.createDirectFloatBuffer(newCapacity);
        }
        if (texCoordBuffer.capacity() < texRequired) {
            int newCapacity = Math.min(MAX_BUFFER_SIZE, Math.max(texRequired, texCoordBuffer.capacity() * 2));
            texCoordBuffer = GLAllocation.createDirectFloatBuffer(newCapacity);
        }
        if (colorBuffer.capacity() < colorRequired) {
            int newCapacity = Math.min(MAX_BUFFER_SIZE, Math.max(colorRequired, colorBuffer.capacity() * 2));
            colorBuffer = GLAllocation.createDirectFloatBuffer(newCapacity);
        }
    }
    
    private void periodicCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_INTERVAL) return;
        
        lastCleanup = now;
        
        
        vboCache.entrySet().removeIf(entry -> {
            if (now - entry.getValue().lastUsed > 10000) {
                entry.getValue().cleanup();
                return true;
            }
            return false;
        });
        
        
        if (pendingCache.size() > 500) {
            pendingCache.clear();
        }
    }
    
    private int getColorCode(char c) {
        switch (c) {
            case '0': return 0x000000;
            case '1': return 0x0000AA;
            case '2': return 0x00AA00;
            case '3': return 0x00AAAA;
            case '4': return 0xAA0000;
            case '5': return 0xAA00AA;
            case '6': return 0xFFAA00;
            case '7': return 0xAAAAAA;
            case '8': return 0x555555;
            case '9': return 0x5555FF;
            case 'A': return 0x55FF55;
            case 'B': return 0x55FFFF;
            case 'C': return 0xFF5555;
            case 'D': return 0xFF55FF;
            case 'E': return 0xFFFF55;
            case 'F': return 0xFFFFFF;
            default: return Integer.MIN_VALUE;
        }
    }
    
    public void cleanup() {
        for (CachedString cached : vboCache.values()) {
            cached.cleanup();
        }
        vboCache.clear();
        pendingCache.clear();
    }
    
    public String getCacheStats() {
        return String.format("VBO Cache: %d, Pending: %d", vboCache.size(), pendingCache.size());
    }
}