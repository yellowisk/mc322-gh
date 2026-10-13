package domain.exceptions;

/**
 * Lançada quando tentam atualizar uma demanda c/ uma quantidade inválida.
 *
 * @author Heitor Almeida
 */
public class InvalidDemandAmountException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**, ex.: negativo
     * Cria a exceção c/ a mensagem que vai aparecer no menu
     *
     * @param message motivo da quantidade ser inválida
     */
    public InvalidDemandAmountException(String message) {
        super(message);
    }
}
