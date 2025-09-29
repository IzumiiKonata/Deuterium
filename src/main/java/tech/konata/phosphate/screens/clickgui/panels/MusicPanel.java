package tech.konata.phosphate.screens.clickgui.panels;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.ncm.OptionsUtil;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.utils.music.dto.PlayList;
import tech.konata.phosphate.utils.music.dto.User;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.rendering.entities.clickable.impl.ClickableIcon;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.rendering.entities.impl.TextField;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.texture.Textures;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.ModuleSettings;
import tech.konata.phosphate.screens.clickgui.Panel;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextEntity;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextMenu;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.entities.ContextLabel;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.entities.SecondaryMenu;
import tech.konata.phosphate.screens.dialog.impl.music.DialogConfirmLogout;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.utils.music.lyric.LyricLine;
import tech.konata.phosphate.utils.network.HttpUtils;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.network.HttpClient;
import tech.konata.phosphate.utils.timing.Timer;
import tech.konata.phosphate.widget.impl.MusicLyrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.*;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 9:32 AM
 */
public class MusicPanel extends Panel implements SharedRenderingConstants {

    public Map<Music, MusicEntity> map = new HashMap<>();
    public TextField searchBox = new TextField(0, 0, 0, 0, 0);
    public LoginRenderer loginRenderer = null;

    public static User profile;
    public static Location AVATAR_LOCATION = Location.of(Phosphate.NAME + "/textures/CloudMusicAvatar.png");
    boolean avatarLoaded = false;
    public static List<PlayList> playLists;
    public static List<Long> likeList;
    public static PlayList selectedList;
    double yScrollPlayList = 0, yScrollSmoothPlayList = 0;
    String lastSearch = "";
    float hoverInfoAlpha = 0;

    public boolean lmbPressed = false, rmbPressed = false;

    Localizable lPlayLists = Localizable.of("panel.music.playlists");
    Localizable lCreatedBy = Localizable.of("panel.music.createdby");
    Localizable lSearch = Localizable.of("panel.music.search");
    Localizable lPlayAll = Localizable.of("panel.music.playall");
    Localizable lRandom = Localizable.of("panel.music.random");
    Localizable lNotPlaying = Localizable.of("panel.music.notplaying");

    public MusicPanel() {
        super("Music");
    }

    @Override
    public void init() {
        searchBox.setPlaceholder("Search (Ctrl + F)");
        searchBox.setDrawLineUnder(false);
    }

    @Override
    public void onSwitchedTo() {
        musicNameLabel.reset();
        musicArtistLabel.reset();
    }

    double progressBarPerc = 0;

    public Timer clickResistTimer = new Timer();

    private boolean canClick() {

        if (this.rightClickMenu != null)
            return false;

        if (ClickGui.getInstance().getDialog() != null)
            return false;

        if (!clickResistTimer.isDelayed(500))
            return false;

        return ClickGui.getInstance().settingsRenderer == null;
    }

    private boolean canRightClick() {

        return ClickGui.getInstance().getDialog() == null;
    }

    public double smoothSelectorY = 0;

    @Override
    public void draw(double mouseX, double mouseY, int dWheel) {

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        if (!Mouse.isButtonDown(1) && rmbPressed)
            rmbPressed = false;

        CFontRenderer unicodeRenderer = FontManager.pf40;
        FontManager.pf25bold.drawString(this.getName().get(), posX + 2, posY + 4, ThemeManager.get(ThemeManager.ThemeColor.Text));

        if (RenderSystem.isHovered(mouseX, mouseY, posX, posY, unicodeRenderer.getStringWidth(this.getName().get()), unicodeRenderer.getHeight()) && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {
            lmbPressed = true;

            Phosphate.getInstance().getConfigManager().refreshNCM();
        }

        searchBox.xPosition = (float) (posX + 20 + FontManager.pf25bold.getStringWidth(this.getName().get()));
        searchBox.yPosition = (float) (posY + 6);
        if (searchBox.isFocused())
            searchBox.onTick();
        searchBox.width = 100;
        searchBox.height = 10;
        searchBox.setTextColor(ThemeManager.get(ThemeManager.ThemeColor.Text));
        searchBox.setDisabledTextColour(Color.GRAY.getRGB());

        this.roundedRect(searchBox.xPosition - 4, searchBox.yPosition - 5, searchBox.width + 8, searchBox.height + 8, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        searchBox.drawTextBox((int) mouseX - 10, (int) mouseY);

        boolean loggedIn = !OptionsUtil.getCookie().isEmpty();

        if (!loggedIn && this.loginRenderer == null) {
            this.loginRenderer = new LoginRenderer();
        }

        if (this.loginRenderer != null) {
            this.doRenderLoginRenderer(mouseX, mouseY, posX, posY, width, height);
        }

        if (!loggedIn || this.loginRenderer != null || profile == null)
            return;

        CFontRenderer shs18 = FontManager.pf18;

        String nickname = profile.getName();
        double nickNameX = posX + width - shs18.getStringWidth(nickname) - 10;

        double avatarWidth = 16, avatarHeight = 16, avatarX = nickNameX - 4 - avatarWidth, avatarY = posY + 10;

        shs18.drawString(nickname, nickNameX, avatarY + avatarHeight * 0.5 - shs18.getHeight() / 2.0, ThemeManager.get(ThemeManager.ThemeColor.Text));

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        ITextureObject avatarTexture = textureManager.getTexture(AVATAR_LOCATION);

        if (avatarTexture != null) {
            GlStateManager.bindTexture(avatarTexture.getGlTextureId());
            roundedRectTextured(avatarX, avatarY, avatarWidth, avatarHeight, 4);
        } else {
            loadAvatarIfNeeded();
            roundedRect(avatarX, avatarY, avatarWidth, avatarHeight, 4, Color.GRAY);
        }

        if (isHovered(mouseX, mouseY, avatarX - 2, avatarY - 2, avatarWidth + 8 + shs18.getStringWidth(nickname), avatarHeight + 2) && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {
            lmbPressed = true;

            ClickGui.getInstance().setDialog(new DialogConfirmLogout());
        }

        buttonSettings.setSmallFr(true);
        buttonSettings.setX(avatarX - 28);
        buttonSettings.setY(avatarY - 2);
        buttonSettings.setWidth(20);
        buttonSettings.setHeight(20);
        buttonSettings.draw(mouseX, mouseY);

        double bottomBarHeight = 56;

        double offsetY = posY + 41;

        posX += 2;
        width -= 1.5;

        if (playLists != null) {

            FontManager.pf16.drawString(lPlayLists.get(), posX, posY + 30, ThemeManager.get(ThemeManager.ThemeColor.Text));

            roundedRect(posX, offsetY, 108, height - bottomBarHeight - 48, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

            Stencil.write();
//            Rect.draw(0, 0, 1920, 1080, -1, Rect.RectType.EXPAND);
            Rect.draw(posX, offsetY + 4, 108, height - bottomBarHeight - 55, -1, Rect.RectType.EXPAND);
            Stencil.erase();

            double listOffsetX = posX + 4;
            double listOffsetY = offsetY + 4;
            double listWidth = 100, listHeight = 26;
            double listSpacing = 4.7;

            double yAdd = 5;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                yAdd *= 2;

            if (RenderSystem.isHovered(mouseX, mouseY, posX, offsetY, 108, height - bottomBarHeight - 48) && dWheel != 0) {
                if (dWheel > 0)
                    yScrollSmoothPlayList -= yAdd;
                else
                    yScrollSmoothPlayList += yAdd;
            }

            yScrollSmoothPlayList = Interpolations.interpBezier(yScrollSmoothPlayList, 0, 0.1f);
            double delta = yScrollPlayList;
            yScrollPlayList = Interpolations.interpBezier(yScrollPlayList, yScrollPlayList + yScrollSmoothPlayList, 1f);

            if (yScrollPlayList < 0)
                yScrollPlayList = Interpolations.interpBezier(yScrollPlayList, 0, 0.2f);

            double totalHeight = (listHeight + listSpacing) * (playLists.size());

            double target = totalHeight - (height - bottomBarHeight - 48) + listSpacing;

            if (totalHeight > height - bottomBarHeight - 48) {

                if (yScrollPlayList > target) {
                    yScrollPlayList = Interpolations.interpBezier(yScrollPlayList, target, 0.2f);
                }
            } else {
                yScrollPlayList = Interpolations.interpBezier(yScrollPlayList, 0, 0.2f);
            }

            delta = yScrollPlayList - delta;
            smoothSelectorY -= delta;

            listOffsetY -= yScrollPlayList;

            int currentListIndex = playLists.indexOf(selectedList);

            if (smoothSelectorY == 0)
                smoothSelectorY = listOffsetY + currentListIndex * (listHeight + listSpacing);

            smoothSelectorY = Interpolations.interpBezier(smoothSelectorY, listOffsetY + currentListIndex * (listHeight + listSpacing), 0.4f);

            if (currentListIndex != -1) {
                this.roundedRectAccentColor(listOffsetX, smoothSelectorY, listWidth, listHeight, 4, 160);
            }

            for (PlayList list : playLists) {

                if (listOffsetY + listHeight < posY + 47) {
                    listOffsetY += listHeight + listSpacing;
                    continue;
                }

                if (listOffsetY > posY + 40 + height - bottomBarHeight - 48) {
                    break;
                }

                PlayList.RenderValues renderValues = list.getRenderValues();

                boolean hovered = RenderSystem.isHovered(mouseX, mouseY, listOffsetX, listOffsetY, listWidth, listHeight) && RenderSystem.isHovered(mouseX, mouseY, posX, offsetY, 108, height - bottomBarHeight - 48);

                if (selectedList != list && hovered && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {
                    lmbPressed = true;
                    selectedList = list;
                    Location coverForList = getPlaylistCoverLocation(selectedList);

                    ITextureObject texture = textureManager.getTexture(coverForList);

                    if (texture == null || texture == TextureUtil.missingTexture) {
                        MultiThreadingUtil.runAsync(new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {
                                BufferedImage img = NativeBackedImage.make(HttpClient.downloadStream(selectedList.coverUrl + "?param=128y128"));
                                Textures.loadTextureAsyncly(coverForList, img);
                            }
                        });
                    }
                }

                if (hovered && selectedList != list) {
                    renderValues.hoveredAlpha = Interpolations.interpBezier(renderValues.hoveredAlpha, 60 * RenderSystem.DIVIDE_BY_255, 0.2f);
                } else {
                    renderValues.hoveredAlpha = Interpolations.interpBezier(renderValues.hoveredAlpha, 0, 0.3f);
                }

                roundedRect(listOffsetX, listOffsetY, listWidth, listHeight, 4, ThemeManager.getAsColor(ThemeManager.ThemeColor.Text, (int) (renderValues.hoveredAlpha * 255)));

                Location coverForList = getPlaylistCoverLocationSmall(list);

                ITextureObject texture = textureManager.getTexture(coverForList);

                if (texture != null && texture != TextureUtil.missingTexture) {
                    GlStateManager.bindTexture(texture.getGlTextureId());
                    roundedRectTextured(listOffsetX + 3, listOffsetY + 3, 20, 20, 3);
                } else {
                    if (!list.coverLoadedSmall) {
                        list.coverLoadedSmall = true;
                        MultiThreadingUtil.runAsync(new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {
                                BufferedImage img = NativeBackedImage.make(HttpUtils.get(list.coverUrl + "?param=40y40", null));
                                Textures.loadTextureAsyncly(coverForList, img);
                            }
                        });
                    }
                }

                String name = list.name;

                if (FontManager.pf16bold.getStringWidth(name) > listWidth - 36) {
                    int idx = name.length() - 1;
                    while (true) {
                        String substring = name.substring(0, idx);

                        if (FontManager.pf16bold.getStringWidth(substring + "...") <= listWidth - 36) {
                            name = substring + "...";
                            break;
                        }

                        idx--;
                    }
                }

                FontManager.pf16bold.drawString(name, listOffsetX + 28, listOffsetY + listHeight * 0.5 - FontManager.pf16bold.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

                listOffsetY += listHeight + listSpacing;
            }

            Stencil.dispose();


        }

        Music hovering = null;

        if (selectedList != null) {

            double selectedX = posX + 112, selectedY = offsetY, selectedWidth = width - 114, selectedHeight = height - bottomBarHeight - 48;
            roundedRect(selectedX, selectedY, selectedWidth, selectedHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

            double coverSize = 64;
            double coverX = selectedX + 4, coverY = selectedY + 4;
            double coverInfoX = coverX + coverSize + 4;


            if (!selectedList.searchMode) {
                Location coverForList = getPlaylistCoverLocation(selectedList);


                if (textureManager.getTexture(coverForList) != null) {

                    textureManager.bindTexture(coverForList);
//                    RenderSystem.linearFilter();
//                    RenderSystem.setBlurMipmapDirect(true, true);
                    roundedRectTextured(coverX, coverY, coverSize, coverSize, 6);

//                    TexturedShadow.drawShadow(coverX, coverY, coverSize, coverSize, 1, 10);
//                    tech.konata.phosphate.rendering.entities.impl.Image.draw(coverForList, coverX, coverY, coverSize, coverSize, tech.konata.phosphate.rendering.entities.impl.Image.Type.Normal);
                } else {
                    if (!selectedList.coverLoaded) {
                        selectedList.coverLoaded = true;
                        MultiThreadingUtil.runAsync(new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {
                                BufferedImage img = NativeBackedImage.make(HttpUtils.get(selectedList.coverUrl + "?param=128y128", null));
                                Textures.loadTextureAsyncly(coverForList, img);
                            }
                        });
                    }
//                    Rect.draw(coverX, coverY, coverSize, coverSize, Color.GRAY.getRGB(), Rect.RectType.EXPAND);
                    roundedRect(coverX, coverY, coverSize, coverSize, 6, Color.GRAY);
                }

                FontManager.pf25.drawString(selectedList.name, coverInfoX, coverY, ThemeManager.get(ThemeManager.ThemeColor.Text));
                FontManager.pf18.drawString(lCreatedBy.get() + selectedList.creator.name, coverInfoX, coverY + FontManager.pf25.getHeight() + 4, Color.GRAY.getRGB());

                FontManager.icon18.drawString("g", coverInfoX, coverY + FontManager.pf25.getHeight() + FontManager.pf18.getHeight() + 10, Color.GRAY.getRGB());

                FontManager.pf18.drawString(String.valueOf(selectedList.getMusics().size()), coverInfoX + 14, coverY + FontManager.pf25.getHeight() + FontManager.pf18.getHeight() + 10, Color.GRAY.getRGB());

                FontManager.icon18.drawString("k", coverInfoX + 16 + FontManager.pf18.getStringWidth(String.valueOf(selectedList.getMusics().size())), coverY + FontManager.pf25.getHeight() + FontManager.pf18.getHeight() + 10, Color.GRAY.getRGB());
                FontManager.pf18.drawString(String.valueOf(selectedList.playCount), coverInfoX + 27 + FontManager.pf18.getStringWidth(String.valueOf(selectedList.getMusics().size())), coverY + FontManager.pf25.getHeight() + FontManager.pf18.getHeight() + 10, Color.GRAY.getRGB());

                if (selectedList.description != null) {
                    FontManager.pf18.drawString(String.join(" ", selectedList.description), coverInfoX + 32 + FontManager.pf18.getStringWidth(String.valueOf(selectedList.getMusics().size()) + selectedList.playCount), coverY + FontManager.pf25.getHeight() + FontManager.pf18.getHeight() + 10, Color.GRAY.getRGB());
                }

            } else {
                FontManager.pf25.drawString(lSearch.get() + ": " + lastSearch, coverX, coverY, ThemeManager.get(ThemeManager.ThemeColor.Text));
            }


            List<Music> musics = selectedList.getMusics();

            if (selectedList.searchMode)
                coverInfoX = coverX + 12;

            double buttonHeight = 16;

            buttonPlayAll.setX(coverInfoX + 4);
            buttonPlayAll.setY(coverY + coverSize - buttonHeight - 6);
            buttonPlayAll.setWidth(20);
            buttonPlayAll.setHeight(20);
            buttonPlayAll.draw(mouseX, mouseY);

//            roundedRect(coverInfoX, coverY + coverSize - buttonHeight, FontManager.pf18.getStringWidth(lPlayAll.get()) + 8, buttonHeight, 5, new Color(0, 0, 0, 100));
//            FontManager.pf18.drawString(lPlayAll.get(), coverInfoX + 4, coverY + coverSize - buttonHeight / 2.0 - FontManager.pf18.getHeight() / 2.0, -1);
//
//            if (RenderSystem.isHovered(mouseX, mouseY, coverInfoX, coverY + coverSize - buttonHeight, FontManager.pf18.getStringWidth(lPlayAll.get()) + 8, buttonHeight) && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {
//                lmbPressed = true;
//                CloudMusic.play(musics);
//            }
//
//            roundedRect(coverInfoX + FontManager.pf18.getStringWidth(lPlayAll.get()) + 8 + 6, coverY + coverSize - buttonHeight, FontManager.pf18.getStringWidth(lRandom.get()) + 8, buttonHeight, 5, new Color(0, 0, 0, 100));
//            FontManager.pf18.drawString(lRandom.get(), coverInfoX + FontManager.pf18.getStringWidth(lPlayAll.get()) + 8 + 10, coverY + coverSize - buttonHeight / 2.0 - FontManager.pf18.getHeight() / 2.0, -1);
//
//            if (RenderSystem.isHovered(mouseX, mouseY, coverInfoX + FontManager.pf18.getStringWidth(lPlayAll.get()) + 8 + 6, coverY + coverSize - buttonHeight, FontManager.pf18.getStringWidth(lRandom.get()) + 8, buttonHeight) && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {
//                lmbPressed = true;
//                List<Music> copy = new ArrayList<>(musics);
//                Collections.shuffle(copy);
//                CloudMusic.play(copy);
//            }

            double xSpacing = 16, ySpacing = 30;
            double panelX = coverX + 5;
            double panelY = coverY + coverSize + 4;
            double panelWidth = selectedWidth - 8;
            double panelHeight = selectedHeight - coverSize - 12;
            double pX = panelX + xSpacing, pY = panelY + xSpacing + selectedList.scrollOffset;
            double eWidth = 80, eHeight = 80;
            int lengthHorizontal = (int) ((panelWidth - xSpacing) / (eWidth + xSpacing));

            Stencil.write();
            Rect.draw(coverX, coverY + coverSize + 4, selectedWidth - 8, selectedHeight - coverSize - 12, -1, Rect.RectType.EXPAND);
            Stencil.erase();

            if (RenderSystem.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight)) {

                double scroll = 15;

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    scroll *= 2;

                if (dWheel != 0) {
                    if (dWheel < 0) {
                        selectedList.scrollSmooth = scroll;
                    } else {
                        selectedList.scrollSmooth = -scroll;
                    }
                }

            }

            if (selectedList.scrollSmooth != 0) {
                selectedList.scrollSmooth = Interpolations.interpBezier(selectedList.scrollSmooth, 0, 0.1f);
                selectedList.scrollOffset = Interpolations.interpBezier(selectedList.scrollOffset, selectedList.scrollOffset - selectedList.scrollSmooth, 1.5f);
            }

            int i = musics.size() / lengthHorizontal;

            if (musics.size() % lengthHorizontal != 0)
                i += 1;

            double val = -(i - 2) * (ySpacing + eHeight) + (ClickGui.getInstance().height - 420);

            if (selectedList.scrollOffset > 0) {
                selectedList.scrollOffset = Interpolations.interpBezier(selectedList.scrollOffset, 0, 0.15f);
            } else if (selectedList.scrollOffset < val) {
                selectedList.scrollOffset = (Interpolations.interpBezier(selectedList.scrollOffset, val, 0.4f));
            }

            int count = 0;
            for (Music music : musics) {
                MusicEntity entity = this.getEntity(music);

                if (count == lengthHorizontal) {
                    count = 0;
                    pX = panelX + xSpacing;

                    pY += ySpacing + eHeight;
                }

                if (pY < coverY + coverSize + 4 - eHeight - ySpacing) {
                    pX += xSpacing + eWidth;
                    ++count;
                    continue;
                }
                if (pY > coverY + coverSize + 4 + selectedHeight - coverSize - 12) break;

                if (!entity.textureLoaded) {
                    entity.textureLoaded = true;

                    CloudMusic.loadMusicCover(music, true);
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(pX + eWidth / 2.0, pY + eHeight / 2.0, 0);
                GlStateManager.scale(entity.scale, entity.scale, 0);
                GlStateManager.translate(-(pX + eWidth / 2.0), -(pY + eHeight / 2.0), 0);

                if (textureManager.getTexture(this.getMusicCover(music)) != null) {

                    textureManager.bindTexture(this.getMusicCover(music));
                    RenderSystem.setBlurMipmapDirect(true, true);

                    roundedRectTextured(pX, pY, eWidth, eHeight, 6);
                } else {
                    roundedRect(pX, pY, eWidth, eHeight, 6, Color.GRAY);
                }

                if (entity.hoverAlpha > 0.02f)
                    roundedRect(pX, pY, eWidth, eHeight, 6, new Color(1, 1, 1, entity.hoverAlpha));

                GlStateManager.popMatrix();

                if (RenderSystem.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight) && RenderSystem.isHovered(mouseX, mouseY, pX, pY, eWidth, eHeight)) {
                    hovering = music;

                    if (!music.prevHover) {
                        music.prevHover = true;
                    }

                    if (this.rightClickMenu == null) {
                        if (Mouse.isButtonDown(0) && this.canClick()) {
                            entity.mousePressed = true;
                            entity.scale = Interpolations.interpBezier(entity.scale, 0.9, 0.2f);
                        } else {
                            if (entity.mousePressed) {
                                entity.mousePressed = false;

                                if (selectedList.searchMode)
                                    CloudMusic.playedFrom = null;
                                else
                                    CloudMusic.playedFrom = selectedList;

                                CloudMusic.play(musics, musics.indexOf(music));
                            }

                            entity.scale = Interpolations.interpBezier(entity.scale, 1.1, 0.2f);
                            entity.hoverAlpha = Interpolations.interpBezier(entity.hoverAlpha, 0.2f, 0.2f);
                        }
                    }

                    if (Mouse.isButtonDown(1) && this.canRightClick() && !rmbPressed) {
                        rmbPressed = true;
                        this.rightClickMenu = this.buildMenu(mouseX, mouseY, music, selectedList);
                    }

                } else {

                    if (music.prevHover)
                        music.prevHover = false;

                    music.dynamicCoverHoverAlpha = Interpolations.interpBezier(music.dynamicCoverHoverAlpha, 0f, 0.2f);

                    entity.scale = Interpolations.interpBezier(entity.scale, 1, 0.2f);
                    entity.hoverAlpha = Interpolations.interpBezier(entity.hoverAlpha, 0f, 0.2f);

                    if (entity.mousePressed) {
                        entity.mousePressed = false;
                    }
                }


                CFontRenderer nameRenderer = FontManager.pf16bold;

                String[] strings = nameRenderer.fitWidth(music.getName() + (music.getTranslatedName() == null ? "" : "\n" + EnumChatFormatting.GRAY + "(" + music.getTranslatedName() + ")"), eWidth);

                double yOffset = pY + eHeight + 2 + 4 * ((entity.scale - 1) / 0.1);

                for (String string : strings) {
                    nameRenderer.drawCenteredString(string, pX + eWidth / 2.0, yOffset, ThemeManager.get(ThemeManager.ThemeColor.Text));
                    yOffset += nameRenderer.getHeight() + 2;
                }

                pX += xSpacing + eWidth;
                ++count;
            }

            Stencil.dispose();

        }

        String musicName = lNotPlaying.get();
        String artistName = "";
        String currentTime = "00:00";
        String totalTime = "00:00";

        double progressLength = width - 2;
        double progressBarLeft = posX;

        Minecraft mc = Minecraft.getMinecraft();

        bottomBarHeight += 2;
        double shrink = 2;

        roundedRect(posX, posY + height - bottomBarHeight, width - 2, bottomBarHeight - shrink, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        Stencil.write();
        Rect.draw(posX - 1, posY + height - bottomBarHeight - 1, width, 5, -1, Rect.RectType.EXPAND);
        Stencil.erase();
        roundedRect(posX, posY + height - bottomBarHeight, width - 2, bottomBarHeight - shrink, 6, new Color(255, 255, 255, 160));

        Stencil.dispose();

        double coverSize = bottomBarHeight - 13;
        double coverY = posY + height - bottomBarHeight + 4;

        Music playing = CloudMusic.currentlyPlaying;

        if (playing != null) {
            musicName = playing.getName();
            artistName = playing.getArtistsName();

            Location cover = this.getMusicCover(playing);


            if (mc.getTextureManager().getTexture(cover) != null) {

                mc.getTextureManager().bindTexture(cover);
                RenderSystem.linearFilter();
                roundedRectTextured(posX + 4, coverY + 3, coverSize, coverSize, 6);

//                TexturedShadow.drawShadow(posX + 4, coverY, coverSize, coverSize, 1, 10);
//                tech.konata.phosphate.rendering.entities.impl.Image.draw(cover, posX + 4, coverY, coverSize, coverSize, tech.konata.phosphate.rendering.entities.impl.Image.Type.Normal);
            } else {
                roundedRect(posX + 4, coverY + 3, coverSize, coverSize, 6, Color.GRAY);
            }

            if (CloudMusic.player != null && CloudMusic.player.player != null) {

                progressBarPerc = Interpolations.interpBezier(progressBarPerc, progressLength * ((double) CloudMusic.player.getCurrentTimeMillis() / (CloudMusic.player.getTotalTimeMillis() + 0.01)), 0.4f);

                Stencil.write();
                Rect.draw(posX - 1, posY + height - bottomBarHeight - 1, progressBarPerc, 5, -1, Rect.RectType.EXPAND);
                Stencil.erase();
                this.roundedRectAccentColor(posX, posY + height - bottomBarHeight, width - 2, bottomBarHeight - shrink, 6);

                Stencil.dispose();

//                Stencil.write();
//                roundedRect(posX, posY + height - bottomBarHeight, width - 2, bottomBarHeight, 3, ThemeManager.getAsColor(ThemeManager.ThemeColor.BaseLighter));
//                Stencil.erase(true);
//
//                Rect.draw(progressBarLeft, posY + height - bottomBarHeight, progressBarPerc, 4, RenderSystem.LOL_WHY, Rect.RectType.EXPAND);
//
//                Stencil.dispose();


                boolean hovered = RenderSystem.isHovered(mouseX, mouseY, progressBarLeft, posY + height - bottomBarHeight, progressLength, 6);

                if (hovered) {
                    double mouseDelta = mouseX - (progressBarLeft);
                    double perc = mouseDelta / progressLength;
                    perc = Math.min(Math.max(0, perc), 1);

                    long songProgress = (long) (perc * (CloudMusic.player.getTotalTimeMillis()));

                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

//                    {
//                        String timeStamp = sdf.format(new Date(songProgress));
//
//                        double lW = FontManager.pf18.getStringWidth(timeStamp) + 8;
//                        double lH = FontManager.pf18.getHeight() + 8;
//
//                        double lX = Math.max(0, Math.min(RenderSystem.getWidth() - lW, mouseX - lW * 0.5));
//                        double lY = posY + height - bottomBarHeight + 8;
//
//                        roundedRect(lX, lY, lW, lH, 4, new Color(0, 0, 0, 180));
//
//                        FontManager.pf18.drawString(timeStamp, lX + 4, lY + 4, -1);
//                    }

                    List<LyricLine> allLyrics = MusicLyrics.allLyrics;

                    if (!allLyrics.isEmpty()) {
                        LyricLine current = allLyrics.get(0);
                        LyricLine next = null;

                        for (int i = 0; i < allLyrics.size(); i++) {

                            LyricLine lyric = allLyrics.get(i);

                            if (lyric.getTimeStamp() <= songProgress) {
                                current = lyric;

                                if (i + 1 < allLyrics.size()) {
                                    next = allLyrics.get(i + 1);
                                }
                            }
                        }

                        if (current == allLyrics.get(0)) {
                            if (1 < allLyrics.size()) {
                                next = allLyrics.get(1);
                            }
                        }

                        String secondaryLyric;

                        if (MusicLyrics.hasSecondaryLyrics())
                            secondaryLyric = MusicLyrics.getSecondaryLyrics(current);
                        else {
                            secondaryLyric = "";
                        }

                        CFontRenderer fr = FontManager.pf20bold;
                        CFontRenderer frSmall = FontManager.pf16;

                        long nextStamp = next == null ? CloudMusic.player.getTotalTimeMillis() : next.timeStamp;

                        String from = sdf.format(new Date(current.timeStamp));
                        String to = sdf.format(new Date(nextStamp));

                        double widthMax = Math.max(frSmall.getStringWidth(from + " " + to), Math.max(fr.getStringWidth(current.getLyric()), frSmall.getStringWidth(secondaryLyric)));

                        double lW = widthMax + 12;
                        double lH = 6 + fr.getHeight() + 2 + frSmall.getHeight() + 2 + 4 + 2 + frSmall.getHeight() + 6;

                        if (secondaryLyric.isEmpty()) {
                            lH -= frSmall.getHeight();
                        }

                        double lX = Math.max(0, Math.min(RenderSystem.getWidth() - lW, mouseX - lW * 0.5));
                        double lY = posY + height - bottomBarHeight - 2 - lH;

                        roundedRect(lX, lY, lW, lH, 4, new Color(0, 0, 0, 180));

                        if (MusicLyrics.timings.isEmpty()) {
                            fr.drawString(current.getLyric(), lX + 6, lY + 6, -1);

                            if (!secondaryLyric.isEmpty()) {
                                frSmall.drawString(secondaryLyric, lX + 6, lY + 8 + fr.getHeight(), -1);
                            }
                        } else {

                            List<MusicLyrics.ScrollTiming> timings = MusicLyrics.timings;

                            double scrollWidth = 0;

                            for (int j = 0; j < timings.size(); j++) {
                                MusicLyrics.ScrollTiming timing = timings.get(j);

                                if (j + 1 < timings.size() && songProgress < timings.get(j + 1).start) {
                                    int cur = 0;

                                    for (int k = 0; k < timing.timings.size(); k++) {
                                        if ((songProgress - timing.start) * 1.0 >= timing.timings.get(k).timing && k + 1 < timing.timings.size()) {
                                            cur = k + 1;
                                        }

                                    }

                                    StringBuilder sb = new StringBuilder();

                                    MusicLyrics.WordTiming prev;
                                    if (cur - 1 < 0) {
                                        if (j - 1 < 0) {
                                            prev = timing.timings.get(0);
                                        } else {
                                            prev = timings.get(j - 1).timings.get(timings.get(j - 1).timings.size() - 1);
                                        }
                                    } else {
                                        prev = timing.timings.get(cur - 1);
                                    }

                                    for (int m = 0; m < cur; m++) {
                                        sb.append(timing.timings.get(m).word);
                                    }

                                    double offsetX = (songProgress - timing.start - (cur == 0 ? 0 : prev.timing)) / (double) (timing.timings.get(cur).timing - (cur == 0 ? 0 : prev.timing)) *
                                            fr.getStringWidth(timing.timings.get(cur).word);


                                    scrollWidth = fr.getStringWidth(sb.toString()) + offsetX;
                                    break;
                                }
                            }

                            fr.drawString(current.getLyric(), lX + 6, lY + 6, hexColor(255, 255, 255, 160));

                            Stencil.write();
                            Rect.draw(lX + 6, lY + 4, scrollWidth, fr.getHeight() + 6, -1, Rect.RectType.EXPAND);
                            Stencil.erase();
                            fr.drawString(current.getLyric(), lX + 6, lY + 6, -1);
                            Stencil.dispose();

                            if (!secondaryLyric.isEmpty()) {
                                frSmall.drawString(secondaryLyric, lX + 6, lY + 8 + fr.getHeight(), -1);
                            }
                        }

                        double p = (double) (songProgress - current.timeStamp) / (nextStamp - current.timeStamp);

                        double pBarOffsetY = lY + lH - 6 - frSmall.getHeight() - 2 - 4;

                        roundedRect(lX + 6, pBarOffsetY, lW - 12, 4, 1, new Color(255, 255, 255, 160));

                        Stencil.write();
                        Rect.draw(lX + 6, pBarOffsetY, p * (lW - 12), 4, -1, Rect.RectType.EXPAND);
                        Stencil.erase();
                        roundedRect(lX + 6, pBarOffsetY, lW - 12, 4, 1, Color.WHITE);
                        Stencil.dispose();

                        double timingsOffsetY = lY + lH - 6 - frSmall.getHeight();

                        frSmall.drawString(from, lX + 6, timingsOffsetY, -1);
                        frSmall.drawString(to, lX + lW - 6 - frSmall.getStringWidth(to), timingsOffsetY, -1);
                    }

                }

                if (hovered && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {
                    double mouseDelta = mouseX - (progressBarLeft);
                    double perc = mouseDelta / progressLength;
                    perc = Math.min(Math.max(0, perc), 1);
                    CloudMusic.player.setProgress((long) (perc * CloudMusic.player.getTotalTimeMillis()));
                    WidgetsManager.musicLyrics.quickResetProgress((long) (perc * (CloudMusic.player.getTotalTimeMillis())));

                    lmbPressed = true;
                }

                int cMin = CloudMusic.player.getCurrentTimeSeconds() / 60;
                int cSec = (CloudMusic.player.getCurrentTimeSeconds() - (CloudMusic.player.getCurrentTimeSeconds() / 60) * 60);
                currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                int tMin = CloudMusic.player.getTotalTimeSeconds() / 60;
                int tSec = (CloudMusic.player.getTotalTimeSeconds() - (CloudMusic.player.getTotalTimeSeconds() / 60) * 60);
                totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

            }
        }

        if (CloudMusic.player == null || CloudMusic.player.isPausing()) {
//            pausePlay.setImage(Location.of(Phosphate.NAME + "/textures/musicgui/play.png"));
            buttonPausePlay.setIcon("B");
        } else {
//            pausePlay.setImage(Location.of(Phosphate.NAME + "/textures/musicgui/pause.png"));
            buttonPausePlay.setIcon("A");

        }

        double buttonsY = coverY + 16;

        buttonPausePlay.setX(posX + width / 2.0 - 10);
        buttonPausePlay.setY(buttonsY);
        buttonPausePlay.setWidth(20);
        buttonPausePlay.setHeight(20);
        buttonPausePlay.draw(mouseX, mouseY);

        double distance = 50;

        buttonPrev.setX(posX + width / 2.0 - distance - 10);
        buttonPrev.setY(buttonsY);
        buttonPrev.setWidth(20);
        buttonPrev.setHeight(20);
        buttonPrev.draw(mouseX, mouseY);

        buttonPlayMode.setX(posX + width / 2.0 - distance * 2 - 10);
        buttonPlayMode.setY(buttonsY);
        buttonPlayMode.setWidth(20);
        buttonPlayMode.setHeight(20);
        buttonPlayMode.draw(mouseX, mouseY);

        buttonNext.setX(posX + width / 2.0 + distance - 10);
        buttonNext.setY(buttonsY);
        buttonNext.setWidth(20);
        buttonNext.setHeight(20);
        buttonNext.draw(mouseX, mouseY);

//        FontManager.pf18.drawString(musicName, posX + 8 + coverSize, coverY + 8, ThemeManager.get(ThemeManager.ThemeColor.Text));
//        FontManager.pf14.drawString(artistName, posX + 8 + coverSize, coverY + 12 + FontManager.pf18.getHeight(), Color.GRAY.getRGB());
//        Rect.draw(posX + 8 + coverSize, coverY + 8, 140, FontManager.pf20bold.getHeight(), 0xff0090ff, Rect.RectType.EXPAND);
        this.musicNameLabel.render(FontManager.pf20bold, musicName, posX + 8 + coverSize, coverY + 8, 140, ThemeManager.get(ThemeManager.ThemeColor.Text));
        this.musicArtistLabel.render(FontManager.pf18bold, artistName, posX + 8 + coverSize, coverY + 12 + FontManager.pf20bold.getHeight(), 140, Color.GRAY.getRGB());

        progressLength = 10;

        FontManager.pf14.drawString(currentTime, posX + width / 2.0d - progressLength / 2.0d - 24, posY + height - 8 - FontManager.pf14.getHeight() / 2.0, ThemeManager.get(ThemeManager.ThemeColor.Text));
        FontManager.pf14.drawString(totalTime, posX + width / 2.0d + progressLength / 2.0d + 4, posY + height - 8 - FontManager.pf14.getHeight() / 2.0, ThemeManager.get(ThemeManager.ThemeColor.Text));

        this.renderNumberSetting(GlobalSettings.volume, mouseX, mouseY, posX + width - 120, coverY + 16, dWheel);

//        FontManager.pf16.drawString("EQ", posX + width - 40, coverY + 24, ThemeManager.get(ThemeManager.ThemeColor.Text));

        buttonEQ.setSmallFr(true);
        buttonEQ.setX(posX + width - 26);
        buttonEQ.setY(coverY + 16);
        buttonEQ.setWidth(20);
        buttonEQ.setHeight(20);
//        buttonEQ.draw(mouseX, mouseY);

        hoverInfoAlpha = Interpolations.interpBezier(hoverInfoAlpha, hovering == null ? 0 : 1, 0.3f);

        if (this.rightClickMenu == null) {
            if (hovering != null) {
                hoveringPrev = hovering;
                this.doRenderHoverInfo(hovering, mouseX + 6, mouseY + 6);
            } else {

                if (hoveringPrev != null) {
                    this.doRenderHoverInfo(hoveringPrev, mouseX + 6, mouseY + 6);
                    if (hoverInfoAlpha < 0.01)
                        hoveringPrev = null;
                }

            }
        } else {
            this.rightClickMenu.render(mouseX, mouseY);

            if (this.rightClickMenu != null && this.rightClickMenu.shouldClose)
                this.rightClickMenu = null;
        }

//        if (ClickGui.getInstance().settingsRenderer != null)
//            this.musicSettings.render(posX, posY, width, height, mouseX, mouseY, dWheel);

//        roundedRect(posX + width + 10, posY, 200, height, 5, Color.WHITE);
//
//        double startX = posX + width + 14;
//        double startY = posY + 4;
//
//        ThemeManager tm = Phosphate.getInstance().getThemeManager();
//        DynamicScheme ds = tm.dynamicScheme;
//
//        List<Tests> list = Arrays.asList(
//                new Tests("primary", tm.mdc.primary().getArgb(ds)),
//                new Tests("primaryFixed", tm.mdc.primaryFixed().getArgb(ds)),
//                new Tests("primaryFixedDim", tm.mdc.primaryFixedDim().getArgb(ds)),
//                new Tests("primaryPaletteKeyColor", tm.mdc.primaryPaletteKeyColor().getArgb(ds)),
//                new Tests("primaryContainer", tm.mdc.primaryContainer().getArgb(ds)),
//
//                new Tests("surfaceTint", tm.mdc.surfaceTint().getArgb(ds)),
//                new Tests("onPrimary", tm.mdc.onPrimary().getArgb(ds)),
//                new Tests("onBackground", tm.mdc.onBackground().getArgb(ds)),
//                new Tests("onSurface", tm.mdc.onSurface().getArgb(ds)),
//                new Tests("onSurfaceVariant", tm.mdc.onSurfaceVariant().getArgb(ds)),
//                new Tests("onError", tm.mdc.onError().getArgb(ds)),
//                new Tests("onErrorContainer", tm.mdc.onErrorContainer().getArgb(ds))
//
//        );
//
//        for (Tests t : list) {
//
//            FontManager.pf18.drawString(t.text, startX, startY + 6, hexColor(0, 0, 0));
//            Rect.draw(startX + 120, startY, 20, 20, t.color, Rect.RectType.EXPAND);
//
//            startY += 24;
//
//        }

    }

    ScrollText musicNameLabel = new ScrollText();
    ScrollText musicArtistLabel = new ScrollText();

    @AllArgsConstructor
    private final class Tests {

        public String text;
        public int color;

    }

    public boolean shouldRenderEQPanel() {
        return eqPanelOpen;
    }

    double eqX, eqY;
    double moveX = 0, moveY = 0;

    Localizable lEQ = Localizable.of("panel.music.eq");
    Localizable lEQReset = Localizable.of("panel.music.eqreset");

    public void renderEqPanel(double mouseX, double mouseY, int dWheel) {
        GlStateManager.pushMatrix();
        double spacing = 8;
        double eqPanelWidth = 220, eqPanelHeight = 110;
        roundedRect(eqX, eqY, eqPanelWidth, eqPanelHeight, 5, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, 255));

        FontManager.pf25.drawString(lEQ.get(), eqX + 4, eqY + 4, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double resetButtonWidth = 36;
        roundedRect(eqX + eqPanelWidth - resetButtonWidth - 4, eqY + 4, resetButtonWidth, 12, 4, new Color(0xff0090ff));

        FontManager.pf16.drawCenteredString(lEQReset.get(), eqX + eqPanelWidth - resetButtonWidth * 0.5 - 4, eqY + 4 + 6 - FontManager.pf16.getHeight() * 0.5, -1);

        List<NumberSetting<Double>> eqBands = Arrays.asList(
                GlobalSettings.EQ_0,
                GlobalSettings.EQ_1,
                GlobalSettings.EQ_2,
                GlobalSettings.EQ_3,
                GlobalSettings.EQ_4,
                GlobalSettings.EQ_5,
                GlobalSettings.EQ_6,
                GlobalSettings.EQ_7,
                GlobalSettings.EQ_8,
                GlobalSettings.EQ_9
        );

        boolean hovered = isHovered(mouseX, mouseY, eqX + eqPanelWidth - resetButtonWidth - 4, eqY + 4, resetButtonWidth, 12);

        if (!(ClickGui.getInstance().currentPanel instanceof MusicPanel))
            if (!Mouse.isButtonDown(0) && lmbPressed)
                lmbPressed = false;

        if (hovered && Mouse.isButtonDown(0) && !lmbPressed) {
            lmbPressed = true;
            eqBands.forEach(NumberSetting::reset);
        }

        double offsetX = eqX + 7, offsetY = eqY + 20;

        double hSpacing = 22;

        for (NumberSetting<Double> eqBand : eqBands) {
            this.renderVerticalNS(eqBand, mouseX, mouseY, offsetX, offsetY, dWheel);

            offsetX += hSpacing;
        }

        offsetX = eqX + 6;

        if (CloudMusic.player != null && CloudMusic.player.player != null/* && CloudMusic.player.player.getStatus() != MediaPlayer.Status.DISPOSED*/) {
//            AudioEqualizer eq = CloudMusic.player.player.getAudioEqualizer();
//            for (EqualizerBand band : eq.getBands()) {
//                FontManager.pf14.drawCenteredString(String.valueOf((int) band.getCenterFrequency()), offsetX + 4, offsetY + 82, ThemeManager.get(ThemeManager.ThemeColor.Text));
//                offsetX += hSpacing;
//            }
        }

        if (isHovered(mouseX, mouseY, eqX, eqY, eqPanelWidth, 10) && Mouse.isButtonDown(0)) {
            lmbPressed = true;
            if (moveX == 0 && moveY == 0) {
                moveX = mouseX - eqX;
                moveY = mouseY - eqY;
            } else {
                eqX = mouseX - moveX;
                eqY = mouseY - moveY;
            }
        } else if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }

        GlStateManager.popMatrix();
    }

    private void renderVerticalNS(NumberSetting setting, double mouseX, double mouseY, double x, double y, int dWheel) {
        CFontRenderer fr16 = FontManager.pf16;

        double sliderWidth = 8, sliderHeight = 70, sliderX = x, sliderY = y + fr16.getHeight() + 4, sliderRadius = 2;
        roundedRect(sliderX, sliderY, sliderWidth, sliderHeight, sliderRadius, new Color(159, 159, 159));
//
        setting.nowWidth = Interpolations.interpBezier(setting.nowWidth, (setting.getValue().doubleValue() - setting.getMinimum().doubleValue()) / (setting.getMaximum().doubleValue() - setting.getMinimum().doubleValue()) * sliderHeight, 0.2);
        this.roundedRectAccentColor(sliderX, sliderY + sliderHeight - setting.nowWidth, sliderWidth, setting.nowWidth, sliderRadius);
//
        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, sliderX, sliderY, sliderWidth, sliderHeight);
        if (hovered && Mouse.isButtonDown(0) && this.canClick()) {
            lmbPressed = true;
            double render = setting.getMinimum().doubleValue();
            double max = setting.getMaximum().doubleValue();
            double inc = setting.getIncrement().doubleValue();
            double valAbs = (sliderY + sliderHeight) - mouseY;
            double perc = valAbs / sliderHeight;
            perc = Math.min(Math.max(0.0D, perc), 1.0D);
            double valRel = (max - render) * perc;
            double val = render + valRel;
            val = (double) Math.round(val * (1.0D / inc)) / (1.0D / inc);

            setting.setValue(val);
        }

        if (hovered && dWheel != 0) {
            if (dWheel > 0) {
                setting.setValue(setting.getValue().doubleValue() + setting.getIncrement().doubleValue());
            } else {
                setting.setValue(setting.getValue().doubleValue() - setting.getIncrement().doubleValue());
            }
        }
//
        String value = setting.df.format(setting.getValue());
        FontManager.pf14.drawCenteredString(value, sliderX + sliderWidth * 0.5, sliderY - 2 - FontManager.pf14.getHeight(), Color.GRAY.getRGB());
    }

    Localizable lPlay = Localizable.of("panel.music.play");
    Localizable lUnlike = Localizable.of("panel.music.unlike");
    Localizable lLike = Localizable.of("panel.music.like");
    Localizable lAddToPlaylist = Localizable.of("panel.music.addtoplaylist");
    Localizable lDeleteFromList = Localizable.of("panel.music.deletefromlist");

    private ContextMenu buildMenu(double mouseX, double mouseY, Music music, PlayList curList) {

//        if (true) {
//
//            List<ContextEntity> contexts = new ArrayList<>();
//
//            contexts.add(new ContextLabel("Test Label", () -> {}));
//
//            contexts.add(new SecondaryMenu("Menu 1", Arrays.asList(
//                    new SecondaryMenu("Menu 2", Arrays.asList(
//                            new SecondaryMenu("Menu 4", Arrays.asList()),
//                            new SecondaryMenu("Menu 5", Arrays.asList())
//                    )),
//                    new SecondaryMenu("Menu 3", Arrays.asList(
//                            new SecondaryMenu("Menu 6", Arrays.asList(
//                                    new SecondaryMenu("Menu 8", Arrays.asList()),
//                                    new SecondaryMenu("Menu 9", Arrays.asList())
//                            )),
//                            new SecondaryMenu("Menu 7", Arrays.asList())
//                    ))
//            )));
//
//            return new ContextMenu(mouseX, mouseY, contexts);
//        }

        List<ContextEntity> contexts = new ArrayList<>();

        contexts.add(new ContextLabel(lPlay.get(), () -> {
            if (selectedList.searchMode)
                CloudMusic.playedFrom = null;
            else
                CloudMusic.playedFrom = selectedList;
            CloudMusic.play(curList.getMusics(), curList.getMusics().indexOf(music));
        }));

        if (likeList.contains(music.getId())) {
            contexts.add(new ContextLabel(lUnlike.get(), () -> {

                PlayList liked = MusicPanel.playLists.get(0);

                MultiThreadingUtil.runAsync(() -> {
                    music.setLike(false);
                    liked.removeFromList(music.getId());
                });

                liked.getMusics().remove(music);
                likeList.remove(music.getId());
            }));
        } else {
            contexts.add(new ContextLabel(lLike.get(), () -> {

                PlayList liked = MusicPanel.playLists.get(0);

                MultiThreadingUtil.runAsync(() -> {
                    music.setLike(true);

                    if (!liked.getMusics().contains(music)) {
                        liked.addToList(music.getId());
                    }
                });

                if (!liked.getMusics().contains(music)) {
                    liked.getMusics().add(0, music);
                    likeList.add(music.getId());
                }

            }));
        }

        List<ContextEntity> subContexts = new ArrayList<>();

        for (PlayList pl : MusicPanel.playLists) {
            if (!pl.getMusics().contains(music)) {
                subContexts.add(new ContextLabel(
                        pl.name,
                        () -> {
                            if (!pl.getMusics().contains(music)) {
                                MultiThreadingUtil.runAsync(() -> pl.addToList(music.getId()));
                                pl.getMusics().add(0, music);
                            }
                        }
                ));
            }
        }

        contexts.add(new SecondaryMenu(
                lAddToPlaylist.get(),
                subContexts
        ));

        if (!curList.searchMode && !curList.subscribed) {
            contexts.add(new ContextLabel(lDeleteFromList.get(), () -> {
                MultiThreadingUtil.runAsync(() -> curList.removeFromList(music.getId()));
                curList.getMusics().remove(music);
            }));
        }

        return new ContextMenu(mouseX, mouseY, contexts);
    }

    ClickableIcon buttonSettings = new ClickableIcon("w", 0, 0, 10, 10, () -> {

        if (ClickGui.getInstance().settingsRenderer == null) {
            ClickGui.getInstance().settingsRenderer = new ModuleSettings(GlobalSettings.dummyMusicModule);
        }

    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });

    public boolean eqPanelOpen = false;

    ClickableIcon buttonEQ = new ClickableIcon("w", 0, 0, 10, 10, () -> {

        eqPanelOpen = !eqPanelOpen;

        if (eqPanelOpen) {
            eqX = posX + width + 8;
            eqY = posY + height - 60 + 4;
        }

    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });

    ClickableIcon buttonPlayAll = new ClickableIcon("B", 0, 0, 10, 10, () -> {
        if (selectedList.searchMode)
            CloudMusic.playedFrom = null;
        else
            CloudMusic.playedFrom = selectedList;
        CloudMusic.play(selectedList.musics, -1);
        MultiThreadingUtil.runAsync(() -> {
            selectedList.updPlayCount();
        });
    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });

    ClickableIcon buttonPausePlay = new ClickableIcon("B", 0, 0, 10, 10, () -> {
        if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) {
            if (CloudMusic.player.isPausing())
                CloudMusic.player.unpause();
            else
                CloudMusic.player.pause();

        }
    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });

    ClickableIcon buttonPrev = new ClickableIcon("H", 0, 0, 10, 10, () -> {
        if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
            CloudMusic.prev();
    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });

    ClickableIcon buttonNext = new ClickableIcon("E", 0, 0, 10, 10, () -> {
        if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
            CloudMusic.next();
    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });

    ClickableIcon buttonPlayMode = new ClickableIcon(CloudMusic.playMode.getIcon(), 0, 0, 10, 10, () -> {

        int nextOrdinal;

        if (CloudMusic.playMode.ordinal() + 1 > CloudMusic.PlayMode.values().length - 1) {
            nextOrdinal = 0;
        } else {
            nextOrdinal = CloudMusic.playMode.ordinal() + 1;
        }

        MusicPanel.this.buttonPlayMode.setIcon(CloudMusic.PlayMode.values()[nextOrdinal].getIcon());
        CloudMusic.playMode = CloudMusic.PlayMode.values()[nextOrdinal];

    }, () -> {

    }, () -> {

    }, () -> {

    }, () -> {

    });


    Music hoveringPrev = null;

    Localizable lArtists = Localizable.of("panel.music.hover.artists");
    Localizable lAlbum = Localizable.of("panel.music.hover.album");
    Localizable lDuration = Localizable.of("panel.music.hover.duration");
    Localizable lPublishTime = Localizable.of("panel.music.hover.publishTime");

    private void doRenderHoverInfo(Music hovering, double mouseX, double mouseY) {

        int mPart = (int) Duration.ofMillis(hovering.getDuration()).toMinutes();
        int sPart = (int) Duration.ofMillis(hovering.getDuration() - (Duration.ofMillis(hovering.getDuration()).toMinutes() * 60 * 1000)).getSeconds();

        String secondsPart = String.valueOf(sPart);

        if (secondsPart.length() == 1)
            secondsPart = "0" + secondsPart;

        List<String> renderInfo = new ArrayList<>();

        renderInfo.add(hovering.getName() + (hovering.getTranslatedName() == null ? "" : EnumChatFormatting.GRAY + " (" + hovering.getTranslatedName() + ")"));
        renderInfo.add(lArtists.get() + hovering.getArtistsName());
        renderInfo.add(lAlbum.get() + hovering.getAlbumName());

        if (hovering.getPublishTime() != 0) {
            renderInfo.add(lPublishTime.get() + hovering.getFormattedPublishTime());
        }

        renderInfo.add(lDuration.get() + mPart + ":" + secondsPart);
        renderInfo.add("ID: " + hovering.getId());

        CFontRenderer fontRenderer = FontManager.pf18;

        double widest = 0;

        for (String s : renderInfo) {
            float stringWidth = fontRenderer.getStringWidth(s);
            if (stringWidth > widest)
                widest = stringWidth;
        }

        double width = 6 + widest;
        double height = 5 + (fontRenderer.getHeight() + 2) * renderInfo.size();

        roundedRect(mouseX, mouseY, width, height, 3, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, (int) (hoverInfoAlpha * 255)));

        double offsetX = mouseX + 3, offsetY = mouseY + 3;

        for (String s : renderInfo) {
            fontRenderer.drawString(s, offsetX, offsetY, ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (hoverInfoAlpha * 200)));
            offsetY += fontRenderer.getHeight() + 2;
        }

    }

    public ContextMenu rightClickMenu = null;

    public MusicEntity getEntity(Music music) {
        if (map.get(music) == null)
            map.put(music, new MusicEntity());

        return map.get(music);
    }

    public static Location getPlaylistCoverLocation(PlayList list) {
        return Location.of(Phosphate.NAME + "/textures/PlayListCover" + list.id + ".png");
    }

    public static Location getPlaylistCoverLocationSmall(PlayList list) {
        return Location.of(Phosphate.NAME + "/textures/PlayListCoverSmall" + list.id + ".png");
    }

    public Location getMusicCover(Music music) {
        return Location.of(Phosphate.NAME + "/textures/MusicCover" + music.getId() + ".png");
    }

    public Location getMusicCoverBlured(Music music) {
        return Location.of(Phosphate.NAME + "/textures/MusicCoverBlur" + music.getId() + ".png");
    }

    private void renderNumberSetting(NumberSetting<Integer> setting, double mouseX, double mouseY, double x, double y, int dWheel) {
        CFontRenderer fr16 = FontManager.pf16;
        fr16.drawString(setting.getName().get(), x, y - 2, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double sliderWidth = 90, sliderHeight = 4.5, sliderX = x, sliderY = y + fr16.getHeight() + 4, sliderRadius = 1;
        roundedRect(sliderX, sliderY, sliderWidth, sliderHeight, sliderRadius, new Color(159, 159, 159));

        setting.nowWidth = Interpolations.interpBezier(setting.nowWidth, (setting.getValue().doubleValue() / setting.getMaximum().doubleValue()) * sliderWidth, 0.2);
        this.roundedRectAccentColor(sliderX, sliderY, setting.nowWidth, sliderHeight, sliderRadius);
        double circleSize = 9, smallCircleSize = 6;

        roundedRect(sliderX + setting.nowWidth - circleSize * 0.5, sliderY + sliderHeight * 0.5 - circleSize * 0.5, circleSize, circleSize, 4, new Color(69, 69, 69));
        this.roundedRectAccentColor(sliderX + setting.nowWidth - smallCircleSize * 0.5, sliderY + sliderHeight * 0.5 - smallCircleSize * 0.5, smallCircleSize, smallCircleSize, 2);

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, sliderX - 2, y + fr16.getHeight(), sliderWidth + 4, fr16.getHeight() + 4);

        if (hovered && Mouse.isButtonDown(0) && !lmbPressed && this.canClick()) {

            double mouseXToLeft = mouseX - sliderX;
            double percent = mouseXToLeft / sliderWidth;

            double min = setting.getMinimum().doubleValue();
            double max = setting.getMaximum().doubleValue();

            double result = max * percent;
            if (result < min)
                result = min;

            if (result > max)
                result = max;

            setting.setValue((int) result);
        }

        if (hovered && dWheel != 0) {
            if (dWheel > 0) {

                if (setting.getValue() < 100) {
                    setting.setValue(setting.getValue() + 1);
                }

            } else {

                if (setting.getValue() > 0) {
                    setting.setValue(setting.getValue() - 1);
                }

            }
        }

        String value = setting.df.format(setting.getValue());
        fr16.drawString(value, sliderX - 6 - fr16.getStringWidth(value), y + fr16.getHeight() + 6 - fr16.getHeight() / 2.0, Color.GRAY.getRGB());
    }

    private void doRenderLoginRenderer(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        this.loginRenderer.render(mouseX, mouseY, posX, posY, width, height);

        if (this.loginRenderer.canClose() && !OptionsUtil.getCookie().isEmpty()) {
            this.loginRenderer = null;
            MultiThreadingUtil.runAsync(Phosphate.getInstance().getConfigManager()::refreshNCM);
        }
    }

    private void loadAvatarIfNeeded() {
        if (!avatarLoaded && profile != null) {
            avatarLoaded = true;
            MultiThreadingUtil.runAsync(() -> {
                try (InputStream inputStream = HttpUtils.get(profile.getAvatarUrl() + "?param=32y32", null)) {
                    if (inputStream != null) {
                        BufferedImage img = ImageIO.read(inputStream);
                        AsyncGLContext.submit(() -> {
                            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
                            if (textureManager.getTexture(AVATAR_LOCATION) != null) {
                                textureManager.deleteTexture(AVATAR_LOCATION);
                            }
                            Textures.loadTexture(AVATAR_LOCATION, img);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.searchBox.isFocused()) {
                this.searchBox.setFocused(false);
                return true;
            }

            if (ClickGui.getInstance().settingsRenderer != null) {
                ClickGui.getInstance().settingsRenderer.closing = true;
                return true;
            }
        }

        if ((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) && searchBox.isFocused() && !searchBox.getText().isEmpty()) {
//            a;

            lastSearch = searchBox.getText();
            PlayList playList = new PlayList();
            selectedList = playList;

            MultiThreadingUtil.runAsync(() -> {
                playList.musics = CloudMusic.search(searchBox.getText());
            });

            return true;
        }

        if (searchBox.isFocused()) {
            this.searchBox.textboxKeyTyped(typedChar, keyCode);
            return true;
        }

        if (keyCode == Keyboard.KEY_SPACE && CloudMusic.currentlyPlaying != null && !CloudMusic.player.isFinished()) {

            if (CloudMusic.player.isPausing())
                CloudMusic.player.unpause();
            else
                CloudMusic.player.pause();
            return true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                && keyCode == Keyboard.KEY_F
                && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
                && !(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))) {

            this.searchBox.setFocused(true);
            this.searchBox.setCursorPositionEnd();
            this.searchBox.setSelectionPos(0);
            return true;
        }

        if (!searchBox.isFocused() && CloudMusic.currentlyPlaying != null && CloudMusic.player != null && (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT)) {

            List<LyricLine> allLyrics = MusicLyrics.allLyrics;
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !allLyrics.isEmpty()) {

                float songProgress = CloudMusic.player.getCurrentTimeMillis();

                LyricLine prev = null;
                LyricLine current = null;
                LyricLine next = null;

                for (int i = 0; i < allLyrics.size(); i++) {

                    LyricLine lyric = allLyrics.get(i);

                    if (lyric.getTimeStamp() <= songProgress) {
                        current = lyric;

                        if (i + 1 < allLyrics.size()) {
                            next = allLyrics.get(i + 1);
                        }

                        if (i > 0) {
                            prev = allLyrics.get(i - 1);
                        }
                    }
                }

                if (current == null) {
                    current = next = allLyrics.get(0);
                }

                if (prev == null) {
                    prev = allLyrics.get(0);
                }

                if (next == null)
                    next = allLyrics.get(allLyrics.size() - 1);

                if (keyCode == Keyboard.KEY_LEFT) {
                    CloudMusic.player.setProgress(prev.timeStamp - 100);
                }

                if (keyCode == Keyboard.KEY_RIGHT) {
                    CloudMusic.player.setProgress(next.timeStamp - 100);
                }

            } else {
                if (keyCode == Keyboard.KEY_LEFT) {
                    CloudMusic.player.setProgress(Math.max(0, CloudMusic.player.getCurrentTimeMillis() - 5000));
                }

                if (keyCode == Keyboard.KEY_RIGHT) {
                    CloudMusic.player.setProgress(Math.min(CloudMusic.player.getTotalTimeMillis(), CloudMusic.player.getCurrentTimeMillis() + 5000));
                }
            }

        }

        return false;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.searchBox.mouseClicked(mouseX, mouseY, mouseButton);

//        if (ClickGui.getInstance().settingsRenderer != null) {
//            ClickGui.getInstance().settingsRenderer.mouseClicked(mouseX, mouseY, mouseButton);
//        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int state) {
        this.searchBox.mouseReleased(mouseX, mouseY, state);

//        if (this.musicSettings != null) {
//            this.musicSettings.mouseReleased(mouseX, mouseY, state);
//        }
    }

    @Override
    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
//        if (this.musicSettings != null) {
//            this.musicSettings.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
//        }
    }

    public class MusicEntity {
        public boolean mousePressed = false;
        public double scale = 1;
        public boolean textureLoaded = false;
        public float hoverAlpha = 0.0f;
    }


}
