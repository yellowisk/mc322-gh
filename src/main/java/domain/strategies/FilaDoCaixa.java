package domain.strategies;

import domain.entities.demand.Demand;
import domain.interfaces.ProductionStrategy;

import java.util.Comparator;
import java.util.List;

/** FIFOOOOOOOOOOO */
public class FilaDoCaixa implements ProductionStrategy {

    @Override
    public Demand selectDemand(List<Demand> demands, double availableBudget) {
        return selectable(demands).stream()
                .min(Comparator.comparingLong(Demand::getArrivalOrder))
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return "Fila do Caixa";
    }

    @Override
    public String getStrategyRule() {
        return "ordem de chegada";
    }
}
