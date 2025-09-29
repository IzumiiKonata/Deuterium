package com.jsyn.util.soundfile;

import com.jsyn.data.FloatSample;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author IzumiiKonata
 * Date: 2025/5/5 12:47
 */
public class StreamedSampleLoader extends CustomSampleLoader {

    public FloatSample loadFloatSample(RandomAccessFile raf) throws IOException {
        StreamedIFFParser parser = new StreamedIFFParser(raf);
        parser.readHead();
        if (parser.isRIFF()) {
            StreamedWAVEFileParser fileParser = new StreamedWAVEFileParser();
            return fileParser.load(parser);
        } else if (parser.isIFF()) {
            throw new UnsupportedOperationException("AIFF is not supported");
        } else {
            throw new IOException("Unsupported audio file type.");
        }
    }

}
