package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.Localizer;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.ColorSetting;
import tech.konata.phosphate.widget.Widget;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 10:04 AM
 */
public class ArrayList extends Widget {

    public BooleanSetting showRenderModules = new BooleanSetting("Show Render Category Modules", true);
    public BooleanSetting arrayListShadow = new BooleanSetting("ArrayListShadow", false);

    public ColorSetting arrayListColor = new ColorSetting("ArrayListColor", new HSBColor(255, 255, 255, 255));
    public BooleanSetting arrayListRect = new BooleanSetting("ArrayListRect", false);
    public BooleanSetting arrayListOutline = new BooleanSetting("ArrayListOutline", false);
    public ColorSetting arrayListOutlineColor = new ColorSetting("ArrayListOutlineColor", new HSBColor(255, 255, 255, 200), () -> arrayListOutline.getValue());
    Map<Module, ModuleNamePosition> renderMap = new HashMap<>();

    public ArrayList() {
        super("ArrayList");
    }

    @Override
    public void onEnable() {
        for (Map.Entry<Module, ModuleNamePosition> entry : renderMap.entrySet()) {
            ModuleNamePosition value = entry.getValue();
            value.y = 0;
            value.width = 0;
        }
    }

    @Override
    public void onRender(boolean editing) {

        double posX = this.getX(), posY = this.getY();
        CFontRenderer fontRenderer = Localizer.getLANG().isUnicode() ? FontManager.pf18 : FontManager.baloo18;
        List<Module> copy = ModuleManager.getModules();

        List<Module> modules = copy.stream().sorted(Comparator.comparingDouble(module -> getModuleNameWidth(fontRenderer, (Module) module)).reversed()).collect(Collectors.toList());
        double factor = 1.2;
        double fontHeight = (fontRenderer.getHeight() + 2) * factor + 3;

        double offsetY = posY;
        Iterator<Module> it = modules.iterator();
        while (it.hasNext()) {
            Module module = it.next();
            ModuleNamePosition position = this.getRenderValue(module);
            if (!module.isEnabled() && position.width <= 0.2 || (!this.showRenderModules.getValue() && module.getCategory() == Module.Category.RENDER) || !module.getShouldRender().get()) {
                it.remove();
            }
        }

        if (modules.isEmpty()) {
            this.setWidth(-100);
        } else {
            this.setWidth(-getModuleNameWidth(fontRenderer, modules.get(0)) - 4);
        }
        this.setHeight(modules.size() * fontHeight);

        NORMAL.add(() -> {
            GlStateManager.pushMatrix();
            this.doScale();
        });

        if (arrayListShadow.getValue()) {
            BLOOM.add(() -> {
                GlStateManager.pushMatrix();
                this.doScale();
            });
        }

        if (arrayListRect.getValue()) {
            BLUR.add(() -> {
                GlStateManager.pushMatrix();
                this.doScale();
            });
        }

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            ModuleNamePosition position = this.getRenderValue(module);

            String translate = module.getName().get();

            if (!module.getSuffix().isEmpty()) {
                translate += " " + EnumChatFormatting.GRAY + "[" + module.getSuffix() + "]";
            }

            double offsetX = fontRenderer.getStringWidth(translate) + 4;

            if (position.y == 0 && module.isEnabled()) {
                position.y = offsetY;
            }

            if (i != modules.size() - 1) {
                Module next = modules.get(i + 1);
                ModuleNamePosition nextValue = this.getRenderValue(next);

                if (nextValue.y - position.y > fontHeight * 0.75 || !module.isEnabled()) {
                    position.width = Interpolations.interpBezier(position.width, module.isEnabled() ? offsetX : 0, 0.2f);
                }
            } else {
                position.width = Interpolations.interpBezier(position.width, module.isEnabled() ? offsetX : 0, 0.2f);
            }

            if (i != 0) {
                Module prev = modules.get(i - 1);
                ModuleNamePosition prevValue = this.getRenderValue(prev);

                if ((prevValue.width == 0 || prev.isEnabled()) && !prevValue.waitingY) {
                    position.waitingY = false;
                    position.y = Interpolations.interpBezier(position.y, offsetY, 0.15f);
                } else {
                    position.waitingY = true;
                }
            } else {
                position.waitingY = false;
                position.y = Interpolations.interpBezier(position.y, offsetY, 0.15f);
            }


            if ((position.width <= 0.2 && !module.isEnabled()) || (!this.showRenderModules.getValue() && module.getCategory() == Module.Category.RENDER) || !module.getShouldRender().get()) {
                continue;
            }

            int alpha = (int) (255 * (position.width / offsetX));

            GlStateManager.color(1, 1, 1, 1);

            if (arrayListShadow.getValue()) {
                int finalI5 = i;
                BLOOM.add(() -> {
                    Rect.draw(posX - position.width, position.y, position.width, fontHeight, RenderSystem.reAlpha(arrayListColor.getRGB(finalI5), 0.6f), Rect.RectType.EXPAND);
                });
            }

            if (this.arrayListRect.getValue()) {

                BLUR.add(() -> {
                    Rect.draw(posX - position.width, position.y, position.width, fontHeight, this.hexColor(0, 0, 0), Rect.RectType.EXPAND);
                });


                NORMAL.add(() -> {
                    Rect.draw(posX - position.width, position.y, position.width, fontHeight, ThemeManager.get(ThemeManager.ThemeColor.Surface, 40), Rect.RectType.EXPAND);

                });
            }

            if (this.arrayListOutline.getValue()) {
                int finalI = i;
                NORMAL.add(() -> {
                    Rect.draw(posX - position.width, position.y, 1, fontHeight, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(finalI), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);
                });
            }


            if (i == 0) {
                if (this.arrayListOutline.getValue()) {
                    int finalI1 = i;
                    NORMAL.add(() -> {
                        Rect.draw(posX - position.width, position.y, position.width, 1, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(finalI1), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);
                    });
                }
            }

            if (i == modules.size() - 1) {
                if (this.arrayListOutline.getValue()) {
                    int finalI2 = i;
                    NORMAL.add(() -> {
                        Rect.draw(posX - position.width, position.y + fontHeight, position.width + 1, 1, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(finalI2), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);
                    });
                }
            } else {
                Module moduleNext = null;

                int idx = i + 1;

                while (idx < modules.size()) {
                    Module m = modules.get(idx);
                    if (m.isEnabled()) {
                        moduleNext = m;
                        break;
                    }

                    idx++;
                }

                ModuleNamePosition nextValue = this.getRenderValue(moduleNext);

                if (moduleNext != null && position.width > fontRenderer.getStringWidth(moduleNext.getName().get())) {
                    if (this.arrayListOutline.getValue()) {
                        int finalI3 = i;
                        NORMAL.add(() -> {
                            Rect.draw(posX - position.width, position.y + fontHeight, position.width - nextValue.width + 1, 1, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(finalI3), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);
                        });
                    }
                }
            }

            String finalTranslate = translate;
            int finalI4 = i;
            NORMAL.add(() -> {
//                System.out.println("!");
//                Rect.draw(100, 100, 100, 100, -1, Rect.RectType.EXPAND);
                fontRenderer.drawString(finalTranslate, posX - position.width + 2, position.y + fontHeight * 0.5 - fontRenderer.getHeight() * 0.5, RenderSystem.reAlpha(arrayListColor.getRGB(finalI4), Math.min(alpha, arrayListColor.getValue().getAlpha()) * 0.003921568627451F));

                if (arrayListRect.getValue()) {
                    Rect.draw(posX, position.y, 1, fontHeight, RenderSystem.reAlpha(arrayListColor.getRGB(finalI4), Math.min(alpha, arrayListColor.getValue().getAlpha()) * 0.003921568627451F), Rect.RectType.EXPAND);
                }
            });

            if (module.isEnabled())
                offsetY += fontHeight;
        }

        if (arrayListRect.getValue()) {
            BLUR.add(() -> {
                GlStateManager.popMatrix();
            });
        }

        if (arrayListShadow.getValue()) {
            BLOOM.add(() -> {
                GlStateManager.popMatrix();
            });
        }

        NORMAL.add(() -> GlStateManager.popMatrix());
    }

    private double getModuleNameWidth(CFontRenderer fontRenderer, Module module) {
        String fullName = module.getName().get();

        if (!module.getSuffix().isEmpty())
            fullName += " " + EnumChatFormatting.GRAY + "[" + module.getSuffix() + "]";

        return fontRenderer.getStringWidth(fullName);
    }

    ModuleNamePosition getRenderValue(Module module) {
        if (!renderMap.containsKey(module))
            renderMap.put(module, new ModuleNamePosition());

        return renderMap.get(module);
    }

    static class ModuleNamePosition {

        double y = 0;
        double width;
        boolean waitingY = false;
    }


}
