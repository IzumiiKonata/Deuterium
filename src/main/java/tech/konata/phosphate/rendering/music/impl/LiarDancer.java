package tech.konata.phosphate.rendering.music.impl;

import net.minecraft.util.Location;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.music.PVRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

/**
 * @author IzumiiKonata
 * Date: 2025/1/25 14:39
 */
public class LiarDancer extends PVRenderer {

    @Override
    public double getBPM() {
        return 170;
    }

    Location[] fall, arms;

    @Override
    protected void onInit() {
        fall = this.loadTextureFrom("LiarDancer/Fall/", ".png", 1791, 1794);
        arms = this.loadTextureFrom("LiarDancer/Arms/", ".png", 1812, 1853);
    }

    @Override
    public void onRender(float playBackTime, long musicID) {
        int beatCount = (int) this.beatCount(playBackTime);
        int beatCount2x = (int) this.beatCount(playBackTime, this.getMillisPerBeat() * 0.5);
        double beatCountDouble = this.beatCount(playBackTime);

        double spacing = 54;

        int imgWidth = 270;
        int imgHeight = 150;

        if (beatCount2x == 332) {
            double texDuration = this.getMillisPerBeat() / 2 / fall.length;
            int curIdx = (int) ((playBackTime / texDuration) % fall.length);
            Image.drawLinear(fall[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount2x >= 333 && beatCount2x <= 335) {
            Image.drawLinear(fall[fall.length - 1], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 168 && beatCount <= 179) {
            double texDuration = this.getMillisPerBeat() * 4 / arms.length;
            int curIdx = (int) ((playBackTime / texDuration) % arms.length);
            Image.drawLinear(arms[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 180 || beatCount == 182) {
            double texDuration = this.getMillisPerBeat() / 10;
            int curIdx = (int) ((playBackTime / texDuration) % 10);
            Image.drawLinear(arms[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 181 || beatCount == 183) {
            double texDuration = this.getMillisPerBeat() / 10;
            int curIdx = (int) ((playBackTime / texDuration) % 10);
            Image.drawLinear(arms[21 + curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 184 && beatCount <= 195) {
            double texDuration = this.getMillisPerBeat() * 4 / arms.length;
            int curIdx = (int) ((playBackTime / texDuration) % arms.length);
            Image.drawLinear(arms[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 196) {
            double texDuration = this.getMillisPerBeat() / 10;
            int curIdx = (int) ((playBackTime / texDuration) % 10);
            Image.drawLinear(arms[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 197) {
            double texDuration = this.getMillisPerBeat() / 10;
            int curIdx = (int) ((playBackTime / texDuration) % 10);
            Image.drawLinear(arms[21 + curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 198) {

            double wait = this.getMillisPerBeat() / 2.0;

            double texDuration = this.getMillisPerBeat() * 2 / 21;
            int curIdx = (int) ((playBackTime / texDuration) % 21);
            Image.drawLinear(arms[curIdx], -spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double cur = playBackTime - beatCount * this.getMillisPerBeat();
            if (cur > wait) {
                Image.drawLinear(arms[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }
        }

        if (beatCount == 199) {
            Image.drawLinear(arms[0], -spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[0],  0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[0], spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 200 && beatCount <= 211) {
            double texDuration = this.getMillisPerBeat() * 4 / arms.length;
            int curIdx = (int) ((playBackTime / texDuration) % arms.length);

            Image.drawLinear(arms[curIdx], -spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[curIdx],  0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[curIdx], spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 212 || beatCount == 214) {
            double texDuration = this.getMillisPerBeat() / 10;
            int curIdx = (int) ((playBackTime / texDuration) % 10);

            Image.drawLinear(arms[curIdx], -spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[curIdx],  0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[curIdx], spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 213 || beatCount == 215) {
            double texDuration = this.getMillisPerBeat() / 10;
            int curIdx = (int) ((playBackTime / texDuration) % 10);

            Image.drawLinear(arms[21 + curIdx], -spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[21 + curIdx],  0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[21 + curIdx], spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 216 && beatCount <= 227) {
            double texDuration = this.getMillisPerBeat() * 4 / arms.length;
            int curIdx = (int) ((playBackTime / texDuration) % arms.length);

            Image.drawLinear(arms[curIdx], -spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[curIdx],  0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(arms[curIdx], spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }
    }

    @Override
    public boolean isApplicable(long id) {
        return id == 2090044617L;
    }

    @Override
    public long waitTime(long id) {
        return 1410;
    }
}
