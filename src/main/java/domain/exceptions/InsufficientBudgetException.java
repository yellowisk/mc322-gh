package domain.exceptions;

public class InsufficientBudgetException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InsufficientBudgetException(String message) {
        super(message);
    }
}