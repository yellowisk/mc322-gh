package domain.exceptions;

public class InvalidDemandException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidDemandException(String message) {
        super(message);
    }
}
