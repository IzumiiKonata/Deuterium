package tritium.rendering.music.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import tritium.rendering.Image;
import tritium.rendering.Rect;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Easing;
import tritium.rendering.music.PVRenderer;
import tritium.rendering.rendersystem.RenderSystem;

/**
 * @author IzumiiKonata
 * Date: 2025/6/9 09:31
 */
public class KyuKurarin extends PVRenderer {

    Location alarm;
    Location[] alarmHold;
    Location[] jump;
    Location idle;
    Location[] idleAct;
    Location t1409, t1430, t1433, t1465, t1497, t1537, t1665;
    Location bed, bed2;
    Location t3545, t3737;
    Location t4713, t4729, t4761;
    Location t5305, t5321, t5329, t5398;
    Location[] blur;
    Location hug, hugDark;

    @Override
    public double getBPM() {
        return 220.0;
    }

    @Override
    protected void onInit() {
        alarm = this.loadTexture("KyuKurarin/0115.png");
        alarmHold = this.loadTextureFrom("KyuKurarin/0", ".png", 116, 122);
        jump = this.loadTextureFrom("KyuKurarin/", ".png", 0, 6);
        idle = this.loadTexture("KyuKurarin/1350.png");
        idleAct = this.loadTextureFrom("KyuKurarin/", ".png", 1377, 1389);
        t1409 = this.loadTexture("KyuKurarin/1409.png");
        t1430 = this.loadTexture("KyuKurarin/1430.png");
        t1433 = this.loadTexture("KyuKurarin/1433.png");
        t1465 = this.loadTexture("KyuKurarin/1465.png");
        t1497 = this.loadTexture("KyuKurarin/1497.png");
        t1537 = this.loadTexture("KyuKurarin/1537.png");
        t1665 = this.loadTexture("KyuKurarin/1665.png");
        bed = this.loadTexture("KyuKurarin/2401.png");
        bed2 = this.loadTexture("KyuKurarin/4505.png");
        t3545 = this.loadTexture("KyuKurarin/3545.png");
        t3737 = this.loadTexture("KyuKurarin/3737.png");
        t4713 = this.loadTexture("KyuKurarin/4713.png");
        t4729 = this.loadTexture("KyuKurarin/4729.png");
        t4761 = this.loadTexture("KyuKurarin/4761.png");
        t5305 = this.loadTexture("KyuKurarin/5305.png");
        t5321 = this.loadTexture("KyuKurarin/5321.png");
        t5329 = this.loadTexture("KyuKurarin/5329.png");
        t5398 = this.loadTexture("KyuKurarin/5398.png");
        blur = this.loadTextureFrom("KyuKurarin/", ".png", 5561, 5586);
        hug = this.loadTexture("KyuKurarin/5785.png");
        hugDark = this.loadTexture("KyuKurarin/6040.png");
    }

    @Override
    public void onRender(float playBackTime, long musicID) {

        GlStateManager.pushMatrix();
//        GlStateManager.translate(0, RenderSystem.getHeight(), 0);
//        GlStateManager.scale(1 / RenderSystem.getScaleMultiplier(), 1 / RenderSystem.getScaleMultiplier(), 1);
//        GlStateManager.scale(2, 2, 1);
//        GlStateManager.translate(0, -RenderSystem.getHeight(), 0);

        int beatCount = (int) this.beatCount(playBackTime);
        double beatCountDouble = this.beatCount(playBackTime);

        double width = 240, height = 135;

        StencilClipManager.beginClip(() -> Rect.draw(0, RenderSystem.getHeight() - height, width, height, -1));


        this.alarmAnim(playBackTime, 0, width, height, false);

//        for (int i = 7; i <= 127; i += 8) {
//            this.renderSub(playBackTime, i, width, height, false);
//        }

        this.renderSub(playBackTime, 7, width, height, false);
        this.renderSub(playBackTime, 15, width, height, false);
        this.renderSub(playBackTime, 23, width, height, false);
        this.renderSub(playBackTime, 31, width, height, false);
        this.renderSub(playBackTime, 39, width, height, false);
        this.renderSub(playBackTime, 47, width, height, false);
        this.renderSub(playBackTime, 55, width, height, false);
        this.renderSub(playBackTime, 63, width, height, false);
        this.renderSub(playBackTime, 71, width, height, false);
        this.renderSub(playBackTime, 79, width, height, false);
        this.renderSub(playBackTime, 87, width, height, false);
        this.renderSub(playBackTime, 95, width, height, false);
        this.renderSub(playBackTime, 103, width, height, false);
        this.renderSub(playBackTime, 111, width, height, false);
        this.renderSub(playBackTime, 119, width, height, false);
        this.renderSub(playBackTime, 127, width, height, false);

        if (beatCount >= 136 && beatCount <= 162) {
            Image.drawLinear(idle, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 163 && beatCount <= 166) {
            double texDuration = this.getMillisPerBeat() * 6 / idleAct.length;
            int curIdx = (int) ((playBackTime / texDuration) % idleAct.length);
            Image.drawLinear(idleAct[curIdx], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount == 167) {
            Image.drawLinear(idleAct[idleAct.length - 1], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        Location[] sequence = new Location[] {
                t1409, null,  null,  null,
                t1409, t1430, t1433, t1433,
                t1409, null, t1433, null,
                t1409, null, t1465, null
        };

        this.renderSequence(sequence, playBackTime, 335, 8, width, height);

        sequence = new Location[] {
                t1409, null,  null,  null,
                t1465, t1433, t1497, t1497,
                t1465, null, t1430, null,
                t1409, t1433, t1409, null
        };

        this.renderSequence(sequence, playBackTime, 351, 8, width, height);

        sequence = new Location[] { t1537 };
        this.renderSequence(sequence, playBackTime, 367, 16, width, height);

        sequence = new Location[] { t1665 };
        this.renderSequence(sequence, playBackTime, 399, 27, width, height);
//        this.renderSequence(sequence, playBackTime, 383, 16, width, height);

        this.alarmAnim(playBackTime, 228, width, height, true);
        this.alarmAnim(playBackTime, 236, width, height, true);
        this.alarmAnim(playBackTime, 244, width, height, true);
        this.alarmAnim(playBackTime, 252, width, height, true);
        this.alarmAnim(playBackTime, 260, width, height, true);
        this.alarmAnim(playBackTime, 268, width, height, true);

        if (beatCount >= 276 && beatCount <= 290) {
            Image.drawLinear(alarm, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 292 && beatCount <= 354) {
            Image.drawLinear(bed, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        this.renderSub(playBackTime, 355, width, height, true);
        this.renderSub(playBackTime, 363, width, height, true);
        this.renderSub(playBackTime, 371, width, height, true);
        this.renderSub(playBackTime, 379, width, height, true);
        this.renderSub(playBackTime, 387, width, height, true);
        this.renderSub(playBackTime, 395, width, height, true);
        this.renderSub(playBackTime, 403, width, height, true);
        this.renderSub(playBackTime, 403, width, height, true);

        sequence = new Location[] {
                t1409, null,  null,  null,
                t1409, t1409, t1409, t1409,
                t1433, null, t1430, null,
                t1409, null
        };

        this.renderSequence(sequence, playBackTime, 855, 7, width, height);
        sequence = new Location[] { t3545 };
        this.renderSequence(sequence, playBackTime, 869, 24, width, height);
        sequence = new Location[] { t3737 };
        this.renderSequence(sequence, playBackTime, 917, 31, width, height);

        if (beatCount >= 555 && beatCount <= 580) {
            Image.drawLinear(bed2, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 581 && beatCount <= 582) {
            Image.drawLinear(t4713, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 583 && beatCount <= 585) {
            Image.drawLinear(t4729, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 587 && beatCount <= 614) {
            Image.drawLinear(t4761, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 619 && beatCount <= 623) {
            double texDuration = this.getMillisPerBeat() * 5 / idleAct.length;
            int curIdx = (int) (((playBackTime - this.getMillisPerBeat() * 619) / texDuration) % idleAct.length);
            Image.drawLinear(idleAct[idleAct.length - 1 - curIdx], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 624 && beatCount <= 650) {
            Image.drawLinear(idle, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        sequence = new Location[] {
                t5305, null,  null,  null,
                t5321, t5321, t5329, t5329,
                t5305, null, t5329, null,
                t5305, null, t5321, null
        };

        this.renderSequence(sequence, playBackTime, 1309, 8, width, height);

        sequence = new Location[] {
                t5305, null,  null,  null,
                t5305, t5329, t5305, t5398,
                t5305, null, t5398, null,
                t5305, t5329, t5398, null
        };

        this.renderSequence(sequence, playBackTime, 1325, 8, width, height);

        sequence = new Location[] { t5305 };

        this.renderSequence(sequence, playBackTime, 1341, 16, width, height);

        if (beatCount >= 687 && beatCount <= 690) {
            double texDuration = this.getMillisPerBeat() * 4 / blur.length;
            int curIdx = (int) (((playBackTime - this.getMillisPerBeat() * 687) / texDuration) % blur.length);
            Image.drawLinear(blur[curIdx], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount >= 691 && beatCount <= 712) {
            Image.drawLinear(blur[blur.length - 1], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        sequence = new Location[] {
                hug, null, null, hug,
                hug, hug,  hug,  hug,
                hug, null, hug,  null,
                hug, null, hug,  null
        };

        this.renderSequence(sequence, playBackTime, 1429, 8, width, height);

        sequence = new Location[] {
                hug, null, null, null,
                hug, hug,  hug,  hug,
                hug, null, hug,  null,
                hug, null, hug,  null
        };

        this.renderSequence(sequence, playBackTime, 1445, 8, width, height);

        sequence = new Location[] { hug };

        this.renderSequence(sequence, playBackTime, 1461, 16, width, height);

        sequence = new Location[] { hugDark };

        this.renderSequence(sequence, playBackTime, 1493, 28, width, height);

//        if (beatCount >= beatStart && beatCount <= beatStart + (shiftOneBeat ? 5 : 6)) {
//            Image.drawLinear(alarm, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
//        }

        if (beatCount == 775) {
            double texDuration = this.getMillisPerBeat() / alarmHold.length;
            int curIdx = (int) ((playBackTime / texDuration) % alarmHold.length);
            Image.drawLinear(alarmHold[curIdx], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        StencilClipManager.endClip();

        GlStateManager.popMatrix();
    }

    private void alarmAnim(float playBackTime, int beatStart, double width, double height, boolean shiftOneBeat) {

        int beatCount = (int) this.beatCount(playBackTime);
        double beatCountDouble = this.beatCount(playBackTime);

        if (beatCount >= beatStart && beatCount <= beatStart + (shiftOneBeat ? 5 : 6)) {
            Image.drawLinear(alarm, 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }

        if (beatCount == beatStart + (shiftOneBeat ? 6 : 7)) {
            double texDuration = this.getMillisPerBeat() / alarmHold.length;
            int curIdx = (int) ((playBackTime / texDuration) % alarmHold.length);
            Image.drawLinear(alarmHold[curIdx], 0, RenderSystem.getHeight() - height, width, height, Image.Type.Normal);
        }
    }

    private void renderSub(float playBackTime, int beatStart, double width, double height, boolean secondPart) {

//        if (secondPart) {
//            playBackTime -= (float) (this.getMillisPerBeat() * .25f);
//        }

        int beatCount = (int) this.beatCount(playBackTime);
        double beatCountDouble = this.beatCount(playBackTime);

        int durationBeats = 8;

        if (beatCount == beatStart + durationBeats && beatCountDouble - beatCount >= 0.75)
            return;

        if (beatCount >= beatStart && beatCount <= beatStart + durationBeats) {
            int idx = beatCount - beatStart;

            for (int i = 0; i < idx; i++) {

                if (i >= 7)
                    continue;

                double spacing = 12.5;
                double x = -((idx - i - 1) * spacing) - spacing * Easing.EASE_IN_OUT_CIRC.getFunction().apply(Math.min(1, (beatCountDouble - beatCount) * 2));
                double y = RenderSystem.getHeight() - height;
                if (idx == 1) {
                    x = -((idx - i) * spacing);
                }

                if (i == idx - 1) {
                    y = RenderSystem.getHeight() - height - (1 - Easing.EASE_IN_OUT_CIRC.getFunction().apply(Math.min(1, (beatCountDouble - beatCount) * 2.5))) * height * .5;
                }

                if (secondPart && i != 1) {
                    y -= height * .5;
                }

                Image.drawLinear(jump[i], x, y, width, height, Image.Type.Normal);

            }
        }
    }

    private void renderSequence(Location[] sequence, float playBackTime, int beatStart2x, int durationBeats, double width, double height) {
        int beatCount = (int) this.beatCount(playBackTime, this.getMillisPerBeat() * 2);
        double beatCountDouble = this.beatCount(playBackTime, this.getMillisPerBeat() * 2);
        int beatCount2x = (int) this.beatCount(playBackTime, this.getMillisPerBeat() * 0.5);
        double beatCount2xDouble = this.beatCount(playBackTime, this.getMillisPerBeat() * 0.5);

        double spacing = 14;

        if (beatCount2x > beatStart2x + durationBeats * 2)
            return;

        int curBeat = beatCount2x - beatStart2x;

        int allNotNull = 0;

        for (int i = 0; i < curBeat; i++) {

            if (i < sequence.length) {
                Location s = sequence[i];
                if (s != null) {
                    allNotNull++;
                }
            }

        }

        int idx = 0;
        for (int i = 0; i < curBeat; i++) {

            if (i < sequence.length) {
                Location s = sequence[i];
                if (s != null) {

                    double x = -(allNotNull - idx - 1) * spacing - Easing.EASE_OUT_CIRC.getFunction().apply(Math.min(1, beatCount2xDouble - beatCount2x)) * spacing;
                    double y = RenderSystem.getHeight() - height;


                    if (curBeat > sequence.length || sequence[curBeat - 1] == null)
                        x = -(allNotNull - idx - 1) * spacing - spacing;

                    if (sequence.length == 1) {
                        x = -(allNotNull - idx - 1) * spacing - Easing.EASE_OUT_CIRC.getFunction().apply(Math.min(1, (playBackTime - this.getMillisPerBeat() * (beatStart2x + 1) * .5) / (this.getMillisPerBeat() * 3))) * spacing;
                    }
                    Image.drawLinear(sequence[i], x, y, width, height, Image.Type.Normal);

                    idx ++;
                }
            }

        }

    }

    @Override
    public boolean isApplicable(long id) {
        return id == 1875436617L;
    }

    @Override
    public long waitTime(long id) {
        return 2172;
    }
}
