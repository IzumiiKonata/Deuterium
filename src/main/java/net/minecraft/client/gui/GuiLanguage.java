package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Location;
import net.optifine.Lang;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GuiLanguage extends GuiScreen {
    /**
     * The parent Gui screen
     */
    protected GuiScreen parentScreen;

    /**
     * The List GuiSlot object reference.
     */
    private GuiLanguage.List list;

    /**
     * Reference to the GameSettings object.
     */
    private final GameSettings game_settings_3;

    /**
     * Reference to the LanguageManager object.
     */
    private final LanguageManager languageManager;

    /**
     * A button which allows the user to determine if the Unicode font should be forced.
     */
    private GuiOptionButton forceUnicodeFontBtn;

    /**
     * The button to confirm the current settings.
     */
    private GuiOptionButton confirmSettingsBtn;

    private final Language prevLanguage;
    private String options_language, language_warning;

    public GuiLanguage(GuiScreen screen, GameSettings gameSettingsObj, LanguageManager manager) {
        this.parentScreen = screen;
        this.game_settings_3 = gameSettingsObj;
        this.languageManager = manager;
        this.prevLanguage = languageManager.getCurrentLanguage();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.buttonList.add(this.forceUnicodeFontBtn = new GuiOptionButton(100, this.width / 2 - 155, this.height - 38, GameSettings.Options.FORCE_UNICODE_FONT, this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
        this.buttonList.add(this.confirmSettingsBtn = new GuiOptionButton(6, this.width / 2 - 155 + 160, this.height - 38, I18n.format("gui.done")));
        this.list = new GuiLanguage.List(this.mc);
        this.list.registerScrollButtons(7, 8);

        this.options_language = this.getLanguageTranslate("options.language");
        this.language_warning = this.getLanguageTranslate("options.languageWarning");
    }

    @Override
    public void onGuiClosed() {
        Lang.resourcesReloaded();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput(int button, boolean pressed) throws IOException {
        super.handleMouseInput(button, pressed);
        this.list.handleMouseInput(button, pressed);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.list.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleDWheel(int wheel) {
        this.list.handleDWheel(wheel);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            if (this.languageManager.getCurrentLanguage() != prevLanguage) {
                GuiLanguage.this.languageManager.setCurrentLanguage(prevLanguage);
                GuiLanguage.this.game_settings_3.language = prevLanguage.getLanguageCode();
                GuiLanguage.this.game_settings_3.saveOptions();
            }
            Lang.resourcesReloaded();

            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            switch (button.id) {
                case 5:
                    break;

                case 6:

                    if (this.languageManager.getCurrentLanguage() != prevLanguage) {
                        GuiLanguage.this.game_settings_3.language = this.languageManager.getCurrentLanguage().getLanguageCode();
                        this.mc.getLanguageManager().onResourceManagerReload(this.mc.getResourceManager());
                        GuiLanguage.this.game_settings_3.saveOptions();
                        Lang.resourcesReloaded();
                    }

                    this.mc.displayGuiScreen(this.parentScreen);
                    break;

                case 100:
                    if (button instanceof GuiOptionButton) {
                        this.game_settings_3.setOptionValue(((GuiOptionButton) button).returnEnumOptions(), 1);
                        button.displayString = this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
                        ScaledResolution scaledresolution = ScaledResolution.get();
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        this.setWorldAndResolution(this.mc, i, j);
                        mc.fontRendererObj.stringWidthMap.clear();
                    }

                    break;

                default:
                    this.list.actionPerformed(button);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.options_language, this.width / 2, 16, 16777215);
        this.drawCenteredString(this.fontRendererObj, "(" + this.language_warning + ")", this.width / 2, this.height - 56, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot {
        private final java.util.List<String> langCodeList = Lists.newArrayList();
        private final Map<String, Language> languageMap = Maps.newHashMap();

        public List(Minecraft mcIn) {
            super(mcIn, GuiLanguage.this.width, GuiLanguage.this.height, 32, GuiLanguage.this.height - 65 + 4, 18);

            for (Language language : GuiLanguage.this.languageManager.getLanguages()) {
                this.languageMap.put(language.getLanguageCode(), language);
                this.langCodeList.add(language.getLanguageCode());
            }
        }

        protected int getSize() {
            return this.langCodeList.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            Language language = this.languageMap.get(this.langCodeList.get(slotIndex));

            String translate = getLanguageTranslate("gui.done");
            String translate2 = getLanguageTranslate("options.forceUnicodeFont");
            String on = getLanguageTranslate("options.on");
            String off = getLanguageTranslate("options.off");
            boolean flag = game_settings_3.getOptionOrdinalValue(GameSettings.Options.FORCE_UNICODE_FONT);

            GuiLanguage.this.languageManager.setCurrentLanguage(language);
            GuiLanguage.this.fontRendererObj.setUnicodeFlag(GuiLanguage.this.languageManager.isCurrentLocaleUnicode() || GuiLanguage.this.game_settings_3.forceUnicodeFont);
            GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.isCurrentLanguageBidirectional());
            GuiLanguage.this.confirmSettingsBtn.displayString = translate;
            GuiLanguage.this.forceUnicodeFontBtn.displayString = translate2 + ": " + (flag ? on : off);
            GuiLanguage.this.options_language = GuiLanguage.this.getLanguageTranslate("options.language");
            GuiLanguage.this.language_warning = GuiLanguage.this.getLanguageTranslate("options.languageWarning");
            LanguageManager.currentLocale.unicode = false;

            Lang.resourcesReloaded();
        }

        protected boolean isSelected(int slotIndex) {
            return this.langCodeList.get(slotIndex).equals(GuiLanguage.this.languageManager.getCurrentLanguage().getLanguageCode());
        }

        protected int getContentHeight() {
            return this.getSize() * 18;
        }

        protected void drawBackground() {
            GuiLanguage.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            GuiLanguage.this.fontRendererObj.setBidiFlag(true);
            GuiLanguage.this.drawCenteredString(GuiLanguage.this.fontRendererObj, this.languageMap.get(this.langCodeList.get(entryID)).toString(), this.width / 2, p_180791_3_ + 3, 16777215);
            GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.getCurrentLanguage().isBidirectional());
        }
    }

    private String getLanguageTranslate(String left) {
        Language language = this.languageManager.getCurrentLanguage();
        String langFile = String.format("lang/%s.lang", language.getLanguageCode());
        IResourceManager resourceManager = mc.getResourceManager();
        Splitter splitter = Splitter.on('=').limit(2);

        String translate = "?";

        for (String domain : resourceManager.getResourceDomains()) {
            try {
                java.util.List<IResource> resourceList = resourceManager.getAllResources(Location.of(domain, langFile));

                for (IResource iresource : resourceList) {
                    InputStream inputstream = iresource.getInputStream();

                    try {
                        for (String s : IOUtils.readLines(inputstream, StandardCharsets.UTF_8)) {
                            if (!s.isEmpty() && s.charAt(0) != 35) {
                                String[] astring = Iterables.toArray(splitter.split(s), String.class);

                                if (astring != null && astring.length == 2 && astring[0].equals(left)) {
                                    translate = astring[1];
                                }
                            }
                        }
                    } finally {
                        IOUtils.closeQuietly(inputstream);
                    }
                }
            } catch (IOException var9) {
            }
        }

        return translate;
    }
}
