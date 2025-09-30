package tritium.rendering;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic on quantize
 * 主题色提取（中位切分法）
 *
 * @author wjw
 * @example // array of pixels as [R,G,B] arrays
 * int[][] arrayOfPixels = new int[][]{
 * {190, 197, 190},
 * {202, 204, 200},
 * {207, 214, 210},
 * {211, 214, 211},
 * {205, 207, 207}
 * };
 * int maximumColorCount = 4;
 *
 * MMCQ mmcq = new MMCQ(arrayOfPixels, maximumColorCount);
 * List<int[]> palette = mmcq.palette();
 */
public class MMCQ {

    private static final int SIGBITS = 5;
    private static final int RSHIFT = 8 - SIGBITS;
    private static final int MAX_ITERATIONS = 1000;
    private static final double FRACT_BY_POPULATIONS = 0.75;

    public static List<int[]> getDominantColors(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        float[] dots = new float[]{0.15f, 0.35f, 0.5f, 0.7f, 0.85f};
        List<int[]> sampleDots = new ArrayList<>();
        for(float dw : dots){
            for(float dh : dots){
                int rgbVal = bi.getRGB((int)(w*dw), (int)(h*dh));
                Color c = new Color(rgbVal);
                sampleDots.add(new int[]{c.getRed(), c.getGreen(), c.getBlue()});
            }
        }
        MMCQ mmcq = new MMCQ(sampleDots.toArray(new int[][]{}), 25);
        return mmcq.palette();
    }


    private final PQueue<VBox> pqueue;

    /**
     * @apiNote 将rgb压缩成1维
     */
    private int getColorIndex(int r, int g, int b) {
        return (r << (2 * SIGBITS)) + (g << SIGBITS) + b;
    }

    /**
     * @apiNote 存储VBox的队列
     */
    private class PQueue<T> {
        private final Comparator<T> comparator;

        private LinkedList<T> contents = new LinkedList<>();

        private boolean sorted = false;

        public PQueue(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        private void sort() {
            contents.sort(comparator);
            sorted = true;
        }

        /**
         * @param t 元素
         * @apiNote 将元素插入到队列第一位
         */
        public void addFirst(T t) {
            contents.addFirst(t);
            sorted = false;
        }

        /**
         * @param index 下标
         * @apiNote 根据下标获取元素
         */
        public T get(int index) {
            if (!sorted) sort();
            return contents.get(index);
        }

        /**
         * @apiNote 删除并返回最后一个元素
         */
        public T pop() {
            if (!sorted) sort();
            return contents.pop();
        }

        public int size() {
            return contents.size();
        }

        public boolean addAll(PQueue<T> c) {
            return contents.addAll(c.contents);
        }

        public LinkedList<T> getContents() {
            if (!sorted) sort();
            return contents;
        }
    }

    /**
     * @apiNote 像素体
     */
    public class VBox {
        private int r1;
        private int r2;
        private int g1;
        private int g2;
        private int b1;
        private int b2;
        private final int[] histo;

        private boolean _flgCount = false;
        private int _count = 0;

        private int _volume = 0;

        private int[] _avg;

        public VBox(int r1, int r2, int g1, int g2, int b1, int b2, int[] histo) {
            this.r1 = r1;
            this.r2 = r2;
            this.g1 = g1;
            this.g2 = g2;
            this.b1 = b1;
            this.b2 = b2;
            this.histo = histo;
        }

        /**
         * @apiNote 计算像素体的体积
         */
        public int volume() {
            return volume(false);
        }

        public int volume(boolean force) {
            if (_volume == 0 || force) {
                _volume = (r2 - r1 + 1) * (g2 - g1 + 1) * (b2 - b1 + 1);
            }
            return _volume;
        }

        /**
         * @apiNote 统计总的像素数
         */
        public int count() {
            return count(false);
        }

        public int count(boolean force) {
            if (!_flgCount || force) {
                int index;
                int npix = 0;
                for (int i = r1; i <= r2; i++) {
                    for (int j = g1; j <= g2; j++) {
                        for (int k = b1; k <= b2; k++) {
                            index = getColorIndex(i, j, k);
                            npix += histo[index];
                        }
                    }
                }
                _count = npix;
                _flgCount = true;
            }
            return _count;
        }

        public VBox copy() {
            return new VBox(r1, r2, g1, g2, b1, b2, histo);
        }

        public int[] avg() {
            return avg(false);
        }

        public int[] avg(boolean force) {
            if (_avg == null || force) {
                int ntot = 0;
                int mult = 1 << (8 - SIGBITS);
                int rsum = 0;
                int gsum = 0;
                int bsum = 0;
                int hval;
                int histoindex;
                for (int i = r1; i <= r2; i++) {
                    for (int j = g1; j <= g2; j++) {
                        for (int k = b1; k <= b2; k++) {
                            histoindex = getColorIndex(i, j, k);
                            hval = histo[histoindex];
                            ntot += hval;
                            rsum += (hval * i * mult);
                            gsum += (hval * j * mult);
                            bsum += (hval * k * mult);
                        }
                    }
                }
                if (ntot > 0) {
                    _avg = new int[]{(rsum / ntot + mult / 2), (gsum / ntot + mult / 2), (bsum / ntot + mult / 2)};
                } else {
                    // empty box
                    _avg = new int[]{(mult * (r1 + r2 + 1) / 2), (mult * (g1 + g2 + 1) / 2), (mult * (b1 + b2 + 1) / 2)};
                }
            }
            return _avg;
        }

        /**
         * @param pixel 像素点
         * @apiNote 判断像素是否包含在当前像素体中
         */
        public boolean contains(int[] pixel) {
            int rval = pixel[0] >> RSHIFT;
            int gval = pixel[1] >> RSHIFT;
            int bval = pixel[2] >> RSHIFT;
            return (rval >= r1 && rval <= r2 &&
                    gval >= g1 && gval <= g2 &&
                    bval >= b1 && bval <= b2);
        }
    }


    /**
     * @param pixels rgb像素数组
     * @apiNote 将rgb三维像素压缩成一维，并统计每种像素的数量
     */
    private int[] getHisto(int[][] pixels) {
        int histosize = 1 << (3 * SIGBITS);
        int[] histo = new int[histosize];
        int index, rval, gval, bval;
        for (int[] pixel : pixels) {
            // 将8位的颜色值，压缩成5位，代价是损失了2^3=8个点的精度
            rval = pixel[0] >> RSHIFT;
            gval = pixel[1] >> RSHIFT;
            bval = pixel[2] >> RSHIFT;
            index = getColorIndex(rval, gval, bval);
            histo[index] = histo[index] + 1;
        }
        return histo;
    }

    /**
     * @param pixels 像素数组
     * @param histo  像素统计数组
     * @apiNote 创建像素体
     */
    private VBox vboxFromPixels(int[][] pixels, int[] histo) {
        int rmin = 1000000,
                rmax = 0,
                gmin = 1000000,
                gmax = 0,
                bmin = 1000000,
                bmax = 0,
                rval, gval, bval;
        for (int[] pixel : pixels) {
            rval = pixel[0] >> RSHIFT;
            gval = pixel[1] >> RSHIFT;
            bval = pixel[2] >> RSHIFT;
            if (rval < rmin) rmin = rval;
            else if (rval > rmax) rmax = rval;
            if (gval < gmin) gmin = gval;
            else if (gval > gmax) gmax = gval;
            if (bval < bmin) bmin = bval;
            else if (bval > bmax) bmax = bval;
        }
        return new VBox(rmin, rmax, gmin, gmax, bmin, bmax, histo);
    }

    private void iter(PQueue<VBox> lh, double target, int[] histo) {
        int ncolors = lh.size();
        int niters = 0;
        VBox vbox;
        while (niters < MAX_ITERATIONS) {
            vbox = lh.pop();
            // PQueue每次addFirst后再pop都会重新排序
            if (vbox.count() == 0) { /* just put it back */
                lh.addFirst(vbox);
                niters++;
                continue;
            }
            // do the cut
            VBox[] vboxes = medianCutApply(histo, vbox);
            VBox vbox1 = vboxes[0];
            VBox vbox2 = vboxes[1];

            if (vbox1 == null) {
                // System.out.println("vbox1 not defined; shouldn't happen!");
                return;
            }
            lh.addFirst(vbox1);
            if (vbox2 != null) { /* vbox2 can be null */
                lh.addFirst(vbox2);
                ncolors++;
            }
            if (ncolors >= target) {
                return;
            };
            niters++;
        }
    }

    /**
     * @param histo 像素数组
     * @param vbox  要切分的像素体
     * @return 切分后的像素体数组
     * @apiNote 中位切分法
     */
    private VBox[] medianCutApply(int[] histo, VBox vbox) {
        VBox[] result = new VBox[2];
        if (vbox.count() == 0) return result;

        int rw = vbox.r2 - vbox.r1 + 1;
        int gw = vbox.g2 - vbox.g1 + 1;
        int bw = vbox.b2 - vbox.b1 + 1;
        // rgb哪个跨度更大，就往哪个方向切
        int maxw = Math.max(Math.max(rw, gw), bw);
        // 如果只有一个像素，则不切割
        if (vbox.count() == 1) {
            result[0] = vbox.copy();
            return result;
        }
        /* Find the partial sum arrays along the selected axis. */
        int total = 0;
        // 存储累加的像素数量，partialsum[5]表示前5种颜色的像素总数
        int[] partialsum = new int[maxw];
        int[] lookaheadsum = new int[maxw];
        int sum;
        int index;

        if (maxw == rw) {
            for (int i = vbox.r1; i <= vbox.r2; i++) {
                sum = 0;
                for (int j = vbox.g1; j <= vbox.g2; j++) {
                    for (int k = vbox.b1; k <= vbox.b2; k++) {
                        index = getColorIndex(i, j, k);
                        sum += histo[index];
                    }
                }
                total += sum;
                partialsum[i - vbox.r1] = total;
            }
        } else if (maxw == gw) {
            for (int i = vbox.g1; i <= vbox.g2; i++) {
                sum = 0;
                for (int j = vbox.r1; j <= vbox.r2; j++) {
                    for (int k = vbox.b1; k <= vbox.b2; k++) {
                        index = getColorIndex(j, i, k);
                        sum += histo[index];
                    }
                }
                total += sum;
                partialsum[i - vbox.g1] = total;
            }
        } else { /* maxw == bw */
            for (int i = vbox.b1; i <= vbox.b2; i++) {
                sum = 0;
                for (int j = vbox.r1; j <= vbox.r2; j++) {
                    for (int k = vbox.g1; k <= vbox.g2; k++) {
                        index = getColorIndex(j, k, i);
                        sum += histo[index];
                    }
                }
                total += sum;
                partialsum[i - vbox.b1] = total;
            }
        }
        int pnum;
        for (int i = 0; i < partialsum.length; i++) {
            pnum = partialsum[i];
            if (pnum > 0) {
                lookaheadsum[i] = total - pnum;
            }
        }

        if (maxw == rw) {
            for (int i = vbox.r1; i <= vbox.r2; i++) {
                if (partialsum[i - vbox.r1] > total / 2) { // 到达像素数量的中位数
                    VBox vbox1 = vbox.copy();
                    VBox vbox2 = vbox.copy();
                    int left = i - vbox.r1;
                    int right = vbox.r2 - i;
                    int d2;
                    // i是中位数下标，d2是要切割的下标
                    // 哪边大，就从哪边下刀
                    if (left <= right) {
                        d2 = Math.min(vbox.r2 - 1, (int)(i + (float)right / 2));
                    } else {
                        d2 = Math.max(vbox.r1, (int)(i - 1 - (float)left / 2));
                    }
                    // avoid 0-count boxes
                    while (partialsum[d2 - vbox.r1] == 0) d2++;
                    int count2 = lookaheadsum[d2 - vbox.r1];
                    while (count2 == 0 && partialsum[d2 - 1 - vbox.r1] > 0)
                        count2 = lookaheadsum[--d2 - vbox.r1];
                    // set dimensions
                    vbox1.r2 = d2;
                    vbox2.r1 = d2 + 1;
                    result[0] = vbox1;
                    result[1] = vbox2;
                    return result;
                }
            }
        } else if (maxw == gw) {
            for (int i = vbox.g1; i <= vbox.g2; i++) {
                if (partialsum[i - vbox.g1] > total / 2) { // 到达像素数量的中位数
                    VBox vbox1 = vbox.copy();
                    VBox vbox2 = vbox.copy();
                    int left = i - vbox.g1;
                    int right = vbox.g2 - i;
                    int d2;
                    // i是中位数下标，d2是要切割的下标
                    if (left <= right) {
                        d2 = Math.min(vbox.g2 - 1, (int)(i + (float)right / 2));
                    } else {
                        d2 = Math.max(vbox.g1, (int)(i - 1 - (float)left / 2));
                    }
                    // avoid 0-count boxes
                    while (partialsum[d2 - vbox.g1] == 0) d2++;
                    int count2 = lookaheadsum[d2 - vbox.g1];
                    while (count2 == 0 && partialsum[d2 - 1 - vbox.g1] > 0)
                        count2 = lookaheadsum[--d2 - vbox.g1];
                    // set dimensions
                    vbox1.g2 = d2;
                    vbox2.g1 = d2 + 1;
                    result[0] = vbox1;
                    result[1] = vbox2;
                    return result;
                }
            }
        } else {
            for (int i = vbox.b1; i <= vbox.b2; i++) {
                if (partialsum[i - vbox.b1] > total / 2) { // 到达像素数量的中位数
                    VBox vbox1 = vbox.copy();
                    VBox vbox2 = vbox.copy();
                    int left = i - vbox.b1;
                    int right = vbox.b2 - i;
                    int d2;
                    // i是中位数下标，d2是要切割的下标
                    if (left <= right) {
                        d2 = Math.min(vbox.b2 - 1, (int)(i + (float)right / 2));
                    } else {
                        d2 = Math.max(vbox.b1, (int)(i - 1 - (float)left / 2));
                    }
                    // avoid 0-count boxes
                    while (partialsum[d2 - vbox.b1] == 0) d2++;
                    int count2 = lookaheadsum[d2 - vbox.b1];
                    while (count2 == 0 && partialsum[d2 - 1 - vbox.b1] > 0)
                        count2 = lookaheadsum[--d2 - vbox.b1];
                    // set dimensions
                    vbox1.b2 = d2;
                    vbox2.b1 = d2 + 1;
                    result[0] = vbox1;
                    result[1] = vbox2;
                    return result;
                }
            }
        }
        return result;
    }

    public MMCQ(int[][] pixels, int maxcolors) throws RuntimeException {
        // short-circuit
        if (pixels.length == 0 || maxcolors < 2 || maxcolors > 256) {
            throw new RuntimeException("wrong number of maxcolors");
        }
        // 将rgb三维映射到一维
        // 因为rgb长度太大 256 * 256 * 256，所以将2^8 压缩到 2^5,也就是 32 * 32 * 32
        // 这样处理会损失低3位的数据，0~7=0,8~15=8
        // 高5位是r，中5位是g，低5位是b
        int[] histo = getHisto(pixels);
        // check that we aren't below maxcolors already
        int nColors = 0;
        for (int h : histo) {
            if (h > 0) nColors++;
        }
        if (nColors <= maxcolors) {
            // 像素种类小于maxcolors时的处理
            // XXX: generate the new colors from the histo and return
        }
        // get the beginning vbox from the colors
        VBox vbox = vboxFromPixels(pixels, histo);
        PQueue<VBox> pq = new PQueue<>((a, b) -> Integer.compare(b.count(), a.count()));
        pq.addFirst(vbox);

        // 第一组，以VBox所包含的像素数作为优先级
        iter(pq, FRACT_BY_POPULATIONS * maxcolors, histo);

        // Re-sort by the product of pixel occupancy times the size in color space.
        PQueue<VBox> pq2 = new PQueue<>((a, b) -> Integer.compare(b.count() * b.volume(), a.count() * a.volume()));
        pq2.addAll(pq);

        // 第二组，以VBox体积*包含像素数作为优先级
        iter(pq2, maxcolors, histo);

        pqueue = pq2;
    }

    /**
     * @apiNote 获取主题色
     */
    public List<int[]> palette() {
        return pqueue.getContents().stream().map(VBox::avg).collect(Collectors.toList());
    }

    public int size() {
        return pqueue.size();
    }

    /**
     * @param color rgb颜色
     * @apiNote 根据颜色获取相近的主题色
     */
    public int[] map(int[] color) {
        for (VBox vBox : pqueue.getContents()) {
            if (vBox.contains(color)) {
                return vBox.avg();
            }
        }
        return nearest(color);
    }

    /**
     * @param color rgb颜色
     * @apiNote 根据颜色获取相近的主题色(优先使用map方法)
     */
    private int[] nearest(int[] color) {
        double cur;
        double min = 0;
        VBox nearBox = null;
        for (VBox vBox : pqueue.getContents()) {
            cur = Math.sqrt(
                    Math.pow(color[0] - vBox.avg()[0], 2) +
                            Math.pow(color[1] - vBox.avg()[1], 2) +
                            Math.pow(color[2] - vBox.avg()[2], 2)
            );
            if (cur < min || min == 0) {
                min = cur;
                nearBox = vBox;
            }
        }
        if (nearBox == null) return null;
        return nearBox.avg();
    }
}