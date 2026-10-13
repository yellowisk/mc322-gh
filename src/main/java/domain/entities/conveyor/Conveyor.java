package domain.entities.conveyor;

import domain.entities.product.Produto;

public class Conveyor {
    // Mandatory attributes
    private final String name;
    private Produto product;
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

    /**
     * Verifica se a esteira consegue carregar a quantidade especificada de
     * matéria prima
     * @param quantity quantidade
     */
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

    /**
     * Remove a matéria prima da esteira retornando a quantidade
     * do material
     * @return quantidade de matéria prima da esteira
     */
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

    /**
     * Adiciona o produto na esteira
     * @param product o produto a ser adicionado
     */
    public void addProduct(Produto product) {
        checkCanCarry(product.getRawMaterialPerUnit());
        this.product = product;
    }

    /**
     * Remove o produto da esteira, retornando-o
     * @return o produto
     */
    public Produto removeProduct() {
        if (!isOn()) {
            throw new IllegalArgumentException("Não é mais ligeira... A esteira está desligada.");
        }

        if (this.product == null) {
            throw new IllegalArgumentException("Dessa vez não é! There's no product on the conveyor.");
        }


        Produto p = this.product;
        this.product = null;
        return p;
    }

    /**
     * Verifica se a esteira suporta o peso fornecido
     * @param weight o peso a ser verificado
     * @return true ou false
     */
    private boolean verifyCapacity(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Dessa vez não é! The weight can't be negative");
        }

        return (weight <= this.maxCapacity);
    }

    public double getRawMaterial() {
        return rawMaterial;
    }

    public Produto getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public boolean isOn() {
        return isOn;
    }
}
