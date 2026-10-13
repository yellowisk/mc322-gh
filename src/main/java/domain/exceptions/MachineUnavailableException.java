package domain.exceptions;

/**
 * Lançada quando tentam usar uma máquina que não está disponível.
 *
 * @author Heitor Almeida
 */
public class MachineUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que vai aparecer pro usuário
     *
     * @param message motivo da máquina estar indisponível
     */
    public MachineUnavailableException(String message) {
        super(message);
    }
}
