package domain.interfaces;

import domain.entities.demand.Demand;
import java.util.List;

public interface ProductionStrategy {
    Demand selectDemand(List<Demand> demands, double availableBudget);

    String getStrategyName();

    /** Same filter for all three strategies */
    default List<Demand> selectable(List<Demand> demands) {
        return demands.stream()
                .filter(d -> d.getStatus().isSelectable() && d.getAmount() > 0)
                .toList();
    }
}