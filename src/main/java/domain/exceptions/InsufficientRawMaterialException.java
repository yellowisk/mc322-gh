package domain.exceptions;

public class InsufficientRawMaterialException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InsufficientRawMaterialException(String message) {
        super(message);
    }
}
