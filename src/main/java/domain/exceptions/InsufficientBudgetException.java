package domain.exceptions;

/**
 * Lançada quando o orçamento não cobre uma operação: comprar matéria-prima, iniciar a fabricação de uma
 * demanda cujo custo estimado é maior que a verba, ou operar uma máquina no meio da produção
 *
 * @author Glayson Oliveira
 */
public class InsufficientBudgetException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public InsufficientBudgetException(String message) {
        super(message);
    }
}
