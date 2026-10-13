package domain.exceptions;

/**
 * Exception para quando uma máquina estiver quebrada
 */
public class MachineNeedsRepairException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MachineNeedsRepairException(String message) {
        super(message);
    }
}
