package tech.konata.obfuscator.exceptions;

public class StackEmulationException extends Exception { // Force handling
    public StackEmulationException(String msg) {
        super(msg);
    }
}
