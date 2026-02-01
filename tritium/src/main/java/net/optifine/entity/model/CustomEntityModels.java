package net.optifine.entity.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Location;
import net.optifine.entity.model.anim.ModelResolver;
import net.optifine.entity.model.anim.ModelUpdater;

import java.io.IOException;
import java.util.*;

public class CustomEntityModels {
    private static boolean active = false;
    private static Map<Class<? extends Entity>, Render<? extends Entity>> originalEntityRenderMap = null;
    private static Map<Class<? extends TileEntity>, TileEntitySpecialRenderer<? extends TileEntity>> originalTileEntityRenderMap = null;

    public static void update() {
        Map<Class<? extends Entity>, Render<? extends Entity>> map = getEntityRenderMap();
        Map<Class<? extends TileEntity>, TileEntitySpecialRenderer<? extends TileEntity>> map1 = getTileEntityRenderMap();

        if (map == null) {
            Config.warn("Entity render map not found, custom entity models are DISABLED.");
        } else if (map1 == null) {
            Config.warn("Tile entity render map not found, custom entity models are DISABLED.");
        } else {
            active = false;
            map.clear();
            map1.clear();
            map.putAll(originalEntityRenderMap);
            map1.putAll(originalTileEntityRenderMap);

            if (Config.isCustomEntityModels()) {
                Location[] aresourcelocation = getModelLocations();

                for (Location resourcelocation : aresourcelocation) {
                    Config.dbg("CustomEntityModel: " + resourcelocation.getResourcePath());
                    IEntityRenderer ientityrenderer = parseEntityRender(resourcelocation);

                    if (ientityrenderer != null) {
                        Class oclass = ientityrenderer.getEntityClass();

                        if (oclass != null) {
                            if (ientityrenderer instanceof Render) {
                                map.put(oclass, (Render) ientityrenderer);
                            } else if (ientityrenderer instanceof TileEntitySpecialRenderer) {
                                map1.put(oclass, (TileEntitySpecialRenderer) ientityrenderer);
                            } else {
                                Config.warn("Unknown renderer type: " + ientityrenderer.getClass().getName());
                            }

                            active = true;
                        }
                    }
                }
            }
        }
    }

    private static Map<Class<? extends Entity>, Render<? extends Entity>> getEntityRenderMap() {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        Map<Class<? extends Entity>, Render<? extends Entity>> map = rendermanager.getEntityRenderMap();

        if (map == null) {
            return null;
        } else {
            if (originalEntityRenderMap == null) {
                originalEntityRenderMap = new HashMap(map);
            }

            return map;
        }
    }

    private static Map<Class<? extends TileEntity>, TileEntitySpecialRenderer<? extends TileEntity>> getTileEntityRenderMap() {
        Map<Class<? extends TileEntity>, TileEntitySpecialRenderer<? extends TileEntity>> map = TileEntityRendererDispatcher.instance.mapSpecialRenderers;

        if (originalTileEntityRenderMap == null) {
            originalTileEntityRenderMap = new HashMap(map);
        }

        return map;
    }

    private static Location[] getModelLocations() {
        String s = "optifine/cem/";
        String s1 = ".jem";
        List<Location> list = new ArrayList();
        String[] astring = CustomModelRegistry.getModelNames();

        for (String s2 : astring) {
            String s3 = s + s2 + s1;
            Location resourcelocation = Location.of(s3);

            if (Config.hasResource(resourcelocation)) {
                list.add(resourcelocation);
            }
        }

        return list.toArray(new Location[0]);
    }

    private static IEntityRenderer parseEntityRender(Location location) {
        try {
            JsonObject jsonobject = CustomEntityModelParser.loadJson(location);
            return parseEntityRender(jsonobject, location.getResourcePath());
        } catch (IOException ioexception) {
            Config.error(ioexception.getClass().getName() + ": " + ioexception.getMessage());
            return null;
        } catch (JsonParseException jsonparseexception) {
            Config.error(jsonparseexception.getClass().getName() + ": " + jsonparseexception.getMessage());
            return null;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static IEntityRenderer parseEntityRender(JsonObject obj, String path) {
        CustomEntityRenderer customentityrenderer = CustomEntityModelParser.parseEntityRender(obj, path);
        String s = customentityrenderer.getName();
        ModelAdapter modeladapter = CustomModelRegistry.getModelAdapter(s);
        checkNull(modeladapter, "Entity not found: " + s);
        Class oclass = modeladapter.getEntityClass();
        checkNull(oclass, "Entity class not found: " + s);
        IEntityRenderer ientityrenderer = makeEntityRender(modeladapter, customentityrenderer);

        if (ientityrenderer == null) {
            return null;
        } else {
            ientityrenderer.setEntityClass(oclass);
            return ientityrenderer;
        }
    }

    private static IEntityRenderer makeEntityRender(ModelAdapter modelAdapter, CustomEntityRenderer cer) {
        Location resourcelocation = cer.getTextureLocation();
        CustomModelRenderer[] acustommodelrenderer = cer.getCustomModelRenderers();
        float f = cer.getShadowSize();

        if (f < 0.0F) {
            f = modelAdapter.getShadowSize();
        }

        ModelBase modelbase = modelAdapter.makeModel();

        if (modelbase == null) {
            return null;
        } else {
            ModelResolver modelresolver = new ModelResolver(modelAdapter, modelbase, acustommodelrenderer);

            if (!modifyModel(modelAdapter, modelbase, acustommodelrenderer, modelresolver)) {
                return null;
            } else {
                IEntityRenderer ientityrenderer = modelAdapter.makeEntityRender(modelbase, f);

                if (ientityrenderer == null) {
                    throw new JsonParseException("Entity renderer is null, model: " + modelAdapter.getName() + ", adapter: " + modelAdapter.getClass().getName());
                } else {
                    if (resourcelocation != null) {
                        ientityrenderer.setLocationTextureCustom(resourcelocation);
                    }

                    return ientityrenderer;
                }
            }
        }
    }

    private static boolean modifyModel(ModelAdapter modelAdapter, ModelBase model, CustomModelRenderer[] modelRenderers, ModelResolver mr) {
        for (CustomModelRenderer custommodelrenderer : modelRenderers) {
            if (!modifyModel(modelAdapter, model, custommodelrenderer, mr)) {
                return false;
            }
        }

        return true;
    }

    private static boolean modifyModel(ModelAdapter modelAdapter, ModelBase model, CustomModelRenderer customModelRenderer, ModelResolver modelResolver) {
        String s = customModelRenderer.getModelPart();
        ModelRenderer modelrenderer = modelAdapter.getModelRenderer(model, s);

        if (modelrenderer == null) {
            Config.warn("Model part not found: " + s + ", model: " + model);
            return false;
        } else {
            if (!customModelRenderer.isAttach()) {
                if (modelrenderer.cubeList != null) {
                    modelrenderer.cubeList.clear();
                }

                if (modelrenderer.spriteList != null) {
                    modelrenderer.spriteList.clear();
                }

                if (modelrenderer.childModels != null) {
                    ModelRenderer[] amodelrenderer = modelAdapter.getModelRenderers(model);
                    Set<ModelRenderer> set = Collections.<ModelRenderer>newSetFromMap(new IdentityHashMap());
                    set.addAll(Arrays.asList(amodelrenderer));
                    List<ModelRenderer> list = modelrenderer.childModels;
                    Iterator iterator = list.iterator();

                    while (iterator.hasNext()) {
                        ModelRenderer modelrenderer1 = (ModelRenderer) iterator.next();

                        if (!set.contains(modelrenderer1)) {
                            iterator.remove();
                        }
                    }
                }
            }

            modelrenderer.addChild(customModelRenderer.getModelRenderer());
            ModelUpdater modelupdater = customModelRenderer.getModelUpdater();

            if (modelupdater != null) {
                modelResolver.setThisModelRenderer(customModelRenderer.getModelRenderer());
                modelResolver.setPartModelRenderer(modelrenderer);

                if (!modelupdater.initialize(modelResolver)) {
                    return false;
                }

                customModelRenderer.getModelRenderer().setModelUpdater(modelupdater);
            }

            return true;
        }
    }

    private static void checkNull(Object obj, String msg) {
        if (obj == null) {
            throw new JsonParseException(msg);
        }
    }

    public static boolean isActive() {
        return active;
    }
}
