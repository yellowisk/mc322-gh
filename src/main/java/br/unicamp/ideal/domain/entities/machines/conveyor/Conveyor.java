package br.unicamp.ideal.domain.entities.machines.conveyor;

import br.unicamp.ideal.domain.entities.product.Product;

public class Conveyor {
    // Mandatory attributes
    private Product product;
    private int rawMaterial;
    private boolean isOn; // emMovimento
    private final int maxCapacity;

    public Conveyor(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Uma máquina não pode ter capacidade zero ou negativa.");
        }
        this.maxCapacity = maxCapacity;
    }

    // Mandatory methods
    private void turnOn() { this.isOn = true; }

    private void turnOff() { this.isOn = false; }

    private boolean canCarry(int quantity) {
        if (!verifyCapacity(quantity)) {
            throw new IllegalArgumentException("A demanda supera a capacidade da máquina.");
        } else if (this.rawMaterial > 0 || this.product != null) {
            throw new IllegalArgumentException("A esteira está ocupada.");
        } else if (!getIsOn()) {
            throw new IllegalArgumentException("A esteira está desligada.");
        }
        return true;
    }

    // >>>>> RECURSO
    private void addRawMaterial(int quantity) {
        try {
            boolean itCanCarry = canCarry(quantity);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }
        setRawMaterial(quantity);
    }

    private int removeRawMaterial() {
        if (this.rawMaterial == 0) {
            throw new IllegalArgumentException("Não há nenhuma matéria prima na esteira."); // TODO: Descobrir o erro certo.
        } else if (!getIsOn()) {
            throw new IllegalArgumentException("A esteira está desligada.");
        }

        int n = this.rawMaterial;
        setRawMaterial(0);
        return n;
    }

    // >>>>> PRODUTO
    private void addProduct(Product product) {
        try {
            boolean itCanCarry = canCarry(product.getRawMaterialAmountNeeded());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }
        setProduct(product);
    }

    private Product removeProduct() {
        if (this.product == null) {
            throw new IllegalArgumentException("Não há nenhum produto na esteira."); // TODO: Descobrir o erro certo.
        } else if (!getIsOn()) {
            throw new IllegalArgumentException("A esteira está desligada.");
        }

        Product p = this.product;
        setProduct(null);
        return p;
    }

    private boolean verifyCapacity(int weight) {
        return (weight <= this.maxCapacity);
    }

    public void setRawMaterial(int quantity) {
        this.rawMaterial = quantity;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean on) {
        isOn = on;
    }
}
