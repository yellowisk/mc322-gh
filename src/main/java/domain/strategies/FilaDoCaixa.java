package domain.strategies;

import domain.entities.demand.Demand;
import domain.interfaces.ProductionStrategy;

import java.util.Comparator;
import java.util.List;

/**
 * FIFOOOOOOOOOOO: estratégia da ordem de chegada, igual fila do caixa do mercadinho.
 * Escolhe a demanda que chegou primeiro (menor {@link Demand#getArrivalOrder()})
 *
 * @author Heitor Almeida
 */
public class FilaDoCaixa implements ProductionStrategy {

    /**
     * Pega a demanda elegível que chegou primeiro. Ignora o orçamento
     *
     * @param demandas todas as demandas cadastradas
     * @param orcamentoDisponivel não é usado por essa estratégia
     * @return demanda mais antiga, ou null se nenhuma for elegível
     */
    @Override
    public Demand selecionarDemanda(List<Demand> demandas, double orcamentoDisponivel) {
        return selecionaveis(demandas).stream()
                .min(Comparator.comparingLong(Demand::getArrivalOrder))
                .orElse(null);
    }

    @Override
    public String getNomeEstrategia() {
        return "Fila do Caixa";
    }

    @Override
    public String getRegraEstrategia() {
        return "ordem de chegada";
    }
}
