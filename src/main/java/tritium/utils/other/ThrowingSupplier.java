package tritium.utils.other;

@FunctionalInterface
public interface ThrowingSupplier<S> {

    S get() throws Exception;

}