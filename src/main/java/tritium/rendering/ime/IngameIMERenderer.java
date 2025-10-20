package tritium.rendering.ime;

import ingameime.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjglx.input.Keyboard;
import org.lwjglx.opengl.Display;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;
import tritium.utils.logging.LogManager;
import tritium.utils.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/8/26 08:33
 */
public class IngameIMERenderer implements SharedRenderingConstants {

    private static final Logger LOG = LogManager.getLogger("IngameIME");

    public static InputContext InputCtx = null;
    static PreEditCallbackImpl preEditCallbackProxy = null;
    static CommitCallbackImpl commitCallbackProxy = null;
    static CandidateListCallbackImpl candidateListCallbackProxy = null;
    static InputModeCallbackImpl inputModeCallbackProxy = null;
    static PreEditCallback preEditCallback = null;
    static CommitCallback commitCallback = null;
    static CandidateListCallback candidateListCallback = null;
    static InputModeCallback inputModeCallback = null;

    static final class Candidate {
        public boolean selected = false;
        public String value;
    }

    static final List<Candidate> candidates = Collections.synchronizedList(new ArrayList<>());

    public static boolean canRender() {
        return IngameIMEJNI.supported && ClientSettings.IN_GAME_IME.getValue();
    }

    public static void draw(double posX, double posY, boolean scale) {

        if (!IngameIMEJNI.supported) {
            return;
        }

        if (scale) {
            GlStateManager.pushMatrix();

            GlStateManager.translate(0, 0, 1000);
            GlStateManager.scale(1 / RenderSystem.getScaleFactor(), 1 / RenderSystem.getScaleFactor(), 1);
        }

        try {
            if (candidates.isEmpty()) {
                if (scale) {
                    GlStateManager.popMatrix();

                }
                return;
            }
            CFontRenderer fr = FontManager.pf25;
            double offsetX = posX, offsetY;

            double width = 8;
            double height = 8 + fr.getHeight();

            if (posY + height * (scale ? RenderSystem.getScaleFactor() : 1) > RenderSystem.getHeight()) {
                offsetY = posY - height - 8;
            } else {
                offsetY = posY + 12 * (scale ? RenderSystem.getScaleFactor() : 1);
            }

            for (Candidate candidate : candidates) {
                width += fr.getStringWidth(candidate.value) + 4;
            }

            if (offsetX + width > RenderSystem.getWidth()) {
                if (width > RenderSystem.getWidth()) {
                    offsetX = 0;
                } else {
                    offsetX = RenderSystem.getWidth() - width;
                }
            }

            Rect.draw(offsetX, offsetY, width, height, ThemeManager.get(ThemeManager.ThemeColor.Surface));
            offsetX += 4;

            for (Candidate candidate : candidates) {

                if (candidate.selected) {
                    double offset = 2;
                    Rect.draw(offsetX - offset, offsetY + 4 - offset, fr.getStringWidth(candidate.value) + offset * 2 + 1, fr.getHeight() + offset * 2, RenderSystem.getOppositeColorHex(ThemeManager.get(ThemeManager.ThemeColor.OnSurface, 60)));
                }

                fr.drawString(candidate.value, offsetX, offsetY + 4, ThemeManager.get(ThemeManager.ThemeColor.Text));

                offsetX += fr.getStringWidth(candidate.value) + 4;
            }

        } catch (ConcurrentModificationException ignored) {

        }

        if (scale) {
            GlStateManager.popMatrix();

        }
    }

    private static long getWindowHandle_LWJGL3() {
        return GLFWNativeWin32.glfwGetWin32Window(Display.getWindow());
    }

    public static void createInputCtx() {

//        LOG.info("Using IngameIME-Native: {}", InputContext.getVersion());

        long hWnd = getWindowHandle_LWJGL3();
        if (hWnd != 0) {
            // Once switched to the full screen, we can't back to not UiLess mode, unless restart the game
            API api = /*Config.API_Windows.getString().equals("TextServiceFramework")*/API.Imm32;
//            LOG.info("Using API: {}, UiLess: {}", api, Config.UiLess_Windows.getBoolean());
            InputCtx = IngameIME.CreateInputContextWin32(hWnd, api, Minecraft.getMinecraft().isFullScreen());
//            LOG.info("InputContext has created!");
        } else {
            LOG.error("InputContext could not init as the hWnd is NULL!");
            return;
        }

        preEditCallbackProxy = new PreEditCallbackImpl() {
            @Override
            protected void call(CompositionState arg0, PreEditContext arg1) {
                try {
//                    LOG.info("PreEdit State: {}", arg0);

                    if (arg1 != null) {
//                        LOG.info("PreEdit Context: {}", arg1.getContent());

                    }

                    //Hide Indicator when PreEdit start
//                    if (arg0 == CompositionState.Begin) ClientProxy.Screen.WInputMode.setActive(false);

//                    if (arg1 != null) ClientProxy.Screen.PreEdit.setContent(arg1.getContent(), arg1.getSelStart());
//                    else ClientProxy.Screen.PreEdit.setContent(null, -1);
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        preEditCallback = new PreEditCallback(preEditCallbackProxy);
        commitCallbackProxy = new CommitCallbackImpl() {
            @Override
            protected void call(String arg0) {
                try {
//                    LOG.info("Commit: {}", arg0);
                    GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                    if (screen != null) {

                        // Normal Minecraft Guis
                        for (char c : arg0.toCharArray()) {
                            screen.handleKeyboardInput(c, Keyboard.KEY_NONE);
                        }
                    }
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        commitCallback = new CommitCallback(commitCallbackProxy);
        candidateListCallbackProxy = new CandidateListCallbackImpl() {
            @Override
            protected void call(CandidateListState arg0, CandidateListContext arg1) {
                try {
//                    LOG.info("candidateListCallbackProxy: {}", arg0);

                    candidates.clear();

                    if (arg1 != null) {
                        int selection = arg1.getSelection();
//                        LOG.info("selection: {}", selection);
                        ArrayList<String> strings = new ArrayList<>(arg1.getCandidates());
//                        for (String candidate : strings) {
//                            LOG.info("Candidate: {}", candidate);
//                        }
                        for (int i = 0; i < strings.size(); i++) {
                            String s = strings.get(i);
//                            System.out.println(s);
                            Candidate cd = new Candidate();

                            cd.value = (i + 1) + " " + s;

                            if (selection == i) {
                                cd.selected = true;
                            }

                            candidates.add(cd);
                        }
                    }
//                        ClientProxy.Screen.CandidateList.setContent(new ArrayList<>(arg1.getCandidates()), arg1.getSelection());
//                    else ClientProxy.Screen.CandidateList.setContent(null, -1);
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        candidateListCallback = new CandidateListCallback(candidateListCallbackProxy);
        inputModeCallbackProxy = new InputModeCallbackImpl() {
            @Override
            protected void call(InputMode arg0) {
                try {
//                    LOG.info("inputModeCallbackProxy");
//                    ClientProxy.Screen.WInputMode.setMode(arg0);
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        inputModeCallback = new InputModeCallback(inputModeCallbackProxy);

        InputCtx.setCallback(preEditCallback);
        InputCtx.setCallback(commitCallback);
        InputCtx.setCallback(candidateListCallback);
        InputCtx.setCallback(inputModeCallback);

        // Free unused native object
        System.gc();
    }

    public static void destroyInputCtx() {
        if (InputCtx != null) {
            InputCtx.delete();
            InputCtx = null;
//            LOG.info("InputContext has destroyed!");
        }
    }

    public static boolean getActivated() {
        if (InputCtx != null) return InputCtx.getActivated();
        else return false;
    }

    public static void setActivated(boolean activated) {
        if (InputCtx != null) {
            InputCtx.setActivated(activated);
//            LOG.info("IM active state: {}", activated);
        }
    }

}
