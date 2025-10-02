package tech.konata.commons.ncm.math;

public interface BinaryEncoder extends Encoder {
    byte[] encode(byte[] var1) throws EncoderException;
}
