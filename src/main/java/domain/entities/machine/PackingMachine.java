package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

public class PackingMachine extends Machine {

    public PackingMachine(String name, int maxCapacity, double failureOdd,
                          double operationCost) {
        super(name, maxCapacity, failureOdd, operationCost);
    }

    @Override
    public Product process(Product product) {
        if (!isOn()) {
            throw new IllegalStateException("Eitcha, João! The machine can't process anything, since it ain't on!");
        }

        tryIncreaseFailureOdd(product, getFailureOdd());

        product.setStatus(ProductStatus.PACKED);
        return product;
    }

    @Override
    public String getType() {
        return "Packing";
    }
}
