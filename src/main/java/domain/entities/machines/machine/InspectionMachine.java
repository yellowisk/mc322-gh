package domain.entities.machines.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

public class InspectionMachine extends Machine {

    public InspectionMachine(String name, int maxCapacity, double failureOdd, double operationCost) {
        super(name, maxCapacity, failureOdd, operationCost);
    }

    @Override
    public Product process(Product product, ProductStatus status) {
        if (!isOn()) {
            throw new IllegalStateException("[Eitcha, João...] The machine can't process anything, since it ain't on!");
        }

        if (isProcessFailure(product)) {
            product.setStatus(ProductStatus.FAILED);
            return product;
        }

        product.setStatus(status);
        return product;
    }

    @Override
    public String getType() {
        return "Inspection";
    }
}
