package domain.strategies;

import domain.entities.demand.Demand;
import domain.interfaces.ProductionStrategy;

import java.util.Comparator;
import java.util.List;

/*
Biggest Demand!!

https://www.instagram.com/p/DcRinHop9NR/
Ferrari is a rich influencer that once visited the mercadinho and bought a lotta products there to get a 'balão surpresa'. Bro is definitely the typa person to buy as much products as possible the imo
*/
public class Ferrari implements ProductionStrategy {

    @Override
    public Demand selectDemand(List<Demand> demands, double availableBudget) {
        return selectable(demands).stream()
                .max(Comparator.comparingInt(Demand::getAmount))
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return "Ferrari";
    }
}
