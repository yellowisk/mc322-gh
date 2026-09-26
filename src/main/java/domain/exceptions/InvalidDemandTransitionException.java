package domain.exceptions;

public class InvalidDemandTransitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidDemandTransitionException(String message) {
        super(message);
    }
}
