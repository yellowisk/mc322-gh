package domain.exceptions;

public class MachineUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MachineUnavailableException(String message) {
        super(message);
    }
}
