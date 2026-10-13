package domain.exceptions;

/**
 * Lançada quando uma {@link domain.entities.demand.Demand} tenta ir pra um status que
 * {@link domain.entities.demand.DemandStatus#canTransitionTo} não permite, ex.: COMPLETED -> IN_PRODUCTION
 *
 * @author Heitor Almeida
 */
public class InvalidDemandTransitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que diz a demanda e a transição inválida
     *
     * @param message motivo, ex.: "Transição de status inválida para Copo: Concluída -> Em Produção"
     */
    public InvalidDemandTransitionException(String message) {
        super(message);
    }
}
