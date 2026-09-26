package domain.strategies;

import domain.entities.demand.Demand;
import domain.interfaces.ProductionStrategy;

import java.util.Comparator;
import java.util.List;

/**
 * Estratégia que seleciona a demanda com a maior quantidade
 * de produtos e que caiba no orçamento.
 *
 * Chamamos de João porque ele sempre ganha a maior quantidade de copos
 * nos vídeos
 */
public class Joaozinho implements ProductionStrategy {
    private final String nome = "Joãozinho";

    @Override
    public Demand selectDemand(List<Demand> demands, double availableBudget) {
        // Entre as demandas que cabem no orçamento, pega a de maior quantidade de produtos
        return selectable(demands).stream()
                .filter(d -> d.isViable(availableBudget))
                .max(Comparator.comparingInt(Demand::getAmount))
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return this.nome;
    }

    @Override
    public String getStrategyRule() {
        return "máximo de produtos no orçamento";
    }
}