package domain.exceptions;

public class MachineNeedsRepairException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MachineNeedsRepairException(String message) {
        super(message);
    }
}
