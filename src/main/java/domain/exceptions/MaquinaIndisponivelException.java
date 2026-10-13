package domain.exceptions;

/**
 * Lançada quando tentam usar um equipamento que não está disponível: uma máquina ou esteira desligada,
 * ou o reparo de uma máquina que não precisa de reparo
 *
 * @author Heitor Almeida
 * @author Glayson Oliveira
 */
public class MaquinaIndisponivelException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message o motivo do equipamento estar indisponível
     */
    public MaquinaIndisponivelException(String message) {
        super(message);
    }
}
