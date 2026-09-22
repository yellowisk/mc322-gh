package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

public class PackingMachine extends Machine {

    public PackingMachine(String name, int maxCapacity, double failureOdd,
                          double operationCost) {
        super(name, maxCapacity, failureOdd, operationCost);
    }

    @Override
    public Product processAux(Product product) {
        tryIncreaseFailureOdd(product, getFailureOdd());

        product.setStatus(ProductStatus.PACKED);
        return product;
    }

    @Override
    public String getType() {
        return "Packing";
    }
}
