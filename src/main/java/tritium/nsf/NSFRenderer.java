package tritium.nsf;

import com.jsyn.util.SampleLoader;
import com.jsyn.util.WaveFileWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.util.Tuple;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;
import zdream.nsfplayer.sound.AbstractNsfSound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2024/12/14 21:43
 */
@UtilityClass
public class NSFRenderer {

    @SneakyThrows
    public List<Tuple<File, NSFRenderInfo>> export(String nsfPath, File outputDir) {
        NsfAudioFactory factory = new NsfAudioFactory();
        NsfAudio nsf;

        try {
            if (nsfPath.startsWith("/")) {
                InputStream is = NSFRenderer.class.getResourceAsStream(nsfPath);
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                nsf = factory.create(buffer);
            } else {
                nsf = factory.createFromFile(nsfPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        NsfRenderer renderer = new NsfRenderer();
        renderer.ready(nsf, 0);

        System.out.println("Channels: " + renderer.allChannelSet().size());

        rendererList = new NsfRenderer[renderer.allChannelSet().size()];
        finished = new boolean[rendererList.length];
        
        List<CompletableFuture<Tuple<File, NSFRenderInfo>>> tasks = new ArrayList<>();

        for (int i = 0; i < renderer.allChannelSet().size(); i++) {

            NsfRenderer r = new NsfRenderer();
            r.ready(nsf, 0);

            ArrayList<Byte> channels = new ArrayList<>(r.allChannelSet());
            for (Byte b : r.allChannelSet()) {
                int indexOf = channels.indexOf(b);

                if (indexOf != i) {
                    r.setChannelMuted(b, true);
                }
            }

            rendererList[i] = r;

            tasks.add(render(r, i, outputDir));
        }

        CompletableFuture<Void> allDone = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));

        allDone.join();

        return tasks.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    static NsfRenderer[] rendererList;
    static boolean[] finished;

    private static CompletableFuture<Tuple<File, NSFRenderInfo>> render(NsfRenderer renderer, int channel, File outputDir) {

        AbstractNsfSound sound = renderer.getExecutor().getSound(renderer.getChannels()[channel].getChannelCode());
        NSFRenderInfo info = new NSFRenderInfo(channel, sound);

        CompletableFuture<Tuple<File, NSFRenderInfo>> future = new CompletableFuture<>();
        FutureTaskWrapper<Tuple<File, NSFRenderInfo>> task = new FutureTaskWrapper<>(new FutureTaskWrapper.Task<Tuple<File, NSFRenderInfo>>() {
            @Override
            @SneakyThrows
            public Tuple<File, NSFRenderInfo> run() {
                int length = 4096;

                File outputFile = new File(outputDir, channel + ".wav");
                outputFile.createNewFile();
                WaveFileWriter waveFileWriter = new WaveFileWriter(outputFile);

                waveFileWriter.setFrameRate(48000);
                waveFileWriter.setSamplesPerFrame(1);
                waveFileWriter.setBitsPerSample(16);

                byte[] bytes = new byte[length * 2];
                short[] channelArray = new short[length];
                float[] convertArray = new float[length];

                boolean started = false;

                do {
                    int size = renderer.render(channelArray, 0, channelArray.length);

                    if (!started && !sameArray(channelArray))
                        started = true;

                    if (started && sameArray(channelArray)) {
                        finished[channel] = true;
                    }

                    shortArrayToByteArray(channelArray, bytes);
                    SampleLoader.decodeLittleI16ToF32(bytes, 0, bytes.length, convertArray, 0);
                    waveFileWriter.write(convertArray);

                    //                    System.out.println("[" + channel + "] Rendered Cycle");
                } while (!allFinished());

                waveFileWriter.close();
                
                return Tuple.of(outputFile, info);
            }
        }, future);

        new Thread(task).start();
        return future;
    }

    private static class FutureTaskWrapper<T> implements Runnable {
        private final Task<T> runnable;
        private final CompletableFuture<T> future;

        public FutureTaskWrapper(Task<T> runnable, CompletableFuture<T> future) {
            this.runnable = runnable;
            this.future = future;
        }

        @Override
        public void run() {
            try {
                future.complete(runnable.run());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }

        public interface Task<T> {
            T run();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class NSFRenderInfo {

        @Getter
        private final int channel;

        @Getter
        private final AbstractNsfSound soundRenderer;

    }

    private static boolean allFinished() {
        for (boolean b : finished) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameArray(short[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] != array[i - 1]) {
                return false;
            }
        }
        return true;
    }

    public static void shortArrayToByteArray(short[] from, byte[] to) {
        if (from == null) {
            return;
        }

        for (int i = 0; i < from.length; i++) {
            // 将每个short值转换为两个byte值
            int val = from[i];
            to[i * 2] = (byte) (val & 0xFF);
            to[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
    }

    public static void busyWaitMicros(long micros){
        long waitUntil = System.nanoTime() + (micros * 1_000);
        while(waitUntil > System.nanoTime()){
            ;
        }
    }

}
