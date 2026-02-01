package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.Location;
import net.minecraft.util.ReportedException;
import net.optifine.CustomGuis;
import net.optifine.EmissiveTextures;
import net.optifine.RandomEntities;
import net.optifine.shaders.ShadersTex;
import tritium.utils.logging.LogManager;
import org.apache.logging.log4j.Logger;
import tritium.utils.other.DevUtils;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class TextureManager implements ITickable, IResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger("TextureManager");
    public final Map<Location, ITextureObject> mapTextureObjects = new ConcurrentHashMap<>(512);
    private final List<ITickable> listTickables = Lists.newArrayList();
    private final Map<String, Integer> mapTextureCounters = Maps.newHashMap();
    private final IResourceManager theResourceManager;
    private ITextureObject boundTexture;
    private Location boundTextureLocation;

    public TextureManager(IResourceManager resourceManager) {
        this.theResourceManager = resourceManager;
    }

    public void triggerLoad(Location resource) {
        if (Config.isRandomEntities()) {
            resource = RandomEntities.getTextureLocation(resource);
        }

        if (Config.isCustomGuis()) {
            resource = CustomGuis.getTextureLocation(resource);
        }

        ITextureObject itextureobject = this.mapTextureObjects.get(resource);

        if (EmissiveTextures.isActive()) {
            itextureobject = EmissiveTextures.getEmissiveTexture(itextureobject, this.mapTextureObjects);
        }

        if (itextureobject == null) {
            itextureobject = new SimpleTexture(resource);
            this.loadTexture(resource, itextureobject);
        }
    }

    public void bindTexture(Location resource) {
        if (Config.isRandomEntities()) {
            resource = RandomEntities.getTextureLocation(resource);
        }

        if (Config.isCustomGuis()) {
            resource = CustomGuis.getTextureLocation(resource);
        }

        ITextureObject itextureobject = this.mapTextureObjects.get(resource);

        if (EmissiveTextures.isActive()) {
            itextureobject = EmissiveTextures.getEmissiveTexture(itextureobject, this.mapTextureObjects);
        }

        if (itextureobject == null) {
            itextureobject = new SimpleTexture(resource);
            this.loadTexture(resource, itextureobject);
        }

        if (Config.isShaders()) {
            ShadersTex.bindTexture(itextureobject);
        } else {
            TextureUtil.bindTexture(itextureobject.getGlTextureId());
        }

        this.boundTexture = itextureobject;
        this.boundTextureLocation = resource;
    }

    public boolean loadTickableTexture(Location textureLocation, ITickableTextureObject textureObj) {
        if (this.loadTexture(textureLocation, textureObj)) {
            this.listTickables.add(textureObj);
            return true;
        } else {
            return false;
        }
    }

    public boolean loadTexture(Location textureLocation, ITextureObject textureObj) {
        boolean flag = true;

        try {
            textureObj.loadTexture(this.theResourceManager);
        } catch (IOException ioexception) {
            logger.warn("Failed to load texture: " + textureLocation, ioexception);
            textureObj = TextureUtil.missingTexture;
            this.mapTextureObjects.put(textureLocation, textureObj);
            flag = false;
        } catch (Throwable throwable) {
            final ITextureObject textureObjf = textureObj;
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "注册贴图");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("正在注册的资源位置");
            crashreportcategory.addCrashSection("资源位置", textureLocation);
            crashreportcategory.addCrashSectionCallable("纹理对象类", new Callable<String>() {
                public String call() throws Exception {
                    return textureObjf.getClass().getName();
                }
            });
            throw new ReportedException(crashreport);
        }

        ITextureObject prev = this.mapTextureObjects.put(textureLocation, textureObj);

        if (prev != null) {
            System.out.println("Key = " + textureLocation + ", Prev = " + prev.getGlTextureId() + ", New = " + textureObj.getGlTextureId());
//            DevUtils.printCurrentInvokeStack();
            TextureUtil.deleteTexture(prev.getGlTextureId());
        }

        return flag;
    }

    public ITextureObject getTexture(Location textureLocation) {
        return this.mapTextureObjects.get(textureLocation);
    }

    public Location getDynamicTextureLocation(String name, DynamicTexture texture) {
        if (name.equals("logo")) {
            texture = Config.getMojangLogoTexture(texture);
        }

        Integer integer = this.mapTextureCounters.get(name);

        if (integer == null) {
            integer = 1;
        } else {
            integer = integer + 1;
        }

        this.mapTextureCounters.put(name, integer);
        Location resourcelocation = Location.of(String.format("dynamic/%s_%d", name, integer));
        this.loadTexture(resourcelocation, texture);
        return resourcelocation;
    }

    public void tick() {
        for (ITickable itickable : this.listTickables) {
            itickable.tick();
        }
    }

    public void deleteTexture(Location textureLocation) {
        ITextureObject itextureobject = this.getTexture(textureLocation);

        if (itextureobject != null) {
            this.mapTextureObjects.remove(textureLocation);
            TextureUtil.deleteTexture(itextureobject.getGlTextureId());
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        Config.dbg("*** 重新加载贴图 ***");
        Config.log("资源包: " + Config.getResourcePackNames());
        Iterator iterator = this.mapTextureObjects.keySet().iterator();

        while (iterator.hasNext()) {
            Location resourcelocation = (Location) iterator.next();
            String s = resourcelocation.getResourcePath();

            if (s.startsWith("mcpatcher/") || s.startsWith("optifine/") || EmissiveTextures.isEmissive(resourcelocation)) {
                ITextureObject itextureobject = this.mapTextureObjects.get(resourcelocation);

                if (itextureobject instanceof AbstractTexture) {
                    AbstractTexture abstracttexture = (AbstractTexture) itextureobject;
                    abstracttexture.deleteGlTexture();
                }

                iterator.remove();
            }
        }

        EmissiveTextures.update();

        for (Entry<Location, ITextureObject> entry : new HashSet<>(this.mapTextureObjects.entrySet())) {
            this.loadTexture(entry.getKey(), entry.getValue());
        }
    }

    public void reloadBannerTextures() {
        for (Entry<Location, ITextureObject> entry : new HashSet<>(this.mapTextureObjects.entrySet())) {
            Location resourcelocation = entry.getKey();
            ITextureObject itextureobject = entry.getValue();

            if (itextureobject instanceof LayeredColorMaskTexture) {
                this.loadTexture(resourcelocation, itextureobject);
            }
        }
    }

    public ITextureObject getBoundTexture() {
        return this.boundTexture;
    }

    public Location getBoundTextureLocation() {
        return this.boundTextureLocation;
    }
}
