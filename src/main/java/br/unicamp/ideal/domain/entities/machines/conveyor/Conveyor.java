package br.unicamp.ideal.domain.entities.machines.conveyor;

import br.unicamp.ideal.domain.entities.product.Product;

public class Conveyor {
    // Mandatory attributes
    private final String name;
    private Product product;
    private int rawMaterial;
    private boolean isOn; // emMovimento
    private final int maxCapacity;

    public Conveyor(String name, int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("[NÃO FOI DESSA VEZ...] A conveyor can't have a negative capacity.");
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

    private void checkCanCarry(int quantity) {
        if (!this.isOn) {
            throw new IllegalStateException("[NÃO FOI DESSA VEZ...] The conveyor is off.");
        }
        if (this.rawMaterial > 0 || this.product != null) {
            throw new IllegalStateException("[NÃO FOI DESSA VEZ...] The conveyor is already occupied.");
        }
        if (!verifyCapacity(quantity)) {
            throw new IllegalArgumentException("[NÃO FOI DESSA VEZ...] The conveyor can't take this weight.");
        }
    }

    // >>>>> RECURSO
    public void addRawMaterial(int quantity) {
        checkCanCarry(quantity);
        this.rawMaterial = quantity;
    }

    public int removeRawMaterial() {
        if (!isOn()) {
            throw new IllegalStateException("[NÃO FOI DESSA VEZ...] The conveyor is off.");
        }

        if (this.rawMaterial == 0) {
            throw new IllegalStateException("[NÃO FOI DESSA VEZ...] There's no product on the conveyor.");
        }

        int n = this.rawMaterial;
        this.rawMaterial = 0;
        return n;
    }

    // >>>>> PRODUTO
    public void addProduct(Product product) {
        checkCanCarry(product.getRawMaterialAmountNeeded());
        this.product = product;
    }

    public Product removeProduct() {
        if (!isOn()) {
            throw new IllegalArgumentException("A esteira está desligada.");
        }

        if (this.product == null) {
            throw new IllegalArgumentException(""[NÃO FOI DESSA VEZ...] There's no product on the conveyor.");
        }


        Product p = this.product;
        this.product = null;
        return p;
    }

    private boolean verifyCapacity(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("[NÃO FOI DESSA VEZ...] The weight can't be negative");
        }

        return (weight <= this.maxCapacity);
    }

    public int getRawMaterial() {
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
