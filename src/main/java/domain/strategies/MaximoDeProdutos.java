package domain.strategies;

import domain.entities.demand.Demand;
import domain.interfaces.ProductionStrategy;

import java.util.Comparator;
import java.util.List;

/**
 * Estratégia que seleciona a demanda com a maior quantidade
 * de produtos e que caiba no orçamento.
 */
public class MaximoDeProdutos implements ProductionStrategy {
    private final String nome = "Estratégia Máximo de Produtos";

    @Override
    public Demand selectDemand(List<Demand> demands, double availableBudget) {
        // Pega a demanda com a maior quantidade de produtos
        demands.sort(Comparator.comparingInt(Demand::getAmount));

        if (demands.getFirst().getEstimatedCost() > availableBudget) {
            return null;
        }

        return demands.getFirst();
    }

    @Override
    public String getStrategyName() {
        return this.nome;
    }
}