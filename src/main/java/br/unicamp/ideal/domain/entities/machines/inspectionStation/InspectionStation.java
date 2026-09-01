package br.unicamp.ideal.domain.entities.machines.inspectionStation;

import br.unicamp.ideal.domain.entities.product.Product;
import br.unicamp.ideal.domain.entities.product.ProductStatus;

public class InspectionStation {
    private boolean isOn; // ativa
    private int inspectedProducts;

    public void turnOn() { setIsOn(true); }

    public void turnOff() { setIsOn(false); }

    public void inspect(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Nenhum produto passado.");
        } else if (!getIsOn()) {
            throw new IllegalArgumentException("A máquina está desligada.");
        }

        product.setStatus(ProductStatus.INSPECTED);
        this.inspectedProducts += 1;
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean on) {
        isOn = on;
    }

    public int getInspectedProducts() {
        return inspectedProducts;
    }
}
