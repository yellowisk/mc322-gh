package domain.exceptions;

/**
 * Lançada quando mandam fabricar pela estratégia mas nenhuma estratégia de produção foi escolhida
 *
 * @author Glayson Oliveira
 */
public class NoStrategySelectedException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public NoStrategySelectedException(String message) {
        super(message);
    }
}
