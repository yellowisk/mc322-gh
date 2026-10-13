package domain.exceptions;

/**
 * Lançada quando uma {@link domain.entities.demand.Demand} tenta ir pra um status que
 * {@link domain.entities.demand.DemandStatus#canTransitionTo} não permite, ex.: COMPLETED -> IN_PRODUCTION,
 * ou quando mandam fabricar uma demanda num status que não pode ser fabricado
 *
 * @author Heitor Almeida
 */
public class InvalidDemandTransitionException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message o motivo, ex.: "Transição de status inválida para Copo: Concluída -> Em Produção"
     */
    public InvalidDemandTransitionException(String message) {
        super(message);
    }
}
