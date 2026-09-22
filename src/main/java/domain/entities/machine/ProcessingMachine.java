package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

public class ProcessingMachine extends Machine {

    public ProcessingMachine(String name, int maxCapacity, double failureOdd,
                             double operationCost) {
        super(name, maxCapacity, failureOdd, operationCost);
    }

    @Override
    public Product processAux(Product product) {
        Product newProduct = product.process(product, ProductStatus.PROCESSED);
        tryIncreaseFailureOdd(newProduct, getFailureOdd());
        return newProduct;
    }

    @Override
    public String getType() {
        return "Processing";
    }
}
