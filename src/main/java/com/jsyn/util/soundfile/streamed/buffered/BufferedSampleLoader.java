package com.jsyn.util.soundfile.streamed.buffered;

import com.jsyn.data.FloatSample;
import com.jsyn.util.soundfile.CustomSampleLoader;
import com.jsyn.util.soundfile.WAVEFileParser;
import lombok.SneakyThrows;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.sound.spi.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * @author IzumiiKonata
 * Date: 2025/5/5 12:47
 */
public class BufferedSampleLoader extends CustomSampleLoader {

    public FloatSample loadFloatSample(BufferedInputStream is) throws IOException {
        BufferedIFFParser parser = new BufferedIFFParser(is);
        parser.readHead();
        if (parser.isRIFF()) {
            BufferedWAVEFileParser fileParser = new BufferedWAVEFileParser();
            return fileParser.load(parser);
        } else if (parser.isIFF()) {
            throw new UnsupportedOperationException("AIFF is not supported");
        } else {
            throw new IOException("Unsupported audio file type.");
        }
    }

    @SneakyThrows
    public FloatSample loadFromFlacStream(BufferedInputStream is) throws IOException {
        FlacAudioFileReader fafr = new FlacAudioFileReader();
        is.mark(0);

        StreamInfo streamInfo = fafr.getStreamInfo(is);

        AudioFileFormat fmt = new AudioFileFormat(FlacFileFormatType.FLAC, new FlacAudioFormat(streamInfo), AudioSystem.NOT_SPECIFIED);

        is.reset();

        BufferedInputStream is1 = new BufferedInputStream(new Flac2PcmAudioInputStream(is,
                new AudioFormat(
                        FlacEncoding.FLAC, streamInfo.getSampleRate(), (int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples()), streamInfo.getChannels(), streamInfo.getBitsPerSample(), streamInfo.getSampleRate(), false
                ),
                (int) streamInfo.getTotalSamples()),
                (int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples() / 4)
        );
        is1.mark(0);

        System.out.println((int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples() / 8));

        System.out.println("(int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples() / 8) = " + (int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples() / 2));
        System.out.println("(int) streamInfo.getTotalSamples() = " + (int) streamInfo.getTotalSamples());
        System.out.println("streamInfo.getChannels() = " + streamInfo.getChannels());
        System.out.println("streamInfo.getBitsPerSample() = " + streamInfo.getBitsPerSample());
        System.out.println("streamInfo.getSampleRate() = " + streamInfo.getSampleRate());

        BufferedIFFParser parser = new BufferedIFFParser(is1) {

        };
        parser.totalBytes = (int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples());
        BufferedWAVEFileParser.StreamedByteDataReader reader = new BufferedWAVEFileParser.StreamedByteDataReader(parser, (int) (streamInfo.getBitsPerSample() * streamInfo.getTotalSamples() / 2), (int) streamInfo.getTotalSamples() * streamInfo.getChannels(), streamInfo.getChannels(), streamInfo.getBitsPerSample(), WAVEFileParser.WAVE_FORMAT_PCM);

        BufferedFloatSample floatSample = new BufferedFloatSample(reader, (int) streamInfo.getTotalSamples(), streamInfo.getChannels());

        floatSample.setChannelsPerFrame(streamInfo.getChannels());
        floatSample.setFrameRate(streamInfo.getSampleRate());
        floatSample.setPitch(60.0);

        return floatSample;
    }

}
