package domain.exceptions;

public class NullDemandException extends RuntimeException {
    public NullDemandException(String message) {
        super(message);
    }
}
