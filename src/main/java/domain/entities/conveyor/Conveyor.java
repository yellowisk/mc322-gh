package domain.entities.conveyor;

import domain.entities.product.Product;

public class Conveyor {
    // Mandatory attributes
    private final String name;
    private Product product;
    private double rawMaterial;
    private boolean isOn; // emMovimento
    private final int maxCapacity;

    public Conveyor(String name, int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Dessa vez não é! A conveyor can't have a negative capacity.");
        }
        this.name = name;
        this.maxCapacity = maxCapacity;
    }

    // Mandatory methods
    public void turnOff() {
        this.isOn = false;
    }

    public void turnOn() {
        this.isOn = true;
    }

    private void checkCanCarry(double quantity) {
        if (!this.isOn) {
            throw new IllegalStateException("Dessa vez não é! The conveyor is off.");
        }
        if (this.rawMaterial > 0 || this.product != null) {
            throw new IllegalStateException("Dessa vez não é! The conveyor is already occupied.");
        }
        if (!verifyCapacity(quantity)) {
            throw new IllegalArgumentException("Dessa vez não é! The conveyor can't take this weight.");
        }
    }

    // >>>>> RECURSO
    public void addRawMaterial(double quantity) {
        checkCanCarry(quantity);
        this.rawMaterial = quantity;
    }

    public double removeRawMaterial() {
        if (!isOn()) {
            throw new IllegalStateException("Dessa vez não é! The conveyor is off.");
        }

        if (this.rawMaterial == 0) {
            throw new IllegalStateException("Dessa vez não é! There's no product on the conveyor.");
        }

        double n = this.rawMaterial;
        this.rawMaterial = 0;
        return n;
    }

    // >>>>> PRODUTO
    public void addProduct(Product product) {
        checkCanCarry(product.getRawMaterialPerUnit());
        this.product = product;
    }

    public Product removeProduct() {
        if (!isOn()) {
            throw new IllegalArgumentException("Não é mais ligeira... A esteira está desligada.");
        }

        if (this.product == null) {
            throw new IllegalArgumentException("Dessa vez não é! There's no product on the conveyor.");
        }


        Product p = this.product;
        this.product = null;
        return p;
    }

    private boolean verifyCapacity(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Dessa vez não é! The weight can't be negative");
        }

        return (weight <= this.maxCapacity);
    }

    public double getRawMaterial() {
        return rawMaterial;
    }

    public Product getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public boolean isOn() {
        return isOn;
    }
}
