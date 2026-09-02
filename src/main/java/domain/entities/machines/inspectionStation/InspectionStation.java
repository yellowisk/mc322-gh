package domain.entities.machines.inspectionStation;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

public class InspectionStation {
    private boolean isOn; // ativa
    private int inspectedProducts;

    public void turnOff() {
        this.isOn = false;
    }

    public void turnOn() {
        this.isOn = true;
    }

    public void inspect(Product product) {
        if (!isOn()) {
            throw new IllegalStateException("[NÃO FOI DESSA VEZ...] The station's off.");
        }

        if (product == null) {
            throw new IllegalArgumentException("[NÃO FOI DESSA VEZ...] There's no product to inspect.");
        }

        product.setStatus(ProductStatus.INSPECTED);
        this.inspectedProducts += 1;
    }

    public boolean isOn() {
        return isOn;
    }

    public int getInspectedProducts() {
        return inspectedProducts;
    }
}
