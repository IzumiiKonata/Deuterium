package com.jsyn.util.soundfile;

import com.jsyn.data.SampleMarker;
import com.jsyn.util.SampleLoader;

import java.io.IOException;

/**
 * @author IzumiiKonata
 * Date: 2025/5/5 12:48
 */
public class StreamedWAVEFileParser extends WAVEFileParser {

    public StreamedFloatSample load(StreamedIFFParser parser) throws IOException {
        this.parser = parser;
        parser.parseAfterHead(this);
        return finish();
    }

    void parseFmtChunk(IFFParser parser, int ckSize) throws IOException {
        format = parser.readShortLittle();
        samplesPerFrame = parser.readShortLittle();
//        System.out.println("Samples Per Frame: " + samplesPerFrame);
        frameRate = parser.readIntLittle();
        parser.readIntLittle(); /* skip dwAvgBytesPerSec */
        blockAlign = parser.readShortLittle();
        bitsPerSample = parser.readShortLittle();
        bytesPerFrame = blockAlign;
        bytesPerSample = bytesPerFrame / samplesPerFrame;
        samplesPerBlock = (8 * blockAlign) / bitsPerSample;

        if (format == WAVE_FORMAT_EXTENSIBLE) {
            int extraSize = parser.readShortLittle();
            short validBitsPerSample = parser.readShortLittle();
            int channelMask = parser.readIntLittle();
            byte[] guid = new byte[16];
            parser.read(guid);
            if (matchBytes(guid, KSDATAFORMAT_SUBTYPE_IEEE_FLOAT)) {
                format = WAVE_FORMAT_IEEE_FLOAT;
            } else if (matchBytes(guid, KSDATAFORMAT_SUBTYPE_PCM)) {
                format = WAVE_FORMAT_PCM;
            }
        }
        if ((format != WAVE_FORMAT_PCM) && (format != WAVE_FORMAT_IEEE_FLOAT)) {
            throw new IOException(
                    "Only WAVE_FORMAT_PCM and WAVE_FORMAT_IEEE_FLOAT supported. format = " + format);
        }
        if ((bitsPerSample != 16) && (bitsPerSample != 24) && (bitsPerSample != 32)) {
            throw new IOException(
                    "Only 16 and 24 bit PCM or 32-bit float WAV files supported. width = "
                            + bitsPerSample);
        }
    }

    @Override
    public void handleChunk(IFFParser parser, int ckID, int ckSize) throws IOException {
//        System.out.println("!handleChunk, ckID: " + ckID + ", ckSize: " + ckSize);
        switch (ckID) {
            case FMT_ID:
//                System.out.println("ParseFmtChunk");
                parseFmtChunk(parser, ckSize);
                break;
            case DATA_ID:
//                System.out.println("ParseDataChunk");
                parseDataChunk(parser, ckSize);
                break;
            /*case CUE_ID:
                parseCueChunk(parser, ckSize);
                break;
            case FACT_ID:
                parseFactChunk(parser, ckSize);
                break;
            case SMPL_ID:
                parseSmplChunk(parser, ckSize);
                break;
            case LABL_ID:
                parseLablChunk(parser, ckSize);
                break;
            case LTXT_ID:
                parseLtxtChunk(parser, ckSize);
                break;*/
            default:
                break;
        }
    }

    private int convertByteToFrame(int byteOffset) throws IOException {
        if (blockAlign == 0) {
            throw new IOException("WAV file has bytesPerBlock = zero");
        }
        if (samplesPerFrame == 0) {
            throw new IOException("WAV file has samplesPerFrame = zero");
        }
        return (samplesPerBlock * byteOffset) / (samplesPerFrame * blockAlign);
    }

    private int calculateNumFrames(int numBytes) throws IOException {
        int nFrames;
        if (numFactSamples > 0) {
            // nFrames = numFactSamples / samplesPerFrame;
            nFrames = numFactSamples; // FIXME which is right
        } else {
            nFrames = convertByteToFrame(numBytes);
        }
        return nFrames;
    }

    StreamedByteDataReader reader;

    public static class StreamedByteDataReader {

        public final StreamedIFFParser parser;
        public final int totalSize, bitsPerSample, numFrames, samplesPerFrame, format;

        private final long startPos;

        public StreamedByteDataReader(StreamedIFFParser parser, int totalSize, int numFrames, int samplesPerFrame, int bitsPerSample, int format) throws IOException {
            this.parser = parser;
            this.totalSize = totalSize;
            this.bitsPerSample = bitsPerSample;
            this.numFrames = numFrames;
            this.samplesPerFrame = samplesPerFrame;
            this.format = format;
            this.startPos = parser.getPos();
        }

        byte[] byteData;
        float[] floatData;

        public final int floatArrLen = 4096;

        {
            int arrLen = this.getArrSize(floatArrLen);
            byteData = new byte[arrLen];
            floatData = new float[floatArrLen];
        }

        public int getFactor() {
            int factor = 2;

            if (bitsPerSample == 24) {
                // 3b to 1f
                factor = 3;
            } else if (bitsPerSample == 32) {
                // 4b to 1f
                factor = 4;
            }

            return factor;
        }

        public int getArrSize(int outputSize) {
            return outputSize * getFactor();
        }

        public float[] read(long pos) throws IOException {

            this.parser.seek(this.startPos + pos);

            int arrSize = this.getArrSize(floatArrLen);

            if (byteData.length != arrSize) {
                byteData = new byte[arrSize];
                floatData = new float[floatArrLen];
            }

            this.parser.read(byteData);

            if (bitsPerSample == 16) {
                SampleLoader.decodeLittleI16ToF32(byteData, 0, byteData.length, floatData, 0);
            } else if (bitsPerSample == 24) {
                // 3b to 1f
                SampleLoader.decodeLittleI24ToF32(byteData, 0, byteData.length, floatData, 0);
            } else if (bitsPerSample == 32) {
                // 4b to 1f
                if (format == WAVE_FORMAT_IEEE_FLOAT) {
                    SampleLoader.decodeLittleF32ToF32(byteData, 0, byteData.length, floatData, 0);
                } else if (format == WAVE_FORMAT_PCM) {
                    SampleLoader.decodeLittleI32ToF32(byteData, 0, byteData.length, floatData, 0);
                }
            }

            return floatData;

        }

    }

    public void parseDataChunk(IFFParser parser, int ckSize) throws IOException {
        dataPosition = parser.getOffset();

//        byteData = new byte[ckSize];
//        numRead = parser.read(byteData);
        numFrames = calculateNumFrames(ckSize);
        reader = new StreamedByteDataReader((StreamedIFFParser) parser, ckSize, numFrames, samplesPerFrame, bitsPerSample, format);
    }

    @Override
    public StreamedFloatSample finish() throws IOException {
        return makeSample(reader);
    }

    StreamedFloatSample makeSample(StreamedByteDataReader reader) throws IOException {
        StreamedFloatSample floatSample = new StreamedFloatSample(reader, numFrames, samplesPerFrame);

        floatSample.setChannelsPerFrame(samplesPerFrame);
        floatSample.setFrameRate(frameRate);
        floatSample.setPitch(originalPitch);

        if (sustainBegin >= 0) {
            floatSample.setSustainBegin(sustainBegin);
            floatSample.setSustainEnd(sustainEnd);
        }

        for (SampleMarker marker : cueMap.values()) {
            floatSample.addMarker(marker);
        }

        /* Set Sustain Loop by assuming first two markers are loop points. */
        if (floatSample.getMarkerCount() >= 2) {
            floatSample.setSustainBegin(floatSample.getMarker(0).position);
            floatSample.setSustainEnd(floatSample.getMarker(1).position);
        }
        return floatSample;
    }

}
