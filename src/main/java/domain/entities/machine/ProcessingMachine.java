package domain.entities.machine;

import domain.entities.product.Produto;
import domain.entities.product.ProductStatus;

public class ProcessingMachine extends Machine {

    public ProcessingMachine(String name, int maxCapacity, double failureOdd,
                             double operationCost, double scenarioMultiplier,
                             int wearDamage) {
        super(name, maxCapacity, failureOdd, operationCost, scenarioMultiplier, wearDamage);
    }

    @Override
    public Produto processAux(Produto product) {
        Produto newProduct = product.process(product, ProductStatus.PROCESSED);
        tryIncreaseFailureOdd(newProduct);
        return newProduct;
    }

    @Override
    public String getType() {
        return "Processing";
    }
}
