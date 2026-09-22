package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

public class InspectionMachine extends Machine {

    public InspectionMachine(String name, int maxCapacity, double failureOdd, double operationCost) {
        super(name, maxCapacity, failureOdd, operationCost);
    }

    @Override
    public Product processAux(Product product) {
        if (isProcessFailure(product)) {
            product.setStatus(ProductStatus.FAILED);
            return product;
        }

        product.setStatus(ProductStatus.APPROVED);
        return product;
    }

    @Override
    public String getType() {
        return "Inspection";
    }
}
