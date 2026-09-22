package domain.exceptions;

public class MachineNeedsRepairException extends RuntimeException {
    public MachineNeedsRepairException(String message) {
        super(message);
    }
}
