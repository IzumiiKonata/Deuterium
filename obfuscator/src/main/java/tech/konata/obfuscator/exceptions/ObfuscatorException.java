package tech.konata.obfuscator.exceptions;

public class ObfuscatorException extends RuntimeException {
    public ObfuscatorException() {
        super();
    }

    public ObfuscatorException(String msg) {
        super(msg);
    }

    public ObfuscatorException(Throwable t) {
        super(t);
    }
}
