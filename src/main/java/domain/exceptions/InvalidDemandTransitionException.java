package domain.exceptions;

public class InvalidDemandTransitionException extends RuntimeException {
    public InvalidDemandTransitionException(String message) {
        super(message);
    }
}
