package tritium.rendering.music.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import tritium.rendering.Image;
import tritium.rendering.music.PVRenderer;
import tritium.rendering.rendersystem.RenderSystem;

/**
 * @author IzumiiKonata
 * Date: 2025/1/22 12:46
 */
public class LagTrain extends PVRenderer {

    Location[] hand, handWhite, jump, jumpWhite, look, lookBackLoop, lookBackSub,
            armsCrossing, lookWhite;

    @Override
    public void onInit() {
        hand = this.loadTextureFrom("LagTrain/0", ".png", 348, 353);
        handWhite = this.loadTextureFrom("LagTrain/w0", ".png", 348, 353);
        jump = this.loadTextureFrom("LagTrain/0", ".png", 446, 451);
        jumpWhite = this.loadTextureFrom("LagTrain/w0", ".png", 446, 451);
        look = this.loadTextureFrom("LagTrain/0", ".png", 391, 402);
        lookWhite = this.loadTextureFrom("LagTrain/w0", ".png", 391, 402);
        lookBackLoop = this.loadTextureFrom("LagTrain/0", ".png", 489, 490);
        lookBackSub = this.loadTextureFrom("LagTrain/0", ".png", 529, 537);
        armsCrossing = this.loadTextureFrom("LagTrain/", ".png", 1616, 1621);
    }

    @Override
    public double getBPM() {
        return 147;
    }

    @Override
    public void onRender(float playBackTime, long musicID) {

        int beatCount = (int) this.beatCount(playBackTime);
        double beatCountDouble = this.beatCount(playBackTime);

        double spacing = 46;

//        FontManager.pf20.drawString("*这不是视频", 0, RenderSystem.getHeight() - 160, RenderSystem.hexColor(255, 255, 255, 120));

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, RenderSystem.getHeight(), 0);
        GlStateManager.scale(1 / RenderSystem.getScaleFactor(), 1 / RenderSystem.getScaleFactor(), 1);
        GlStateManager.scale(1.5, 1.5, 1);
        GlStateManager.translate(0, -RenderSystem.getHeight(), 0);

        this.doRender(beatCount, beatCountDouble, spacing, playBackTime);

        GlStateManager.popMatrix();

    }

    private void doRender(int beatCount, double beatCountDouble, double spacing, float playBackTime) {

        int imgWidth = 180;
        int imgHeight = 100;
        
        int lookBackLoopSpeed = 3;

        if (beatCount >= 48 && beatCount <= 55) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + ((beatCount - 48) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 56 && beatCount <= 63) {
            double texDuration = this.getMillisPerBeat() * 2 / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], (((beatCount - 56) / 2) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 64 && beatCount <= 71) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + ((beatCount - 64) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 72 && beatCount <= 78) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 79) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 80 && beatCount <= 87) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + ((beatCount - 80) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 88 && beatCount <= 95) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + ((beatCount - 88) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 96 && beatCount <= 103) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            double offsetXWhite = -82;

            if (beatCount >= 100)
                beatCount -= 1;

            double third = offsetXWhite + ((beatCount - 96) % 8 - 2) * spacing;
            if (third >= -36 && third <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], third, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 96) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double first = offsetXWhite + ((beatCount - 96) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);


            int i = (beatCount - 96) % 8;

            if (i > 3)
                i = 3;

            Image.drawLinear(hand[curIdx], -36 + i * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 104 && beatCount <= 107) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + ((beatCount - 104) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 111) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 176) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 177) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 178) {
            double texDuration = this.getMillisPerBeat() / 6;
            int i = (int) ((playBackTime / texDuration) % 6);

            if (i <= 3)
                i += 2;

            int curIdx = i;
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 179 && beatCount <= 183) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 184 && beatCount <= 191) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + ((beatCount - 184) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 192 && beatCount <= 199) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + ((beatCount - 192) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 200 && beatCount <= 206) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 207) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 208 && beatCount <= 215) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            double offsetXWhite = -82;

            if (beatCount >= 212)
                beatCount -= 1;

            double first = offsetXWhite + ((beatCount - 208) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 208) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double third = offsetXWhite + ((beatCount - 208) % 8 - 2) * spacing;
            if (third >= -36 && third <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], third, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            int i = (beatCount - 208) % 8;

            if (i > 3)
                i = 3;

            Image.drawLinear(hand[curIdx], -36 + i * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 216 && beatCount <= 223) {
            double texDuration = this.getMillisPerBeat() * 2 / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], (((beatCount - 216) / 2) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 224 && beatCount <= 231) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            if (beatCount == 230) {
                Image.drawLinearFlippedY(jump[curIdx], -36 + ((beatCount - 224) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            } else {
                Image.drawLinear(jump[curIdx], -36 + ((beatCount - 224) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }

        }

        if (beatCount >= 232 && beatCount <= 238) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], 1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 239) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 240 && beatCount <= 247) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + ((beatCount - 240) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 248 && beatCount <= 255) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            double offsetXWhite = -82;

            double first = offsetXWhite + ((beatCount - 248) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(jumpWhite[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 248) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(jumpWhite[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            Image.drawLinear(jump[curIdx], -36 + (beatCount - 248) % 4 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 256) {
            double texDuration = this.getMillisPerBeat() / armsCrossing.length;
            int curIdx = (int) ((playBackTime / texDuration) % armsCrossing.length);

            Image.drawLinear(armsCrossing[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 257 && beatCount <= 263) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            double offsetXWhite = -82;

            if (beatCount >= 260)
                beatCount -= 1;

            double third = offsetXWhite + ((beatCount - 256) % 8 - 2) * spacing;
            if (third >= -36 && third <= -36 + 3 * spacing)
                Image.drawLinear(hand[curIdx], third, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 256) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(hand[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double first = offsetXWhite + ((beatCount - 256) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(hand[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);


            int i = (beatCount - 256) % 8;

            if (i > 3)
                i = 3;

            Image.drawLinear(hand[curIdx], -36 + i * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 264 && beatCount <= 267) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + ((beatCount - 264) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 271) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 272) {
            double texDuration = this.getMillisPerBeat() / armsCrossing.length;
            int curIdx = (int) ((playBackTime / texDuration) % armsCrossing.length);
            Image.drawLinear(armsCrossing[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 273 && beatCount <= 279) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(armsCrossing[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 280) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 281 && beatCount <= 285) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 286) {
            double texDuration = this.getMillisPerBeat() / 6;
            int i = (int) ((playBackTime / texDuration) % 6);

            if (i <= 3)
                i += 2;

            int curIdx = i;
            Image.drawLinear(hand[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 287) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 288) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 289) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 290) {
            double texDuration = this.getMillisPerBeat() / 6;
            int i = (int) ((playBackTime / texDuration) % 6);

            if (i <= 3)
                i += 2;

            int curIdx = i;
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 291 && beatCount <= 298) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 299) {
            double texDuration = this.getMillisPerBeat() / 6;
            int i = (int) ((playBackTime / texDuration) % 6);

            if (i <= 3)
                i += 2;

            int curIdx = i;
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 300) {
            double texDuration = this.getMillisPerBeat() / 8;
            int curIdx = 4 + (int) ((playBackTime / texDuration) % 2);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 301) {
            double texDuration = this.getMillisPerBeat() / 6;
            int i = (int) ((playBackTime / texDuration) % 6);

            if (i <= 3)
                i += 2;

            int curIdx = i;
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 368 && beatCount <= 375) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + 2 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 376 && beatCount <= 383) {
            double texDuration = this.getMillisPerBeat() * 2 / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            if (beatCount >= 378) {
                Image.drawLinear(lookWhite[curIdx], spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }

            if (beatCount >= 380) {
                Image.drawLinear(look[curIdx], spacing * 2, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }

            if (beatCount >= 382) {
                Image.drawLinear(lookWhite[curIdx], spacing * 3, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }

        }

        if (beatCount >= 384 && beatCount <= 387) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 388 && beatCount <= 391) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + ((beatCount - 388) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 392 && beatCount <= 398) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinearFlippedXAndY(lookBackSub[7 + curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 399) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            int flippedIndex = lookBackSub.length - 3 - curIdx;

            if (flippedIndex < 0) {
                Image.drawLinearFlippedXAndY(lookBackLoop[Math.abs(flippedIndex + 1)], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            } else {
                Image.drawLinearFlippedXAndY(lookBackSub[flippedIndex], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }

        }

        if (beatCount == 400) {
            double texDuration = this.getMillisPerBeat() / handWhite.length;
            int curIdx = (int) ((playBackTime / texDuration) % handWhite.length);
            Image.drawLinearFlippedX(handWhite[curIdx], -120, RenderSystem.getHeight() - 120, 302.4, 168, Image.Type.Normal);
        }

        if (beatCount >= 401 && beatCount <= 407) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 409) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 411) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 413) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 415) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 416) {
            double texDuration = this.getMillisPerBeat() / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 417 && beatCount <= 423) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            double offsetXWhite = -82;

            if (beatCount >= 420)
                beatCount -= 1;

            double third = offsetXWhite + ((beatCount - 416) % 8 - 2) * spacing;
            if (third >= -36 && third <= -36 + 3 * spacing)
                Image.drawLinear(hand[curIdx], third, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 416) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(hand[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double first = offsetXWhite + ((beatCount - 416) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(hand[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            int i = (beatCount - 416) % 8;

            if (i > 3)
                i = 3;

            Image.drawLinear(hand[curIdx], -36 + i * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 425) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinearRotate90R(jump[curIdx], 0, RenderSystem.getHeight() - 120, imgHeight, imgWidth, Image.Type.Normal);
        }

        if (beatCount == 427) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinearRotate90L(jump[curIdx], spacing * 2.5, RenderSystem.getHeight() - 120, imgHeight, imgWidth, Image.Type.Normal);
        }

        if (beatCount >= 504 && beatCount <= 511) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            double offsetXWhite = -82;

            if (beatCount >= 508)
                beatCount -= 1;

            double first = offsetXWhite + ((beatCount - 504) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 504) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double third = offsetXWhite + ((beatCount - 504) % 8 - 2) * spacing;
            if (third >= -36 && third <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], third, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            int i = (beatCount - 504) % 8;

            if (i > 3)
                i = 3;

            Image.drawLinear(hand[curIdx], -36 + i * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 512 && beatCount <= 519) {
            double texDuration = this.getMillisPerBeat() * 2 / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], (((beatCount - 512) / 2) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            if (beatCount >= 518) {
                Image.drawLinearFlippedXAndY(look[curIdx], -2.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            }
        }

        if (beatCount >= 520 && beatCount <= 525 || beatCount == 527) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(beatCount == 521 ? jumpWhite[curIdx] : jump[curIdx], -36 + ((beatCount - 520) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 526) {
            double texDuration = this.getMillisPerBeat()/ look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], ((beatCount - 524) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 528) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 529) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 530) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], -50, RenderSystem.getHeight() - 140, 280, imgWidth, Image.Type.Normal);
        }

        if (beatCount == 531) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], -50, RenderSystem.getHeight() - 140, 280, imgWidth, Image.Type.Normal);
        }

        if (beatCount == 536) {
            double texDuration = this.getMillisPerBeat() / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 537 && beatCount <= 543) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            double offsetXWhite = -82;

            if (beatCount >= 540)
                beatCount -= 1;

            double first = offsetXWhite + ((beatCount - 536) % 8) * spacing;
            if (first >= -36 && first <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], first, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double second = offsetXWhite + ((beatCount - 536) % 8 - 1) * spacing;
            if (second >= -36 && second <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], second, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            double third = offsetXWhite + ((beatCount - 536) % 8 - 2) * spacing;
            if (third >= -36 && third <= -36 + 3 * spacing)
                Image.drawLinear(handWhite[curIdx], third, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            int i = (beatCount - 536) % 8;

            if (i > 3)
                i = 3;

            if (beatCount >= 540)
                Image.drawLinear(hand[curIdx], -36 + 0 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            if (beatCount >= 541)
                Image.drawLinear(hand[curIdx], -36 + 1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            if (beatCount >= 542)
                Image.drawLinear(hand[curIdx], -36 + 2 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);


            Image.drawLinear(hand[curIdx], -36 + i * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 544 && beatCount <= 551) {
            double texDuration = this.getMillisPerBeat() * 2 / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);

            if (beatCount == 547) {
                curIdx = look.length - curIdx;
            }

            if (beatCount >= 550)
                Image.drawLinear(look[curIdx], 0, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            Image.drawLinear(look[curIdx], (((beatCount - 544) / 2) % 4) * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 552 || beatCount == 556) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinearRotate90R(jump[curIdx], 0, RenderSystem.getHeight() - 120, imgHeight, imgWidth, Image.Type.Normal);
        }

        if (beatCount == 553 || beatCount == 557 || beatCount == 577) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinear(jump[curIdx], -36 + 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 554 || beatCount == 558) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinearRotate90L(jump[curIdx], spacing * 2.5, RenderSystem.getHeight() - 120, imgHeight, imgWidth, Image.Type.Normal);
        }

        if (beatCount == 555 || beatCount == 559) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinearFlippedY(jump[curIdx], -36 + 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 560 && beatCount <= 566) {
            double texDuration = this.getMillisPerBeat() / lookBackLoopSpeed / lookBackLoop.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackLoop.length);
            Image.drawLinear(lookBackLoop[curIdx], 1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(lookBackLoop[curIdx], -1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 567) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(lookBackSub[curIdx], -1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 569) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 570 && beatCount <= 571) {
            double texDuration = this.getMillisPerBeat() * 2 / look.length;
            int curIdx = (int) ((playBackTime / texDuration) % look.length);
            Image.drawLinear(look[curIdx], 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 579) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinear(jump[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCountDouble >= 579.5 && beatCountDouble <= 580.5) {
            double texDuration = this.getMillisPerBeat() * 1.5 / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);

            Image.drawLinear(jump[curIdx], -36 + 2 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 584) {
            double texDuration = this.getMillisPerBeat() / lookBackSub.length;
            int curIdx = (int) ((playBackTime / texDuration) % lookBackSub.length);
            Image.drawLinear(lookBackSub[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 585) {
            double texDuration = this.getMillisPerBeat() / armsCrossing.length;
            int curIdx = (int) ((playBackTime / texDuration) % armsCrossing.length);

            Image.drawLinear(armsCrossing[curIdx], -1.25 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
            Image.drawLinear(armsCrossing[curIdx], 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 586) {
            double texDuration = this.getMillisPerBeat() / jump.length;
            int curIdx = (int) ((playBackTime / texDuration) % jump.length);
            Image.drawLinear(jump[curIdx], -36 + 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount == 587) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);
            Image.drawLinear(hand[curIdx], -36 + 1.5 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }

        if (beatCount >= 592 && beatCount <= 595) {
            double texDuration = this.getMillisPerBeat() / hand.length;
            int curIdx = (int) ((playBackTime / texDuration) % hand.length);

            if (beatCount == 592)
                Image.drawLinear(hand[curIdx], -36, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            if (beatCount <= 593)
                Image.drawLinear(hand[curIdx], -36 + 1 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            if (beatCount <= 594)
                Image.drawLinear(hand[curIdx], -36 + 2 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);

            Image.drawLinear(hand[curIdx], -36 + 3 * spacing, RenderSystem.getHeight() - imgHeight, imgWidth, imgHeight, Image.Type.Normal);
        }
    }

    @Override
    public boolean isApplicable(long id) {
        return id == 1492827692 || id == 1921983207 || id == 2697321582L;
    }

    @Override
    public long waitTime(long id) {

        if (id == 2697321582L) {
            return 3290;
        }

        return 3100;
    }
}
