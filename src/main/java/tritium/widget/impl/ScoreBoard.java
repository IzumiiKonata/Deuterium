package tritium.widget.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import tritium.interfaces.IFontRenderer;
import tritium.management.FontManager;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.widget.Widget;
import tritium.interfaces.SharedRenderingConstants;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreBoard extends Widget {

    public ScoreBoard() {
        super("ScoreBoard");
    }

    public BooleanSetting clientChat = new BooleanSetting("Use client font renderer", false);

    @Override
    public void onRender(boolean editing) {
        Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.thePlayer.getName());

        if (scoreplayerteam != null) {
            int i1 = scoreplayerteam.getChatFormat().getColorIndex();

            if (i1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreobjective1 != null) {
            this.renderScoreboard(this.getX(), this.getY(), scoreobjective1);
        }
    }

    private void renderScoreboard(double x, double y, ScoreObjective objective) {
        Scoreboard scoreboard = objective.getScoreboard();
        List<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> scores = collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());

        if (scores.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(scores, collection.size() - 15));
        } else {
            collection = scores;
        }

        int maxWidth = this.getFontRenderer().getStringWidth(objective.getDisplayName());

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            maxWidth = Math.max(maxWidth, this.getFontRenderer().getStringWidth(s));
        }

        Collections.reverse(collection);

        double round = 2;

        int finalMaxWidth = maxWidth;
        List<Score> finalCollection = collection;

        SharedRenderingConstants.BLUR.add(() -> {
            GlStateManager.pushMatrix();
            this.doScale();
            Rect.draw(x - 1, y, finalMaxWidth + 3, (this.getFontRenderer().getHeight() + 2) * (finalCollection.size() + 1) - 2, -1, Rect.RectType.EXPAND);
            GlStateManager.popMatrix();
        });

        SharedRenderingConstants.BLOOM.add(() -> {
            GlStateManager.pushMatrix();
            this.doScale();
            Rect.draw(x - 1, y, finalMaxWidth + 3, (this.getFontRenderer().getHeight() + 2) * (finalCollection.size() + 1) - 2, hexColor(0, 0, 0, 80), Rect.RectType.EXPAND);
            GlStateManager.popMatrix();
        });

        final double[] offsetY = {this.getFontRenderer().getHeight()};
        List<Score> finalCollection1 = collection;
        int finalMaxWidth1 = maxWidth;
        int finalMaxWidth2 = maxWidth;
        SharedRenderingConstants.NORMAL.add(() -> {
            GlStateManager.pushMatrix();
            this.doScale();


            for (int i = 0; i < finalCollection1.size(); i++) {
                if (i == 0) {
//                    GlStateManager.enableAlpha();
//                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);

                    Rect.draw(x - 1, y, finalMaxWidth + 3, this.getFontRenderer().getHeight() + 2, hexColor(0, 0, 0, 80), Rect.RectType.EXPAND);

                    String name = objective.getDisplayName();
                    this.drawString(name, x + finalMaxWidth1 * 0.5 - this.getFontRenderer().getStringWidth(name) * 0.5, y, RenderSystem.hexColor(255, 255, 255, 255));
                }

                Rect.draw(x - 1, y + offsetY[0], finalMaxWidth + 3, this.getFontRenderer().getHeight() + 2, hexColor(0, 0, 0, 80), Rect.RectType.EXPAND);

                Score score1 = finalCollection1.get(i);

                ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
                String left = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
                String right = EnumChatFormatting.RED + String.valueOf(score1.getScorePoints());

                this.drawString(left, x + 2, y + offsetY[0], RenderSystem.hexColor(255, 255, 255, 255));
                this.drawString(right, x + finalMaxWidth1 - this.getFontRenderer().getStringWidth(right), y + offsetY[0], RenderSystem.hexColor(255, 255, 255, 255));
                offsetY[0] += this.getFontRenderer().getHeight() + 2;

            }

            GlStateManager.popMatrix();
            this.setWidth(finalMaxWidth2);
            this.setHeight(offsetY[0]);
        });


    }

    private void drawString(String text, double x, double y, int color) {
        IFontRenderer fontRenderer = this.getFontRenderer();

        fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    private IFontRenderer getFontRenderer() {
        return this.clientChat.getValue() ? FontManager.pf25 : mc.fontRendererObj;
    }
}
