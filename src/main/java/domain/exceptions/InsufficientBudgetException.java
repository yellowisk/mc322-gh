package domain.exceptions;

/**
 * Exception para quando não houver orçamento suficiente
 */
public class InsufficientBudgetException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InsufficientBudgetException(String message) {
        super(message);
    }
}