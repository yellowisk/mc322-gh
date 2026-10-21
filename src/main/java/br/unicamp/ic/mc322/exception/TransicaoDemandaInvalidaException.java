package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando uma {@link br.unicamp.ic.mc322.model.demand.Demanda} tenta ir pra um status que
 * {@link br.unicamp.ic.mc322.model.demand.StatusDemanda#podeTransicionarPara} não permite, ex.: CONCLUIDA -> EM_PRODUCAO,
 * ou quando mandam fabricar uma demanda num status que não pode ser fabricado
 *
 * @author Heitor Almeida
 */
public class TransicaoDemandaInvalidaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message o motivo, ex.: "Transição de status inválida para Copo: Concluída -> Em Produção"
     */
    public TransicaoDemandaInvalidaException(String message) {
        super(message);
    }
}
